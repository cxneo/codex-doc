package com.example.pockettasks.data

import com.example.pockettasks.model.Task
import com.example.pockettasks.model.TaskFilter
import com.example.pockettasks.model.visibleFor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

data class FilteredTasks(
    val filter: TaskFilter,
    val tasks: List<Task>,
)

class TaskRepository(
    private val taskDao: TaskDao,
    private val filterPreferences: FilterPreferences,
) {
    val filteredTasks: Flow<FilteredTasks> = combine(
        taskDao.observeAll().map { entities -> entities.map(TaskEntity::asExternalModel) },
        filterPreferences.selectedFilter,
    ) { tasks, filter ->
        FilteredTasks(filter = filter, tasks = tasks.visibleFor(filter))
    }

    suspend fun addTask(title: String) {
        val normalized = title.trim()
        if (normalized.isNotEmpty()) {
            taskDao.upsert(TaskEntity(title = normalized))
        }
    }

    suspend fun setCompleted(id: Long, completed: Boolean) {
        taskDao.setCompleted(id, completed)
    }

    suspend fun setFilter(filter: TaskFilter) {
        filterPreferences.setSelectedFilter(filter)
    }
}
