package ir.androidir.SelfAccounting.ui.debtPage

interface DateEvent {
    //mode : 0 = start date change, 1 = end date change
    fun onDateChange(newDate :String , mode: Int)
}