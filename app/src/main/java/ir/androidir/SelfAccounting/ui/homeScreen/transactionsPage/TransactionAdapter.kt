package ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.ItemTransactionBinding
import ir.androidir.SelfAccounting.model.HesabchiDataBase
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.ModelsOfTransaction
import ir.androidir.SelfAccounting.model.dataClasses.TransactionModel
import ir.androidir.SelfAccounting.utils.BankSelector

class TransactionAdapter(
    val TransactionData: ArrayList<TransactionModel>,
    val transactionEvent: TransactionEvent,
) :
    RecyclerView.Adapter<TransactionAdapter.TransactionHolder>() {

    inner class TransactionHolder(
        val binding: ItemTransactionBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {
        val context = binding.root.context!!

        @SuppressLint("SetTextI18n")
        fun bindDataTransaction(position: Int) {
            binding.textCategory.text = context.getString(R.string.category)
            binding.textCard.text = context.getString(R.string.card)


            //Set mode =>
            when (TransactionData[position].mode) {
                ModelsOfTransaction.Income -> {
                    binding.txtMode.text = context.getString(R.string.income)
                    binding.txtMode.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.textColorGreen
                        )
                    )
                    binding.txtValue.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.textColorGreen
                        )
                    )

                }

                ModelsOfTransaction.Expense -> {
                    binding.txtMode.text = context.getString(R.string.expense)
                    binding.txtMode.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.textColorRed
                        )
                    )
                    binding.txtValue.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.textColorRed
                        )
                    )
                }

                else -> {
                    binding.txtMode.text = context.getString(R.string.transfer)
                    binding.txtMode.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.colorSecondaryContainer
                        )
                    )
                    binding.txtValue.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.colorSecondaryContainer
                        )
                    )

                    binding.textCategory.text = context.getString(R.string.from_card)
                    binding.textCard.text = context.getString(R.string.to_card)
                }
            }


            //General information
            binding.txtValue.text = TransactionData[position].value
            binding.txtDetails.text = TransactionData[position].details
            binding.txtDate.text = TransactionData[position].date
            binding.txtCategory.text = TransactionData[position].category
            binding.txtCard.text = TransactionData[position].card

            //Icons =>
            val card = TransactionData[position].card
            val category = TransactionData[position].category

            if (TransactionData[position].mode == ModelsOfTransaction.Transfer) {
                setCardIcon(category, 1)
            } else
                setCategoryIcon(category)

            if (card != context.getString(R.string.all))
                setCardIcon(card)


            //Clickers =>
            itemView.setOnClickListener {
                transactionEvent.clickShort(TransactionData[position])
            }
            itemView.setOnLongClickListener {
                transactionEvent.clickLong(TransactionData[position], adapterPosition)
                true
            }
            binding.btnDelete.setOnClickListener {
                transactionEvent.clickLong(TransactionData[position], adapterPosition)
            }
        }

        private fun setCategoryIcon(category: String) {
            val dao = HesabchiDataBase.getDataBase(binding.root.context)!!.categoryDao

            dao.selectData().forEach {
                if (it.name == category) {
                    binding.imgCategory.setImageResource(it.icon)
                    binding.imgCategory.setColorFilter(
                        ContextCompat.getColor(
                            binding.root.context, it.color
                        )
                    )
                }
            }
        }

        private fun setCardIcon(card: String, mode: Int = 0) {
            val cardDao = HesabchiDataBase.getDataBase(binding.root.context)!!.cardDao

            cardDao.selectData().forEach {
                val cardNumber = it.cardNumber.substring(12, 16)
                if (cardNumber == card.substring(0, 4)) {
                    when (mode) {
                        0 -> {
                            binding.imgTransactionCard.setImageResource(
                                BankSelector().selectBankImage(it.cardNumber.substring(0, 6))
                            )
                        }

                        1 -> {
                            binding.imgCategory.setImageResource(
                                BankSelector().selectBankImage(it.cardNumber.substring(0, 6))
                            )
                        }
                    }

                }
            }
        }

        fun translateData(position: Int) {
            val context = binding.root.context!!
            val sharedPreferences =
                context.getSharedPreferences("sharedData", Context.MODE_PRIVATE)
            val languageTag = sharedPreferences.getString("languageTag", "fa")!!
            val item = TransactionData[position]

            if (languageTag == "fa") {
                if (item.card == "All")
                    TransactionData[position].card = "همه"

                if (item.category == "No Category")
                    TransactionData[position].category = "بدون دسته بندی"
            } else {
                if (item.card == "همه")
                    TransactionData[position].card = "All"

                if (item.category == "بدون دسته بندی")
                    TransactionData[position].category = "No Category"

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionHolder {
        return TransactionHolder(
                ItemTransactionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
    }

    override fun onBindViewHolder(holder: TransactionHolder, position: Int) {
        holder.translateData(position)
        holder.bindDataTransaction(position)
    }

    override fun getItemCount(): Int {
        return TransactionData.size
    }

    fun deleteItem(item: TransactionModel, position: Int) {
        TransactionData.remove(item)
        notifyItemRemoved(position)
    }
}
