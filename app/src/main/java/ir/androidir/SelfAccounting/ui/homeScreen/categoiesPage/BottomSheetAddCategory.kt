package ir.androidir.SelfAccounting.ui.homeScreen.categoiesPage

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.DialogChangeCategoryModeBinding
import ir.androidir.SelfAccounting.databinding.FragmentAddCategoryBinding
import ir.androidir.SelfAccounting.model.HesabchiDataBase
import ir.androidir.SelfAccounting.model.dao.BudgetDao
import ir.androidir.SelfAccounting.model.dao.CategoryDao
import ir.androidir.SelfAccounting.model.dao.TransactionDao
import ir.androidir.SelfAccounting.model.dataClasses.CategoryModel
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.CategoryType
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.LimitType
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.ModelsOfTransaction
import ir.androidir.SelfAccounting.ui.homeScreen.categoiesPage.colorBsh.BottomSheetColor
import ir.androidir.SelfAccounting.ui.homeScreen.categoiesPage.iconBsh.BottomSheetIcon
import ir.androidir.SelfAccounting.ui.subscriptionPage.SubscriptionManager.isPlanOk
import ir.androidir.SelfAccounting.ui.subscriptionPage.SubscriptionManager.showNeedToSubscriptionDialog
import ir.androidir.SelfAccounting.utils.BottomSheetEvent

class BottomSheetAddCategory(private val event: BottomSheetEvent) : BottomSheetDialogFragment(),
    BottomSheetCategoryEvent {
    private lateinit var binding: FragmentAddCategoryBinding
    private var mode = 0
    private lateinit var categoryDao: CategoryDao
    private lateinit var transactionDao: TransactionDao
    private lateinit var budgetDao: BudgetDao
    private var dataThisCategory = CategoryModel()
    private var categoryType = CategoryType.All
    private var firstType: CategoryType? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddCategoryBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val hesabchiDataBase = HesabchiDataBase.getDataBase(binding.root.context)!!
        transactionDao = hesabchiDataBase.transactionDao
        categoryDao = hesabchiDataBase.categoryDao
        budgetDao = hesabchiDataBase.budgetDao



        checkArgument()
        buttons()
        radioGroup()
    }

    private fun radioGroup() {
        binding.radioGroup3.setOnCheckedChangeListener { _, checkedId ->
            categoryType = when (checkedId) {
                binding.incomeRadio.id -> CategoryType.Income
                binding.expenseRadio.id -> CategoryType.Expense
                binding.allRadio.id -> CategoryType.All
                else -> {
                    CategoryType.All
                }
            }

        }
    }

    private fun buttons() {
        binding.buttonOk.setOnClickListener {
            clickOnOk()
        }

        binding.cardIcon.setOnClickListener {
            val bottomSheetFragment = BottomSheetIcon(this)

            val bundle = Bundle()
            bundle.putParcelable("item", dataThisCategory)
            bottomSheetFragment.arguments = bundle

            bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
        }

        binding.cardColor.setOnClickListener {
            val bottomSheetFragment = BottomSheetColor(this)

            val bundle = Bundle()
            bundle.putParcelable("item", dataThisCategory)
            bottomSheetFragment.arguments = bundle

            bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
        }

        val sharedPreferences =
            requireContext().getSharedPreferences("sharedData", Context.MODE_PRIVATE)
        val themeMode = sharedPreferences.getString("theme", "light")!!

        if (themeMode != "light") {
            binding.cardColor.setCardBackgroundColor(
                ContextCompat.getColor(
                    requireContext(), R.color.gray_dark
                )
            )

            binding.cardIcon.setCardBackgroundColor(
                ContextCompat.getColor(
                    requireContext(), R.color.gray_dark
                )
            )
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun checkArgument() {
        if (arguments != null) {
            mode = 1
            val bundle = arguments!!.getStringArray("item")!!
            dataThisCategory = CategoryModel(
                id = bundle[0].toInt(),
                name = bundle[1],
                totalValue = bundle[2].toLong(),
                valueIncomes = bundle[3].toLong(),
                valueExpenses = bundle[4].toLong(),
                totalTransactionsCount = bundle[5].toInt(),
                incomesCount = bundle[6].toInt(),
                expensesCount = bundle[7].toInt(),
                color = bundle[8].toInt(),
                icon = bundle[9].toInt(),
                type = bundle[10]
            )
            categoryType = when (bundle[10]) {
                CategoryType.Income.toString() -> CategoryType.Income
                CategoryType.Expense.toString() -> CategoryType.Expense
                CategoryType.All.toString() -> CategoryType.All
                else -> {
                    CategoryType.All
                }
            }
            firstType = categoryType
            setUI()
        }
    }

    private fun setUI() {
        binding.txtSheetMode.text = getString(R.string.change_category)
        binding.edtTxtName.setText(dataThisCategory.name)


        val icon = dataThisCategory.icon
        if (dataThisCategory.icon != 0)
            binding.imgIcon.setImageResource(icon)

        if (dataThisCategory.color != 0) {
            binding.imgIcon.setColorFilter(
                ContextCompat.getColor(
                    binding.root.context,
                    dataThisCategory.color
                )
            )

            binding.color.setCardBackgroundColor(
                ContextCompat.getColor(
                    binding.root.context,
                    dataThisCategory.color
                )
            )
        }

        when (categoryType) {
            CategoryType.Income -> {
                binding.incomeRadio.isChecked = true
            }
            CategoryType.Expense -> {
                binding.expenseRadio.isChecked = true
            }
            CategoryType.All -> {
                binding.allRadio.isChecked = true
            }
        }
    }

    private fun dialogUpdateCategory() {
        if (firstType != categoryType) {
            val dialog = AlertDialog.Builder(requireContext()).create()
            val dialogBinding = DialogChangeCategoryModeBinding.inflate(layoutInflater)

            dialog.setView(dialogBinding.root)
            dialog.show()

            dialogBinding.btnNo.setOnClickListener {
                dialog.dismiss()
            }

            dialogBinding.btnYes.setOnClickListener {
                dialog.dismiss()
                updateCategory()
            }
        } else
            updateCategory()
    }

    private fun updateBudgetsInCategory(category: String) {
        Thread {
            budgetDao.selectData().forEach {
                if (it.budgetCategory == dataThisCategory.name) {
                    val newItem = it
                    it.budgetCategory = category
                    budgetDao.updateData(newItem)
                }
            }
        }.start()
    }

    private fun updateTransactionsInCategory(newCategory: CategoryModel) {
        Thread {
            transactionDao.selectData().forEach {
                if (it.category != getString(R.string.without_category)) {
                    if (it.category == dataThisCategory.name) {
                        //change category name =>
                        val newItem = it
                        newItem.category = newCategory.name

                        //clear category if type has changed =>
                        if (it.mode.toString() != newCategory.type)
                            newItem.category = getString(R.string.without_category)

                        transactionDao.updateData(newItem)
                    }
                }
            }
        }.start()
    }

    private fun updateCategory(){
        val newItem = makeItem()

        Thread {
            categoryDao.updateData(newItem)
        }.start()
        updateTransactionsInCategory(newItem)
        updateBudgetsInCategory(newItem.name)

        dismiss()
        event.onUpdateItem(getString(R.string.category_changed))
    }

    private fun addCategory() {
        if (isPlanOk(LimitType.Category, requireContext())) {
            val newItem = CategoryModel(
                name = binding.edtTxtName.text.toString(),
                icon = dataThisCategory.icon,
                color = dataThisCategory.color,
                type = categoryType.toString()
            )

            Thread {
                categoryDao.insertData(newItem)
            }.start()

            dismiss()
            event.onAddItem(getString(R.string.category_added))
        } else
            showNeedToSubscriptionDialog(requireContext())
    }

    private fun makeItem(): CategoryModel {
        val name = binding.edtTxtName.text.toString()
        var totalValue = 0L
        var incomeValue = 0L
        var expenseValue = 0L
        var transactionsCount = 0
        var incomesCount = 0
        var expenseCount = 0


        transactionDao.selectData().forEach {
            if (it.category == name) {
                transactionsCount++
                if (it.mode == ModelsOfTransaction.Income) {
                    totalValue += it.value.replace(",", "").replace("٬", "").toLong()
                    incomeValue += it.value.replace(",", "").replace("٬", "").toLong()
                    incomesCount++
                } else {
                    totalValue += -(it.value.replace(",", "").replace("٬", "").toLong())
                    expenseValue += it.value.replace(",", "").replace("٬", "").toLong()
                    expenseCount++
                }
            }
        }


        return CategoryModel(
            id = dataThisCategory.id,
            name = name,
            totalValue = totalValue,
            valueIncomes = incomeValue,
            valueExpenses = expenseValue,
            totalTransactionsCount = transactionsCount,
            incomesCount = incomesCount,
            expensesCount = expenseCount,
            color = dataThisCategory.color,
            icon = dataThisCategory.icon,
            type = categoryType.toString()
        )
    }

    private fun clickOnOk() {
        val name = binding.edtTxtName.text.toString()
        if (name.isNotBlank()) {
            if (mode == 0)
                addCategory()
            else
                dialogUpdateCategory()
        } else
            Toast.makeText(
                binding.root.context,
                getString(R.string.please_enter_category_name),
                Toast.LENGTH_LONG
            ).show()
    }

    override fun moveItem(item: CategoryModel) {
        dataThisCategory = item
        setUI()
    }
}
