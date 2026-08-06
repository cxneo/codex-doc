package com.example.pockettasks.model

data class Task(
    val id: Long,
    val title: String,
    val isCompleted: Boolean,
    val isArchived: Boolean = false,
)
