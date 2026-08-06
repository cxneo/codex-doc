package com.example.pockettasks.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.pockettasks.model.Task

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,
    val archived: Boolean = false,
)

fun TaskEntity.asExternalModel() = Task(
    id = id,
    title = title,
    isCompleted = isCompleted,
    isArchived = archived,
)
