package ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage

import ir.androidir.SelfAccounting.model.dataClasses.TransactionModel

interface DeleteTransactionEvent {
    fun onDelete(viewOld: TransactionModel, position: Int)
}
