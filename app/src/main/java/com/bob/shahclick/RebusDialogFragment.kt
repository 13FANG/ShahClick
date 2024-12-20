package com.bob.shahclick

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class RebusDialogFragment : DialogFragment() {

    lateinit var gameData: GameData // Убираем private
    var listener: RebusDialogListener? = null

    // Добавляем companion object
    companion object {
        fun newInstance(gameData: GameData): RebusDialogFragment {
            val fragment = RebusDialogFragment()
            fragment.gameData = gameData
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as RebusDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement RebusDialogListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        gameData = GameData(requireContext())
        gameData.loadData()

        val currentRebus = gameData.getCurrentRebus()

        val view = layoutInflater.inflate(R.layout.dialog_rebus, null)
        val rebusImageView = view.findViewById<ImageView>(R.id.rebusImageView)
        val answerEditText = view.findViewById<EditText>(R.id.answerEditText)

        // Установка изображения ребуса
        val rebusImageId = resources.getIdentifier("rebus${currentRebus + 1}", "drawable", requireContext().packageName)
        if (rebusImageId != 0) {
            rebusImageView.setImageResource(rebusImageId)
        } else {
            // Обработка ошибки: изображение не найдено
            Log.e("RebusDialogFragment", "Rebus image not found for currentRebus: $currentRebus")
            // Можно установить изображение по умолчанию или показать сообщение об ошибке
            rebusImageView.setImageResource(R.drawable.crystal_small) //Или другое изображение
        }

        return activity?.let { activity ->
            val builder = AlertDialog.Builder(activity)
            builder.setView(view)
                .setTitle(R.string.rebus_title)
                .setPositiveButton(R.string.check) { dialog, id ->
                    Log.d("RebusDialogFragment", "Check button clicked")
                    val userAnswer = answerEditText.text.toString().trim().lowercase()

                    val rebusAnswers = resources.getStringArray(R.array.rebus_answers)
                    Log.d("RebusDialogFragment", "currentRebus: $currentRebus, userAnswer: $userAnswer, isRebusCompleted: ${gameData.isRebusCompleted()}")
                    if (currentRebus in 0 until rebusAnswers.size) {
                        if (userAnswer == rebusAnswers[currentRebus].lowercase() && !gameData.isRebusCompleted()) {
                            activity.runOnUiThread {
                                gameData.completeRebus()
                            }
                            Toast.makeText(activity, getString(R.string.rebus_correct_toast), Toast.LENGTH_SHORT).show()

                            // Сообщаем MainActivity о решении ребуса через интерфейс:
                            if (listener != null) {
                                listener?.onRebusSolved(gameData.getCrystals())
                            } else {
                                Log.e("RebusDialogFragment", "Listener is null!")
                            }
                        } else if (gameData.isRebusCompleted()) {
                            Toast.makeText(activity, getString(R.string.rebus_already_solved), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(activity, getString(R.string.rebus_incorrect_toast), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("RebusDialogFragment", "Invalid currentRebus: $currentRebus")
                    }
                }
                .setNegativeButton(R.string.cancel) { dialog, id ->
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}