package ir.androidir.SelfAccounting.model.dataClasses.nonDataBase

import ir.androidir.SelfAccounting.model.dataClasses.BudgetModel
import ir.androidir.SelfAccounting.model.dataClasses.CardModel
import ir.androidir.SelfAccounting.model.dataClasses.CategoryModel
import ir.androidir.SelfAccounting.model.dataClasses.DebtModel
import ir.androidir.SelfAccounting.model.dataClasses.TransactionModel

data class BackupFile(
    val BudgetData :List<BudgetModel>,
    val CardData :List<CardModel>,
    val CategoryData :List<CategoryModel>,
    val DebtData :List<DebtModel>,
    val TransactionData :List<TransactionModel>
)
