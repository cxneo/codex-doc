package com.example.pockettasks.data

import com.example.pockettasks.model.TaskFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskRepositoryTest {
    private val dao = FakeTaskDao(
        TaskEntity(id = 1, title = "进行中的任务"),
        TaskEntity(id = 2, title = "已完成任务", isCompleted = true),
    )
    private val preferences = FakeFilterPreferences()
    private val repository = TaskRepository(dao, preferences)

    @Test
    fun `changing filter updates the observable task list`() = runTest {
        repository.setFilter(TaskFilter.COMPLETED)

        val result = repository.filteredTasks.first()

        assertEquals(TaskFilter.COMPLETED, result.filter)
        assertEquals(listOf("已完成任务"), result.tasks.map { it.title })
    }

    @Test
    fun `blank title is not inserted`() = runTest {
        repository.addTask("   ")

        assertEquals(2, dao.observeAll().first().size)
    }
}

private class FakeFilterPreferences : FilterPreferences {
    private val state = MutableStateFlow(TaskFilter.ALL)

    override val selectedFilter: Flow<TaskFilter> = state

    override suspend fun setSelectedFilter(filter: TaskFilter) {
        state.value = filter
    }
}

private class FakeTaskDao(vararg initial: TaskEntity) : TaskDao {
    private val state = MutableStateFlow(initial.toList())

    override fun observeAll(): Flow<List<TaskEntity>> = state

    override suspend fun upsert(task: TaskEntity): Long {
        val id = task.id.takeIf { it != 0L } ?: ((state.value.maxOfOrNull { it.id } ?: 0L) + 1)
        state.value = state.value.filterNot { it.id == id } + task.copy(id = id)
        return id
    }

    override suspend fun setCompleted(id: Long, completed: Boolean) {
        state.value = state.value.map { task ->
            if (task.id == id) task.copy(isCompleted = completed) else task
        }
    }
}
