package com.bob.shahclick

data class Upgrade(
    val name: String,
    val baseCost: Int,
    val crystalsPerClickIncrease: Int,
    var level: Int
) {
    fun getCurrentCost(): Int {
        return (baseCost * (1 + 0.2 * level)).toInt() //Пример формулы увеличения стоимости
    }
}