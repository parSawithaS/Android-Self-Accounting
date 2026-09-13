package ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage.categoryMode

import ir.androidir.SelfAccounting.model.dataClasses.nonDataBase.Transaction2Item

interface Transaction2Event {
    fun clickShort(item :Transaction2Item)
}