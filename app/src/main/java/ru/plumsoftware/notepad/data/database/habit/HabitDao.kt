package ru.plumsoftware.notepad.data.database.habit

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.plumsoftware.notepad.data.model.habit.Habit
import ru.plumsoftware.notepad.data.model.habit.HabitEntry
import ru.plumsoftware.notepad.data.model.habit.HabitWithHistory

@Dao
interface HabitDao {

    // Получаем все привычки вместе с их историей выполнения
    // Транзакция нужна, так как Room делает два запроса под капотом
    @Transaction
    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabitsWithHistory(): Flow<List<HabitWithHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Update
    suspend fun updateHabit(habit: Habit)

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: String): Habit?

    // --- Управление выполнением ---

    // Отметить выполненным (Вставить запись в историю)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEntry(entry: HabitEntry)

    // Отменить выполнение (Удалить запись за конкретную дату)
    @Query("DELETE FROM habit_history WHERE habitId = :habitId AND date = :date")
    suspend fun deleteEntry(habitId: String, date: Long)

    // Получить запись за конкретный день (проверка, выполнено ли сегодня)
    @Query("SELECT * FROM habit_history WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getEntryForDate(habitId: String, date: Long): HabitEntry?
}