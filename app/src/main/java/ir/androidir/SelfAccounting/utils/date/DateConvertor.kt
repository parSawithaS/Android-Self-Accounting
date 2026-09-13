package ir.androidir.SelfAccounting.utils.date

class DateConvertor {

    fun gregorianToJalali(gy: Int, gm: Int, gd: Int , mode :Int = 0): Array<String> {
        val g_d_m = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2: Int = if (gm > 2) (gy + 1) else gy
        var days: Int =
            355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd + g_d_m[gm - 1]
        var jy: Int = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += ((days - 1) / 365)
            days = (days - 1) % 365
        }
        var jm: Int
        var jd: Int
        if (days < 186) {
            jm = 1 + (days / 31)
            jd = 1 + (days % 31)
        } else {
            jm = 7 + ((days - 186) / 30)
            jd = 1 + ((days - 186) % 30)
        }
        val txtJm = when (jm) {
            1 -> "فروردین"
            2 -> "اردیبهشت"
            3 -> "خرداد"
            4 -> "تیر"
            5 -> "مرداد"
            6 -> "شهریور"
            7 -> "مهر"
            8 -> "آبان"
            9 -> "آذر"
            10 -> "دی"
            11 -> "بهمن"
            12 -> "اسفند"
            else -> ""
        }

        return if (mode == 0)
            arrayOf(jd.toString() , txtJm , jy.toString())
        else
            arrayOf(jd.toString() , jm.toString() , jy.toString())
    }

    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): IntArray {
        var jy1: Int = jy + 1595
        var days: Int =
            -355668 + (365 * jy1) + ((jy1 / 33).toInt() * 8) + (((jy1 % 33) + 3) / 4).toInt() + jd + (if (jm < 7) ((jm - 1) * 31) else (((jm - 7) * 30) + 186))
        var gy: Int = 400 * (days / 146097).toInt()
        days %= 146097
        if (days > 36524) {
            gy += 100 * (--days / 36524).toInt()
            days %= 36524
            if (days >= 365) days++
        }
        gy += 4 * (days / 1461).toInt()
        days %= 1461
        if (days > 365) {
            gy += ((days - 1) / 365).toInt()
            days = (days - 1) % 365
        }
        var gd: Int = days + 1
        var sal_a: IntArray = intArrayOf(
            0,
            31,
            if ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0)) 29 else 28,
            31,
            30,
            31,
            30,
            31,
            31,
            30,
            31,
            30,
            31
        )
        var gm: Int = 0
        while (gm < 13 && gd > sal_a[gm]) gd -= sal_a[gm++]
        return intArrayOf(gy, gm, gd)
    }

}