package com.bob.shahclick

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.util.Calendar

class GameData(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("game_data", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    private var crystals: Int = 0
    private var crystalsPerClick: Int = 1
    private var record: Int = 0

    // Для 2-й версии:
    private var currentDailyTask: Int = 0
    private var currentRebus: Int = 0
    private var isDailyTaskCompleted: Boolean = false
    private var isRebusCompleted: Boolean = false
    private var lastDailyReset: Long = 0
    private var clicksSinceLastDailyReset: Int = 0
    private var boughtUpgrades: Int = 0

    // Уровни улучшений
    private val upgrades = listOf(
        Upgrade("Upgrade 1", 10, 1, 0),
        Upgrade("Upgrade 2", 100, 5, 0),
        Upgrade("Upgrade 3", 500, 25, 0),
        Upgrade("Upgrade 4", 5000, 100, 0),
        Upgrade("Upgrade 5", 15000, 300, 0),
        Upgrade("Upgrade 6", 40000, 750, 0),
        Upgrade("Upgrade 7", 100000, 1500, 0)
    )

    init {
        loadData()
    }

    fun loadData() {
        crystals = sharedPreferences.getInt("crystals", 0)
        crystalsPerClick = sharedPreferences.getInt("crystalsPerClick", 1)
        record = sharedPreferences.getInt("record", 0)
        clicksSinceLastDailyReset = sharedPreferences.getInt("clicksSinceLastDailyReset", 0)
        boughtUpgrades = sharedPreferences.getInt("boughtUpgrades", 0)

        // Для 2-й версии:
        currentDailyTask = sharedPreferences.getInt("currentDailyTask", 0) // Загружаем currentDailyTask, по умолчанию 0
        if (currentDailyTask == -1) currentDailyTask = 0 //Если все же currentDailyTask оказался равен -1, то присваиваем ему 0
        currentRebus = sharedPreferences.getInt("currentRebus", -1)
        isDailyTaskCompleted = sharedPreferences.getBoolean("isDailyTaskCompleted", false)
        isRebusCompleted = sharedPreferences.getBoolean("isRebusCompleted", false)
        lastDailyReset = sharedPreferences.getLong("lastDailyReset", 0)

        for (upgrade in upgrades) {
            upgrade.level = sharedPreferences.getInt(upgrade.name + "_level", 0)
        }
        Log.d("GameData", "Data loaded. currentDailyTask: $currentDailyTask") // Логируем currentDailyTask после загрузки
    }

    fun saveData() {
        try {
            editor.putInt("crystals", crystals)
            editor.putInt("crystalsPerClick", crystalsPerClick)
            editor.putInt("record", record)
            editor.putInt("clicksSinceLastDailyReset", clicksSinceLastDailyReset)
            editor.putInt("boughtUpgrades", boughtUpgrades)

            // Для 2-й версии:
            editor.putInt("currentDailyTask", currentDailyTask)
            editor.putInt("currentRebus", currentRebus)
            editor.putBoolean("isDailyTaskCompleted", isDailyTaskCompleted)
            editor.putBoolean("isRebusCompleted", isRebusCompleted)
            editor.putLong("lastDailyReset", lastDailyReset)

            for (upgrade in upgrades) {
                editor.putInt(upgrade.name + "_level", upgrade.level)
            }
            editor.apply()
            Log.d("GameData", "Data saved successfully")
        } catch (e: Exception) {
            Log.e("GameData", "Error saving data: ${e.message}")
        }
    }

    fun getCrystals(): Int {
        Log.d("GameData", "getCrystals called, returning: $crystals")
        return crystals
    }

    fun addCrystals(amount: Int) {
        crystals += amount
        if (crystals > record) {
            record = crystals
        }
        clicksSinceLastDailyReset++
    }

    fun getCrystalsPerClick(): Int {
        return crystalsPerClick
    }

    fun buyUpgrade(upgrade: Upgrade) {
        if (crystals >= upgrade.getCurrentCost()) {
            crystals -= upgrade.getCurrentCost()
            upgrade.level++
            crystalsPerClick += upgrade.crystalsPerClickIncrease
            boughtUpgrades++
            saveData()
        }
    }

    fun getUpgrades(): List<Upgrade> {
        return upgrades
    }

    fun getClicksSinceLastDailyReset(): Int {
        return clicksSinceLastDailyReset
    }

    fun getBoughtUpgrades(): Int {
        return boughtUpgrades
    }

    // Методы для 2-й версии:
    fun getCurrentDailyTask(): Int {
        return currentDailyTask
    }

    fun getCurrentRebus(): Int {
        return currentRebus
    }

    fun isDailyTaskCompleted(): Boolean {
        return isDailyTaskCompleted
    }

    fun isRebusCompleted(): Boolean {
        //Возвращаем значение из SharedPreferences
        return sharedPreferences.getBoolean("isRebusCompleted", false)
    }

    fun completeDailyTask() {
        crystalsPerClick += (currentDailyTask + 1)
        isDailyTaskCompleted = true
        saveData()
    }

    fun completeRebus() {
        Log.d("GameData", "completeRebus called")
        crystals += 1000
        isRebusCompleted = true
        saveData() // Сохраняем изменения
        Log.d("GameData", "completeRebus: isRebusCompleted set to true and data saved")
    }

    fun isNewDay(): Boolean {
        // Способ 1 (для тестирования): Каждый запуск приложения считается новым днём
        // Раскомментируй следующую строку, чтобы включить тестовый режим:
        // return true
        // Способ 2 (оригинальный): Проверка по дате
        // Закомментируй код ниже, если раскомментировал тестовый режим выше:
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        Log.d("GameData", "isNewDay called. today: ${today.timeInMillis}, lastDailyReset: $lastDailyReset")

        if (today.timeInMillis > lastDailyReset) {
            lastDailyReset = today.timeInMillis
            Log.d("GameData", "isNewDay: lastDailyReset: $lastDailyReset")
            // Генерируем новые задания и ребусы, только если наступил новый день

            currentDailyTask = (0 until 10).random() // Убедитесь, что currentDailyTask всегда в диапазоне 0-9

            if (currentRebus == -1){
                currentRebus = (0 until 5).random()
            }
            isDailyTaskCompleted = false
            isRebusCompleted = false
            clicksSinceLastDailyReset = 0
            boughtUpgrades = 0
            Log.d("GameData", "New day detected. currentDailyTask: $currentDailyTask") // Логируем currentDailyTask после определения нового дня
            return true
        }
        return false

    }

    fun getRecord(): Int {
        return record
    }
}