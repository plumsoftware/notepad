package ru.plumsoftware.notepad.data.database

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ru.plumsoftware.notepad.data.convertor.Converters
import ru.plumsoftware.notepad.data.database.habit.HabitDao
import ru.plumsoftware.notepad.data.model.Group
import ru.plumsoftware.notepad.data.model.Note
import ru.plumsoftware.notepad.data.model.habit.Habit
import ru.plumsoftware.notepad.data.model.habit.HabitEntry

@Database(entities = [Note::class, Group::class, Habit::class, HabitEntry::class], version = 7)
@TypeConverters(Converters::class)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun groupDao(): GroupDao
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE notes ADD COLUMN createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE notes ADD COLUMN reminderDate INTEGER"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE notes ADD COLUMN photos TEXT NOT NULL DEFAULT '[]'"
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
            CREATE TABLE groups (
                id TEXT PRIMARY KEY NOT NULL,
                title TEXT NOT NULL,
                color INTEGER NOT NULL,
                createdAt INTEGER NOT NULL
            )
        """.trimIndent()
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE notes ADD COLUMN groupId TEXT NOT NULL DEFAULT '0'"
                )
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Создаем таблицу Habits
                // Обрати внимание: типы данных соответствуют Kotlin (String->TEXT, Long/Int/Boolean->INTEGER)
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `habits` (
                        `id` TEXT NOT NULL, 
                        `title` TEXT NOT NULL, 
                        `color` INTEGER NOT NULL, 
                        `emoji` TEXT NOT NULL, 
                        `frequency` TEXT NOT NULL, 
                        `repeatDays` TEXT NOT NULL, 
                        `reminderHour` INTEGER, 
                        `reminderMinute` INTEGER, 
                        `isReminderEnabled` INTEGER NOT NULL, 
                        `createdAt` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent()
                )

                // 2. Создаем таблицу Истории (habit_history) с составным ключом
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `habit_history` (
                        `habitId` TEXT NOT NULL, 
                        `date` INTEGER NOT NULL, 
                        `completedAt` INTEGER NOT NULL, 
                        PRIMARY KEY(`habitId`, `date`),
                        FOREIGN KEY(`habitId`) REFERENCES `habits`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """.trimIndent()
                )

                // 3. Создаем Индекс для быстрого поиска по habitId (важно для производительности JOIN)
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS `index_habit_history_habitId` ON `habit_history` (`habitId`)
                """.trimIndent()
                )
            }
        }

        fun getDatabase(application: Application): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    application,
                    NoteDatabase::class.java,
                    "note_database"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}