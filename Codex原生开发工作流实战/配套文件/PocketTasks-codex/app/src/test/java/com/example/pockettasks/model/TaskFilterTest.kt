package com.example.pockettasks.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TaskFilterTest {
    private val tasks = listOf(
        Task(id = 1, title = "编写 Spec", isCompleted = false),
        Task(id = 2, title = "运行测试", isCompleted = true),
        Task(id = 3, title = "已归档", isCompleted = false, isArchived = true),
    )

    @Test
    fun `all hides archived tasks but keeps active and completed tasks`() {
        assertEquals(listOf(1L, 2L), tasks.visibleFor(TaskFilter.ALL).map(Task::id))
    }

    @Test
    fun `active returns only unfinished visible tasks`() {
        assertEquals(listOf(1L), tasks.visibleFor(TaskFilter.ACTIVE).map(Task::id))
    }

    @Test
    fun `completed returns only completed visible tasks`() {
        assertEquals(listOf(2L), tasks.visibleFor(TaskFilter.COMPLETED).map(Task::id))
    }
}
