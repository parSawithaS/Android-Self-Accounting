package ir.androidir.SelfAccounting.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import ir.androidir.SelfAccounting.model.dataClasses.CategoryModel

@Dao
interface BaseDao<I> {
    @Insert
    fun insertData(item: I)

    @Update
    fun updateData(item: I)

    @Delete
    fun deleteData(item: I)
}