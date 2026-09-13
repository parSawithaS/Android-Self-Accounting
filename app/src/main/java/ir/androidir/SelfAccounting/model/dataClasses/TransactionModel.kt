package ir.androidir.SelfAccounting.model.dataClasses

import androidx.room.Entity
import androidx.room.PrimaryKey
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.ModelsOfTransaction

@Entity
data class TransactionModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val mode: ModelsOfTransaction = ModelsOfTransaction.Income,
    val details: String = "",
    val value: String = "0",
    val date: String = "",
    val day: String = "",
    val month: String = "",
    val year: String = "",
    var category: String = "",
    var card: String = ""
)
