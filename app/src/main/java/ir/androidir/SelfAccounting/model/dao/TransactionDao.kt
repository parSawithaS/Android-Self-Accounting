package ir.androidir.SelfAccounting.model.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ir.androidir.SelfAccounting.model.dataClasses.BudgetModel
import ir.androidir.SelfAccounting.model.dataClasses.CategoryModel
import ir.androidir.SelfAccounting.model.dataClasses.TransactionModel

@Dao
interface TransactionDao :BaseDao<TransactionModel> {

    @Query("SELECT  * FROM TransactionModel")
    fun selectData() :List<TransactionModel>

    @Query("DELETE FROM TransactionModel")
    fun deleteAll()

    @Transaction
    fun insertAll(list: List<TransactionModel>) {
        list.forEach {
            insertData(it)
        }
    }

}