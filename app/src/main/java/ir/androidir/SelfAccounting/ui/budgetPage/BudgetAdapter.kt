package ir.androidir.SelfAccounting.ui.budgetPage

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.ItemBudgetBinding
import ir.androidir.SelfAccounting.model.HesabchiDataBase
import ir.androidir.SelfAccounting.model.dao.BudgetDao
import ir.androidir.SelfAccounting.model.dao.CardDao
import ir.androidir.SelfAccounting.model.dao.CategoryDao
import ir.androidir.SelfAccounting.model.dao.TransactionDao
import ir.androidir.SelfAccounting.model.dataClasses.BudgetModel
import ir.androidir.SelfAccounting.model.dataClasses.TransactionModel
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.ModelsOfTransaction
import ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage.TransactionFragment
import ir.androidir.SelfAccounting.utils.BankSelector
import java.text.DecimalFormat

@SuppressLint("SetTextI18n")
class BudgetAdapter(
    private val BudgetData: ArrayList<BudgetModel>, val budgetEvent: BudgetEvent
) : RecyclerView.Adapter<BudgetAdapter.BudgetHolder>() {
    var myPosition: Int = 0

    inner class BudgetHolder(
        private val binding: ItemBudgetBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var transactionDao: TransactionDao
        private lateinit var budgetDao: BudgetDao
        private lateinit var categoryDao: CategoryDao
        private lateinit var cardDao: CardDao
        private var dateMode = 0        //0 = current date, 1 = user is in a past month
        private lateinit var yearAndMonth: Array<String>
        private lateinit var context: Context

        fun bindData() {
            context = binding.root.context
            val hesabchiDataBase = HesabchiDataBase.getDataBase(context)!!
            transactionDao = hesabchiDataBase.transactionDao
            budgetDao = hesabchiDataBase.budgetDao
            categoryDao = hesabchiDataBase.categoryDao
            cardDao = hesabchiDataBase.cardDao
            val fragment = TransactionFragment()
            yearAndMonth = arrayOf(fragment.getDate()[2], fragment.getDate()[1])
            binding.txtTopDate.text = "${yearAndMonth[1]} ${yearAndMonth[0]}"



            translateData()
            setDateButtons()
            calculateBudgetValues()
            setUI()
            seeTransactionsButton()
        }

        private fun seeTransactionsButton() {
            binding.btnSeeTransactions.root.setOnClickListener {
                budgetEvent.onCardClick(BudgetData[myPosition].budgetCategory)
            }
        }

        private fun translateData() {
            val context = binding.root.context!!
            val sharedPreferences =
                context.getSharedPreferences("sharedData", Context.MODE_PRIVATE)
            val languageTag = sharedPreferences.getString("languageTag", "fa")!!

            BudgetData.forEach {
                if (languageTag == "fa") {
                    if (it.budgetCard == "All")
                        it.budgetCard = "همه"
                } else {
                    if (it.budgetCard == "همه")
                        it.budgetCard = "All"
                }
            }
        }

        private fun setUI() {
            //set month data =>
            val monthData = arrayListOf<TransactionModel>()
            transactionDao.selectData().forEach {
                if (it.year == yearAndMonth[0] && it.month == yearAndMonth[1]) monthData.add(it)
            }


            //values =>
            val decimalFormat = DecimalFormat("#,###")
            binding.budgetName.text = BudgetData[myPosition].name
            binding.budgetValue.text = decimalFormat.format(BudgetData[myPosition].totalValue)
            binding.budgetCategory.text = BudgetData[myPosition].budgetCategory
            binding.budgetCard.text = BudgetData[myPosition].budgetCard

            val usedValue = BudgetData[myPosition].usedValue.toString()
                .replace("-", "").toLong()
            val otherValue = BudgetData[myPosition].totalValue - usedValue

            if (otherValue < 0) {
                binding.txtOtherMoneyValue.text = "0"
            } else
                binding.txtOtherMoneyValue.text = decimalFormat.format(otherValue)


            //category icon =>
            var categoryIcon = 0
            var color = android.R.color.transparent
            categoryDao.selectData().forEach {
                if (it.name == BudgetData[myPosition].budgetCategory) {
                    categoryIcon = it.icon
                    color = it.color
                }
            }
            binding.imgIconCategory.setImageResource(categoryIcon)
            binding.imgIconCategory.setColorFilter(
                ContextCompat.getColor(
                    binding.root.context,
                    color
                )
            )


            //card icon =>
            if (BudgetData[myPosition].budgetCard == context.getString(R.string.all))
                BudgetData[myPosition].budgetCard = "0000"
            var cardIcon = 0
            cardDao.selectData().forEach {
                val cardNumber = it.cardNumber.substring(
                    12,
                    16
                )
                if (cardNumber == BudgetData[myPosition].budgetCard.substring(0, 4)) {
                    cardIcon = BankSelector().selectBankImage(it.cardNumber.substring(0, 6))
                }
            }
            binding.imgIconCard.setImageResource(cardIcon)


            //set precent =>
            binding.txtUsedValue.text = decimalFormat.format(usedValue)
            try {
                val usedPrecent = (usedValue * 100) / (usedValue + otherValue)
                binding.txtUsedPrecent.text = "$usedPrecent%"


                val otherMoneyPrecent = 100L - usedPrecent
                if (otherMoneyPrecent < 0)
                    binding.txtOtherMoneyPrecent.text = "0%"
                else
                    binding.txtOtherMoneyPrecent.text = "$otherMoneyPrecent%"


                binding.budgetProgress.progress = usedPrecent.toInt()
            } catch (_: Exception) {
                binding.txtUsedPrecent.text = "0%"
                binding.txtOtherMoneyPrecent.text = "0%"
                binding.budgetProgress.progress = 0
            }


            //clickers =>
            itemView.setOnClickListener {
                budgetEvent.clickShort(BudgetData[myPosition])
            }
            itemView.setOnLongClickListener {
                budgetEvent.clickLong(BudgetData[myPosition], adapterPosition)
                true
            }
            binding.btnDelete.setOnClickListener {
                budgetEvent.clickLong(BudgetData[myPosition], adapterPosition)
            }
        }

        private fun calculateBudgetValues() {
            val monthData = arrayListOf<TransactionModel>()
            transactionDao.selectData().forEach {
                if (it.year == yearAndMonth[0] && it.month == yearAndMonth[1]) monthData.add(it)
            }


            var value = 0L
            monthData.forEach {
                if (it.category == BudgetData[myPosition].budgetCategory) {
                    value += when (it.mode) {
                        ModelsOfTransaction.Expense -> -(it.value.replace(",", "")
                            .toLong())

                        else -> 0L
                    }
                }

            }
            BudgetData[myPosition].usedValue = value
            Thread {
                budgetDao.updateData(BudgetData[myPosition])
            }.start()
        }

        private fun setDateButtons() {
            val sharedPreferences =
                binding.root.context.getSharedPreferences("sharedData", Context.MODE_PRIVATE)
            val themeMode = sharedPreferences.getString("theme", "light")!!

            val fragment = TransactionFragment()
            val nowDate = fragment.getDate()[1] + " ${fragment.getDate()[2]}"

            dateMode = if (nowDate == binding.txtTopDate.text.toString()) {
                if (themeMode == "light") {
                    binding.btnNext.setImageResource(R.drawable.arrow_left_gray)
                    binding.btnPrevious.setImageResource(R.drawable.arrow_right_black)
                } else {
                    binding.btnNext.setImageResource(R.drawable.arrow_left_gray)
                    binding.btnPrevious.setImageResource(R.drawable.arrow_right_white)
                }
                0
            } else {
                if (themeMode == "light") {
                    binding.btnNext.setImageResource(R.drawable.arrow_left_black)
                    binding.btnPrevious.setImageResource(R.drawable.arrow_right_black)
                } else {
                    binding.btnNext.setImageResource(R.drawable.arrow_left_white)
                    binding.btnPrevious.setImageResource(R.drawable.arrow_right_white)
                }
                1
            }


            val appDate = yearAndMonth[1] + " ${yearAndMonth[0]}"
            binding.btnNext.setOnClickListener {
                if (dateMode == 1)
                    nextDate(appDate)
            }

            binding.btnPrevious.setOnClickListener {
                previousDate(appDate)
            }
        }

        private fun previousDate(appDate: String) {
            val newMonth = when (appDate.split(" ")[0]) {
                "فروردین" -> "اسفند"
                "اردیبهشت" -> "فروردین"
                "خرداد" -> "اردیبهشت"
                "تیر" -> "خرداد"
                "مرداد" -> "تیر"
                "شهریور" -> "مرداد"
                "مهر" -> "شهریور"
                "آبان" -> "مهر"
                "آذر" -> "آبان"
                "دی" -> "آذر"
                "بهمن" -> "دی"
                "اسفند" -> "بهمن"
                else -> ""
            }
            var newYear = appDate.split(" ")[1]
            if (newMonth == "اسفند") newYear = (newYear.toInt() - 1).toString()


            yearAndMonth = arrayOf(newYear, newMonth)

            binding.txtTopDate.text = "${yearAndMonth[1]} ${yearAndMonth[0]}"
            calculateBudgetValues()
            setUI()
            setDateButtons()
            TransactionFragment().updateDate(context, yearAndMonth)
        }

        private fun nextDate(appDate: String) {
            val newMonth = when (appDate.split(" ")[0]) {
                "فروردین" -> "اردیبهشت"
                "اردیبهشت" -> "خرداد"
                "خرداد" -> "تیر"
                "تیر" -> "مرداد"
                "مرداد" -> "شهریور"
                "شهریور" -> "مهر"
                "مهر" -> "آبان"
                "آبان" -> "آذر"
                "آذر" -> "دی"
                "دی" -> "بهمن"
                "بهمن" -> "اسفند"
                "اسفند" -> "فروردین"
                else -> ""
            }
            var newYear = appDate.split(" ")[1]
            if (newMonth == "فروردین") newYear = (newYear.toInt() + 1).toString()


            yearAndMonth = arrayOf(newYear, newMonth)

            binding.txtTopDate.text = "${yearAndMonth[1]} ${yearAndMonth[0]}"
            calculateBudgetValues()
            setUI()
            setDateButtons()
            TransactionFragment().updateDate(context,yearAndMonth)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemBudgetBinding.inflate(layoutInflater, parent, false)
        return BudgetHolder(binding)
    }

    override fun onBindViewHolder(holder: BudgetHolder, position: Int) {
        myPosition = position
        holder.bindData()
    }

    override fun getItemCount(): Int {
        return BudgetData.size
    }

    fun deleteItem(item: BudgetModel, position: Int) {
        BudgetData.remove(item)
        notifyItemRemoved(position)
    }
}
