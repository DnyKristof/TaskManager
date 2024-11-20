package com.example.taskmanager

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insert(task: Task): Long

    @Update
     fun update(task: Task): Int

    @Delete
     fun delete(task: Task) : Int

    @Query("SELECT * FROM tasks ORDER BY priority ASC")
    fun getAllTasks(): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = :isCompleted ORDER BY priority ASC")
    fun getTasksByCompletionStatus(isCompleted: Boolean): LiveData<List<Task>>
}
