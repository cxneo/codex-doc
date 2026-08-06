package com.example.pockettasks.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.pockettasks.model.TaskFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface FilterPreferences {
    val selectedFilter: Flow<TaskFilter>
    suspend fun setSelectedFilter(filter: TaskFilter)
}

private val Context.taskPreferences by preferencesDataStore(name = "task_preferences")

class DataStoreFilterPreferences(
    private val context: Context,
) : FilterPreferences {
    override val selectedFilter: Flow<TaskFilter> = context.taskPreferences.data.map { preferences ->
        preferences[FILTER_KEY]
            ?.let { stored -> TaskFilter.entries.firstOrNull { it.name == stored } }
            ?: TaskFilter.ALL
    }

    override suspend fun setSelectedFilter(filter: TaskFilter) {
        context.taskPreferences.edit { preferences ->
            preferences[FILTER_KEY] = filter.name
        }
    }

    private companion object {
        val FILTER_KEY: Preferences.Key<String> = stringPreferencesKey("selected_filter")
    }
}
