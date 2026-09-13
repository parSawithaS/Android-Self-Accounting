package ir.androidir.SelfAccounting.model.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ir.androidir.SelfAccounting.model.dataClasses.BudgetModel
import ir.androidir.SelfAccounting.model.dataClasses.CardModel
import ir.androidir.SelfAccounting.model.dataClasses.CategoryModel

@Dao
interface CardDao :BaseDao<CardModel> {

    @Query("SELECT  * FROM CardModel")
    fun selectData() :List<CardModel>

    @Query("DELETE FROM CardModel")
    fun deleteAll()

    @Transaction
    fun insertAll(list: List<CardModel>) {
        list.forEach {
            insertData(it)
        }
    }

}