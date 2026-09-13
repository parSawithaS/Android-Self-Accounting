package ir.androidir.SelfAccounting.ui.budgetPage

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.FragmentAddBudgetBinding
import ir.androidir.SelfAccounting.model.HesabchiDataBase
import ir.androidir.SelfAccounting.model.dao.BudgetDao
import ir.androidir.SelfAccounting.model.dao.CardDao
import ir.androidir.SelfAccounting.model.dao.CategoryDao
import ir.androidir.SelfAccounting.model.dataClasses.BudgetModel
import ir.androidir.SelfAccounting.model.dataClasses.CardModel
import ir.androidir.SelfAccounting.model.dataClasses.CategoryModel
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.CategoryType
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.ItemMode
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.LimitType
import ir.androidir.SelfAccounting.ui.chooseItemBsh.BottomSheetChooseItem
import ir.androidir.SelfAccounting.ui.homeScreen.cardsPage.CardEvent
import ir.androidir.SelfAccounting.ui.homeScreen.categoiesPage.CategoryEvent
import ir.androidir.SelfAccounting.ui.subscriptionPage.SubscriptionManager.isPlanOk
import ir.androidir.SelfAccounting.ui.subscriptionPage.SubscriptionManager.showNeedToSubscriptionDialog
import ir.androidir.SelfAccounting.utils.BankSelector
import ir.androidir.SelfAccounting.utils.BottomSheetEvent
import ir.androidir.SelfAccounting.utils.ItemHandler
import ir.androidir.SelfAccounting.utils.substring2
import java.text.DecimalFormat

class BottomSheetAddBudget(private val event: BottomSheetEvent) : BottomSheetDialogFragment(),
	CategoryEvent, CardEvent {
	private lateinit var binding: FragmentAddBudgetBinding
	private lateinit var budgetDao: BudgetDao
	private lateinit var cardDao: CardDao
	private lateinit var categoryDao: CategoryDao
	private var mode = 0
	private var dataThisBudget = BudgetModel()
	private val categoryMode = ItemMode.Category
	private val cardMode = ItemMode.Card

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	): View {
		binding = FragmentAddBudgetBinding.inflate(layoutInflater)
		return binding.root
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setStyle(STYLE_NORMAL, R.style.DialogStyle)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val hesabchiDataBase = HesabchiDataBase.getDataBase(binding.root.context)!!
		budgetDao = hesabchiDataBase.budgetDao
		cardDao = hesabchiDataBase.cardDao
		categoryDao = hesabchiDataBase.categoryDao



		setBundleData()
		setEditText()
		setAutoCompleteText()
		binding.btnAddBudget.setOnClickListener {
			clickOnOk()
		}
		translateData()
	}

	private fun translateData() {
		val context = binding.root.context!!
		val sharedPreferences = context.getSharedPreferences("sharedData", Context.MODE_PRIVATE)
		val languageTag = sharedPreferences.getString("languageTag", "fa")!!


		if (languageTag == "fa") {
			if (dataThisBudget.budgetCard == "All") dataThisBudget.budgetCard = "همه"
		} else {
			if (dataThisBudget.budgetCard == "همه") dataThisBudget.budgetCard = "All"
		}
	}

	private fun setEditText() {
		binding.edtTxtBudgetValue.addTextChangedListener(object : TextWatcher {
			override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

			}

			override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

			}

			override fun afterTextChanged(s: Editable) {
				val decimalFormat = DecimalFormat("#,###")
				binding.edtTxtBudgetValue.removeTextChangedListener(this)


				var number = s.toString()

				number = number.replace(",", "")
				number = number.replace("٬", "")

				if (number.isNotBlank()) {
					binding.edtTxtBudgetValue.setText(decimalFormat.format(number.toLong()))
					binding.edtTxtBudgetValue.setSelection(binding.edtTxtBudgetValue.text.length)
				}


				binding.edtTxtBudgetValue.addTextChangedListener(this)
			}

		})
	}

	private fun setUI() {
		val decimalFormat = DecimalFormat("#,###")

		binding.edtTxtName.setText(dataThisBudget.name)
		binding.edtTxtBudgetValue.setText(decimalFormat.format(dataThisBudget.totalValue))
		binding.txtAddBudget.text = getString(R.string.change_budget)
		binding.txtCategory.text = dataThisBudget.budgetCategory

		if (dataThisBudget.budgetCard == "0000") binding.txtCard.text = getString(R.string.all)
		else binding.txtCard.text = dataThisBudget.budgetCard
	}

	private fun setBundleData() {
		if (arguments != null) {
			mode = 1

			val item = requireArguments().getStringArray("data")!!
			dataThisBudget = BudgetModel(
				item[0].toInt(), item[1], item[2].toLong(), item[3], item[4], item[5].toLong()
			)
			setUI()
		}
	}

	private fun setAutoCompleteText() {
		//clickers =>
		binding.txtCard.setOnClickListener {
			val bottomSheet = BottomSheetChooseItem(cardMode, this, this)
			bottomSheet.show(childFragmentManager, bottomSheet.tag)
		}
		binding.imgAddCard.setOnClickListener {
			val bottomSheet = BottomSheetChooseItem(cardMode, this, this)
			bottomSheet.show(childFragmentManager, bottomSheet.tag)
		}

		binding.txtCategory.setOnClickListener {
			val bottomSheet = BottomSheetChooseItem(categoryMode, this, this, CategoryType.Expense)
			bottomSheet.show(childFragmentManager, bottomSheet.tag)
		}
		binding.imgAddCategory.setOnClickListener {
			val bottomSheet = BottomSheetChooseItem(categoryMode, this, this, CategoryType.Expense)
			bottomSheet.show(childFragmentManager, bottomSheet.tag)
		}


		//images =>
		val category = binding.txtCategory.text.toString()
		val card = binding.txtCard.text.toString()


		//category =>
		categoryDao.selectData().forEach {
			if (it.name == category) {
				binding.icCategory.setImageResource(it.icon)
				binding.icCategory.setColorFilter(
					ContextCompat.getColor(requireContext(), it.color)
				)
				binding.icCategory.visibility = View.VISIBLE
			}

		}


		//card =>
		if (card != getString(R.string.all)) {
			val cardNumber = card.substring2(0, 4)
			cardDao.selectData().forEach {
				if (it.cardNumber.substring2(12, 16) == cardNumber) {
					val image = BankSelector().selectBankImage(it.cardNumber.substring2(0, 6))
					binding.icCard.setImageResource(image)
					binding.icCard.visibility = View.VISIBLE
				}
			}
		} else binding.icCard.visibility = View.GONE

	}

	private fun clickOnOk() {
		val isBudgetOk = ItemHandler().budgetHandler(binding)
		if (isBudgetOk == "ok") {
			if (mode == 0) addBudget()
			else updateBudget()
		} else Toast.makeText(requireContext(), isBudgetOk, Toast.LENGTH_SHORT).show()
	}

	private fun updateBudget() {
		val newItem = makeItem()

		Thread {
			budgetDao.updateData(newItem)
		}.start()

		dismiss()
		event.onUpdateItem(getString(R.string.budget_changed))
	}

	private fun addBudget() {
		if (isPlanOk(LimitType.Budget, requireContext())) {
			val newItem = makeItem()

			Thread {
				budgetDao.insertData(newItem)
			}.start()

			dismiss()
			event.onAddItem(getString(R.string.budget_added))
		} else showNeedToSubscriptionDialog(requireContext())
	}

	private fun makeItem(): BudgetModel {
		val name = binding.edtTxtName.text.toString()
		val totalValue = binding.edtTxtBudgetValue.text.toString().replace(",", "").replace("٬", "")
		val category = binding.txtCategory.text.toString()
		val card = binding.txtCard.text.toString()


		return if (mode == 0) BudgetModel(
			name = name,
			totalValue = totalValue.toLong(),
			budgetCategory = category,
			budgetCard = card
		) else BudgetModel(
			dataThisBudget.id,
			name,
			totalValue.toLong(),
			category,
			card,
		)
	}

	//category
	override fun clickShort(item: CategoryModel) {
		binding.txtCategory.text = item.name
		dataThisBudget.budgetCategory = item.name
		setAutoCompleteText()
	}

	override fun clickLong(viewOld: CategoryModel, position: Int) {}

	//card
	override fun clickShort(item: CardModel, mode: Int) {
		if (item.id!! > 0) {
			val cardNumber = item.cardNumber.substring2(12, 16)
			val bankName = BankSelector().selectBank(item.cardNumber)
			val cardName = "$cardNumber $bankName"

			binding.txtCard.text = cardName
			dataThisBudget.budgetCard = cardName
			setAutoCompleteText()
		} else {
			binding.txtCard.text = item.cardNumber
			dataThisBudget.budgetCard = item.cardNumber
			setAutoCompleteText()
		}
	}

	override fun clickLong(viewOld: CardModel, position: Int) {}
}
