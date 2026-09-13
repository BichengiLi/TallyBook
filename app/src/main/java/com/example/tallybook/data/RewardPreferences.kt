package com.example.tallybook.data

import android.content.Context

class RewardPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getRewardAmount(): Double = prefs.getFloat(KEY_AMOUNT, DEFAULT_AMOUNT.toFloat()).toDouble()

    fun setRewardAmount(amount: Double) {
        prefs.edit().putFloat(KEY_AMOUNT, amount.toFloat()).apply()
    }

    companion object {
        private const val PREFS_NAME = "reward_prefs"
        private const val KEY_AMOUNT = "reward_amount"
        const val DEFAULT_AMOUNT = 5.0
    }
}
