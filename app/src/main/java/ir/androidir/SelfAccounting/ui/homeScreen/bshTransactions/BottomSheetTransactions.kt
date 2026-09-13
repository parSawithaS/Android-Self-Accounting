package ir.androidir.SelfAccounting.ui.homeScreen.bshTransactions

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.BottomSheetTransactionsBinding
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
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.TransactionRadioMode.Card
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.TransactionRadioMode.Category
import ir.androidir.SelfAccounting.ui.FragmentManager.openTransaction
import ir.androidir.SelfAccounting.ui.chooseItemBsh.BottomSheetChooseItem
import ir.androidir.SelfAccounting.ui.homeScreen.cardsPage.CardEvent
import ir.androidir.SelfAccounting.ui.homeScreen.categoiesPage.CategoryEvent
import ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage.BottomSheetAddTransaction
import ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage.DeleteTransactionEvent
import ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage.TransactionAdapter
import ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage.TransactionEvent
import ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage.TransactionFragment
import ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage.dialog.deleteTransaction
import ir.androidir.SelfAccounting.utils.BankSelector
import ir.androidir.SelfAccounting.utils.BottomSheetEvent
import ir.androidir.SelfAccounting.utils.CountActions
import ir.androidir.SelfAccounting.utils.SHARED_PREFERENCES_TAG
import ir.androidir.SelfAccounting.utils.findCardByName
import ir.androidir.SelfAccounting.utils.findCategoryByName
import ir.androidir.SelfAccounting.utils.setCardValues
import ir.androidir.SelfAccounting.utils.setCategoryValues
import ir.androidir.SelfAccounting.utils.substring2

class BottomSheetTransactions(
	private val itemName: String = "",
	private val itemMode: TransactionRadioMode? = null,
	private val dismissEvent: DismissEvent? = null,
	private val isBudget: Boolean = false,
	private val tMode: ModelsOfTransaction
) : BottomSheetDialogFragment(), TransactionEvent, DeleteTransactionEvent, BottomSheetEvent,
	CountActions {
	private lateinit var binding: BottomSheetTransactionsBinding
	private lateinit var adapter: TransactionAdapter
	private lateinit var sharedPreferences: SharedPreferences
	private lateinit var transactionDao: TransactionDao
	private lateinit var cardDao: CardDao
	private lateinit var categoryDao: CategoryDao
	private lateinit var totalData: ArrayList<TransactionModel>
	private lateinit var yearAndMonth: Array<String>
	private val tFragment = TransactionFragment()
	private var dateMode = 0        //0 = current date, 1 = user is in a past month

	//shows whether an item has changed or deleted
	private var needToReload = false

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	): View {
		binding = BottomSheetTransactionsBinding.inflate(layoutInflater)
		return binding.root
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setStyle(STYLE_NORMAL, R.style.DialogStyle)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		//Dao =>
		val hesabchiDataBase = HesabchiDataBase.getDataBase(binding.root.context)!!
		transactionDao = hesabchiDataBase.transactionDao
		cardDao = hesabchiDataBase.cardDao
		categoryDao = hesabchiDataBase.categoryDao

		//sharedPreferences =>
		sharedPreferences =
			requireContext().getSharedPreferences(SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE)


		//Set current date =>
		yearAndMonth = arrayOf(
			tFragment.getDate()[2], tFragment.getDate()[1]
		)
		tFragment.updateDate(requireContext(), yearAndMonth)


		setGravity()
		setAdapter()
		setFilter()
		setDate()
		setDateButtons()
		buttonAdd()

		if (isBudget) binding.txtTransactionsIn.text = getString(R.string.transactions_in_budget)
	}

	private fun buttonAdd() {
		binding.addButton.root.setOnClickListener {
			val txtValue = if (itemMode == Category) itemName
			else itemName.substring2(12, 16) + " " + BankSelector().selectBank(itemName)
			val bottomSheetFragment = BottomSheetAddTransaction(this, txtValue, tMode, itemMode)
			bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
		}
	}

	override fun onDestroy() {
		dismissEvent?.onDismiss(needToReload)
		super.onDestroy()
	}

	@SuppressLint("SetTextI18n")
	private fun setDate() {
		binding.txtTopDate.text = "${yearAndMonth[1]} ${yearAndMonth[0]}"
	}

	private fun setDateButtons() {
		val sharedPreferences =
			requireContext().getSharedPreferences("sharedData", Context.MODE_PRIVATE)
		val themeMode = sharedPreferences.getString("theme", "light")!!


		val nowDate = tFragment.getDate()[1] + " ${tFragment.getDate()[2]}"
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
			if (dateMode == 1) nextDate(appDate)
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
		tFragment.updateDate(requireContext(), yearAndMonth)
		setAdapter(binding.autoCompleteText2.text.toString())
		setDate()
		setDateButtons()
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
		tFragment.updateDate(requireContext(), yearAndMonth)
		setAdapter(binding.autoCompleteText2.text.toString())
		setDate()
		setDateButtons()
	}

	private fun setGravity() {
		val tag = sharedPreferences.getString("languageTag", "fa")!!

		if (tag == "fa") binding.autoCompleteText2.gravity = Gravity.END
		else binding.autoCompleteText2.gravity = Gravity.START
	}

	private fun setAdapter(mode: String = getString(R.string.all)) {
		val data = arrayListOf<TransactionModel>()
		val myList = arrayListOf<TransactionModel>()
		transactionDao.selectData().forEach {
			if (it.year == yearAndMonth[0] && it.month == yearAndMonth[1]) data.add(it)
		}
		binding.autoCompleteText2.setText(mode)


		//set total data value =>
		when (mode) {
			getString(R.string.all) -> myList.addAll(data)

			getString(R.string.all_cards) -> {
				data.forEach {
					if (it.card != getString(R.string.all) && it.card.isNotBlank()) myList.add(it)
				}
			}

			getString(R.string.all_categories) -> {
				data.forEach {
					if (it.category != getString(R.string.without_category) && it.category.isNotBlank()) myList.add(
						it
					)
				}
			}

			else -> {
				//true = category    false = card
				var isCardOrCategory = false
				categoryDao.selectData().forEach {
					if (it.name == mode) isCardOrCategory = true
				}


				if (isCardOrCategory) {
					data.forEach {
						if (it.category == mode) myList.add(it)
					}
				} else {
					data.forEach {
						if (it.card != getString(R.string.all)) {
							val cardNumber = mode.substring(0, 4)
							if (it.card.substring(0, 4) == cardNumber) myList.add(it)
						}

						if (it.category == mode) myList.add(it)
					}
				}
			}
		}

		//delete useless items
		val listToDelete = arrayListOf<TransactionModel>()
		if (itemMode == Category) {
			myList.forEach {
				if (it.category != itemName) listToDelete.add(it)
			}
		} else if (itemMode == Card) {
			myList.forEach {
				if (itemName == getString(R.string.all)) {
					if (it.card != getString(R.string.all)) listToDelete.add(it)
				} else {
					if (it.card.length <= 4) listToDelete.add(it)
					else {
						if (it.card.substring2(0, 4) != itemName.substring2(
								12,
								16
							) && it.category.substring2(0, 4) != itemName.substring2(12, 16)
						) listToDelete.add(it)
					}
				}
			}
		}
		myList.removeAll(listToDelete.toSet())

		totalData = myList


		//set adapter for recycler view =>
		if (totalData.isNotEmpty()) {
			binding.recyclerMain.visibility = View.VISIBLE
			binding.txtToTransaction.visibility = View.GONE

			//set adapter
			adapter = TransactionAdapter(totalData, this)
			binding.recyclerMain.adapter = adapter


			binding.recyclerMain.isNestedScrollingEnabled = false
			binding.recyclerMain.layoutManager =
				LinearLayoutManager(binding.root.context, RecyclerView.VERTICAL, false)
		} else {
			binding.recyclerMain.visibility = View.GONE
			binding.txtToTransaction.visibility = View.VISIBLE
		}
	}

	private fun setFilter() {
		val items = arrayListOf(getString(R.string.all))
		when (itemMode) {
			Category -> {
				cardDao.selectData().forEach {
					items.add(it.cardNumber.substring(12, 16) + " ${it.bank}")
				}
			}

			Card -> {
				categoryDao.selectData().forEach {
					items.add(it.name)
				}
			}

			else -> {
				categoryDao.selectData().forEach {
					items.add(it.name)
				}
				cardDao.selectData().forEach {
					items.add(it.cardNumber.substring2(12, 16) + " ${it.bank}")
				}
			}
		}


		val itemMode = when (itemMode) {
			Category -> ItemMode.Card
			Card -> ItemMode.Category
			else -> ItemMode.Both
		}
		val bottomSheet = BottomSheetChooseItem(
			itemMode,
			object : CategoryEvent {
				override fun clickShort(item: CategoryModel) {
					setAdapter(item.name)
				}

				override fun clickLong(viewOld: CategoryModel, position: Int) {
					//item is all =>
					when (position) {
						1 -> {
							setAdapter(getString(R.string.all))
						}

						2 -> {
							setAdapter(getString(R.string.all_cards))
						}

						3 -> {
							setAdapter(getString(R.string.all_categories))
						}
					}
				}

			},
			object : CardEvent {
				override fun clickShort(item: CardModel, mode: Int) {
					setAdapter(
						"${
							item.cardNumber.substring2(
								12, 16
							)
						} ${BankSelector().selectBank(item.cardNumber)}"
					)
				}

				//item is all
				override fun clickLong(viewOld: CardModel, position: Int) {
					setAdapter()
				}

			},
		)


		binding.autoCompleteText2.setOnClickListener {
			bottomSheet.show(childFragmentManager, bottomSheet.tag)
		}
	}

	override fun clickShort(item: TransactionModel) {
		openTransaction(item, childFragmentManager, this)
	}

	override fun clickLong(viewOld: TransactionModel, position: Int) {
		deleteTransaction(viewOld, position, requireContext(), this)
	}

	override fun onDelete(viewOld: TransactionModel, position: Int) {
		needToReload = true
		adapter.deleteItem(viewOld, position)
		transactionDao.deleteData(viewOld)

		if (viewOld.category != getString(R.string.without_category)) {
			Thread {
				categoryDao.selectData().forEach {
					setCategoryValues(
						findCategoryByName(categoryDao, it.name), transactionDao, categoryDao
					)
				}
			}.start()
		}

		if (viewOld.card != getString(R.string.all)) {
			Thread {
				cardDao.selectData().forEach {
					setCardValues(
						findCardByName(cardDao, it.cardNumber), transactionDao, cardDao
					)
				}
			}.start()
		}

		setAdapter(binding.autoCompleteText2.text.toString())
	}

	override fun onAddItem(text: String) {
		needToReload = true
		setAdapter(binding.autoCompleteText2.text.toString())
		addAction(requireContext())
	}

	override fun onUpdateItem(text: String) {
		needToReload = true
		val snack = Snackbar.make(
			binding.root.context, binding.root, text, Snackbar.LENGTH_SHORT
		)
		snack.show()
		snack.setAction(getString(R.string.submit)) { snack.dismiss() }

		setAdapter(binding.autoCompleteText2.text.toString())
		addAction(requireContext())
	}
}
