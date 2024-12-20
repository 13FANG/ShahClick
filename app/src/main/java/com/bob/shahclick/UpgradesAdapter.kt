package com.bob.shahclick

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UpgradesAdapter(
    private var upgrades: List<Upgrade>,
    private var gameData: GameData,
    private val onUpgradeBought: () -> Unit
) : RecyclerView.Adapter<UpgradesAdapter.UpgradeViewHolder>() {

    class UpgradeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.upgradeNameTextView)
        val costTextView: TextView = view.findViewById(R.id.upgradeCostTextView)
        val levelTextView: TextView = view.findViewById(R.id.upgradeLevelTextView)
        val buyButton: Button = view.findViewById(R.id.buyUpgradeButton)
        val infoTextView: TextView = view.findViewById(R.id.upgradeInfoTextView) // Добавлено
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpgradeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.upgrade_item, parent, false)
        return UpgradeViewHolder(view)
    }

    override fun onBindViewHolder(holder: UpgradeViewHolder, position: Int) {
        val upgrade = upgrades[position]
        holder.nameTextView.text = upgrade.name
        holder.costTextView.text = holder.itemView.context.getString(R.string.upgrade_cost, upgrade.getCurrentCost())
        holder.levelTextView.text = holder.itemView.context.getString(R.string.upgrade_level, upgrade.level)
        holder.infoTextView.text = holder.itemView.context.getString(R.string.upgrade_info, upgrade.crystalsPerClickIncrease) // Добавлено
        holder.buyButton.setOnClickListener {
            if (gameData.getCrystals() >= upgrade.getCurrentCost()) {
                gameData.buyUpgrade(upgrade)
                notifyItemChanged(position)
                onUpgradeBought()
            }
        }
    }

    override fun getItemCount() = upgrades.size
}