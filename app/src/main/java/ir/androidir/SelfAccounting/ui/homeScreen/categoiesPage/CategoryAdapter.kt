package ir.androidir.SelfAccounting.ui.homeScreen.categoiesPage

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.ItemCategoryBinding
import ir.androidir.SelfAccounting.model.HesabchiDataBase
import ir.androidir.SelfAccounting.model.dao.CategoryDao
import ir.androidir.SelfAccounting.model.dao.TransactionDao
import ir.androidir.SelfAccounting.model.dataClasses.CategoryModel
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.ModelsOfTransaction
import ir.androidir.SelfAccounting.utils.SHARED_PREFERENCES_TAG
import ir.androidir.SelfAccounting.utils.setCategoryValues
import ir.androidir.SelfAccounting.utils.setTxtFormat
import java.text.DecimalFormat

class CategoryAdapter(
    private val CategoryData: ArrayList<CategoryModel>,
    val categoryEvent: CategoryEvent
) : RecyclerView.Adapter<CategoryAdapter.CategoryHolder>() {

    inner class CategoryHolder(
        private val binding: ItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var transactionDao: TransactionDao
        private lateinit var categoryDao: CategoryDao
        private lateinit var sharedPreferences: SharedPreferences
        private val hesabchiDataBase = HesabchiDataBase.getDataBase(binding.root.context)!!

        @SuppressLint("SetTextI18n")
        fun bindData(position: Int) {
            //set variables =>
            transactionDao = hesabchiDataBase.transactionDao
            categoryDao = hesabchiDataBase.categoryDao
            sharedPreferences =
                binding.root.context.getSharedPreferences(SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE)
            val itemCategory =
                setCategoryValues(CategoryData[position], transactionDao, categoryDao)
            var buttonMode =
                sharedPreferences.getBoolean(CategoryData[position].id.toString(), false)

            val themeMode = sharedPreferences.getString("theme", "light")!!
            val language = sharedPreferences.getString("languageTag", "fa")!!


            val incomeValue = itemCategory.valueIncomes
            val expenseValue = itemCategory.valueExpenses.toString().replace("-", "").toLong()
            val decimalFormat = DecimalFormat("#,###")

            val context = binding.root.context


            //general information =>
            binding.txtName.text = itemCategory.name
            binding.txtIncomesCount.text =
                itemCategory.incomesCount.toString() + " ${context.getString(R.string.income)}"
            binding.txtExpensesCount.text =
                itemCategory.expensesCount.toString() + " ${context.getString(R.string.expense)}"
            binding.txtTransactionsCount.text =
                itemCategory.totalTransactionsCount.toString() +
                        " ${context.getString(R.string.transaction)}"
            binding.txtIncomeValue.text = decimalFormat.format(incomeValue) +
                    " ${context.getString(R.string.toman)}"

            binding.txtExpenseValue.text = decimalFormat.format(expenseValue) +
                    " ${context.getString(R.string.toman)}"


            //image =>
            binding.imageIcon.setImageResource(itemCategory.icon)
            binding.imageIcon.setColorFilter(
                ContextCompat.getColor(
                    binding.root.context,
                    itemCategory.color
                )
            )


            //expand and shrink button =>
            if (buttonMode) {
                if (themeMode == "dark")
                    binding.imgExpandShrink.setImageResource(R.drawable.ic_expand_white)
                else
                    binding.imgExpandShrink.setImageResource(R.drawable.ic_expand)


                expandItem()
            } else {
                if (themeMode == "dark")
                    binding.imgExpandShrink.setImageResource(R.drawable.ic_shrink_white)
                else
                    binding.imgExpandShrink.setImageResource(R.drawable.ic_shrink)


                shrinkItem()
            }

            binding.imgExpandShrink.setOnClickListener {
                buttonMode = if (buttonMode) {
                    shrinkItem()
                    if (themeMode == "dark")
                        binding.imgExpandShrink.setImageResource(R.drawable.ic_shrink_white)
                    else
                        binding.imgExpandShrink.setImageResource(R.drawable.ic_shrink)



                    sharedPreferences.edit().putBoolean(CategoryData[position].id.toString(), false)
                        .apply()
                    false
                } else {
                    expandItem()
                    if (themeMode == "dark")
                        binding.imgExpandShrink.setImageResource(R.drawable.ic_expand_white)
                    else
                        binding.imgExpandShrink.setImageResource(R.drawable.ic_expand)


                    sharedPreferences.edit().putBoolean(CategoryData[position].id.toString(), true)
                        .apply()
                    true
                }
            }


            //set text color according to total value
            val totalData = itemCategory.totalValue
            if (totalData == 0L) {
                binding.txtTotalValue.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.gray)
                )
            } else if (totalData > 0) {
                binding.txtTotalValue.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.textColorGreen)
                )
            } else {
                binding.txtTotalValue.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.textColorRed)
                )
            }


            //set total value =>
            if (totalData < 0) {
                val negativeValue = totalData.toString().replace("-", "").toLong()

                if (language == "fa")
                    binding.txtTotalValue.text = "${setTxtFormat(negativeValue)}-"
                else
                    binding.txtTotalValue.text = "-${setTxtFormat(negativeValue)}"
            } else
                binding.txtTotalValue.text = setTxtFormat(totalData)


            //set precent =>
            try {
                val incomePrecent = (incomeValue * 100) / (incomeValue + expenseValue)

                binding.txtIncomePrecent.text = "$incomePrecent%"
                binding.txtExpensesPrecent.text = (100 - incomePrecent).toString() + "%"
            } catch (_: Exception) {
                binding.txtIncomePrecent.text = "0%"
                binding.txtExpensesPrecent.text = "0%"
            }


            //set type color =>
            val color = when (itemCategory.type) {
                "Income" -> R.color.green
                "Expense" -> R.color.red
                else -> R.color.colorPrimaryContainer
            }
            binding.viewTypeColor.setBackgroundColor(ContextCompat.getColor(context, color))


            //clickers
            itemView.setOnClickListener {
                categoryEvent.clickShort(itemCategory)
            }

            itemView.setOnLongClickListener {
                categoryEvent.clickLong(itemCategory, adapterPosition)
                true
            }
            binding.btnDelete.setOnClickListener {
                categoryEvent.clickLong(itemCategory, adapterPosition)
            }

            binding.includeCardSee.root.setOnClickListener {
                val tMode = when (itemCategory.type) {
                    "Income" -> ModelsOfTransaction.Income
                    "Expense" -> ModelsOfTransaction.Expense
                    else -> ModelsOfTransaction.Income
                }
                categoryEvent.onCardClick(itemCategory.name,tMode)
            }
        }

        private fun shrinkItem() {
            binding.txtTransactionsCount.visibility = View.GONE
            binding.txtIncomesCount.visibility = View.GONE
            binding.txtIncomeValue.visibility = View.GONE
            binding.txtIncomePrecent.visibility = View.GONE
            binding.txtExpensesCount.visibility = View.GONE
            binding.txtExpenseValue.visibility = View.GONE
            binding.txtExpensesPrecent.visibility = View.GONE
            binding.includeCardSee.root.visibility = View.GONE
        }

        private fun expandItem() {
            binding.txtTransactionsCount.visibility = View.VISIBLE
            binding.txtIncomesCount.visibility = View.VISIBLE
            binding.txtIncomeValue.visibility = View.VISIBLE
            binding.txtIncomePrecent.visibility = View.VISIBLE
            binding.txtExpensesCount.visibility = View.VISIBLE
            binding.txtExpenseValue.visibility = View.VISIBLE
            binding.txtExpensesPrecent.visibility = View.VISIBLE
            binding.includeCardSee.root.visibility = View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemCategoryBinding.inflate(layoutInflater, parent, false)
        return CategoryHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
        holder.bindData(position)
    }

    override fun getItemCount(): Int {
        return CategoryData.size
    }

    fun deleteItem(item: CategoryModel, position: Int) {
        CategoryData.remove(item)
        notifyItemRemoved(position)
    }
}