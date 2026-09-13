package ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import ir.androidir.SelfAccounting.databinding.DialogDeleteTransactionBinding
import ir.androidir.SelfAccounting.model.dataClasses.TransactionModel
import ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage.DeleteTransactionEvent

fun deleteTransaction(
    viewOld: TransactionModel,
    position: Int,
    context: Context,
    event: DeleteTransactionEvent
) {
    val dialog = AlertDialog.Builder(context).create()
    val dialogBinding = DialogDeleteTransactionBinding.inflate(
        LayoutInflater.from(context)
    )


    dialog.setView(dialogBinding.root)
    dialog.show()


    dialogBinding.txtNo.setOnClickListener {
        dialog.dismiss()
    }
    dialogBinding.txtYes.setOnClickListener {
        dialog.dismiss()


        event.onDelete(viewOld, position)
    }
}

