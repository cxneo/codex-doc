package com.example.pockettasks.data

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation = InstrumentationRegistry.getInstrumentation(),
        databaseClass = PocketTasksDatabase::class.java,
    )

    private var migratedDatabase: PocketTasksDatabase? = null

    @After
    fun closeDatabase() {
        migratedDatabase?.close()
    }

    @Test
    fun migrate1To2PreservesTasksAndDefaultsArchivedToFalse() {
        helper.createDatabase(TEST_DATABASE, 1).apply {
            execSQL("INSERT INTO tasks (id, title, is_completed) VALUES (1, '保留我', 0)")
            close()
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        migratedDatabase = Room.databaseBuilder(
            context,
            PocketTasksDatabase::class.java,
            TEST_DATABASE,
        )
            .addMigrations(PocketTasksDatabase.MIGRATION_1_2)
            .build()

        val sqlite: SupportSQLiteDatabase = migratedDatabase!!.openHelper.writableDatabase
        sqlite.query("SELECT title, archived FROM tasks WHERE id = 1").use { cursor ->
            check(cursor.moveToFirst())
            assertEquals("保留我", cursor.getString(0))
            assertEquals(0, cursor.getInt(1))
        }
    }

    private companion object {
        const val TEST_DATABASE = "migration-test"
    }
}
