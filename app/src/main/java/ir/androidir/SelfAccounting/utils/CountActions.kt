package ir.androidir.SelfAccounting.utils

import android.app.Activity
import android.content.Context

interface CountActions {
    fun addAction(context :Context){
        val sharedPref =
            context.getSharedPreferences("sharedData" ,Activity.MODE_PRIVATE)
        val previousAmount = sharedPref.getInt("pagesOpened" , 0)
        sharedPref.edit().putInt("pagesOpened" , previousAmount + 1).apply()
    }
}