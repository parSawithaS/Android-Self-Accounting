package ir.androidir.SelfAccounting.ui.subscriptionPage

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import ir.androidir.SelfAccounting.databinding.DialogNeedSubscriptionBinding
import ir.androidir.SelfAccounting.model.HesabchiDataBase
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.LimitType
import ir.androidir.SelfAccounting.utils.getSubscription

object SubscriptionManager{
    val showNeedToSubscriptionDialog: (Context) -> Unit = { context ->
        val dialog = AlertDialog.Builder(context).create()
        val dialogBinding = DialogNeedSubscriptionBinding.inflate(
            LayoutInflater.from(context)
        )

        dialog.setView(dialogBinding.root)
        dialog.show()

        dialogBinding.txtCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.txtPurchase.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(context, SubscriptionActivity::class.java)
            context.startActivity(intent)
        }
    }

    fun isPlanOk(
        mode: LimitType,
        context: Context
    ): Boolean {
        val sharedPreferences =
            context.getSharedPreferences("sharedData", Activity.MODE_PRIVATE)
        val hesabchiDataBase = HesabchiDataBase.getDataBase(context)!!
        val isSubscriptionAvailable = sharedPreferences.getSubscription()


        return if (!isSubscriptionAvailable) {
            return when (mode) {
                LimitType.Transaction -> {
                    val dao = hesabchiDataBase.transactionDao
                    (dao.selectData().size + 1) <= 30
                }

                LimitType.Category -> {
                    val dao = hesabchiDataBase.categoryDao
                    (dao.selectData().size + 1) <= 12
                }

                LimitType.Card -> {
                    val dao = hesabchiDataBase.cardDao
                    (dao.selectData().size + 1) <= 5
                }

                LimitType.Debt -> {
                    val dao = hesabchiDataBase.debtDao
                    (dao.selectData().size + 1) <= 3
                }

                LimitType.Budget -> {
                    val dao = hesabchiDataBase.budgetDao
                    (dao.selectData().size + 1) <= 3
                }
            }
        } else {
            true
        }
    }
}
