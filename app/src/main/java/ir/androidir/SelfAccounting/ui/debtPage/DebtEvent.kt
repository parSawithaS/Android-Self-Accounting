package ir.androidir.SelfAccounting.ui.debtPage

import ir.androidir.SelfAccounting.model.dataClasses.DebtModel


interface DebtEvent {
    fun onClick(item :DebtModel)
    fun longClick(item: DebtModel ,position: Int)
}