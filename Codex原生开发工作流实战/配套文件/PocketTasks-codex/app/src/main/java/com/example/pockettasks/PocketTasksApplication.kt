package com.example.pockettasks

import android.app.Application
import androidx.room.Room
import com.example.pockettasks.data.DataStoreFilterPreferences
import com.example.pockettasks.data.PocketTasksDatabase
import com.example.pockettasks.data.TaskRepository

class PocketTasksApplication : Application() {
    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            PocketTasksDatabase::class.java,
            "pockettasks.db",
        )
            .addMigrations(PocketTasksDatabase.MIGRATION_1_2)
            .build()
    }

    val taskRepository by lazy {
        TaskRepository(
            taskDao = database.taskDao(),
            filterPreferences = DataStoreFilterPreferences(applicationContext),
        )
    }
}
