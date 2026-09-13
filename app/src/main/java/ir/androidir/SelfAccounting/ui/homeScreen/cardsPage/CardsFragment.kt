package ir.androidir.SelfAccounting.ui.homeScreen.cardsPage

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.DialogDeleteTransactionBinding
import ir.androidir.SelfAccounting.databinding.FragmentCardsBinding
import ir.androidir.SelfAccounting.model.HesabchiDataBase
import ir.androidir.SelfAccounting.model.dao.BudgetDao
import ir.androidir.SelfAccounting.model.dao.CardDao
import ir.androidir.SelfAccounting.model.dao.TransactionDao
import ir.androidir.SelfAccounting.model.dataClasses.CardModel
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.ModelsOfTransaction
import ir.androidir.SelfAccounting.utils.BottomSheetEvent
import ir.androidir.SelfAccounting.utils.CountActions

class CardsFragment : Fragment(), CardEvent, BottomSheetEvent, CountActions {
    private lateinit var binding: FragmentCardsBinding
    private lateinit var cardDao: CardDao
    private lateinit var transactionDao: TransactionDao
    private lateinit var budgetDao: BudgetDao
    private lateinit var adapter: CardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCardsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val hesabchiDataBase = HesabchiDataBase.getDataBase(binding.root.context)!!
        cardDao = hesabchiDataBase.cardDao
        transactionDao = hesabchiDataBase.transactionDao
        budgetDao = hesabchiDataBase.budgetDao



        setAdapter()
    }

    private fun setAdapter() {
        val data = cardDao.selectData() as ArrayList<CardModel>
        adapter = CardAdapter(data, this)
        binding.recyclerCard.adapter = adapter
        binding.recyclerCard.layoutManager =
            LinearLayoutManager(binding.root.context, RecyclerView.VERTICAL, false)


        if (data.size == 0)
            binding.txtNoItem.visibility = View.VISIBLE
        else
            binding.txtNoItem.visibility = View.GONE
    }

    override fun clickShort(item: CardModel, mode: Int) {
        val bottomSheetFragment =
           BottomSheetAddCard(this)

        val bundle = Bundle()
        bundle.putStringArray(
            "item", arrayOf(
                item.id.toString(),
                item.cardNumber,
                item.cardDefaultValue,
                item.bank,
                item.cardValue
            )
        )
        bottomSheetFragment.arguments = bundle


        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }

    override fun clickLong(viewOld: CardModel, position: Int) {
        val dialog = AlertDialog.Builder(binding.root.context).create()
        val dialogBinding = DialogDeleteTransactionBinding.inflate(layoutInflater)


        dialog.setView(dialogBinding.root)
        dialogBinding.textView.text = getString(R.string.sure_about_delete_card)
        dialog.show()


        dialogBinding.txtNo.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.txtYes.setOnClickListener {
            dialog.dismiss()
            adapter.deleteItem(viewOld, position)
            cardDao.deleteData(viewOld)
            setAdapter()
            setTransactionsCard(viewOld.cardNumber.substring(12, 16) + " ${viewOld.bank}")
            setBudgetInCard(viewOld.cardNumber.substring(12, 16) + " ${viewOld.bank}")
        }
    }

    private fun setBudgetInCard(card: String) {
        Thread {
            budgetDao.selectData().forEach {
                if (it.budgetCard == card) {
                    val newItem = it
                    newItem.budgetCard = getString(R.string.all)
                    budgetDao.updateData(newItem)
                }
            }
        }.start()
    }

    private fun setTransactionsCard(card: String) {
        transactionDao.selectData().forEach {
            if (it.card == card) {
                Thread {
                    if (it.mode != ModelsOfTransaction.Transfer) {
                        val newItem = it
                        newItem.card = getString(R.string.all)
                        transactionDao.updateData(newItem)
                    } else {
                        val newItem = it
                        newItem.card = getString(R.string.without_card)
                        transactionDao.updateData(newItem)
                    }

                }.start()
            }

            if (it.category == card) {
                Thread {
                    if (it.mode != ModelsOfTransaction.Transfer) {
                        val newItem = it
                        newItem.category = getString(R.string.without_card)
                        transactionDao.updateData(newItem)
                    } else {
                        val newItem = it
                        newItem.category = getString(R.string.without_card)
                        transactionDao.updateData(newItem)
                    }

                }.start()
            }
        }
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