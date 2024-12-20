package com.bob.shahclick

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class UpgradesActivity : AppCompatActivity() {

    private lateinit var upgradesRecyclerView: RecyclerView
    private lateinit var upgradesAdapter: UpgradesAdapter

    private lateinit var gameData: GameData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upgrades)

        gameData = GameData(this)
        gameData.loadData() // Загружаем данные, чтобы знать текущие уровни улучшений

        upgradesRecyclerView = findViewById(R.id.upgradesRecyclerView)
        upgradesRecyclerView.layoutManager = LinearLayoutManager(this)

        upgradesAdapter = UpgradesAdapter(gameData.getUpgrades(), gameData) {
            // Обновляем TextView с количеством кристаллов после покупки
            val crystalsTextView = (this.parent as? MainActivity)?.findViewById<TextView>(R.id.crystalsTextView)
            crystalsTextView?.text = getString(R.string.crystals_count, gameData.getCrystals())
            upgradesAdapter.notifyDataSetChanged()
        }
        upgradesRecyclerView.adapter = upgradesAdapter

    }

    override fun onResume() {
        super.onResume()
        gameData.loadData()
        upgradesAdapter.notifyDataSetChanged()
    }
}