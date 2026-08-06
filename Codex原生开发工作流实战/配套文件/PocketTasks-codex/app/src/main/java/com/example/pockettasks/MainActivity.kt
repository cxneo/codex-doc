package com.example.pockettasks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.pockettasks.ui.PocketTasksScreen
import com.example.pockettasks.ui.TaskViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: TaskViewModel by viewModels {
        TaskViewModel.Factory((application as PocketTasksApplication).taskRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                val state by viewModel.uiState.collectAsState()
                PocketTasksScreen(
                    state = state,
                    onAddTask = viewModel::addTask,
                    onSelectFilter = viewModel::selectFilter,
                    onSetCompleted = viewModel::setCompleted,
                )
            }
        }
    }
}
