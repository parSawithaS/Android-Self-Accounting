package ir.androidir.SelfAccounting.model.dataClasses

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DebtModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String = "",
    val totalValue: Long = 0L,
    var paidValue: Long = 0L,
    val debtTo: String = "",
    var date: String = "",
    var dateFinish: String? = "",
    val turnsCount: Int? = 0,
    var paidTurns: Int? = 0,
    val turnValue: Long? = 0,
    val registerAutomatically: Int = 1,
    val intervalBetweenBorrowers: Int? = 1
)
