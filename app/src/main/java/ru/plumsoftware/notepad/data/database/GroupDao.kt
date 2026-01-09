package ru.plumsoftware.notepad.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.plumsoftware.notepad.data.model.Group

@Dao
interface GroupDao {

    @Transaction
    @Query("""
        SELECT 
            groups.*, 
            COUNT(notes.id) as noteCount 
        FROM groups 
        LEFT JOIN notes ON groups.id = notes.groupId 
        GROUP BY groups.id 
        ORDER BY groups.createdAt DESC
    """)
    fun getGroupsWithCounts(): Flow<List<GroupWithCount>>

    @Query("SELECT * FROM groups ORDER BY createdAt DESC")
    fun getAllGroups(): Flow<List<Group>>

    @Insert
    suspend fun insert(group: Group)

    @Update
    suspend fun update(group: Group)

    @Delete
    suspend fun delete(group: Group)
}