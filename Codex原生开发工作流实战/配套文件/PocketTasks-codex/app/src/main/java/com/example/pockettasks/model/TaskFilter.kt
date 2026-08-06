package com.example.pockettasks.model

enum class TaskFilter {
    ALL,
    ACTIVE,
    COMPLETED,
}

fun List<Task>.visibleFor(filter: TaskFilter): List<Task> =
    asSequence()
        .filterNot(Task::isArchived)
        .filter { task ->
            when (filter) {
                TaskFilter.ALL -> true
                TaskFilter.ACTIVE -> !task.isCompleted
                TaskFilter.COMPLETED -> task.isCompleted
            }
        }
        .toList()
