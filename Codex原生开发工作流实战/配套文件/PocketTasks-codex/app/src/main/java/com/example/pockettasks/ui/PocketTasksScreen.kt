package com.example.pockettasks.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.pockettasks.model.Task
import com.example.pockettasks.model.TaskFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PocketTasksScreen(
    state: TaskUiState,
    onAddTask: (String) -> Unit,
    onSelectFilter: (TaskFilter) -> Unit,
    onSetCompleted: (Task, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var title by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("PocketTasks") }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = state.filter == filter,
                        onClick = { onSelectFilter(filter) },
                        label = { Text(filter.label) },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("任务标题") },
                    singleLine = true,
                )
                Button(
                    onClick = {
                        onAddTask(title)
                        title = ""
                    },
                    enabled = title.isNotBlank(),
                ) {
                    Text("添加")
                }
            }

            when {
                state.isLoading -> Text("正在读取任务…")
                state.tasks.isEmpty() -> Text("当前筛选下没有任务")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.tasks, key = Task::id) { task ->
                        TaskRow(task = task, onSetCompleted = onSetCompleted)
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskRow(
    task: Task,
    onSetCompleted: (Task, Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = { completed -> onSetCompleted(task, completed) },
            modifier = Modifier.semantics {
                contentDescription = if (task.isCompleted) {
                    "将 ${task.title} 标记为未完成"
                } else {
                    "将 ${task.title} 标记为已完成"
                }
            },
        )
        Text(text = task.title, style = MaterialTheme.typography.bodyLarge)
    }
}

private val TaskFilter.label: String
    get() = when (this) {
        TaskFilter.ALL -> "全部"
        TaskFilter.ACTIVE -> "进行中"
        TaskFilter.COMPLETED -> "已完成"
    }
