package com.bob.shahclick

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class DailyTaskDialogFragment : DialogFragment() {

    private lateinit var gameData: GameData

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        gameData = GameData(requireContext())
        gameData.loadData()

        val tasks = resources.getStringArray(R.array.daily_tasks)
        // Проверяем, что currentDailyTask находится в допустимом диапазоне
        val currentTask = gameData.getCurrentDailyTask()
        val taskIndex = if (currentTask in tasks.indices) {
            currentTask
        } else {
            Log.w("DailyTaskDialogFragment", "Invalid currentDailyTask: $currentTask, using 0 as default")
            0 // Или другое значение по умолчанию в пределах допустимого диапазона
        }

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.daily_task_title)
                .setMessage(tasks[taskIndex]) // Используем taskIndex вместо currentTask
                .setPositiveButton(R.string.ok) { dialog, id ->
                    //Удаление не нужной части кода
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}