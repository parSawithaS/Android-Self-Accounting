package ir.androidir.SelfAccounting.utils

import android.content.Context
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.FragmentAddBudgetBinding
import ir.androidir.SelfAccounting.databinding.FragmentAddCardBinding
import ir.androidir.SelfAccounting.databinding.FragmentAddDebtBinding
import ir.androidir.SelfAccounting.databinding.FragmentAddTransactionBinding

class ItemHandler {
    private lateinit var context: Context
    private var messageToBack = "ok"

    fun debtHandler(binding: FragmentAddDebtBinding): String {
        val debtName = binding.edtTxtName.text.toString()
        val debtTo = binding.edtTxtDebtTo.text.toString()
        val debtValue = binding.edtTxtDebtValue.text.toString().ifBlank { "0" }
            .replace(",", "")
            .replace("٬", "")
            .toLong()
        val paidValue = binding.edtTxtPaidValue.text.toString().ifBlank { "0" }
            .replace(",", "")
            .replace("٬", "")
            .toLong()
        val turnsCount = binding.edtTxtBorrowerCount.text.toString().ifBlank { "0" }.toInt()
        val paidTurns = binding.edtTxtPaidBorrower.text.toString().ifBlank { "0" }.toInt()
        val turnValue = binding.edtTxtBorrowerValue.text.toString().ifBlank { "0" }
            .replace(",", "")
            .replace("٬", "")
            .toLong()
        context = binding.root.context


        if (paidValue > debtValue)
            messageToBack = context.getString(R.string.paid_and_total_value_error)
        if (debtName.isEmpty() ||
            debtTo.isEmpty() ||
            debtValue == 0L ||
            turnsCount.toString() == "0" ||
            turnValue.toString() == "0" ||
            turnsCount.toString() == "0" ||
            turnValue.toString() == "0"
        )
            messageToBack = context.getString(R.string.please_enter_all_values)

        if (turnsCount < paidTurns)
            messageToBack =
                context.getString(R.string.paid_installments_cannot_be_more_than_all_installments)
        if (turnValue > debtValue)
            messageToBack =
                context.getString(R.string.installment_value_cannot_be_more_than_total_value)

        if (!checkDates(binding))
            messageToBack = context.getString(R.string.please_enter_date_correctly)

        return messageToBack
    }

    private fun checkDates(binding: FragmentAddDebtBinding): Boolean {
        val startDate = binding.txtStartDate.text.toString()
        val finishDate = binding.txtFinishDate.text.toString()

        if (finishDate == context.getString(R.string.unknown))
            return false
        else {
            val startMonthName = startDate.split(" ")[1]
            val finishMonthName = finishDate.split(" ")[1]

            val startMonthNumber = when (startMonthName) {
                "فروردین" -> 1
                "اردیبهشت" -> 2
                "خرداد" -> 3
                "تیر" -> 4
                "مرداد" -> 5
                "شهریور" -> 6
                "مهر" -> 7
                "آبان" -> 8
                "آذر" -> 9
                "دی" -> 10
                "بهمن" -> 11
                "اسفند" -> 12
                else -> 1
            }
            val finishMonthNumber = when (finishMonthName) {
                "فروردین" -> 1
                "اردیبهشت" -> 2
                "خرداد" -> 3
                "تیر" -> 4
                "مرداد" -> 5
                "شهریور" -> 6
                "مهر" -> 7
                "آبان" -> 8
                "آذر" -> 9
                "دی" -> 10
                "بهمن" -> 11
                "اسفند" -> 12
                else -> 1
            }

            val startYear = startDate.split(" ")[2].toInt()
            val finishYear = finishDate.split(" ")[2].toInt()

            val startDay = startDate.split(" ")[0].toInt()
            val finishDay = finishDate.split(" ")[0].toInt()


            if (finishYear == startYear) {
                if (finishMonthNumber >= startMonthNumber && finishDay >= startDay)
                    return true
            }

            if (finishYear > startYear){
                return true
            }

            return false
        }
    }

    fun transactionHandler(binding: FragmentAddTransactionBinding): String {
        context = binding.root.context
        val card = binding.txtCard.text.toString()
        val category = binding.txtCategory.text.toString()
        val value = binding.edtTxtValue.text.toString()


        if (value.isEmpty())
            messageToBack = context.getString(R.string.please_enter_transaction_value)
        if (card == context.getString(R.string.without_card) ||
            category == context.getString(R.string.without_card)
        )
            messageToBack = context.getString(R.string.please_choose_two_cards)
        if (card == category)
            messageToBack = context.getString(R.string.please_choose_difrent_cards)



        return messageToBack
    }

    fun cardHandler(binding: FragmentAddCardBinding): String {
        context = binding.root.context
        val cardNumber = binding.edtTxtCardNumber.text.toString()


        if (cardNumber.length != 19)
            messageToBack = context.getString(R.string.card_number_is_too_short)
        else {
            if (BankSelector().selectBank(cardNumber) == "")
                messageToBack = context.getString(R.string.please_enter_card_number_curectly)
        }
        if (cardNumber.isEmpty())
            messageToBack = context.getString(R.string.plese_enter_card_number)



        return messageToBack
    }

    fun budgetHandler(binding: FragmentAddBudgetBinding): String {
        context = binding.root.context
        val budgetValue = binding.edtTxtBudgetValue.text.toString()
        val budgetName = binding.edtTxtName.text.toString()
        val category = binding.txtCategory.text.toString()

        if (budgetValue.isEmpty())
            messageToBack = context.getString(R.string.please_enter_value)
        if (budgetName.isEmpty())
            messageToBack = context.getString(R.string.please_enter_budget_name)
        if (category == context.getString(R.string.without_category))
            messageToBack = context.getString(R.string.please_enter_category)


        return messageToBack
    }
}
