package ru.plumsoftware.notepad.data.database.habit

import java.util.Calendar
import kotlinx.coroutines.flow.Flow
import ru.plumsoftware.notepad.data.model.habit.Habit
import ru.plumsoftware.notepad.data.model.habit.HabitEntry
import ru.plumsoftware.notepad.data.model.habit.HabitWithHistory

class HabitRepository(private val habitDao: HabitDao) {

    // Получаем поток всех привычек с историей
    fun getAllHabits(): Flow<List<HabitWithHistory>> {
        return habitDao.getAllHabitsWithHistory()
    }

    suspend fun createHabit(habit: Habit) {
        habitDao.insertHabit(habit)
    }

    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit)
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
    }

    // --- Логика переключения (Toggle) ---
    // Если привычка выполнена сегодня -> удаляем запись.
    // Если не выполнена -> добавляем.
    suspend fun toggleHabitCompletion(habitId: String) {
        val todayStart = getStartOfDayInMillis()

        // Проверяем, есть ли запись за сегодня
        val existingEntry = habitDao.getEntryForDate(habitId, todayStart)

        if (existingEntry != null) {
            // Уже выполнено -> Отменяем
            habitDao.deleteEntry(habitId, todayStart)
        } else {
            // Не выполнено -> Отмечаем
            val newEntry = HabitEntry(
                habitId = habitId,
                date = todayStart,
                completedAt = System.currentTimeMillis()
            )
            habitDao.insertEntry(newEntry)
        }
    }

    // Вспомогательная функция для получения 00:00:00 текущего дня
    private fun getStartOfDayInMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}