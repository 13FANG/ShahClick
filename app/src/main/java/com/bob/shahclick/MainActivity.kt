package com.bob.shahclick

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.Button
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout

class MainActivity : AppCompatActivity(), RebusDialogListener {

    private lateinit var crystalImageView: ImageView
    private lateinit var crystalsTextView: TextView
    private lateinit var upgradesButton: Button
    private lateinit var recordTextView: TextView
    private lateinit var crystalsPerClickTextView: TextView
    private lateinit var dailyTaskTextView: TextView

    private lateinit var gameData: GameData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        crystalImageView = findViewById(R.id.crystalImageView)
        crystalsTextView = findViewById(R.id.crystalsTextView)
        upgradesButton = findViewById(R.id.upgradesButton)
        recordTextView = findViewById(R.id.recordTextView)
        crystalsPerClickTextView = findViewById(R.id.crystalsPerClickTextView)
        dailyTaskTextView = findViewById(R.id.dailyTaskTextView)

        gameData = GameData(this)

        crystalsTextView.text = getString(R.string.crystals_count, gameData.getCrystals())
        crystalsPerClickTextView.text = getString(R.string.crystals_per_click, gameData.getCrystalsPerClick())

        if (gameData.isNewDay()) {
            showDailyTaskDialog()
            showRebusDialog()
        }
        updateDailyTaskTextView()

        crystalImageView.setOnClickListener {
            animateCrystalClick()
            gameData.addCrystals(gameData.getCrystalsPerClick())
            crystalsTextView.text = getString(R.string.crystals_count, gameData.getCrystals())
            recordTextView.text = getString(R.string.record, gameData.getRecord())
            crystalsPerClickTextView.text = getString(R.string.crystals_per_click, gameData.getCrystalsPerClick())
            checkDailyTaskCompletion()
        }

        upgradesButton.setOnClickListener {
            startActivity(Intent(this, UpgradesActivity::class.java))
        }

        recordTextView.text = getString(R.string.record, gameData.getRecord())
    }

    private fun animateCrystalClick() {
        // Анимация увеличения и уменьшения размера
        val scaleUpX = ObjectAnimator.ofFloat(crystalImageView, "scaleX", 1f, 1.1f)
        val scaleUpY = ObjectAnimator.ofFloat(crystalImageView, "scaleY", 1f, 1.1f)
        val scaleDownX = ObjectAnimator.ofFloat(crystalImageView, "scaleX", 1.1f, 1f)
        val scaleDownY = ObjectAnimator.ofFloat(crystalImageView, "scaleY", 1.1f, 1f)

        val scaleUp = AnimatorSet().apply {
            play(scaleUpX).with(scaleUpY)
            duration = 100
        }

        val scaleDown = AnimatorSet().apply {
            play(scaleDownX).with(scaleDownY)
            duration = 100
        }

        AnimatorSet().apply {
            playSequentially(scaleUp, scaleDown)
            start()
        }

        // Анимация разлетающихся кристаллов
        val maxParticles = 50
        val numParticles = if (gameData.getCrystalsPerClick() > maxParticles) maxParticles else gameData.getCrystalsPerClick()
        for (i in 0 until numParticles) {
            createFlyingCrystal()
        }
    }

    private fun createFlyingCrystal() {
        val flyingCrystal = ImageView(this)
        flyingCrystal.setImageResource(R.drawable.crystal_small) // Убедитесь, что у вас есть изображение crystal_small
        val size = resources.getDimensionPixelSize(R.dimen.small_crystal_size) // Размер маленького кристалла
        val layoutParams = ConstraintLayout.LayoutParams(size, size)
        flyingCrystal.layoutParams = layoutParams

        // Устанавливаем начальную позицию кристалла на большой кристалл
        val crystalImageViewLocation = IntArray(2)
        crystalImageView.getLocationOnScreen(crystalImageViewLocation)
        val startX = crystalImageViewLocation[0] + crystalImageView.width / 2 - size / 2
        val startY = crystalImageViewLocation[1] + crystalImageView.height / 2 - size / 2

        (crystalImageView.parent as ConstraintLayout).addView(flyingCrystal)

        flyingCrystal.x = startX.toFloat()
        flyingCrystal.y = startY.toFloat()

        // Случайное направление полета
        val endX = startX + (Math.random() * 400 - 200).toInt()
        val endY = startY - (Math.random() * 400).toInt()

        val animX = ObjectAnimator.ofFloat(flyingCrystal, "x", startX.toFloat(), endX.toFloat())
        val animY = ObjectAnimator.ofFloat(flyingCrystal, "y", startY.toFloat(), endY.toFloat())
        val alpha = ObjectAnimator.ofFloat(flyingCrystal, "alpha", 1f, 0f)

        AnimatorSet().apply {
            playTogether(animX, animY, alpha)
            duration = (500 + Math.random() * 500).toLong() // Случайное время анимации
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    (flyingCrystal.parent as ViewGroup).removeView(flyingCrystal)
                }
            })
            start()
        }
    }

    private fun showDailyTaskDialog() {
        val dialog = DailyTaskDialogFragment()
        dialog.show(supportFragmentManager, "DailyTaskDialogFragment")
    }

    private fun showRebusDialog() {
        val dialog = RebusDialogFragment()
        dialog.show(supportFragmentManager, "RebusDialogFragment")
    }

    private fun updateDailyTaskTextView() {
        val tasks = resources.getStringArray(R.array.daily_tasks)
        val currentTask = gameData.getCurrentDailyTask()

        // Проверка на валидный индекс
        val taskIndex = if (currentTask in tasks.indices) {
            currentTask
        } else {
            Log.w("MainActivity", "Invalid currentDailyTask: $currentTask, using 0 as default")
            0 // Или другое значение по умолчанию в пределах допустимого диапазона
        }

        if (!gameData.isDailyTaskCompleted()) {
            dailyTaskTextView.text = getString(R.string.daily_task_template, tasks[taskIndex]) // Используем taskIndex
            Log.d("MainActivity", "Daily task updated: ${tasks[taskIndex]}")
        } else {
            dailyTaskTextView.text = getString(R.string.daily_task_completed)
            Log.d("MainActivity", "Daily task completed")
        }
    }

    private fun checkDailyTaskCompletion() {
        if (!gameData.isDailyTaskCompleted()) {
            val tasks = resources.getStringArray(R.array.daily_tasks)
            val currentTask = gameData.getCurrentDailyTask()
            val taskCompleted = when (currentTask) {
                0, 1, 2 -> gameData.getClicksSinceLastDailyReset() >= getRequiredClicksForTask(currentTask)
                3, 4, 5 -> gameData.getCrystals() >= getRequiredCrystalsForTask(currentTask)
                6, 7 -> gameData.getBoughtUpgrades() >= getRequiredUpgradesForTask(currentTask)
                8, 9 -> gameData.getCrystalsPerClick() >= getRequiredClicksPerClickForTask(currentTask)
                else -> false
            }

            if (taskCompleted) {
                gameData.completeDailyTask()
                updateDailyTaskTextView()
                // Дополнительные действия при выполнении задания, например, показ уведомления
            }
        }
    }

    private fun getRequiredClicksForTask(taskId: Int): Int {
        return when (taskId) {
            0 -> 50
            1 -> 100
            2 -> 200
            else -> 0
        }
    }

    private fun getRequiredCrystalsForTask(taskId: Int): Int {
        return when (taskId) {
            3 -> 500
            4 -> 1000
            5 -> 2000
            else -> 0
        }
    }

    private fun getRequiredUpgradesForTask(taskId: Int): Int {
        return when (taskId) {
            6 -> 3
            7 -> 5
            else -> 0
        }
    }

    private fun getRequiredClicksPerClickForTask(taskId: Int): Int {
        return when (taskId) {
            8 -> 5
            9 -> 10
            else -> 0
        }
    }

    override fun onResume() {
        super.onResume()
        gameData.loadData()
        crystalsTextView.text = getString(R.string.crystals_count, gameData.getCrystals())
        crystalsPerClickTextView.text = getString(R.string.crystals_per_click, gameData.getCrystalsPerClick())
        updateDailyTaskTextView()
    }

    override fun onPause() {
        super.onPause()
        gameData.saveData()
    }

    override fun onRebusSolved(crystals: Int) {
        Log.d("MainActivity", "onRebusSolved called with crystals: $crystals")
        runOnUiThread {
            crystalsTextView.text = getString(R.string.crystals_count, crystals)
        }
    }
}