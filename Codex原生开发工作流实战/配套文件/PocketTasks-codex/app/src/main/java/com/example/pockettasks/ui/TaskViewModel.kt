package com.example.pockettasks.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pockettasks.data.TaskRepository
import com.example.pockettasks.model.Task
import com.example.pockettasks.model.TaskFilter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TaskUiState(
    val filter: TaskFilter = TaskFilter.ALL,
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = true,
)

class TaskViewModel(
    private val repository: TaskRepository,
) : ViewModel() {
    val uiState: StateFlow<TaskUiState> = repository.filteredTasks
        .map { result ->
            TaskUiState(
                filter = result.filter,
                tasks = result.tasks,
                isLoading = false,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TaskUiState(),
        )

    fun addTask(title: String) {
        viewModelScope.launch { repository.addTask(title) }
    }

    fun selectFilter(filter: TaskFilter) {
        viewModelScope.launch { repository.setFilter(filter) }
    }

    fun setCompleted(task: Task, completed: Boolean) {
        viewModelScope.launch { repository.setCompleted(task.id, completed) }
    }

    class Factory(
        private val repository: TaskRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(TaskViewModel::class.java))
            return TaskViewModel(repository) as T
        }
    }
}
