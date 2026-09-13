package ir.androidir.SelfAccounting.model.dataClasses

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CardModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val cardNumber: String = "0",
    var cardDefaultValue: String = "0",
    var cardValue: String = "0",
    val bank: String = "-",
)

