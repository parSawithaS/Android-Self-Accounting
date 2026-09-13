package ir.androidir.SelfAccounting.model.dataClasses.nonDataBase

import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.TransactionRadioMode

data class Transaction2Item(
    var name: String,
    val icon: Int,
    val iconColor: Int,
    var value: Long,
    val mode: TransactionRadioMode
)
