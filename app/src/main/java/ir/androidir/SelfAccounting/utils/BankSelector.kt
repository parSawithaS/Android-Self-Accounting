package ir.androidir.SelfAccounting.utils

import ir.androidir.SelfAccounting.R

class BankSelector {
    fun selectBank(cardNumber: String): String {
        return when (cardNumber.replace("-", "").substring2(0, 6).toInt().toString()) {
            "603799" -> "بانک ملی"
            "589210" -> "بانک سپه"
            "627648" -> "بانک توسعه صادرات"
            "627961" -> "بانک صنعت و معدن"
            "603770" -> "بانک کشاورزی"
            "628023" -> "بانک مسکن"
            "627760" -> "پست بانک"
            "502908" -> "بانک توسعه تعاون"
            "627412" -> "بانک اقتصاد نوین"
            "622106" -> "بانک پارسیان"
            "502229" -> "بانک پاسارگاد"
            "627488" -> "بانک کارآفرین"
            "621986" -> "بانک سامان"
            "639346" -> "بانک سینا"
            "639607" -> "بانک سرمایه"
            "504706" -> "بانک شهر"
            "502938" -> "بانک دی"
            "603769" -> "بانک صادرات"
            "610433" -> "بانک ملت"
            "627353" -> "بانک تجارت"
            "589463" -> "بانک رفاه"
            "606277" -> "موسسه ملل"
            "507677" -> "موسسه نور"
            "606373" -> "بانک قرض الحسنه مهر ایرانیان"
            "505416" -> "بانک گردشگری"
            "504172" -> "بانک رسالت"
            else -> ""
        }
    }

    fun selectBankImage(bin: String): Int {
        return when(bin){
            "603799" -> R.drawable.melli
            "589210" -> R.drawable.sepah
            "627648" -> R.drawable.tose_saderat
            "627961" -> R.drawable.sanatm
            "603770" -> R.drawable.keshavarzi
            "628023" -> R.drawable.maskan
            "627760" -> R.drawable.postbank
            "502908" -> R.drawable.tose_tavan
            "627412" -> R.drawable.eghtesad_novin
            "622106" -> R.drawable.parsian
            "502229" -> R.drawable.pasargad
            "627488" -> R.drawable.kar_afarin
            "621986" -> R.drawable.saman
            "639346" -> R.drawable.sina
            "639607" -> R.drawable.sarmaye
            "504706" -> R.drawable.shahr
            "502938" -> R.drawable.dey
            "603769" -> R.drawable.saderat
            "610433" -> R.drawable.melat
            "627353" -> R.drawable.tejarat
            "589463" -> R.drawable.refah
            "606277" -> R.drawable.mellal
            "507677" -> R.drawable.noor
            "606373" -> R.drawable.gharz
            "505416" -> R.drawable.gardeshgari
            "504172" -> R.drawable.resalat
            else -> 0
        }
    }
}
