package ru.plumsoftware.notepad.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.plumsoftware.notepad.data.model.Note

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') AND groupId != '-1' ORDER BY isPinned DESC, createdAt DESC")
    fun searchNotes(query: String): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND groupId != '-1' ORDER BY isPinned DESC, createdAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND (:groupId IS NULL OR groupId = :groupId) ORDER BY isPinned DESC, createdAt DESC")
    fun getNotesFilteredByGroup(groupId: String?): Flow<List<Note>>

    @Insert
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT COUNT(*) FROM notes WHERE isDeleted = 0")
    fun getAllNotesCount(): Flow<Int>

    // 1. Измененный поиск (исключаем секретные, если мы не в секретной папке)
    // Если groupId не передан (поиск по всем), исключаем '-1'
    @Transaction
    @Query("""
        SELECT
            notes.*,
            groups.title AS group_title,
            groups.color AS group_color
        FROM notes
        LEFT JOIN groups ON notes.groupId = groups.id
        WHERE
            notes.isDeleted = 0
            AND notes.groupId != '-1'
            AND (:query IS NULL OR notes.title LIKE '%' || :query || '%' OR notes.description LIKE '%' || :query || '%')
        ORDER BY notes.isPinned DESC, notes.createdAt DESC
    """)
    fun getNormalNotesWithGroups(query: String? = null): Flow<List<NoteWithGroupInfo>>

    // 2. Получение конкретно секретных заметок
    @Transaction
    @Query("""
        SELECT
            notes.*,
            NULL AS group_title,
            NULL AS group_color
        FROM notes
        WHERE notes.isDeleted = 0 AND notes.groupId = '-1'
        ORDER BY notes.isPinned DESC, notes.createdAt DESC
    """)
    fun getSecureNotes(): Flow<List<NoteWithGroupInfo>>

    // 3. Подсчет заметок для счетчика "Все" (Исключая секретные)
    @Query("SELECT COUNT(*) FROM notes WHERE isDeleted = 0 AND groupId != '-1'")
    fun getNonSecretNotesCount(): Flow<Int>

    // 4. Подсчет секретных заметок (для отображения на папке)
    @Query("SELECT COUNT(*) FROM notes WHERE isDeleted = 0 AND groupId = '-1'")
    fun getSecretNotesCount(): Flow<Int>

    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND groupId = :groupId ORDER BY isPinned DESC, createdAt DESC")
    fun getNotesByGroupId(groupId: String): Flow<List<Note>>

    @Query("""
    SELECT * FROM notes
    WHERE isDeleted = 0 AND groupId = :groupId
      AND (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
    ORDER BY isPinned DESC, createdAt DESC
    """)
    fun searchNotesInGroup(query: String, groupId: String): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: String): Flow<Note?>

    // --- КОРЗИНА ---

    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getTrashedNotes(): Flow<List<Note>>

    @Query("SELECT COUNT(*) FROM notes WHERE isDeleted = 1")
    fun getTrashedNotesCount(): Flow<Int>

    @Query("UPDATE notes SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long)

    @Query("UPDATE notes SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restore(id: String)

    // Заметки, которые пора удалить окончательно (старше порога) — чтобы сначала удалить их файлы
    @Query("SELECT * FROM notes WHERE isDeleted = 1 AND deletedAt IS NOT NULL AND deletedAt < :threshold")
    suspend fun getExpiredTrash(threshold: Long): List<Note>

    @Query("DELETE FROM notes WHERE isDeleted = 1 AND deletedAt IS NOT NULL AND deletedAt < :threshold")
    suspend fun deleteExpiredTrash(threshold: Long)

    @Query("SELECT * FROM notes WHERE isDeleted = 1")
    suspend fun getAllTrashedOnce(): List<Note>

    @Query("DELETE FROM notes WHERE isDeleted = 1")
    suspend fun emptyTrash()
}
