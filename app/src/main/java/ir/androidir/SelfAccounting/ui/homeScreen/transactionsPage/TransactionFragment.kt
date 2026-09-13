//DO NOT CHANGE OR DELETE ANY THING IN THIS FILE EVEN IF IT SEEMS USELESS!!!
package ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.DialogAskForCommentBinding
import ir.androidir.SelfAccounting.databinding.FragmentTransactionsBinding
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
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.TransactionRadioMode.Transaction
import ir.androidir.SelfAccounting.model.dataClasses.nonDataBase.Transaction2Item
import ir.androidir.SelfAccounting.ui.FragmentManager.openTransaction
import ir.androidir.SelfAccounting.ui.chooseItemBsh.BottomSheetChooseItem
import ir.androidir.SelfAccounting.ui.homeScreen.MainActivity
import ir.androidir.SelfAccounting.ui.homeScreen.bshTransactions.BottomSheetTransactions
import ir.androidir.SelfAccounting.ui.homeScreen.bshTransactions.DismissEvent
import ir.androidir.SelfAccounting.ui.homeScreen.cardsPage.CardEvent
import ir.androidir.SelfAccounting.ui.homeScreen.categoiesPage.CategoryEvent
import ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage.categoryMode.Transaction2Adapter
import ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage.categoryMode.Transaction2Event
import ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage.dialog.deleteTransaction
import ir.androidir.SelfAccounting.utils.BankSelector
import ir.androidir.SelfAccounting.utils.BottomSheetEvent
import ir.androidir.SelfAccounting.utils.CountActions
import ir.androidir.SelfAccounting.utils.SHARED_PREFERENCES_TAG
import ir.androidir.SelfAccounting.utils.convertAllCategories
import ir.androidir.SelfAccounting.utils.convertDataList
import ir.androidir.SelfAccounting.utils.date.DateConvertor
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.properties.Delegates

@SuppressLint("SetTextI18n", "SimpleDateFormat")
class TransactionFragment(
	private val filter: String? = null, private val mode: TransactionRadioMode? = null
) : Fragment(), TransactionEvent, Transaction2Event, BottomSheetEvent, CountActions,
	DeleteTransactionEvent {
	private lateinit var binding: FragmentTransactionsBinding
	private lateinit var transactionDao: TransactionDao
	private lateinit var cardDao: CardDao
	private lateinit var categoryDao: CategoryDao
	private lateinit var sharedPreferences: SharedPreferences
	private lateinit var totalData: ArrayList<TransactionModel>
	private var adapter = TransactionAdapter(arrayListOf(), this)
	private var dateMode = 0        //0 = current date, 1 = user is in a past month
	private var radioMode: TransactionRadioMode by Delegates.observable(Transaction) { _, _, _ ->
			setAdapter()
			setFilter()
		}

	companion object {
		lateinit var yearAndMonth: Array<String>
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	): View {
		binding = FragmentTransactionsBinding.inflate(layoutInflater, container, false)
		return binding.root
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
		yearAndMonth = arrayOf(getDate()[2], getDate()[1])
		updateDate(requireContext(), yearAndMonth)


		//Functions =>
		setBottomUI()
		setTopUI()
		setGravity()
		checkForAskCommentDialog()


		//Load previous data if fragment is reloaded =>
		if (filter != null && mode != null) {
			val itemToCheck = when (mode) {
				Transaction -> R.id.btnTransaction
				Category -> R.id.btnCategory
				Card -> R.id.btnCard
			}

			binding.layoutMainBottom.includeRadio.radioGroup.check(itemToCheck)
			binding.layoutMainBottom.autoCompleteText2.setText(filter)

			setAdapter(filter)
		}
	}

	override fun onDestroy() {
		MainActivity.tMode = radioMode
		MainActivity.tFilter = binding.layoutMainBottom.autoCompleteText2.text.toString()
		super.onDestroy()
	}

	private fun setRadio() {
		val layout = binding.layoutMainBottom.includeRadio
		layout.radioGroup.setOnCheckedChangeListener { _, checkedId ->
			radioMode = when (checkedId) {
				layout.btnTransaction.id -> {
					Transaction
				}

				layout.btnCategory.id -> {
					Category
				}

				layout.btnCard.id -> {
					Card
				}

				else -> {
					Transaction
				}
			}
		}
	}

	private fun checkForAskCommentDialog() {
		val pagesOpened = sharedPreferences.getInt("pagesOpened", 0)
		val maxOpenedPages = sharedPreferences.getInt("maxOpened", 25)

		if (pagesOpened >= maxOpenedPages) {
			val dialog = AlertDialog.Builder(requireContext()).create()
			val dialogBinding = DialogAskForCommentBinding.inflate(layoutInflater)

			dialog.setView(dialogBinding.root)
			dialog.setCancelable(false)
			dialog.show()

			dialogBinding.btnNo.setOnClickListener {
				dialog.dismiss()

				sharedPreferences.edit().putInt("maxOpened", maxOpenedPages + 130).apply()
				sharedPreferences.edit().putInt("pagesOpened", 0).apply()
			}

			dialogBinding.btnYes.setOnClickListener {
				dialog.dismiss()
				val uri = "https://cafebazaar.ir/app/ir.androidir.SelfAccounting"
				val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
				Toast.makeText(
					requireContext(), getString(R.string.please_rate_app), Toast.LENGTH_SHORT
				).show()
				startActivity(webIntent)

				sharedPreferences.edit().putInt("maxOpened", maxOpenedPages + 999999999).apply()
			}
		}
	}

	private fun setGravity() {
		val tag = sharedPreferences.getString("languageTag", "fa")!!

		if (tag == "fa") binding.layoutMainBottom.autoCompleteText2.gravity = Gravity.END
		else binding.layoutMainBottom.autoCompleteText2.gravity = Gravity.START
	}

	private fun setFilter() {
		val itemMode = when (radioMode) {
			Category -> ItemMode.Category
			Card -> ItemMode.Card
			else -> ItemMode.Both
		}
		val bottomSheet = BottomSheetChooseItem(
			itemMode,
			object : CategoryEvent {
				override fun clickShort(item: CategoryModel) {
					setAdapter(item.name)
					setTopUI()
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

					setTopUI()
				}

			},
			object : CardEvent {
				override fun clickShort(item: CardModel, mode: Int) {
					setAdapter(
						"${
							item.cardNumber.substring(
								12, 16
							)
						} ${BankSelector().selectBank(item.cardNumber)}"
					)
					setTopUI()
				}

				override fun clickLong(viewOld: CardModel, position: Int) {}

			},
		)


		binding.layoutMainBottom.autoCompleteText2.setOnClickListener {
			bottomSheet.show(childFragmentManager, bottomSheet.tag)
		}
	}

	private fun setTopUI() {
		setDate()
		setTotalValue()
		setChart()
		setIncomeAndExpenseText()
		setDateButtons()
	}

	private fun setBottomUI() {
		setRadio()
		setAdapter()
		setFilter()
	}

	private fun setDateButtons() {
		val sharedPreferences =
			requireContext().getSharedPreferences("sharedData", Context.MODE_PRIVATE)
		val themeMode = sharedPreferences.getString("theme", "light")!!


		val nowDate = getDate()[1] + " ${getDate()[2]}"
		dateMode = if (nowDate == binding.include.txtTopDate.text.toString()) {
			if (themeMode == "light") {
				binding.include.btnNext.setImageResource(R.drawable.arrow_left_gray)
				binding.include.btnPrevious.setImageResource(R.drawable.arrow_right_black)
			} else {
				binding.include.btnNext.setImageResource(R.drawable.arrow_left_gray)
				binding.include.btnPrevious.setImageResource(R.drawable.arrow_right_white)
			}
			0
		} else {
			if (themeMode == "light") {
				binding.include.btnNext.setImageResource(R.drawable.arrow_left_black)
				binding.include.btnPrevious.setImageResource(R.drawable.arrow_right_black)
			} else {
				binding.include.btnNext.setImageResource(R.drawable.arrow_left_white)
				binding.include.btnPrevious.setImageResource(R.drawable.arrow_right_white)
			}
			1
		}


		val appDate = yearAndMonth[1] + " ${yearAndMonth[0]}"
		binding.include.btnNext.setOnClickListener {
			if (dateMode == 1) nextDate(appDate)
		}

		binding.include.btnPrevious.setOnClickListener {
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
		updateDate(requireContext(), yearAndMonth)
		setAdapter(binding.layoutMainBottom.autoCompleteText2.text.toString())
		setTopUI()
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
		updateDate(requireContext(), yearAndMonth)
		setAdapter(binding.layoutMainBottom.autoCompleteText2.text.toString())
		setTopUI()
	}

	fun updateDate(context: Context, date: Array<String>) {
		val sharedPreferences = context.getSharedPreferences("sharedData", Context.MODE_PRIVATE)
		sharedPreferences.edit().putString("date", "${date[0]} ${date[1]}").apply()
	}

	fun getDate(): Array<String> {
		val gregorianDate = arrayOf(
			SimpleDateFormat("dd").format(Date()).toInt(),
			SimpleDateFormat("M").format(Date()).toInt(),
			SimpleDateFormat("yyyy").format(Date()).toInt()
		)
		return DateConvertor().gregorianToJalali(
			gregorianDate[2], gregorianDate[1], gregorianDate[0]
		)
	}

	private fun setIncomeAndExpenseText() {
		val decimalFormat = DecimalFormat("#,###")

		val income = decimalFormat.format(getIncomesAndExpense()[0]).toString()
		val expense =
			decimalFormat.format(getIncomesAndExpense()[1].toString().replace("-", "").toLong())
				.toString()


		binding.include.txtIncome.text = income
		if (expense == "0") {
			binding.include.txtExpense.text = "0"
		} else {
			binding.include.txtExpense.text = expense
		}
	}

	private fun setChart() {
		if (totalData.isEmpty()) {
			binding.include.circularProgressBar.progress = 0f
			binding.include.circularProgressBar.backgroundProgressBarColor = ContextCompat.getColor(
				requireContext(), R.color.gray
			)

			binding.include.PercentOfExpense.text = "0%"
			binding.include.PercentOfIncome.text = "0%"
		} else {
			binding.include.circularProgressBar.backgroundProgressBarColor = ContextCompat.getColor(
				requireContext(), R.color.red
			)


			val income = getIncomesAndExpense()[0]
			val expense = getIncomesAndExpense()[1].toString().replace("-", "").toLong()

			if (income == 0L && expense == 0L) {
				binding.include.circularProgressBar.backgroundProgressBarColor =
					ContextCompat.getColor(
						requireContext(), R.color.gray
					)
			}


			try {
				val incomePercent = (income * 100) / (income + expense)
				binding.include.circularProgressBar.progress = incomePercent.toFloat()

				binding.include.PercentOfExpense.text = (100 - incomePercent).toString() + "%"
				binding.include.PercentOfIncome.text = "$incomePercent%"
			} catch (_: Exception) {
				binding.include.circularProgressBar.progress = 0f

				binding.include.PercentOfExpense.text = "0%"
				binding.include.PercentOfIncome.text = "0%"
			}
		}
	}

	private fun setTotalValue() {
		val income = getIncomesAndExpense()[0]
		val expense = getIncomesAndExpense()[1]

		val decimalFormat = DecimalFormat("#,###")
		val totalValue = income - expense
		if (totalValue < 0) {
			val negativeValue = totalValue.toString().replace("-", "").toLong()

			val sharedPreferences =
				requireContext().getSharedPreferences("sharedData", Context.MODE_PRIVATE)
			val language = sharedPreferences.getString("languageTag", "fa")

			if (language == "fa") binding.include.txtValue.text =
				"${decimalFormat.format(negativeValue)}-"
			else binding.include.txtValue.text = "-${decimalFormat.format(negativeValue)}"

		} else binding.include.txtValue.text = decimalFormat.format(totalValue)
	}

	private fun getIncomesAndExpense(): Array<Long> {
		val thisMonth = yearAndMonth[1]
		var incomeValue = 0L
		var expenseValue = 0L

		val mode = binding.layoutMainBottom.autoCompleteText2.text.toString()
		totalData.forEach {
			if (it.month == thisMonth) {
				if (mode == getString(R.string.all) || mode == getString(R.string.all_categories) || mode == getString(
						R.string.all_cards
					)
				) {
					if (it.mode == ModelsOfTransaction.Income) incomeValue += it.value.replace(
						",", ""
					).replace("٬", "").toLong()
					else if (it.mode == ModelsOfTransaction.Expense) expenseValue += it.value.replace(
						",", ""
					).replace("٬", "").toLong()
				} else {
					if (it.mode == ModelsOfTransaction.Transfer) {
						if (it.category == mode) expenseValue += it.value.replace(
							",", ""
						).replace("٬", "").toLong()
						else if (it.card == mode) {
							incomeValue += it.value.replace(
								",", ""
							).replace("٬", "").toLong()
						}
					} else {
						if (it.card == mode || it.category == mode) {
							when (it.mode) {
								ModelsOfTransaction.Expense -> expenseValue += it.value.replace(
									",", ""
								).replace("٬", "").toLong()

								ModelsOfTransaction.Income -> incomeValue += it.value.replace(
									",", ""
								).replace("٬", "").toLong()

								else -> {}
							}
						}
					}
				}
			}
		}


		return arrayOf(incomeValue, expenseValue)
	}

	private fun setDate() {
		binding.include.txtTopDate.text = "${yearAndMonth[1]} ${yearAndMonth[0]}"
	}

	private fun setAdapter(mode: String = getString(R.string.all)) {
		val monthData = arrayListOf<TransactionModel>()
		val list = arrayListOf<TransactionModel>()
		transactionDao.selectData().forEach {
			if (it.year == yearAndMonth[0] && it.month == yearAndMonth[1]) monthData.add(it)
		}
		binding.layoutMainBottom.autoCompleteText2.setText(mode)


		when (mode) {
			getString(R.string.all) -> list.addAll(monthData)

			getString(R.string.all_cards) -> {
				monthData.forEach {
					if (it.card != getString(R.string.all) && it.card.isNotBlank()) list.add(it)
				}
			}

			getString(R.string.all_categories) -> {
				monthData.forEach {
					if (it.category != getString(R.string.without_category) && it.category.isNotBlank()) list.add(
						it
					)
				}
			}

			else -> {
				//true = category    false = card
				var isCategory = false
				categoryDao.selectData().forEach {
					if (it.name == mode) isCategory = true
				}


				if (isCategory) {
					monthData.forEach {
						if (it.category == mode) list.add(it)
					}
				} else {
					monthData.forEach {
						if (it.card != getString(R.string.all)) {
							val cardNumber = mode.substring(0, 4)
							if (it.card.substring(0, 4) == cardNumber) list.add(it)
						}

						if (it.category == mode) list.add(it)
					}
				}
			}
		}
		totalData = list



		if (totalData.isNotEmpty()) {
			binding.layoutMainBottom.recyclerMain.visibility = View.VISIBLE
			binding.layoutMainBottom.txtToTransaction.visibility = View.GONE

			//set adapter by transaction or category
			when (radioMode) {
				Transaction -> {
					adapter = TransactionAdapter(list, this)
					binding.layoutMainBottom.recyclerMain.adapter = adapter
				}

				else -> {
					val filter = binding.layoutMainBottom.autoCompleteText2.text.toString()

					val convertedList = if (filter == getString(R.string.all)) convertDataList(
						list,
						radioMode,
						requireContext()
					)
					else convertAllCategories(list, radioMode, requireContext())


					val adapterCategory = Transaction2Adapter(
						convertedList, this
					)



					binding.layoutMainBottom.recyclerMain.adapter = adapterCategory

					if (convertedList.isEmpty()) {
						binding.layoutMainBottom.recyclerMain.visibility = View.GONE
						binding.layoutMainBottom.txtToTransaction.visibility = View.VISIBLE
					}
				}
			}

			binding.layoutMainBottom.recyclerMain.isNestedScrollingEnabled = false
			binding.layoutMainBottom.recyclerMain.layoutManager =
				LinearLayoutManager(binding.root.context, RecyclerView.VERTICAL, false)
		} else {
			binding.layoutMainBottom.recyclerMain.visibility = View.GONE
			binding.layoutMainBottom.txtToTransaction.visibility = View.VISIBLE
		}
	}

	override fun clickShort(item: TransactionModel) {
		openTransaction(item, childFragmentManager, this)
	}

	override fun clickLong(viewOld: TransactionModel, position: Int) {
		deleteTransaction(viewOld, position, requireContext(), this)
	}

	override fun onAddItem(text: String) {
		setAdapter(binding.layoutMainBottom.autoCompleteText2.text.toString())
		addAction(requireContext())
	}

	override fun onUpdateItem(text: String) {
		val snack = Snackbar.make(
			binding.root.context, binding.root, text, Snackbar.LENGTH_SHORT
		)
		snack.show()
		snack.setAction(getString(R.string.submit)) { snack.dismiss() }

		setAdapter(binding.layoutMainBottom.autoCompleteText2.text.toString())
		setTopUI()
		addAction(requireContext())
	}

	override fun clickShort(item: Transaction2Item) {
		val tMode = if (item.mode == Category && item.value.toString()
				.contains('-')
		) ModelsOfTransaction.Expense
		else ModelsOfTransaction.Income

		val bsh = BottomSheetTransactions(item.name, item.mode, object : DismissEvent {
			override fun onDismiss(needToReload: Boolean) {
				if (needToReload) {
					setAdapter(binding.layoutMainBottom.autoCompleteText2.text.toString())
				}
			}
		}, tMode = tMode)
		bsh.show(childFragmentManager, bsh.tag)
	}

	override fun onDelete(viewOld: TransactionModel, position: Int) {
		adapter.deleteItem(viewOld, position)
		Thread {
			transactionDao.deleteData(viewOld)
		}.start()
		setAdapter(binding.layoutMainBottom.autoCompleteText2.text.toString())
		setTopUI()
	}
}
