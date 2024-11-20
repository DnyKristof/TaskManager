package com.example.taskmanager

import androidx.lifecycle.LiveData

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: LiveData<List<Task>> = taskDao.getAllTasks()

    suspend fun insert(task: Task) {
        taskDao.insert(task)
    }

    suspend fun update(task: Task) {
        taskDao.update(task)
    }

    suspend fun delete(task: Task) {
        taskDao.delete(task)
    }

    fun getTasksByCompletionStatus(isCompleted: Boolean): LiveData<List<Task>> {
        return taskDao.getTasksByCompletionStatus(isCompleted)
    }
}
