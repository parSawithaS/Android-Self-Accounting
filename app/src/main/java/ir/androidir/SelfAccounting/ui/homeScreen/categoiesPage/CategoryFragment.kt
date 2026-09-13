package ir.androidir.SelfAccounting.ui.homeScreen.categoiesPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.DialogDeleteTransactionBinding
import ir.androidir.SelfAccounting.databinding.FragmentCategoryBinding
import ir.androidir.SelfAccounting.model.HesabchiDataBase
import ir.androidir.SelfAccounting.model.dao.BudgetDao
import ir.androidir.SelfAccounting.model.dao.CategoryDao
import ir.androidir.SelfAccounting.model.dao.TransactionDao
import ir.androidir.SelfAccounting.model.dataClasses.CategoryModel
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.CategoryType
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.ModelsOfTransaction
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.TransactionRadioMode
import ir.androidir.SelfAccounting.ui.homeScreen.MainActivity
import ir.androidir.SelfAccounting.ui.homeScreen.bshTransactions.BottomSheetTransactions
import ir.androidir.SelfAccounting.ui.homeScreen.bshTransactions.DismissEvent
import ir.androidir.SelfAccounting.utils.BottomSheetEvent
import ir.androidir.SelfAccounting.utils.CountActions
import kotlin.properties.Delegates

class CategoryFragment(
    private val previousType: CategoryType? = null,
    private val mode: CategoryType? = null
) : Fragment(), CategoryEvent,
    BottomSheetEvent, CountActions {
    private lateinit var binding: FragmentCategoryBinding
    private lateinit var categoryDao: CategoryDao
    private lateinit var transactionDao: TransactionDao
    private lateinit var budgetDao: BudgetDao
    private lateinit var adapter: CategoryAdapter
    private var type: CategoryType by
    Delegates.observable(previousType ?: CategoryType.All) { _, _, newValue ->
        companionType = newValue
        setAdapter()
    }

    companion object {
        lateinit var companionType: CategoryType
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCategoryBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val hesabchiDataBase = HesabchiDataBase.getDataBase(binding.root.context)!!
        categoryDao = hesabchiDataBase.categoryDao
        transactionDao = hesabchiDataBase.transactionDao
        budgetDao = hesabchiDataBase.budgetDao



        setRadioGroup()

        //Load previous data if fragment is reloaded =>
        if (mode != null) {
            val itemToCheck = when (mode) {
                CategoryType.Expense -> R.id.radioExpense
                CategoryType.Income -> R.id.radioIncome
                CategoryType.All -> R.id.radioAll
            }

            binding.radioGroup.check(itemToCheck)
        }

        setAdapter()
    }

    override fun onDestroy() {
        MainActivity.cRadio = type
        super.onDestroy()
    }

    private fun setRadioGroup() {
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            type = when (checkedId) {
                R.id.radioAll -> CategoryType.All
                R.id.radioIncome -> CategoryType.Income
                R.id.radioExpense -> CategoryType.Expense
                else -> CategoryType.All
            }
        }


        when (previousType) {
            CategoryType.All -> binding.radioAll.isChecked = true
            CategoryType.Income -> binding.radioIncome.isChecked = true
            CategoryType.Expense -> binding.radioExpense.isChecked = true
            else -> binding.radioAll.isChecked = true
        }
    }

    private fun setAdapter() {
        val data = getCategoryData(type)
        adapter = CategoryAdapter(data, this)
        binding.recyclerCategory.adapter = adapter
        binding.recyclerCategory.layoutManager =
            LinearLayoutManager(binding.root.context, RecyclerView.VERTICAL, false)

        if (data.size == 0)
            binding.txtNoItem2.visibility = View.VISIBLE
        else
            binding.txtNoItem2.visibility = View.GONE
    }

    private fun getCategoryData(type: CategoryType): ArrayList<CategoryModel> {
        val allData = categoryDao.selectData() as ArrayList
        return when (type) {
            CategoryType.All -> {
                val dataToBack = arrayListOf<CategoryModel>()
                allData.forEach {
                    if (it.type == "All")
                        dataToBack.add(it)
                }
                dataToBack
            }

            else -> {
                val newList = arrayListOf<CategoryModel>()
                allData.forEach {
                    if (it.type == type.toString())
                        newList.add(it)
                }
                newList
            }
        }
    }

    private fun deleteBudgetInCategory(category: String) {
        Thread {
            budgetDao.selectData().forEach {
                if (it.budgetCategory == category) {
                    budgetDao.deleteData(it)
                }
            }
        }.start()
    }

    private fun setCategoryCard(category: String) {
        transactionDao.selectData().forEach {
            if (it.category == category) {
                Thread {
                    val newItem = it
                    newItem.category = getString(R.string.without_category)
                    transactionDao.updateData(newItem)
                }.start()
            }
        }
    }

    override fun clickShort(item: CategoryModel) {
        val bottomSheetFragment = BottomSheetAddCategory(this)

        val bundle = Bundle()
        bundle.putStringArray(
            "item", arrayOf(
                item.id.toString(),
                item.name,
                item.totalValue.toString(),
                item.valueIncomes.toString(),
                item.valueExpenses.toString(),
                item.totalTransactionsCount.toString(),
                item.incomesCount.toString(),
                item.expensesCount.toString(),
                item.color.toString(),
                item.icon.toString(),
                item.type
            )
        )
        bottomSheetFragment.arguments = bundle


        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }

    override fun clickLong(viewOld: CategoryModel, position: Int) {
        val dialog = AlertDialog.Builder(binding.root.context).create()
        val dialogBinding = DialogDeleteTransactionBinding.inflate(layoutInflater)


        dialog.setView(dialogBinding.root)
        dialogBinding.textView.text = getString(R.string.sure_about_delete_category)
        dialog.show()


        dialogBinding.txtNo.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.txtYes.setOnClickListener {
            dialog.dismiss()
            adapter.deleteItem(viewOld, position)
            categoryDao.deleteData(viewOld)
            setAdapter()
            setCategoryCard(viewOld.name)
            deleteBudgetInCategory(viewOld.name)
        }
    }

    override fun onCardClick(name: String, tMode: ModelsOfTransaction) {
        val bsh = BottomSheetTransactions(
            name,
            TransactionRadioMode.Category,
            tMode = tMode,
            dismissEvent = object : DismissEvent {
                override fun onDismiss(needToReload: Boolean) {
                    setAdapter()
                }

            }
        )
        bsh.show(childFragmentManager, bsh.tag)
    }

    override fun onAddItem(text: String) {
        addAction(requireContext())
    }

    override fun onUpdateItem(text: String) {
        val snack = Snackbar.make(
            binding.root.context, binding.root, text, Snackbar.LENGTH_SHORT
        )
        snack.show()
        snack.setAction(getString(R.string.submit)) { snack.dismiss() }

        setAdapter()
        addAction(requireContext())
    }
}
