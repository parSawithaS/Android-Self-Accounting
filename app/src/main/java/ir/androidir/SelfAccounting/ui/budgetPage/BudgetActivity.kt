package ir.androidir.SelfAccounting.ui.budgetPage

import android.app.AlertDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.ActivityBudgetBinding
import ir.androidir.SelfAccounting.databinding.DialogDeleteTransactionBinding
import ir.androidir.SelfAccounting.model.HesabchiDataBase
import ir.androidir.SelfAccounting.model.dao.BudgetDao
import ir.androidir.SelfAccounting.model.dataClasses.BudgetModel
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.ModelsOfTransaction
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.TransactionRadioMode
import ir.androidir.SelfAccounting.ui.homeScreen.bshTransactions.BottomSheetTransactions
import ir.androidir.SelfAccounting.ui.homeScreen.bshTransactions.DismissEvent
import ir.androidir.SelfAccounting.utils.BottomSheetEvent
import ir.androidir.SelfAccounting.utils.CountActions

class BudgetActivity : AppCompatActivity(), BottomSheetEvent, BudgetEvent, CountActions {
    private lateinit var binding: ActivityBudgetBinding
    private lateinit var adapter: BudgetAdapter
    private lateinit var budgetDao: BudgetDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBudgetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        budgetDao = HesabchiDataBase.getDataBase(this)!!.budgetDao
        addAction(this)



        setAdapter()
        setActionBar()
        binding.imgBtnAdd.setOnClickListener {
            val bottomSheetFragment = BottomSheetAddBudget(this)
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }
    }

    private fun setActionBar() {
        binding.include2.toolbar.title = getString(R.string.budget)

        setSupportActionBar(binding.include2.toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            onBackPressedDispatcher.onBackPressed()

        return true
    }

    private fun setAdapter() {
        val data = budgetDao.selectData() as ArrayList<BudgetModel>

        if (data.isNotEmpty()) {
            binding.budgetRecycler.visibility = View.VISIBLE
            binding.textView28.visibility = View.GONE

            adapter = BudgetAdapter(data, this)
            binding.budgetRecycler.adapter = adapter
            binding.budgetRecycler.layoutManager =
                LinearLayoutManager(binding.root.context, RecyclerView.VERTICAL, false)
        } else {
            binding.budgetRecycler.visibility = View.GONE
            binding.textView28.visibility = View.VISIBLE
        }
    }

    override fun onAddItem(text: String) {
        showSnack(text)
    }

    override fun onUpdateItem(text: String) {
        showSnack(text)
    }

    private fun showSnack(text: String) {
        val snack = Snackbar.make(
            binding.root.context, binding.root, text, Snackbar.LENGTH_SHORT
        )
        snack.show()
        snack.setAction(getString(R.string.submit)) { snack.dismiss() }


        setAdapter()
    }

    override fun clickShort(item: BudgetModel) {
        val bottomSheetFragment = BottomSheetAddBudget(this)

        val bundle = Bundle()
        bundle.putStringArray(
            "data",
            arrayOf(
                item.id.toString(),
                item.name,
                item.totalValue.toString(),
                item.budgetCategory,
                item.budgetCard,
                item.usedValue.toString()
            )
        )

        bottomSheetFragment.arguments = bundle
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }

    override fun clickLong(viewOld: BudgetModel, position: Int) {
        val dialog = AlertDialog.Builder(binding.root.context).create()
        val dialogBinding = DialogDeleteTransactionBinding.inflate(layoutInflater)


        dialog.setView(dialogBinding.root)
        dialog.show()
        dialogBinding.textView.text = getString(R.string.sure_about_delete_budget)

        dialogBinding.txtNo.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.txtYes.setOnClickListener {
            dialog.dismiss()
            adapter.deleteItem(viewOld, position)
            budgetDao.deleteData(viewOld)
            setAdapter()
        }
    }

    override fun onCardClick(name: String) {
        val bsh =
            BottomSheetTransactions(name, TransactionRadioMode.Category, object : DismissEvent {
                override fun onDismiss(needToReload: Boolean) {
                    setAdapter()
                }

            }, true, ModelsOfTransaction.Expense)

        bsh.show(supportFragmentManager, bsh.tag)
    }

}
