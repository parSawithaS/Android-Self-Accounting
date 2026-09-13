package ir.androidir.SelfAccounting.ui.budgetPage

import ir.androidir.SelfAccounting.model.dataClasses.BudgetModel

interface BudgetEvent {
    fun clickShort(item: BudgetModel)
    fun clickLong(viewOld: BudgetModel, position: Int)
    fun onCardClick(name: String)
}