package ir.androidir.SelfAccounting.ui

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import ir.androidir.SelfAccounting.model.dataClasses.TransactionModel
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.ModelsOfTransaction
import ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage.BottomSheetAddTransaction
import ir.androidir.SelfAccounting.utils.BottomSheetEvent

object FragmentManager {
    fun openTransaction(
        item: TransactionModel,
        childFragmentManager: FragmentManager,
        event: BottomSheetEvent
    ) {
        val bottomSheetFragment = BottomSheetAddTransaction(event)

        val mode = when (item.mode) {
            ModelsOfTransaction.Income -> "income"
            ModelsOfTransaction.Expense -> "expense"
            else -> "transfer"

        }
        val bundle = Bundle()
        bundle.putStringArray(
            "item", arrayOf(
                mode,
                item.value,
                item.details,
                item.id.toString(),
                item.date,
                item.year,
                item.month,
                item.day,
                item.category,
                item.card
            )
        )
        bottomSheetFragment.arguments = bundle


        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }

}