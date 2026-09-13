package ir.androidir.SelfAccounting.ui.homeScreen.cardsPage

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.ItemCardBinding
import ir.androidir.SelfAccounting.model.HesabchiDataBase
import ir.androidir.SelfAccounting.model.dao.CardDao
import ir.androidir.SelfAccounting.model.dao.TransactionDao
import ir.androidir.SelfAccounting.model.dataClasses.CardModel
import ir.androidir.SelfAccounting.utils.BankSelector
import ir.androidir.SelfAccounting.utils.setCardValues
import java.text.DecimalFormat

class CardAdapter(
    private val CardData: ArrayList<CardModel>, val cardEvent: CardEvent
) : RecyclerView.Adapter<CardAdapter.CardHolder>() {

    inner class CardHolder(
        private val binding: ItemCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var transactionDao: TransactionDao
        private lateinit var cardDao: CardDao

        @SuppressLint("SetTextI18n")
        fun bindData(position: Int) {
            //set variables =>
            val hesabchiDataBase = HesabchiDataBase.getDataBase(binding.root.context)!!
            transactionDao = hesabchiDataBase.transactionDao
            cardDao = hesabchiDataBase.cardDao
            val decimalFormat = DecimalFormat("#,###")
            val decimalFormat2 = DecimalFormat("#,####")
            val sharedPreferences =
                binding.root.context.getSharedPreferences("sharedData", Context.MODE_PRIVATE)
            val language = sharedPreferences.getString("languageTag", "fa")

            val itemCard = setCardValues(CardData[position], transactionDao, cardDao)

            //general data =>
            setImage(position)
            binding.txtBankName.text = itemCard.bank
            binding.txtCardNumber.text = decimalFormat2.format(itemCard.cardNumber.toLong())
                .replace(",", "-")
                .replace("٬", "-")
            binding.txtDefaultCardValue.text = itemCard.cardDefaultValue


            //set transaction value =>
            val transactionsValue: Long
            val transactionsValueBeta = itemCard.cardValue
                .replace(",", "")
                .replace("٬", "")
            transactionsValue = if (transactionsValueBeta.contains('-')) {
                -(transactionsValueBeta.replace("-", "").toLong())
            } else
                transactionsValueBeta.replace("−", "").toLong()


            if (transactionsValue < 0) {
                binding.txtTransactionsValue.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context, R.color.textColorRed
                    )
                )
            }

            if (transactionsValue < 0) {
                val negativeValue = transactionsValue.toString().replace("-", "").toLong()

                if (language == "fa") {
                    binding.txtTransactionsValue.text =
                        "${decimalFormat.format(negativeValue)}-"
                } else {
                    binding.txtTransactionsValue.text =
                        "-${decimalFormat.format(negativeValue)}"
                }
            } else
                binding.txtTransactionsValue.text = decimalFormat.format(transactionsValue)


            //clickers :
            itemView.setOnClickListener {
                cardEvent.clickShort(itemCard)
            }
            itemView.setOnLongClickListener {
                cardEvent.clickLong(itemCard, adapterPosition)
                true
            }
            binding.btnDelete.setOnClickListener {
                cardEvent.clickLong(itemCard, adapterPosition)
            }


            //set color base on total value for text view
            val defValue = itemCard.cardDefaultValue
                .replace(",", "")
                .replace("٬", "")
            val defaultValue = if (defValue.contains('-')) {
                -(defValue.replace("-", "").toLong())
            } else
                defValue.replace("−", "").toLong()


            val totalValue = defaultValue + transactionsValue

            if (totalValue < 0) {
                binding.txtCardValue.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context, R.color.textColorRed
                    )
                )
            }


            //set and format total value(if it's more than 3 number)
            binding.txtCardValue.text = decimalFormat.format(totalValue)

            if (totalValue < 0) {
                val negativeValue = totalValue.toString().replace("-", "").toLong()

                if (language == "fa") {
                    binding.txtCardValue.text =
                        "${decimalFormat.format(negativeValue)}-"
                } else {
                    binding.txtCardValue.text =
                        "-${decimalFormat.format(negativeValue)}"
                }
            } else
                binding.txtCardValue.text =
                    decimalFormat.format(totalValue)
        }

        private fun setImage(position: Int) {
            val image = BankSelector().selectBankImage(
                CardData[position].cardNumber.replace("-", "").substring(0, 6).toInt().toString()
            )
            if (image != 0) binding.imgBank.setImageResource(image)
            else binding.imgBank.visibility = View.GONE

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemCardBinding.inflate(layoutInflater, parent, false)
        return CardHolder(binding)
    }

    override fun onBindViewHolder(holder: CardHolder, position: Int) {
        holder.bindData(position)
    }

    override fun getItemCount(): Int {
        return CardData.size
    }

    fun deleteItem(item: CardModel, position: Int) {
        CardData.remove(item)
        notifyItemRemoved(position)
    }
}