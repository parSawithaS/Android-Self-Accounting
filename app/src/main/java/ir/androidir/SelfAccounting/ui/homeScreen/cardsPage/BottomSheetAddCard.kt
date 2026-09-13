package ir.androidir.SelfAccounting.ui.homeScreen.cardsPage

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.FragmentAddCardBinding
import ir.androidir.SelfAccounting.model.HesabchiDataBase
import ir.androidir.SelfAccounting.model.dao.BudgetDao
import ir.androidir.SelfAccounting.model.dao.CardDao
import ir.androidir.SelfAccounting.model.dao.TransactionDao
import ir.androidir.SelfAccounting.model.dataClasses.CardModel
import ir.androidir.SelfAccounting.model.dataClasses.TransactionModel
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.LimitType
import ir.androidir.SelfAccounting.ui.subscriptionPage.SubscriptionManager.isPlanOk
import ir.androidir.SelfAccounting.ui.subscriptionPage.SubscriptionManager.showNeedToSubscriptionDialog
import ir.androidir.SelfAccounting.utils.BankSelector
import ir.androidir.SelfAccounting.utils.BottomSheetEvent
import ir.androidir.SelfAccounting.utils.ItemHandler
import java.text.DecimalFormat

class BottomSheetAddCard(private val event: BottomSheetEvent) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentAddCardBinding
    private var mode = 0
    private lateinit var cardDao: CardDao
    private lateinit var transactionDao: TransactionDao
    private lateinit var budgetDao: BudgetDao
    private var dataThisCard = CardModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddCardBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Thread {
            val hesabchiDataBase = HesabchiDataBase.getDataBase(binding.root.context)!!
            cardDao = hesabchiDataBase.cardDao
            transactionDao = hesabchiDataBase.transactionDao
            budgetDao = hesabchiDataBase.budgetDao
        }.start()



        checkArgument()
        formatEditText()
        binding.btnOk.setOnClickListener {
            clickOnOk()
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun checkArgument() {
        if (arguments != null) {
            mode = 1
            val bundle = arguments!!.getStringArray("item")!!
            dataThisCard = CardModel(
                id = bundle[0].toInt(),
                cardNumber = bundle[1],
                cardDefaultValue = bundle[2],
                bank = bundle[3],
                cardValue = bundle[4]
            )
            setUI()
        }
    }

    private fun setUI() {
        val decimalFormatCard = DecimalFormat("#,####")
        setBankImage(dataThisCard.cardNumber.replace("-", ""))
        binding.edtTxtDefaultValue.setText(dataThisCard.cardDefaultValue)
        binding.edtTxtCardNumber.setText(
            decimalFormatCard.format(dataThisCard.cardNumber.toLong())
                .replace(",", "-")
                .replace("٬", "-")
        )
        binding.btnOk.text = getString(R.string.change)


        val decimalFormat = DecimalFormat("#,###")

        val text =
            dataThisCard.cardDefaultValue.replace(
                ",",
                ""
            ).replace(
                "٬",
                ""
            )

        binding.edtTxtDefaultValue.setText(decimalFormat.format(text.toLong()))
    }

    private fun updateCard() {
        val cardNumber = binding.edtTxtCardNumber.text.toString()
            .replace("-", "")
            .replace(",", "")
            .replace("٬", "")
        val value = binding.edtTxtDefaultValue.text.toString().ifBlank { "0" }
        val bank = BankSelector().selectBank(cardNumber)

        var isNumberOk = true
        cardDao.selectData().forEach {
            if (it.cardNumber == cardNumber) {
                isNumberOk = false
            }
        }


        if (dataThisCard.cardNumber == cardNumber)
            isNumberOk = true



        if (isNumberOk) {
            Thread {
                cardDao.updateData(
                    CardModel(
                        id = dataThisCard.id,
                        cardNumber = cardNumber,
                        cardDefaultValue = value,
                        bank = bank,
                        cardValue = dataThisCard.cardValue
                    )
                )
            }.start()
            updateTransactionsInCard(cardNumber, bank)
            updateBudgetsInCard(cardNumber.substring(12, 16) + " $bank")
            dismiss()
            event.onUpdateItem(getString(R.string.card_changed))
        } else


            Toast.makeText(
                binding.root.context,
                getString(R.string.card_number_is_repited),
                Toast.LENGTH_LONG
            ).show()
    }

    private fun updateBudgetsInCard(card: String) {
        Thread {
            budgetDao.selectData().forEach {
                if (it.budgetCard == dataThisCard.cardNumber.substring(
                        12, 16
                    ) + " ${dataThisCard.bank}"
                ) {
                    val newItem = it
                    it.budgetCard = card
                    budgetDao.updateData(newItem)
                }
            }
        }.start()
    }

    private fun updateTransactionsInCard(newCardNumber: String, newCardBank: String) {
        Thread {
            transactionDao.selectData().forEach {
                if (it.card != getString(R.string.all)) {
                    if (it.card.substring(0, 4) == dataThisCard.cardNumber.substring(12, 16)) {
                        val newItem = TransactionModel(
                            id = it.id,
                            mode = it.mode,
                            details = it.details,
                            value = it.value,
                            date = it.date,
                            day = it.day,
                            month = it.month,
                            year = it.year,
                            category = it.category,
                            card = newCardNumber.substring(
                                12, 16
                            ) + " $newCardBank"
                        )
                        transactionDao.deleteData(it)
                        transactionDao.insertData(newItem)
                    }

                }

            }
        }.start()
    }

    private fun addCard() {
        val cardNumber = binding.edtTxtCardNumber.text.toString()
            .replace("-", "")
            .replace(",", "")
            .replace("٬", "")
        val value = binding.edtTxtDefaultValue.text.toString().ifBlank { "0" }
        val bank = BankSelector().selectBank(cardNumber)


        var isNumberOk = true
        cardDao.selectData().forEach {
            if (it.cardNumber == cardNumber) {
                isNumberOk = false
            }
        }

        if (isNumberOk) {
            if (isPlanOk(LimitType.Card, requireContext())) {
                Thread {
                    cardDao.insertData(
                        CardModel(
                            cardNumber = cardNumber,
                            cardDefaultValue = value,
                            bank = bank,
                            cardValue = "0"
                        )
                    )
                }.start()
                dismiss()
                event.onAddItem(getString(R.string.card_added))
            } else
                showNeedToSubscriptionDialog(requireContext())

        } else

            Toast.makeText(
                binding.root.context,
                getString(R.string.card_number_is_repited),
                Toast.LENGTH_LONG
            ).show()
    }

    private fun clickOnOk() {
        val isCardOk = ItemHandler().cardHandler(binding)
        if (isCardOk == "ok") {
            if (mode == 0) addCard()
            else updateCard()
        } else
            Toast.makeText(requireContext(), isCardOk, Toast.LENGTH_SHORT).show()
    }

    private fun formatEditText() {
        binding.edtTxtDefaultValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                val decimalFormat = DecimalFormat("#,###")
                binding.edtTxtDefaultValue.removeTextChangedListener(this)


                var number = s.toString()
                number = number.replace(",", "").replace("٬", "")

                if (number.isNotBlank()) {
                    binding.edtTxtDefaultValue.setText(decimalFormat.format(number.toLong()))
                    binding.edtTxtDefaultValue.setSelection(binding.edtTxtDefaultValue.text.length)
                }


                binding.edtTxtDefaultValue.addTextChangedListener(this)
            }

        })


        binding.edtTxtCardNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                binding.edtTxtCardNumber.removeTextChangedListener(this)
                val decimalFormat = DecimalFormat("#,####")


                val number = s.toString()
                    .replace("-", "")
                    .replace(",", "")
                    .replace("٬", "")
                if (number.isNotBlank()) {


                    binding.edtTxtCardNumber.setText(decimalFormat.format(number.toLong()))
                    val txtCard = binding.edtTxtCardNumber.text
                    val text = txtCard.toString()
                        .replace(",", "-")
                        .replace("٬", "-")
                    binding.edtTxtCardNumber.setText(text)


                    binding.edtTxtCardNumber.setSelection(binding.edtTxtCardNumber.text.length)
                }




                setBankImage(binding.edtTxtCardNumber.text.toString().replace("-", ""))
                binding.edtTxtCardNumber.addTextChangedListener(this)
            }
        })
    }

    private fun setBankImage(bin: String) {
        if (bin.length >= 6) {
            val image = BankSelector().selectBankImage(
                bin.replace("-", "").substring(0, 6).toInt().toString()
            )
            if (image == 0)
                binding.txtBankNotFound.visibility = View.VISIBLE
            else {
                binding.imgBank2.setImageResource(image)
                binding.txtBankNotFound.visibility = View.GONE
                binding.imgBank2.visibility = View.VISIBLE
            }
        } else {
            binding.txtBankNotFound.visibility = View.GONE
            binding.imgBank2.visibility = View.INVISIBLE
        }

    }
}
