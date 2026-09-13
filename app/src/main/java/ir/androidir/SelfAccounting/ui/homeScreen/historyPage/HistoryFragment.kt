package ir.androidir.SelfAccounting.ui.homeScreen.historyPage

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.DialogDeleteTransactionBinding
import ir.androidir.SelfAccounting.databinding.FragmentHistoryBinding
import ir.androidir.SelfAccounting.model.HesabchiDataBase
import ir.androidir.SelfAccounting.model.dao.CardDao
import ir.androidir.SelfAccounting.model.dao.CategoryDao
import ir.androidir.SelfAccounting.model.dao.TransactionDao
import ir.androidir.SelfAccounting.model.dataClasses.CardModel
import ir.androidir.SelfAccounting.model.dataClasses.CategoryModel
import ir.androidir.SelfAccounting.model.dataClasses.TransactionModel
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.ItemMode
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.ModelsOfTransaction
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.TransactionRadioMode
import ir.androidir.SelfAccounting.model.dataClasses.nonDataBase.PackedTransactionModel
import ir.androidir.SelfAccounting.model.dataClasses.nonDataBase.Transaction2Item
import ir.androidir.SelfAccounting.ui.chooseItemBsh.BottomSheetChooseItem
import ir.androidir.SelfAccounting.ui.homeScreen.MainActivity
import ir.androidir.SelfAccounting.ui.homeScreen.bshTransactions.BottomSheetTransactions
import ir.androidir.SelfAccounting.ui.homeScreen.bshTransactions.DismissEvent
import ir.androidir.SelfAccounting.ui.homeScreen.cardsPage.CardEvent
import ir.androidir.SelfAccounting.ui.homeScreen.categoiesPage.CategoryEvent
import ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage.BottomSheetAddTransaction
import ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage.TransactionAdapter
import ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage.TransactionEvent
import ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage.categoryMode.Transaction2Adapter
import ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage.categoryMode.Transaction2Event
import ir.androidir.SelfAccounting.utils.BankSelector
import ir.androidir.SelfAccounting.utils.BottomSheetEvent
import ir.androidir.SelfAccounting.utils.convertAllCategories
import ir.androidir.SelfAccounting.utils.convertDataList
import ir.androidir.SelfAccounting.utils.date.DateConvertor
import ir.androidir.SelfAccounting.utils.substring2
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.properties.Delegates

class HistoryFragment(
    private val radio1: TransactionRadioMode? = null,
    private val packed: Boolean? = null,
    private val radio3: String? = null,
    private val filter: String? = null
) : Fragment(), TransactionEvent, BottomSheetEvent, Transaction2Event {
    private lateinit var binding: FragmentHistoryBinding
    private var adapter = TransactionAdapter(arrayListOf(), this)
    private lateinit var historyDao: TransactionDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var cardDao: CardDao
    private var isPacked = false
    private lateinit var radioType: String
    private var radioMode: TransactionRadioMode by Delegates.observable(TransactionRadioMode.Transaction) { _, _, newValue ->
        setDropDownMenu()
        setAdapters(binding.autoCompleteText2.text.toString())
        if (newValue == TransactionRadioMode.Transaction)
            binding.radioGroup.visibility = View.VISIBLE
        else
            binding.radioGroup.visibility = View.GONE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val hesabchiDataBase = HesabchiDataBase.getDataBase(binding.root.context)!!
        historyDao = hesabchiDataBase.transactionDao
        categoryDao = hesabchiDataBase.categoryDao
        cardDao = hesabchiDataBase.cardDao
        radioType = getString(R.string.today)



        setTopUI()
        setAdapters()
        setGravity()


        //Load previous data if fragment is reloaded =>
        if (filter != null && radio1 != null && packed != null && radio3 != null) {
            val itemToCheck1 = when (radio1) {
                TransactionRadioMode.Transaction -> R.id.btnTransaction
                TransactionRadioMode.Category -> R.id.btnCategory
                TransactionRadioMode.Card -> R.id.btnCard
            }
            val itemToCheck2 = if (packed) R.id.btnPacked else R.id.btnSIngle
            val itemToCheck3 = when (radio3) {
                getString(R.string.today) -> {
                    R.id.today
                }

                getString(R.string.this_week) -> {
                    R.id.week
                }

                getString(R.string.this_month) -> {
                    R.id.month
                }

                getString(R.string.this_year) -> {
                    R.id.year
                }

                getString(R.string.all) -> {
                    R.id.all
                }

                else -> {
                    R.id.today
                }
            }

            binding.includeRadio.radioGroup.check(itemToCheck1)
            binding.radioGroup.check(itemToCheck2)
            binding.radioGroup2.check(itemToCheck3)

            binding.autoCompleteText2.setText(filter)
            setAdapters(filter)
        }
    }

    override fun onDestroy() {
        MainActivity.hRadio1 = radioMode
        MainActivity.hPacked = isPacked
        MainActivity.hRadio3 = radioType
        MainActivity.hFilter = binding.autoCompleteText2.text.toString()
        super.onDestroy()
    }

    private fun setGravity() {
        val sharedPreferences =
            requireContext().getSharedPreferences("sharedData", Context.MODE_PRIVATE)
        val tag = sharedPreferences.getString("languageTag", "fa")!!

        if (tag == "fa") binding.autoCompleteText2.gravity = Gravity.END
        else binding.autoCompleteText2.gravity = Gravity.START
    }

    private fun setTopUI() {
        setDropDownMenu()
        setRadios()
    }

    private fun setRadios() {
        binding.btnSIngle.isChecked = true
        binding.today.isChecked = true


        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.btnPacked.id -> {
                    isPacked = true
                    setAdapters()
                }

                binding.btnSIngle.id -> {
                    isPacked = false
                    setAdapters(binding.autoCompleteText2.text.toString())
                }
            }
        }


        binding.radioGroup2.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.today.id -> {
                    radioType = getString(R.string.today)
                    setAdapters(binding.autoCompleteText2.text.toString())
                }

                binding.week.id -> {
                    radioType = getString(R.string.this_week)
                    setAdapters(binding.autoCompleteText2.text.toString())
                }

                binding.month.id -> {
                    radioType = getString(R.string.this_month)
                    setAdapters(binding.autoCompleteText2.text.toString())
                }

                binding.year.id -> {
                    radioType = getString(R.string.this_year)
                    setAdapters(binding.autoCompleteText2.text.toString())
                }

                binding.all.id -> {
                    radioType = getString(R.string.all)
                    setAdapters(binding.autoCompleteText2.text.toString())
                }
            }
        }


        val layout = binding.includeRadio
        layout.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            radioMode = when (checkedId) {
                layout.btnTransaction.id -> {
                    TransactionRadioMode.Transaction
                }

                layout.btnCategory.id -> {
                    binding.radioGroup.check(R.id.btnSIngle)
                    TransactionRadioMode.Category
                }

                layout.btnCard.id -> {
                    binding.radioGroup.check(R.id.btnSIngle)
                    TransactionRadioMode.Card
                }

                else -> {
                    TransactionRadioMode.Transaction
                }
            }
        }
    }

    private fun setDropDownMenu() {
        binding.autoCompleteText2.setText(getString(R.string.all))

        val itemMode = when (radioMode) {
            TransactionRadioMode.Category -> ItemMode.Category
            TransactionRadioMode.Card -> ItemMode.Card
            else -> ItemMode.Both
        }

        val bottomSheet = BottomSheetChooseItem(itemMode, object : CategoryEvent {
            override fun clickShort(item: CategoryModel) {
                binding.autoCompleteText2.setText(item.name)
                setAdapters(item.name)
            }

            //item is all =>
            override fun clickLong(viewOld: CategoryModel, position: Int) {
                var name = getString(R.string.all)
                when (position) {
                    1 -> {
                        setAdapters()
                        name = getString(R.string.all)
                    }

                    2 -> {
                        setAdapters(getString(R.string.all_cards))
                        name = getString(R.string.all_cards)
                    }

                    3 -> {
                        setAdapters(getString(R.string.all_categories))
                        name = getString(R.string.all_categories)
                    }
                }

                binding.autoCompleteText2.setText(name)
            }

        }, object : CardEvent {
            override fun clickShort(item: CardModel, mode: Int) {
                val cardName = "${
                    item.cardNumber.substring2(
                        12, 16
                    )
                } ${BankSelector().selectBank(item.cardNumber)}"

                binding.autoCompleteText2.setText(cardName)
                setAdapters(cardName)
            }

            override fun clickLong(viewOld: CardModel, position: Int) {}

        })


        binding.autoCompleteText2.setOnClickListener {
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun setAdapters(type: String = getString(R.string.all)) {
        //set layout managers =>
        binding.recycler.layoutManager =
            LinearLayoutManager(binding.root.context, RecyclerView.VERTICAL, false)


        //dates =>
        val allData = historyDao.selectData() as ArrayList<TransactionModel>
        val list = arrayListOf<TransactionModel>()
        val gregorianDate = arrayOf(
            SimpleDateFormat("dd").format(Date()).toInt(),
            SimpleDateFormat("M").format(Date()).toInt(),
            SimpleDateFormat("yyyy").format(Date()).toInt()
        )
        val jalaliDate = DateConvertor().gregorianToJalali(
            gregorianDate[2], gregorianDate[1], gregorianDate[0]
        )
        val allDate = "${jalaliDate[0]} ${jalaliDate[1]} ${jalaliDate[2]}"
        val today = jalaliDate[0]
        val thisMonth = jalaliDate[1]
        val thisYear = jalaliDate[2]


        //get list of data =>
        when (type) {
            getString(R.string.all) -> list.addAll(allData)

            getString(R.string.all_cards) -> {
                allData.forEach {
                    if (it.card != getString(R.string.all) && it.card.isNotBlank()) list.add(it)
                }
            }

            getString(R.string.all_categories) -> {
                allData.forEach {
                    if (it.category != getString(R.string.without_category) && it.category.isNotBlank()) list.add(
                        it
                    )
                }
            }

            else -> {
                var isCardOrCategory = false
                categoryDao.selectData().forEach {
                    if (it.name == type) isCardOrCategory = true
                }


                if (isCardOrCategory) {
                    allData.forEach {
                        if (it.category == type) list.add(it)
                    }
                } else {
                    val cardNumber = type.substring(0, 4)
                    allData.forEach {
                        if (it.card != getString(R.string.all)) if (it.card.substring(
                                0,
                                4
                            ) == cardNumber
                        ) list.add(it)
                    }
                }
            }
        }


        //init the adapter for recycler view =>
        var packedAdapter = PackedTransactionAdapter(PackedTransactionModel(), this)
        var listTimed = arrayListOf<TransactionModel>()
        when (radioType) {
            getString(R.string.today) -> {
                //Today =>
                val listToday = arrayListOf<TransactionModel>()
                list.forEach {
                    if (it.date == allDate) listToday.add(it)
                }
                if (listToday.isNotEmpty()) {
                    binding.recycler.visibility = View.VISIBLE
                    binding.txtToTransaction.visibility = View.GONE


                    if (!isPacked) {
                        adapter = TransactionAdapter(listToday, this)
                        listTimed = listToday
                    } else {
                        val packedData = packTransactions(listToday)
                        packedAdapter = PackedTransactionAdapter(packedData, this)
                    }
                } else {
                    binding.recycler.visibility = View.GONE
                    binding.txtToTransaction.visibility = View.VISIBLE
                }
            }

            getString(R.string.this_week) -> {
                //Week =>
                val listWeek = arrayListOf<TransactionModel>()
                list.forEach {
                    if (it.year == thisYear && it.month == thisMonth && it.day.toInt() > (today.toInt() - 7)) listWeek.add(
                        it
                    )
                }
                if (listWeek.isNotEmpty()) {
                    binding.recycler.visibility = View.VISIBLE
                    binding.txtToTransaction.visibility = View.GONE

                    if (!isPacked) {
                        adapter = TransactionAdapter(listWeek, this)
                        listTimed = listWeek
                    } else {
                        val packedData = packTransactions(listWeek)
                        packedAdapter = PackedTransactionAdapter(packedData, this)
                    }
                } else {
                    binding.recycler.visibility = View.GONE
                    binding.txtToTransaction.visibility = View.VISIBLE
                }
            }

            getString(R.string.this_month) -> {
                //Month =>
                val listMonth = arrayListOf<TransactionModel>()
                list.forEach {
                    if (it.year == thisYear && it.month == thisMonth) listMonth.add(it)
                }
                if (listMonth.isNotEmpty()) {
                    binding.recycler.visibility = View.VISIBLE
                    binding.txtToTransaction.visibility = View.GONE

                    if (!isPacked) {
                        adapter = TransactionAdapter(listMonth, this)
                        listTimed = listMonth
                    } else {
                        val packedData = packTransactions(listMonth)
                        packedAdapter = PackedTransactionAdapter(packedData, this)
                    }
                } else {
                    binding.recycler.visibility = View.GONE
                    binding.txtToTransaction.visibility = View.VISIBLE
                }
            }

            getString(R.string.this_year) -> {
                //year =>
                val listYear = arrayListOf<TransactionModel>()
                list.forEach {
                    if (it.year == thisYear) listYear.add(it)
                }
                if (listYear.isNotEmpty()) {
                    binding.recycler.visibility = View.VISIBLE
                    binding.txtToTransaction.visibility = View.GONE

                    if (!isPacked) {
                        adapter = TransactionAdapter(listYear, this)
                        listTimed = listYear
                    } else {
                        val packedData = packTransactions(listYear)
                        packedAdapter = PackedTransactionAdapter(packedData, this)
                    }
                } else {
                    binding.recycler.visibility = View.GONE
                    binding.txtToTransaction.visibility = View.VISIBLE
                }
            }

            getString(R.string.all) -> {
                //all =>
                if (list.isNotEmpty()) {
                    binding.recycler.visibility = View.VISIBLE
                    binding.txtToTransaction.visibility = View.GONE

                    if (!isPacked) {
                        adapter = TransactionAdapter(list, this)
                        listTimed = list
                    }
                    else {
                        val packedData = packTransactions(list)
                        packedAdapter = PackedTransactionAdapter(packedData, this)
                    }
                } else {
                    binding.recycler.visibility = View.GONE
                    binding.txtToTransaction.visibility = View.VISIBLE
                }
            }
        }


        //set the adapter based on radio modes =>
        if (!isPacked) {
            when (radioMode) {
                TransactionRadioMode.Transaction -> {
                    binding.recycler.adapter = adapter
                }

                else -> {
                    val filter = binding.autoCompleteText2.text.toString()

                    val convertedList = if (filter == getString(R.string.all))
                        convertDataList(listTimed, radioMode, requireContext())
                    else
                        convertAllCategories(listTimed, radioMode, requireContext())


                    val adapter2 = Transaction2Adapter(convertedList, this)
                    binding.recycler.adapter = adapter2
                }
            }
        } else {
            binding.recycler.adapter = packedAdapter
        }
    }

    private fun packTransactions(list: ArrayList<TransactionModel>): PackedTransactionModel {
        var data = arrayListOf<TransactionModel>()
        val mode = binding.autoCompleteText2.text.toString()
        when (mode) {
            getString(R.string.all) -> {
                data = list
            }

            getString(R.string.all_cards) -> {
                list.forEach {
                    if (it.card != getString(R.string.all)) data.add(it)
                }
            }

            getString(R.string.all_categories) -> {
                list.forEach {
                    if (it.category != getString(R.string.without_category)) data.add(it)
                }
            }

            else -> {
                var isCardOrCategory = false
                categoryDao.selectData().forEach {
                    if (it.name == mode) isCardOrCategory = true
                }


                if (isCardOrCategory) {
                    list.forEach {
                        if (it.category == mode) data.add(it)
                    }
                } else {
                    val cardNumber = mode.substring(0, 4)
                    list.forEach {
                        if (it.card != getString(R.string.all)) if (it.card.substring(
                                0,
                                4
                            ) == cardNumber
                        ) data.add(it)
                    }
                }
            }
        }


        var incomeSize = 0
        var incomeValue = 0L
        var expenseSize = 0
        var expenseValue = 0L
        data.forEach {
            if (it.mode == ModelsOfTransaction.Income) {
                incomeSize += 1
                incomeValue += it.value.replace(",", "").toLong()
            } else {
                expenseSize += 1
                expenseValue += -(it.value.replace(",", "").toLong())
            }
        }

        val decimalFormat = DecimalFormat("#,###")
        return PackedTransactionModel(
            mode,
            data.size,
            incomeSize,
            expenseSize,
            decimalFormat.format(incomeValue),
            decimalFormat.format(expenseValue),
            decimalFormat.format(incomeValue + expenseValue)
        )

    }

    override fun clickShort(item: TransactionModel) {
        val bottomSheetFragment = BottomSheetAddTransaction(this)


        val mode = when (item.mode) {
            ModelsOfTransaction.Income -> "income"
            ModelsOfTransaction.Expense -> "expense"
            else -> {
                ""
            }
        }
        val bundle = Bundle()
        bundle.putStringArray(
            "item", arrayOf(
                mode,
                item.value,
                item.details,
                item.id.toString(),
                item.date,
                item.year,
                item.month,
                item.day,
                item.category,
                item.card
            )
        )
        bottomSheetFragment.arguments = bundle


        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }

    override fun clickLong(viewOld: TransactionModel, position: Int) {
        val dialog = AlertDialog.Builder(binding.root.context).create()
        val dialogBinding = DialogDeleteTransactionBinding.inflate(layoutInflater)


        dialog.setView(dialogBinding.root)
        dialog.show()



        dialogBinding.txtNo.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.txtYes.setOnClickListener {
            dialog.dismiss()
            adapter.deleteItem(viewOld, position)
            historyDao.deleteData(viewOld)
            setAdapters()
        }
    }

    override fun onAddItem(text: String) {
        val snack = Snackbar.make(
            binding.root.context, binding.root, text, Snackbar.LENGTH_SHORT
        )
        snack.show()
        snack.setAction(getString(R.string.submit)) { snack.dismiss() }

        setAdapters()
    }

    override fun onUpdateItem(text: String) {
        val snack = Snackbar.make(
            binding.root.context, binding.root, text, Snackbar.LENGTH_SHORT
        )
        snack.show()
        snack.setAction(getString(R.string.submit)) { snack.dismiss() }


        setAdapters()
    }

    override fun clickShort(item: Transaction2Item) {
        val tMode =
            if (item.mode == TransactionRadioMode.Category && item.value.toString().contains('-'))
                ModelsOfTransaction.Expense
            else
                ModelsOfTransaction.Income

        val bsh = BottomSheetTransactions(item.name, item.mode, object : DismissEvent {
            override fun onDismiss(needToReload: Boolean) {
                if (needToReload) {
                    setAdapters(binding.autoCompleteText2.text.toString())
                }
            }
        }, tMode = tMode)
        bsh.show(childFragmentManager, bsh.tag)
    }
}
