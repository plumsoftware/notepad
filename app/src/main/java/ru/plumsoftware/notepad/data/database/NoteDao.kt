package ru.plumsoftware.notepad.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.plumsoftware.notepad.data.model.Note

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchNotes(query: String): Flow<List<Note>>

    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE (:groupId IS NULL OR groupId = :groupId) ORDER BY createdAt DESC")
    fun getNotesFilteredByGroup(groupId: String?): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE groupId = :groupId ORDER BY createdAt DESC")
    fun getNotesByGroupId(groupId: String): Flow<List<Note>>

    @Query("""
    SELECT * FROM notes 
    WHERE groupId = :groupId 
      AND (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
    ORDER BY createdAt DESC
    """)
    fun searchNotesInGroup(query: String, groupId: String): Flow<List<Note>>

    @Insert
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)
}