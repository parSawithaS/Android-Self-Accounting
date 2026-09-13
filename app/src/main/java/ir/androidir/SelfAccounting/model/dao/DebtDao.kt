package ir.androidir.SelfAccounting.model.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ir.androidir.SelfAccounting.model.dataClasses.BudgetModel
import ir.androidir.SelfAccounting.model.dataClasses.CategoryModel
import ir.androidir.SelfAccounting.model.dataClasses.DebtModel

@Dao
interface DebtDao :BaseDao<DebtModel> {

    @Query("SELECT * FROM DebtModel")
    fun selectData() :List<DebtModel>

    @Query("DELETE FROM DebtModel")
    fun deleteAll()

    @Transaction
    fun insertAll(list: List<DebtModel>) {
        list.forEach {
            insertData(it)
        }
    }

}