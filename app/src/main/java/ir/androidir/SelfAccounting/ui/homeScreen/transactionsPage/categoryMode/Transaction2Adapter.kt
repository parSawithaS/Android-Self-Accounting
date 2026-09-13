package ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage.categoryMode

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.ItemCategory2Binding
import ir.androidir.SelfAccounting.model.dataClasses.nonDataBase.Transaction2Item
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.TransactionRadioMode
import ir.androidir.SelfAccounting.utils.getColor
import ir.androidir.SelfAccounting.utils.setTxtFormat
import java.text.DecimalFormat

class Transaction2Adapter(
    private val Transaction2Data: ArrayList<Transaction2Item>,
    val transactionEvent: Transaction2Event
) :
    RecyclerView.Adapter<Transaction2Adapter.Transaction2Holder>() {

    inner class Transaction2Holder(
        val binding: ItemCategory2Binding
    ) :
        RecyclerView.ViewHolder(binding.root) {
        val context = binding.root.context!!
        var dataTranslated = false

        fun translateData(position: Int) {
            val context = binding.root.context!!
            val sharedPreferences =
                context.getSharedPreferences("sharedData", Context.MODE_PRIVATE)
            val languageTag = sharedPreferences.getString("languageTag", "fa")!!
            val item = Transaction2Data[position]

            if (languageTag == "fa") {
                if (item.name == "No Category")
                    Transaction2Data[position].name = "بدون دسته بندی"
            } else {
                if (item.name == "بدون دسته بندی")
                    Transaction2Data[position].name = "No Category"
            }

            dataTranslated = true
        }

        @SuppressLint("SetTextI18n")
        fun bindDataCategory(position: Int) {
            val item = Transaction2Data[position]
            val sharedPreferences = context.getSharedPreferences("sharedData", Context.MODE_PRIVATE)
            val language = sharedPreferences.getString("language","fa")


            //set total value color text =>
            val totalValue = item.value.toString()
            val color = if (totalValue.contains('-')) {
                R.color.textColorRed
            } else if(totalValue == "0")
                R.color.gray
            else
                R.color.textColorGreen

            binding.txtTotalValue2.setTextColor(getColor(color, context))


            //if total value was negative =>
            if (totalValue.toLong() < 0) {
                val negativeValue = totalValue.replace("-", "").toLong()
                if (language == "fa")
                    binding.txtTotalValue2.text = "${setTxtFormat(negativeValue)}-"
                else
                    binding.txtTotalValue2.text = "-${setTxtFormat(negativeValue)}"
            } else
                binding.txtTotalValue2.text = setTxtFormat(totalValue.toLong())


            //set name based on card or category =>
            if (item.mode == TransactionRadioMode.Category || item.name == context.getString(R.string.all))
                binding.txtName2.text = item.name
            else if (item.mode == TransactionRadioMode.Card) {
                val decimalFormat = DecimalFormat("#,####")
                val txt = decimalFormat.format(item.name.toLong())
                    .replace(",", "-")
                    .replace("٬", "-")
                binding.txtName2.text = txt
            }


            //set icon based on card or category =>
            if (item.icon == 0)
                binding.imgIcon2.visibility = View.GONE
            else {
                binding.imgIcon2.setImageResource(item.icon)
                if (item.iconColor != 0)
                    binding.imgIcon2.setColorFilter(getColor(item.iconColor, context))
            }


            //click events =>
            itemView.setOnClickListener {
                transactionEvent.clickShort(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Transaction2Holder {
        return Transaction2Holder(
            ItemCategory2Binding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Transaction2Holder, position: Int) {
        if (!holder.dataTranslated)
            holder.translateData(position)
        holder.bindDataCategory(position)
    }

    override fun getItemCount(): Int {
        //categories plus one for "without category" transactions
        return Transaction2Data.size
    }
}