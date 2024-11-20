package com.example.taskmanager

import android.os.Bundle
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskmanager.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var adapter: TaskAdapter
    private lateinit var database: TaskDatabase
    private lateinit var taskDao: TaskDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = TaskDatabase.getDatabase(this)
        taskDao = database.taskDao()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)

        adapter = TaskAdapter { task, action -> handleTaskAction(task, action) }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        taskViewModel.allTasks.observe(this) { tasks ->
            adapter.submitList(tasks)
        }

        binding.fab.setOnClickListener {
            showTaskDialog(null)
        }
    }

    private fun handleTaskAction(task: Task, action: String) {
        when (action) {
            "delete" -> lifecycleScope.launch(Dispatchers.IO) { taskDao.delete(task) }
            "edit" -> showTaskDialog(task)
            "toggle" -> lifecycleScope.launch(Dispatchers.IO) {
                taskDao.update(task.copy(isCompleted = !task.isCompleted))
            }
        }
    }

    private fun showTaskDialog(task: Task?) {
        val dialogBinding = layoutInflater.inflate(R.layout.dialog_add_task, null)

        val titleInput = dialogBinding.findViewById<EditText>(R.id.etTaskTitle)
        val descriptionInput = dialogBinding.findViewById<EditText>(R.id.etTaskDescription)
        val prioritySpinner = dialogBinding.findViewById<Spinner>(R.id.spinnerPriority)
        val completedCheckbox = dialogBinding.findViewById<CheckBox>(R.id.cbCompleted)

        task?.let {
            titleInput.setText(it.title)
            descriptionInput.setText(it.description)
            prioritySpinner.setSelection(it.priority - 1)
            completedCheckbox.isChecked = it.isCompleted
        }

        MaterialAlertDialogBuilder(this)
            .setView(dialogBinding)
            .setTitle(if (task == null) "Add Task" else "Edit Task")
            .setPositiveButton("Save") { _, _ ->
                val title = titleInput.text.toString().trim()
                val description = descriptionInput.text.toString().trim()
                val priority = prioritySpinner.selectedItemPosition + 1
                val isCompleted = completedCheckbox.isChecked

                if (title.isNotEmpty()) {
                    val newTask = Task(
                        id = task?.id ?: 0,
                        title = title,
                        description = description,
                        priority = priority,
                        isCompleted = isCompleted
                    )
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (task == null) taskDao.insert(newTask) else taskDao.update(newTask)
                    }
                } else {
                    Toast.makeText(this, "Task title is required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
