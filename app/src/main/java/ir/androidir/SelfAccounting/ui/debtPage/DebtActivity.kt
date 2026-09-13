package ir.androidir.SelfAccounting.ui.debtPage

import android.app.AlertDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.ActivityDebtBinding
import ir.androidir.SelfAccounting.databinding.DialogDeleteTransactionBinding
import ir.androidir.SelfAccounting.model.HesabchiDataBase
import ir.androidir.SelfAccounting.model.dao.DebtDao
import ir.androidir.SelfAccounting.model.dataClasses.DebtModel
import ir.androidir.SelfAccounting.utils.BottomSheetEvent
import ir.androidir.SelfAccounting.utils.CountActions

class DebtActivity : AppCompatActivity(), DebtEvent, BottomSheetEvent, CountActions {
    private lateinit var binding: ActivityDebtBinding
    private lateinit var debtDao: DebtDao
    private lateinit var adapter: DebtAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDebtBinding.inflate(layoutInflater)
        debtDao = HesabchiDataBase.getDataBase(this)!!.debtDao
        setContentView(binding.root)
        addAction(this)



        setActionBar()
        setAdapter()
        binding.imgBtnAdd.setOnClickListener {
            val bottomSheetFragment = BottomSheetAddDebt(this)
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }
    }

    private fun setActionBar() {
        binding.include3.toolbar.title = getString(R.string.debt)

        setSupportActionBar(binding.include3.toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            onBackPressedDispatcher.onBackPressed()

        return true
    }

    private fun setAdapter() {
        val listData = debtDao.selectData() as ArrayList<DebtModel>

        if (listData.isNotEmpty()) {
            binding.recycler.visibility = View.VISIBLE
            binding.txtNothingToShow.visibility = View.GONE

            adapter = DebtAdapter(listData, this)
            binding.recycler.adapter = adapter
            binding.recycler.layoutManager =
                LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        } else {
            binding.recycler.visibility = View.GONE
            binding.txtNothingToShow.visibility = View.VISIBLE
        }
    }

    override fun onClick(item: DebtModel) {
        val bottomSheetFragment = BottomSheetAddDebt(this)

        val bundle = Bundle()
        bundle.putStringArray(
            "data",
            arrayOf(
                item.id.toString(),
                item.name,
                item.totalValue.toString(),
                item.paidValue.toString(),
                item.debtTo,
                item.date,
                item.dateFinish,
                item.turnsCount.toString(),
                item.paidTurns.toString(),
                item.turnValue.toString()
            )
        )
        bottomSheetFragment.arguments = bundle

        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }

    override fun longClick(item: DebtModel, position: Int) {
        val dialog = AlertDialog.Builder(binding.root.context).create()
        val dialogBinding = DialogDeleteTransactionBinding.inflate(layoutInflater)


        dialog.setView(dialogBinding.root)
        dialog.show()
        dialogBinding.textView.text = getString(R.string.delete_this_debt)

        dialogBinding.txtNo.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.txtYes.setOnClickListener {
            dialog.dismiss()
            adapter.deleteItem(item, position)
            debtDao.deleteData(item)
            setAdapter()
        }
    }

    override fun onAddItem(text: String) {
        val snack = Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT)
        snack.setAction(getString(R.string.submit)) {
            snack.dismiss()
        }
        snack.show()

        setAdapter()
    }

    override fun onUpdateItem(text: String) {
        val snack = Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT)
        snack.setAction(getString(R.string.submit)) {
            snack.dismiss()
        }
        snack.show()

        setAdapter()
    }
}
