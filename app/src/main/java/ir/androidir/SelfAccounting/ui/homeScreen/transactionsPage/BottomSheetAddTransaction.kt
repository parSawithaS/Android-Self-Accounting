package ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.FragmentAddTransactionBinding
import ir.androidir.SelfAccounting.model.HesabchiDataBase
import ir.androidir.SelfAccounting.model.dao.CardDao
import ir.androidir.SelfAccounting.model.dao.CategoryDao
import ir.androidir.SelfAccounting.model.dao.TransactionDao
import ir.androidir.SelfAccounting.model.dataClasses.CardModel
import ir.androidir.SelfAccounting.model.dataClasses.CategoryModel
import ir.androidir.SelfAccounting.model.dataClasses.TransactionModel
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.CategoryType
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.ItemMode
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.LimitType
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.ModelsOfTransaction
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.ModelsOfTransaction.Expense
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.ModelsOfTransaction.Income
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.ModelsOfTransaction.Transfer
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.TransactionRadioMode
import ir.androidir.SelfAccounting.ui.chooseItemBsh.BottomSheetChooseItem
import ir.androidir.SelfAccounting.ui.homeScreen.cardsPage.CardEvent
import ir.androidir.SelfAccounting.ui.homeScreen.categoiesPage.CategoryEvent
import ir.androidir.SelfAccounting.ui.subscriptionPage.SubscriptionManager.isPlanOk
import ir.androidir.SelfAccounting.ui.subscriptionPage.SubscriptionManager.showNeedToSubscriptionDialog
import ir.androidir.SelfAccounting.utils.BankSelector
import ir.androidir.SelfAccounting.utils.BottomSheetEvent
import ir.androidir.SelfAccounting.utils.ItemHandler
import ir.androidir.SelfAccounting.utils.SHARED_PREFERENCES_TAG
import ir.androidir.SelfAccounting.utils.date.DateUtils.getDate
import ir.androidir.SelfAccounting.utils.findCardByName
import ir.androidir.SelfAccounting.utils.findCategoryByName
import ir.androidir.SelfAccounting.utils.setCardValues
import ir.androidir.SelfAccounting.utils.setCategoryValues
import ir.androidir.SelfAccounting.utils.substring2
import java.text.DecimalFormat

class BottomSheetAddTransaction(
    private val event: BottomSheetEvent,
    private val txtValue: String? = null,
    private val tMode: ModelsOfTransaction? = null,
    private val tType: TransactionRadioMode? = null
) :
    BottomSheetDialogFragment(),
    CategoryEvent, CardEvent {
    private lateinit var binding: FragmentAddTransactionBinding
    private lateinit var transactionDao: TransactionDao
    private lateinit var cardDao: CardDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var sharedPreferences: SharedPreferences
    private var buttonMode = Income
    private var dataThisTransaction = TransactionModel()
    private var mode = 0        //0 = add || 1 = update
    private var themeMode = "light"
    private val categoryMode = ItemMode.Category
    private val cardMode = ItemMode.Card

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddTransactionBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val hesabchiDataBase = HesabchiDataBase.getDataBase(binding.root.context)!!
        transactionDao = hesabchiDataBase.transactionDao
        cardDao = hesabchiDataBase.cardDao
        categoryDao = hesabchiDataBase.categoryDao
        sharedPreferences =
            requireContext().getSharedPreferences(SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE)
        themeMode = sharedPreferences.getString("theme", "light")!!



        setAutoCompleteText()
        buttons()
        setEditText()
        setDataFromArgument()
        if (mode == 0) setButtonIncome()
        if (txtValue != null && tMode != null && tType != null) setCategory()
    }

    private fun setCategory() {
        when(tMode) {
            Income -> setButtonIncome()
            Expense -> setButtonExpense()
            Transfer -> setButtonTransfer()
            null -> println()
        }

        if (tType == TransactionRadioMode.Category)
            binding.txtCategory.text = txtValue
        else
            binding.txtCard.text = txtValue

        setImage()
    }

    private fun setDataFromArgument() {
        if (arguments != null) {
            val bundle = requireArguments().getStringArray("item")!!
            dataThisTransaction = TransactionModel(
                mode = when (bundle[0]) {
                    "income" -> Income
                    "expense" -> Expense
                    else -> Transfer
                },
                value = bundle[1],
                details = bundle[2],
                id = bundle[3].toInt(),
                date = bundle[4],
                year = bundle[5],
                month = bundle[6],
                day = bundle[7],
                category = bundle[8],
                card = bundle[9]
            )
            mode = 1
            setUI()
        }
    }

    private fun setAutoCompleteText() {
        if (buttonMode == Transfer) {
            clearViews()
            //category =>
            binding.txtCategoryName.text = getString(R.string.from_card)

            if (mode == 1) {
                if (dataThisTransaction.mode == Transfer) binding.txtCategory.text =
                    dataThisTransaction.category
                else binding.txtCategory.text = getString(R.string.without_card)
            } else binding.txtCategory.text = getString(R.string.without_card)

            //card =>
            binding.txtCardName.text = getString(R.string.to_card)

            if (mode == 1) {
                if (dataThisTransaction.mode == Transfer) binding.txtCard.text =
                    dataThisTransaction.card
                else binding.txtCard.text = getString(R.string.without_card)
            } else binding.txtCard.text = getString(R.string.without_card)


            Log.e("test1", buttonMode.toString())
            //clickers =>
            binding.txtCategory.setOnClickListener {
                val bottomSheet = BottomSheetChooseItem(ItemMode.Card2, this, this)
                bottomSheet.show(childFragmentManager, bottomSheet.tag)
            }
            binding.imgBtnCategory.setOnClickListener {
                val bottomSheet = BottomSheetChooseItem(ItemMode.Card2, this, this)
                bottomSheet.show(childFragmentManager, bottomSheet.tag)
            }

            binding.txtCard.setOnClickListener {
                val bottomSheet = BottomSheetChooseItem(cardMode, this, this)
                bottomSheet.show(childFragmentManager, bottomSheet.tag)
            }
            binding.imgBtnCard.setOnClickListener {
                val bottomSheet = BottomSheetChooseItem(cardMode, this, this)
                bottomSheet.show(childFragmentManager, bottomSheet.tag)
            }


            //images =>
            val card1 = binding.txtCategory.text.toString()
            val card2 = binding.txtCard.text.toString()

            val cardNumber1 = card1.substring(0, 4)
            val cardNumber2 = card2.substring(0, 4)

            cardDao.selectData().forEach {

                if (it.cardNumber.substring(12, 16) == cardNumber1) {
                    val image = BankSelector().selectBankImage(it.cardNumber.substring(0, 6))
                    binding.imgCategory.setImageResource(image)
                    binding.imgCategory.visibility = View.VISIBLE
                }

                if (it.cardNumber.substring(12, 16) == cardNumber2) {
                    val image = BankSelector().selectBankImage(it.cardNumber.substring(0, 6))
                    binding.imgCard.setImageResource(image)
                    binding.imgCard.visibility = View.VISIBLE
                }
            }

        } else {
            if (dataThisTransaction.mode == buttonMode) {
                //category =>
                binding.txtCategoryName.text = getString(R.string.category)

                if (mode == 1) {
                    if (dataThisTransaction.mode != Transfer) binding.txtCategory.text =
                        dataThisTransaction.category
                    else binding.txtCategory.text = getString(R.string.without_category)
                } else binding.txtCategory.text = getString(R.string.without_category)


                //card =>
                binding.txtCardName.text = getString(R.string.card)

                if (mode == 1) {
                    if (dataThisTransaction.mode != Transfer) binding.txtCard.text =
                        dataThisTransaction.card
                    else binding.txtCard.text = getString(R.string.all)
                } else binding.txtCard.text = getString(R.string.all)


            } else
                clearViews()


            //clickers =>
            binding.txtCard.setOnClickListener {
                val bottomSheet = BottomSheetChooseItem(cardMode, this, this)
                bottomSheet.show(childFragmentManager, bottomSheet.tag)
            }
            binding.imgBtnCard.setOnClickListener {
                val bottomSheet = BottomSheetChooseItem(cardMode, this, this)
                bottomSheet.show(childFragmentManager, bottomSheet.tag)
            }


            val categoryType = when (buttonMode) {
                Income -> CategoryType.Income
                Expense -> CategoryType.Expense
                else -> CategoryType.All
            }
            binding.txtCategory.setOnClickListener {
                val bottomSheet = BottomSheetChooseItem(categoryMode, this, this, categoryType)
                bottomSheet.show(childFragmentManager, bottomSheet.tag)
            }
            binding.imgBtnCategory.setOnClickListener {
                val bottomSheet = BottomSheetChooseItem(categoryMode, this, this, categoryType)
                bottomSheet.show(childFragmentManager, bottomSheet.tag)
            }
        }
        //images =>
        setImage()
    }

    private fun setImage() {
        if (buttonMode == Transfer) {
            val card1 = binding.txtCategory.text.toString()
            val card2 = binding.txtCard.text.toString()


            //card1 =>
            if (card1 != getString(R.string.all) && card1.length >= 4) {
                val cardNumber = card1.substring(0, 4)
                cardDao.selectData().forEach {
                    if (it.cardNumber.substring(12, 16) == cardNumber) {
                        val image = BankSelector().selectBankImage(it.cardNumber.substring(0, 6))
                        binding.imgCategory.setImageResource(image)
                        binding.imgCategory.visibility = View.VISIBLE
                    }
                }
            } else
                binding.imgCategory.visibility = View.GONE


            //card2 =>
            if (card2 != getString(R.string.all) && card2.length >= 4) {
                val cardNumber = card2.substring(0, 4)
                cardDao.selectData().forEach {
                    if (it.cardNumber.substring(12, 16) == cardNumber) {
                        val image = BankSelector().selectBankImage(it.cardNumber.substring(0, 6))
                        binding.imgCard.setImageResource(image)
                        binding.imgCard.visibility = View.VISIBLE
                    }
                }
            } else
                binding.imgCard.visibility = View.GONE


        } else {
            val category = binding.txtCategory.text.toString()
            val card = binding.txtCard.text.toString()

            //category =>
            categoryDao.selectData().forEach {
                if (it.name == category) {
                    binding.imgCategory.setImageResource(it.icon)
                    binding.imgCategory.setColorFilter(
                        ContextCompat.getColor(requireContext(), it.color)
                    )
                    binding.imgCategory.visibility = View.VISIBLE
                }
            }


            //card =>
            if (card != getString(R.string.all) && card.length >= 4) {
                val cardNumber = card.substring(0, 4)
                cardDao.selectData().forEach {
                    if (it.cardNumber.substring(12, 16) == cardNumber) {
                        val image = BankSelector().selectBankImage(it.cardNumber.substring(0, 6))
                        binding.imgCard.setImageResource(image)
                        binding.imgCard.visibility = View.VISIBLE
                    }
                }
            } else
                binding.imgCard.visibility = View.GONE
        }
    }

    private fun setUI() {
        when (dataThisTransaction.mode) {
            Income -> setButtonIncome()
            Expense -> setButtonExpense()
            else -> setButtonTransfer()
        }

        val decimalFormat = DecimalFormat("#,###")

        val text =
            if (dataThisTransaction.value.contains(',')) dataThisTransaction.value.replace(",", "")
            else dataThisTransaction.value

        binding.edtTxtValue.setText(decimalFormat.format(text.toLong()))
        binding.edtTxtDetails.setText(dataThisTransaction.details)
    }

    private fun setEditText() {
        binding.edtTxtValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                val decimalFormat = DecimalFormat("#,###")
                binding.edtTxtValue.removeTextChangedListener(this)

                var number = s.toString().replace(",", "")
                number = number.replace("٬", "")

                if (number.isNotBlank()) {
                    binding.edtTxtValue.setText(decimalFormat.format(number.toLong()))
                    binding.edtTxtValue.setSelection(binding.edtTxtValue.text.length)
                }


                binding.edtTxtValue.addTextChangedListener(this)
            }

        })
    }

    private fun buttons() {
        binding.btnIncome.root.setOnClickListener {
            setButtonIncome()
        }
        binding.btnExpense.root.setOnClickListener {
            setButtonExpense()
        }
        binding.btnTransfer.root.setOnClickListener {
            setButtonTransfer()
        }

        binding.btnOk.setOnClickListener {
            val isItemOk = ItemHandler().transactionHandler(binding)
            if (isItemOk == "ok") {
                if (mode == 0) addTransaction()
                else updateTransaction()
            } else
                Toast.makeText(requireContext(), isItemOk, Toast.LENGTH_SHORT).show()
        }

        //to change buttons color in the night mode
        setButtonTransfer()
        setButtonExpense()
        setButtonIncome()
    }

    private fun updateTransaction() {
        val item = makeItem(1)
        Thread {
            transactionDao.updateData(item)
        }.start()

        if (item.category != getString(R.string.without_category)) {
            Thread {
                categoryDao.selectData().forEach {
                    setCategoryValues(
                        findCategoryByName(categoryDao, it.name),
                        transactionDao,
                        categoryDao
                    )
                }
            }.start()
        }

        if (item.card != getString(R.string.all)) {
            Thread {
                cardDao.selectData().forEach {
                    setCardValues(
                        findCardByName(cardDao, it.cardNumber),
                        transactionDao,
                        cardDao
                    )
                }
            }.start()
        }


        event.onUpdateItem(getString(R.string.transation_changed))
        dismiss()
    }

    private fun addTransaction() {
        if (isPlanOk(LimitType.Transaction, requireContext())) {
            val newItem = makeItem(0)
            Thread {
                transactionDao.insertData(newItem)
            }.start()


            if (newItem.category != getString(R.string.without_category)) {
                Thread {
                    categoryDao.selectData().forEach {
                        setCategoryValues(
                            findCategoryByName(categoryDao, it.name),
                            transactionDao,
                            categoryDao
                        )
                    }
                }.start()
            }

            if (newItem.card != getString(R.string.all)) {
                Thread {
                    cardDao.selectData().forEach {
                        setCardValues(
                            findCardByName(cardDao, it.cardNumber),
                            transactionDao,
                            cardDao
                        )
                    }
                }.start()
            }

            dismiss()
            event.onAddItem(getString(R.string.transaction_added))
        } else
            showNeedToSubscriptionDialog(requireContext())
    }

    @SuppressLint("SimpleDateFormat")
    private fun makeItem(mode: Int): TransactionModel {
        val itemMode = when (buttonMode) {
            Income -> Income
            Expense -> Expense
            Transfer -> Transfer
        }
        val details = binding.edtTxtDetails.text.toString().ifBlank { "" }
        val value = binding.edtTxtValue.text.toString().replace("٬", ",")
        val date = getDate(requireContext())

        return if (mode == 0) {
            TransactionModel(
                id = dataThisTransaction.id,
                mode = itemMode,
                details = details,
                value = value,
                date = "${date[0]} ${date[1]} ${date[2]}",
                day = date[0],
                month = date[1],
                year = date[2],
                category = binding.txtCategory.text.toString(),
                card = binding.txtCard.text.toString()
            )
        } else {
            TransactionModel(
                id = dataThisTransaction.id,
                mode = itemMode,
                details = details,
                value = value,
                date = dataThisTransaction.date,
                day = dataThisTransaction.day,
                month = dataThisTransaction.month,
                year = dataThisTransaction.year,
                category = binding.txtCategory.text.toString(),
                card = binding.txtCard.text.toString()
            )
        }
    }

    private fun clearViews() {
        if (buttonMode == Transfer) {
            binding.txtCategory.text = getString(R.string.without_card)
            binding.txtCard.text = getString(R.string.without_card)
        } else {
            binding.txtCategory.text = getString(R.string.without_category)
            binding.txtCard.text = getString(R.string.all)
        }


        binding.imgCategory.setImageResource(0)
        binding.imgCategory.setColorFilter(0)
        binding.imgCategory.visibility = View.GONE

        binding.imgCard.setImageResource(0)
        binding.imgCard.setColorFilter(0)
        binding.imgCard.visibility = View.GONE
    }

    private fun setButtonIncome() {
        uncheckAllButtons()
        binding.btnIncome.cons.setBackgroundResource(R.drawable.storke_green)

        if (themeMode == "light") {
            binding.btnIncome.textView17.setTextColor(
                ContextCompat.getColor(
                    binding.root.context, R.color.black
                )
            )
            binding.btnIncome.imageView.setImageResource(R.drawable.ic_income_transaction_black)
        } else {
            binding.btnIncome.textView17.setTextColor(
                ContextCompat.getColor(
                    binding.root.context, R.color.white
                )
            )
            binding.btnIncome.imageView.setImageResource(R.drawable.ic_income_transaction_white)
        }

        buttonMode = Income

        setAutoCompleteText()
    }

    private fun setButtonExpense() {
        uncheckAllButtons()
        binding.btnExpense.cons.setBackgroundResource(R.drawable.storke_red)

        if (themeMode == "light") {
            binding.btnExpense.imageView.setImageResource(R.drawable.ic_expense_transaction_black)
            binding.btnExpense.textView17.setTextColor(
                ContextCompat.getColor(
                    binding.root.context, R.color.black
                )
            )
        } else {
            binding.btnExpense.imageView.setImageResource(R.drawable.ic_expense_transaction_white)
            binding.btnExpense.textView17.setTextColor(
                ContextCompat.getColor(
                    binding.root.context, R.color.white
                )
            )
        }

        buttonMode = Expense
        setAutoCompleteText()
    }

    private fun setButtonTransfer() {
        uncheckAllButtons()
        binding.btnTransfer.cons.setBackgroundResource(R.drawable.storke_blue)

        if (themeMode == "light") {
            binding.btnTransfer.imageView.setImageResource(R.drawable.ic_transfer_black)
            binding.btnTransfer.textView17.setTextColor(
                ContextCompat.getColor(
                    binding.root.context, R.color.black
                )
            )
        } else {
            binding.btnTransfer.imageView.setImageResource(R.drawable.ic_transfer_white)
            binding.btnTransfer.textView17.setTextColor(
                ContextCompat.getColor(
                    binding.root.context, R.color.white
                )
            )
        }

        buttonMode = Transfer
        setAutoCompleteText()
    }

    private fun uncheckAllButtons() {
        binding.btnIncome.cons.setBackgroundResource(R.drawable.storke_gray)
        binding.btnIncome.imageView.setImageResource(R.drawable.ic_income_transaction_gray)
        binding.btnIncome.textView17.setTextColor(
            ContextCompat.getColor(
                binding.root.context, R.color.gray
            )
        )

        binding.btnExpense.cons.setBackgroundResource(R.drawable.storke_gray)
        binding.btnExpense.imageView.setImageResource(R.drawable.ic_expense_transaction_gray)
        binding.btnExpense.textView17.setTextColor(
            ContextCompat.getColor(
                binding.root.context, R.color.gray
            )
        )

        binding.btnTransfer.cons.setBackgroundResource(R.drawable.storke_gray)
        binding.btnTransfer.imageView.setImageResource(R.drawable.ic_transfer_gray)
        binding.btnTransfer.textView17.setTextColor(
            ContextCompat.getColor(
                binding.root.context, R.color.gray
            )
        )
    }

    //category
    override fun clickShort(item: CategoryModel) {
        binding.txtCategory.text = item.name
        dataThisTransaction.category = item.name
        setImage()

        if (item.id!! < 0)
            binding.imgCategory.visibility = View.GONE
    }

    //card
    override fun clickShort(item: CardModel, mode: Int) {
        if (item.id!! > 0) {
            val cardNumber = item.cardNumber.substring2(12, 16)
            val bankName = BankSelector().selectBank(item.cardNumber)
            val cardName = "$cardNumber $bankName"
            if (mode == 0) {
                binding.txtCard.text = cardName
                dataThisTransaction.card = cardName
                setImage()
            } else {
                binding.txtCategory.text = cardName
                dataThisTransaction.category = cardName
                setImage()
            }
        } else {
            binding.txtCard.text = item.cardNumber
            dataThisTransaction.card = item.cardNumber
            binding.imgCard.visibility = View.GONE
        }

    }

    override fun clickLong(viewOld: CategoryModel, position: Int) {}
    override fun clickLong(viewOld: CardModel, position: Int) {}
}
