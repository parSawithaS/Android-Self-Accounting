package ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage

import ir.androidir.SelfAccounting.model.dataClasses.TransactionModel

interface TransactionEvent {
    fun clickShort(item: TransactionModel)
    fun clickLong(viewOld: TransactionModel, position: Int)
}