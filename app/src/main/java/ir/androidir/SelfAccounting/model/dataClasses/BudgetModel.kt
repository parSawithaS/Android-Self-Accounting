package ir.androidir.SelfAccounting.model.dataClasses

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BudgetModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String = "",
    var totalValue: Long = 0L,
    var budgetCategory: String = "",
    var budgetCard: String = "",
    var usedValue: Long = 0L
)

