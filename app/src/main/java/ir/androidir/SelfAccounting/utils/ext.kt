package ir.androidir.SelfAccounting.utils

import android.content.SharedPreferences

fun SharedPreferences.getSubscription(): Boolean {
    return getBoolean("subscriptionIsAvailable", false)
}

fun String.substring2(startIndex: Int, endIndex: Int): String {
    return if (this.length >= endIndex) {
        substring(startIndex,endIndex)
    } else {
        this
    }
}
