package ir.androidir.SelfAccounting.model.dataClasses

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.CategoryType
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class CategoryModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String = "",
    val totalValue: Long = 0,
    val valueIncomes: Long = 0,
    val valueExpenses: Long = 0,
    val totalTransactionsCount: Int = 0,
    val incomesCount: Int = 0,
    val expensesCount: Int = 0,
    var color: Int = R.color.black,
    var icon: Int = 0,
    var type: String = CategoryType.All.toString()
) :Parcelable
