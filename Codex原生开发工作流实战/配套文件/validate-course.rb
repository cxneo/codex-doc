#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "open3"
require "pathname"
require "uri"
require "yaml"

COURSE_ROOT = File.expand_path("..", __dir__)
PROJECT_ROOT = File.join(COURSE_ROOT, "配套文件", "PocketTasks-codex")

run_android = ARGV.delete("--android")
if ARGV.any?
  warn "Usage: ruby 配套文件/validate-course.rb [--android]"
  exit 2
end

passes = []
failures = []

def ignored_path?(path)
  path.include?("/.gradle/") ||
    path.include?("/.kotlin/") ||
    path.include?("/build/")
end

def run_command(label, command, chdir, passes, failures)
  stdout, stderr, status = Open3.capture3(*command, chdir: chdir)
  if status.success?
    passes << label
    return
  end

  combined = [stdout, stderr].reject(&:empty?).join("\n")
  excerpt = combined.lines.last(40).join
  failures << "#{label}\n#{excerpt}"
rescue StandardError => e
  failures << "#{label}\n#{e.class}: #{e.message}"
end

def markdown_anchors(markdown_path)
  anchors = []
  occurrences = Hash.new(0)
  fence = nil

  File.foreach(markdown_path, encoding: "UTF-8") do |line|
    fence_match = line.match(/^\s*(`{3,}|~{3,})/)
    unless fence_match.nil?
      marker = fence_match[1][0]
      fence = fence.nil? ? marker : nil if fence.nil? || fence == marker
      next
    end
    next unless fence.nil?

    match = line.match(/^\#{1,6}\s+(.+?)\s*#*\s*$/)
    next if match.nil?

    heading = match[1]
      .gsub(/\[([^\]]+)\]\([^)]+\)/, "\\1")
      .gsub(/<[^>]+>/, "")
      .delete("`*")
      .downcase
      .gsub(/[^\p{Alnum}\p{M}\-_ ]/u, "")
      .strip
      .gsub(/\s+/, "-")

    count = occurrences[heading]
    occurrences[heading] += 1
    anchors << (count.zero? ? heading : "#{heading}-#{count}")
  end

  anchors
end


def run_execpolicy_check(label, rules_path, command, expected, chdir, passes, failures)
  stdout, stderr, status = Open3.capture3(
    "codex",
    "execpolicy",
    "check",
    "--rules",
    rules_path,
    *command,
    chdir: chdir
  )
  payload = JSON.parse(stdout)
  if status.success? && payload["decision"] == expected
    passes << label
  else
    failures << "#{label}\n预期 #{expected.inspect}，实际 #{payload["decision"].inspect}\n#{stderr}"
  end
rescue JSON::ParserError, Errno::ENOENT => e
  failures << "#{label}\n#{e.class}: #{e.message}\n请确认已安装包含 execpolicy 的 Codex CLI。"
end

# 1. 课程结构
chapter_files = Dir.glob(File.join(COURSE_ROOT, "[0-9][0-9]｜*.md")).sort
chapter_numbers = chapter_files.map { |path| File.basename(path)[0, 2] }
expected_numbers = (1..24).map { |number| format("%02d", number) }

if chapter_numbers == expected_numbers
  passes << "24 个章节连续且唯一"
else
  failures << "章节编号异常：预期 #{expected_numbers.join(', ')}，实际 #{chapter_numbers.join(', ')}"
end

required_guides = [
  "README.md",
  "课程导读｜怎么学怎么练怎么验收.md",
  "讲师手册.md",
  "术语表.md",
  "版本与兼容性.md",
  "章节审校记录.md",
  "结课实践.md",
  "CHANGELOG.md"
]
missing_guides = required_guides.reject { |name| File.file?(File.join(COURSE_ROOT, name)) }
if missing_guides.empty?
  passes << "课程入口与培训材料齐全"
else
  failures << "缺少课程材料：#{missing_guides.join(', ')}"
end

# 2. Markdown 本地链接与标题锚点。外部链接不联网检查。
markdown_files = Dir.glob(
  File.join(COURSE_ROOT, "**", "*.md"),
  File::FNM_DOTMATCH
).reject { |path| ignored_path?(path) }
broken_links = []
markdown_files.each do |markdown_path|
  content = File.read(markdown_path, encoding: "UTF-8")
  content.scan(/!?\[[^\]]*\]\(([^)]+)\)/).flatten.each do |raw_target|
    target = raw_target.strip
    target = target[1...-1] if target.start_with?("<") && target.end_with?(">")
    next if target.empty?
    next if target.match?(%r{\A(?:https?|mailto|data):}i)

    target = target.split(/\s+["']/).first
    path_part, fragment = target.split("#", 2)

    decoded_path = URI::DEFAULT_PARSER.unescape(path_part || "")
    resolved = if decoded_path.empty?
      markdown_path
    else
      File.expand_path(decoded_path, File.dirname(markdown_path))
    end

    relative_source = Pathname.new(markdown_path).relative_path_from(Pathname.new(COURSE_ROOT))
    unless File.exist?(resolved)
      broken_links << "#{relative_source}: #{raw_target}"
      next
    end

    next if fragment.nil? || fragment.empty? || File.extname(resolved).downcase != ".md"

    decoded_fragment = URI::DEFAULT_PARSER.unescape(fragment).downcase
    next if markdown_anchors(resolved).include?(decoded_fragment)

    broken_links << "#{relative_source}: 标题锚点不存在 #{raw_target}"
  rescue URI::InvalidURIError => e
    relative_source = Pathname.new(markdown_path).relative_path_from(Pathname.new(COURSE_ROOT))
    broken_links << "#{relative_source}: 无法解析 #{raw_target.inspect}（#{e.message}）"
  end
end

if broken_links.empty?
  passes << "#{markdown_files.length} 个 Markdown 文件的本地链接与标题锚点有效"
else
  failures << "发现失效的 Markdown 本地链接：\n#{broken_links.join("\n")}"
end

# 3. 配套工程中的 JSON、YAML 与 TOML 语法。
json_files = Dir.glob(
  File.join(PROJECT_ROOT, "**", "*.json"),
  File::FNM_DOTMATCH
).reject { |path| ignored_path?(path) }
json_errors = json_files.each_with_object([]) do |path, errors|
  JSON.parse(File.read(path, encoding: "UTF-8"))
rescue JSON::ParserError => e
  errors << "#{path.sub("#{COURSE_ROOT}/", "")}: #{e.message}"
end

if json_errors.empty?
  passes << "#{json_files.length} 个 JSON 文件语法有效"
else
  failures << "JSON 语法错误：\n#{json_errors.join("\n")}"
end

yaml_files = Dir.glob(
  File.join(PROJECT_ROOT, "**", "*.{yml,yaml}"),
  File::FNM_DOTMATCH
).reject { |path| ignored_path?(path) }
yaml_errors = yaml_files.each_with_object([]) do |path, errors|
  YAML.parse_file(path)
rescue Psych::SyntaxError => e
  errors << "#{path.sub("#{COURSE_ROOT}/", "")}: #{e.message}"
end

if yaml_errors.empty?
  passes << "#{yaml_files.length} 个 YAML 文件语法有效"
else
  failures << "YAML 语法错误：\n#{yaml_errors.join("\n")}"
end

toml_files = Dir.glob(
  File.join(PROJECT_ROOT, "**", "*.toml"),
  File::FNM_DOTMATCH
).reject { |path| ignored_path?(path) }
toml_parser = <<~'PYTHON'
  import sys

  try:
      import tomllib
  except ModuleNotFoundError:
      try:
          import tomli as tomllib
      except ModuleNotFoundError:
          raise SystemExit("需要 Python 3.11+，或为旧版 Python 安装 tomli，才能校验 TOML")

  for path in sys.argv[1:]:
      with open(path, "rb") as stream:
          tomllib.load(stream)
PYTHON
run_command(
  "#{toml_files.length} 个 TOML 文件语法有效",
  ["python3", "-c", toml_parser, *toml_files],
  PROJECT_ROOT,
  passes,
  failures
)

# 4. 可执行教学资产。
Dir.glob(File.join(PROJECT_ROOT, "scripts", "*.sh")).sort.each do |script|
  run_command(
    "Shell 语法：#{File.basename(script)}",
    ["bash", "-n", script],
    PROJECT_ROOT,
    passes,
    failures
  )
end

run_command(
  "Hook 黑盒测试",
  ["python3", ".codex/hooks/test_pre_tool_use.py"],
  PROJECT_ROOT,
  passes,
  failures
)

%w[19-tdd 20-review 22-migration].each do |lab|
  run_command(
    "故障补丁可应用：#{lab}",
    ["./scripts/lab-patch.sh", "check", lab],
    PROJECT_ROOT,
    passes,
    failures
  )
end

rules_path = File.join(PROJECT_ROOT, ".codex", "rules", "default.rules")
run_execpolicy_check(
  "Rules 决策：git push → prompt",
  rules_path,
  ["git", "push"],
  "prompt",
  PROJECT_ROOT,
  passes,
  failures
)
run_execpolicy_check(
  "Rules 决策：adb pm clear → forbidden",
  rules_path,
  ["adb", "shell", "pm", "clear", "com.example.pockettasks"],
  "forbidden",
  PROJECT_ROOT,
  passes,
  failures
)

# 5. 可选 Android 验证。设备测试刻意不在此处运行。
if run_android
  run_command(
    "Android JVM 测试、Lint、构建与 AndroidTest 编译",
    [
      "./gradlew",
      ":app:testDebugUnitTest",
      ":app:lintDebug",
      ":app:assembleDebug",
      ":app:assembleDebugAndroidTest",
      "--console=plain"
    ],
    PROJECT_ROOT,
    passes,
    failures
  )
end

puts "课程校验结果"
passes.each { |label| puts "  PASS  #{label}" }

unless failures.empty?
  warn "\n失败项"
  failures.each { |message| warn "  FAIL  #{message}" }
  exit 1
end

puts "\n全部通过（#{passes.length} 项）。"
puts "说明：外部链接与设备测试不包含在本次自动校验中。"
