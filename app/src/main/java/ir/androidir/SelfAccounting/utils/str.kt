package ir.androidir.SelfAccounting.utils

import android.content.Context
import ir.androidir.SelfAccounting.R
import java.text.DecimalFormat

val convertStringValue: (String, Context) -> Long = { value, context ->
    val value1 = value.replace(context.getString(R.string.toman), "")
    val value2 = value1.replace(",", "").replace("٬", "")
    value2.replace(" ", "").toLong()
}

val setTxtFormat: (Long) -> String = {
    val format = DecimalFormat("#,###")
    format.format(it)
}