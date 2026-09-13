package ir.androidir.SelfAccounting.ui.homeScreen.categoiesPage

import ir.androidir.SelfAccounting.model.dataClasses.CategoryModel
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.ModelsOfTransaction

interface CategoryEvent {
    fun clickShort(item: CategoryModel)
    fun clickLong(viewOld: CategoryModel, position: Int)
    fun onCardClick(name: String ,tMode: ModelsOfTransaction) {}
}