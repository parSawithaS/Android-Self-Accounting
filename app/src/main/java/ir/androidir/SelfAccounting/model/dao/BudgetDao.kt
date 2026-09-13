package ir.androidir.SelfAccounting.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import ir.androidir.SelfAccounting.model.dataClasses.BudgetModel
import ir.androidir.SelfAccounting.model.dataClasses.CategoryModel

@Dao
interface BudgetDao : BaseDao<BudgetModel> {

    @Query("SELECT * FROM BudgetModel")
    fun selectData() :List<BudgetModel>

    @Query("DELETE FROM BudgetModel")
    fun deleteAll()

    @Transaction
    fun insertAll(list: List<BudgetModel>) {
        list.forEach {
            insertData(it)
        }
    }

}
