package ir.androidir.SelfAccounting.model.dataClasses.enumClasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class TransactionRadioMode :Parcelable {
    Transaction, Category, Card
}