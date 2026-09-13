package ir.androidir.SelfAccounting.utils.date

import android.annotation.SuppressLint
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date

object DateUtils {
    @SuppressLint("SimpleDateFormat")
    fun getDate(context: Context, mode: Int = 0): Array<String> {
        val sharedPreferences =
            context.getSharedPreferences("sharedData", Context.MODE_PRIVATE)
        val defaultDate = sharedPreferences.getString("date", "")!!


        val gregorianDate = arrayOf(
            SimpleDateFormat("dd").format(Date()).toInt(),
            SimpleDateFormat("M").format(Date()).toInt(),
            SimpleDateFormat("yyyy").format(Date()).toInt()
        )

        //day , month , year
        val date =
            DateConvertor().gregorianToJalali(gregorianDate[2], gregorianDate[1], gregorianDate[0])
        return if (mode == 0) {
            if (defaultDate != "") {
                val splitDate = defaultDate.split(" ")
                arrayOf(date[0], splitDate[1], splitDate[0])
            } else {
                date
            }
        } else
            DateConvertor().gregorianToJalali(gregorianDate[2], gregorianDate[1], gregorianDate[0], 1)

    }
}