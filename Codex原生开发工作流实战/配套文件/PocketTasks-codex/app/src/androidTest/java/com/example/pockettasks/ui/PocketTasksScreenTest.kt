package com.example.pockettasks.ui

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.pockettasks.model.Task
import com.example.pockettasks.model.TaskFilter
import org.junit.Rule
import org.junit.Test

class PocketTasksScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun selectedFilterAndTasksAreExposedToTheUser() {
        composeRule.setContent {
            PocketTasksScreen(
                state = TaskUiState(
                    filter = TaskFilter.ACTIVE,
                    tasks = listOf(Task(1, "编写迁移测试", false)),
                    isLoading = false,
                ),
                onAddTask = {},
                onSelectFilter = {},
                onSetCompleted = { _, _ -> },
            )
        }

        composeRule.onNodeWithText("进行中").assertIsSelected()
        composeRule.onNodeWithText("编写迁移测试").assertTextEquals("编写迁移测试")
    }
}
