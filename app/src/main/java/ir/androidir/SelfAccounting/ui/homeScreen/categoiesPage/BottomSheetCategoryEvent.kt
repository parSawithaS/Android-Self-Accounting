package ir.androidir.SelfAccounting.ui.homeScreen.categoiesPage

import ir.androidir.SelfAccounting.model.dataClasses.CategoryModel

interface BottomSheetCategoryEvent {
    fun moveItem(item :CategoryModel)
}