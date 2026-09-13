package ir.androidir.SelfAccounting.model.dataClasses.nonDataBase


data class PackedTransactionModel(
    val name: String = "",
    val allTransactions: Int = 0,
    val incomes: Int = 0,
    val expenses: Int = 0,
    val incomeValue: String = "",
    val expensesValue: String = "",
    val totalValue: String = ""
)
