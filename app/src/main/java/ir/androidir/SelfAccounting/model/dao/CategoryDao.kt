package ir.androidir.SelfAccounting.model.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ir.androidir.SelfAccounting.model.dataClasses.CategoryModel

@Dao
interface CategoryDao : BaseDao<CategoryModel> {

    @Query("SELECT * FROM CategoryModel")
    fun selectData(): List<CategoryModel>

    @Query("DELETE FROM CategoryModel")
    fun deleteAll()

    @Transaction
    fun insertAll(list: List<CategoryModel>) {
        list.forEach {
            insertData(it)
        }
    }

}