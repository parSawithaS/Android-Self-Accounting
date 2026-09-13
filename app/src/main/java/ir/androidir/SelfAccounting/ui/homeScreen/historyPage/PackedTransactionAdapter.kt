package ir.androidir.SelfAccounting.ui.homeScreen.historyPage

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.ItemTransactionPackedBinding
import ir.androidir.SelfAccounting.model.dataClasses.nonDataBase.PackedTransactionModel
import ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage.TransactionEvent
import java.text.DecimalFormat

class PackedTransactionAdapter(
    private val TransactionData: PackedTransactionModel,
    val transactionEvent: TransactionEvent
) :
    RecyclerView.Adapter<PackedTransactionAdapter.TransactionHolder>() {

    inner class TransactionHolder(
        private val binding: ItemTransactionPackedBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bindData() {
            val context = binding.root.context

            binding.name.text = TransactionData.name
            binding.allTransactions.text = TransactionData.allTransactions.toString() +
                    " ${context.getString(R.string.transaction)}"
            binding.incomes.text = TransactionData.incomes.toString() +
                    " ${context.getString(R.string.income)}"
            binding.expenses.text = TransactionData.expenses.toString() +
                    " ${context.getString(R.string.expense)}"

            binding.valueOfIncomes.text = TransactionData.incomeValue
            binding.valueOfExpenses.text = TransactionData.expensesValue.replace("-", "")

            //set total value =>
            val sharedPreferences =
                binding.root.context.getSharedPreferences("sharedData", Context.MODE_PRIVATE)
            val language = sharedPreferences.getString("languageTag", "fa")!!
            val decimalFormat = DecimalFormat("#,###")
            val totalData = TransactionData.totalValue
                .replace(",", "")
                .replace("٬", "")
                .toLong()

            if (totalData < 0) {
                val negativeValue = totalData.toString().replace("-", "").toLong()

                if (language == "fa")
                    binding.totalValue.text = "${decimalFormat.format(negativeValue)}-"
                else
                    binding.totalValue.text = "-${decimalFormat.format(negativeValue)}"
            } else
                binding.totalValue.text = decimalFormat.format(totalData)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionHolder {
        return TransactionHolder(
            ItemTransactionPackedBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TransactionHolder, position: Int) {
        holder.bindData()
    }

    override fun getItemCount(): Int {
        return 1
    }
}