package ir.androidir.SelfAccounting.ui.chooseItemBsh

interface ChooseEvent {
    //0 = category --- card = 1
    fun onClick(itemId: Int, itemMode: Int)
}