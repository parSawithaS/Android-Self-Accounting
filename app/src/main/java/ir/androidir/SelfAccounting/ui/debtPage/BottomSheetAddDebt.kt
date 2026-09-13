package ir.androidir.SelfAccounting.ui.debtPage

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.FragmentAddDebtBinding
import ir.androidir.SelfAccounting.model.HesabchiDataBase
import ir.androidir.SelfAccounting.model.dao.DebtDao
import ir.androidir.SelfAccounting.model.dataClasses.DebtModel
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.LimitType
import ir.androidir.SelfAccounting.ui.subscriptionPage.SubscriptionManager.isPlanOk
import ir.androidir.SelfAccounting.ui.subscriptionPage.SubscriptionManager.showNeedToSubscriptionDialog
import ir.androidir.SelfAccounting.utils.BottomSheetEvent
import ir.androidir.SelfAccounting.utils.ItemHandler
import ir.androidir.SelfAccounting.utils.date.DateConvertor
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date

class BottomSheetAddDebt(private val event: BottomSheetEvent) : BottomSheetDialogFragment(),
    DateEvent {
    private lateinit var binding: FragmentAddDebtBinding
    private var dataThisDebt = DebtModel()
    private var mode = 0
    private lateinit var debtDao: DebtDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddDebtBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        debtDao = HesabchiDataBase.getDataBase(binding.root.context)!!.debtDao



        setBundleData()
        setEditText()
        buttons()
    }

    private fun buttons() {
        val bottomSheetDatePickerStart = BottomSheetDatePickerStart(this)
        val bottomSheetDatePickerFinish = BottomSheetDatePickerFinish(this)

        val bundle = Bundle()
        binding.txtStartDate.setOnClickListener {
            bundle.putStringArray("dates", arrayOf(binding.txtStartDate.text.toString(), "s"))
            bottomSheetDatePickerStart.arguments = bundle
            bottomSheetDatePickerStart.show(childFragmentManager, bottomSheetDatePickerStart.tag)
        }
        binding.titleStartDate.setOnClickListener {
            bundle.putStringArray("dates", arrayOf(binding.txtStartDate.text.toString(), "s"))
            bottomSheetDatePickerStart.arguments = bundle
            bottomSheetDatePickerStart.show(childFragmentManager, bottomSheetDatePickerStart.tag)
        }

        binding.txtFinishDate.setOnClickListener {
            bundle.putStringArray("dates", arrayOf(binding.txtFinishDate.text.toString(), "e"))
            bottomSheetDatePickerFinish.arguments = bundle
            bottomSheetDatePickerFinish.show(childFragmentManager, bottomSheetDatePickerFinish.tag)
        }
        binding.titleFinishDate.setOnClickListener {
            bundle.putStringArray("dates", arrayOf(binding.txtFinishDate.text.toString(), "e"))
            bottomSheetDatePickerFinish.arguments = bundle
            bottomSheetDatePickerFinish.show(childFragmentManager, bottomSheetDatePickerFinish.tag)
        }

        binding.btnAddDebt.setOnClickListener {
            clickOnOk()
        }
    }

    private fun setEditText() {
        binding.edtTxtDebtValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                val decimalFormat = DecimalFormat("#,###")
                binding.edtTxtDebtValue.removeTextChangedListener(this)


                var number = s.toString()

                number = number.replace(",", "")
                number = number.replace("٬", "")

                if (number.isNotBlank()) {
                    binding.edtTxtDebtValue.setText(decimalFormat.format(number.toLong()))
                    binding.edtTxtDebtValue.setSelection(binding.edtTxtDebtValue.text.length)
                }


                binding.edtTxtDebtValue.addTextChangedListener(this)
            }

        })

        binding.edtTxtPaidValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                val decimalFormat = DecimalFormat("#,###")
                binding.edtTxtPaidValue.removeTextChangedListener(this)


                var number = s.toString()

                number = number.replace(",", "")
                number = number.replace("٬", "")

                if (number.isNotBlank()) {
                    binding.edtTxtPaidValue.setText(decimalFormat.format(number.toLong()))
                    binding.edtTxtPaidValue.setSelection(binding.edtTxtPaidValue.text.length)
                }


                binding.edtTxtPaidValue.addTextChangedListener(this)
            }

        })

        binding.edtTxtBorrowerValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                val decimalFormat = DecimalFormat("#,###")
                binding.edtTxtBorrowerValue.removeTextChangedListener(this)


                var number = s.toString()

                number = number.replace(",", "")
                number = number.replace("٬", "")

                if (number.isNotBlank()) {
                    binding.edtTxtBorrowerValue.setText(decimalFormat.format(number.toLong()))
                    binding.edtTxtBorrowerValue.setSelection(binding.edtTxtBorrowerValue.text.length)
                }


                binding.edtTxtBorrowerValue.addTextChangedListener(this)
            }

        })
    }

    private fun setUI() {
        val decimalFormat = DecimalFormat("#,###")


        binding.txtTitle.text = getString(R.string.change_debt)
        binding.edtTxtName.setText(dataThisDebt.name)
        binding.edtTxtDebtTo.setText(dataThisDebt.debtTo)

        binding.edtTxtDebtValue.setText(decimalFormat.format(dataThisDebt.totalValue))
        binding.edtTxtPaidValue.setText(decimalFormat.format(dataThisDebt.paidValue))

        binding.txtStartDate.text = dataThisDebt.date
        binding.txtFinishDate.text = dataThisDebt.dateFinish

        binding.edtTxtBorrowerCount.setText(dataThisDebt.turnsCount!!.toString())
        binding.edtTxtPaidBorrower.setText(dataThisDebt.paidTurns!!.toString())
        binding.edtTxtBorrowerValue.setText(decimalFormat.format(dataThisDebt.turnValue!!))
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun setBundleData() {
        if (arguments != null) {
            mode = 1

            val item = requireArguments().getStringArray("data")!!
            dataThisDebt = DebtModel(
                item[0].toInt(),
                item[1],
                item[2].toLong(),
                item[3].toLong(),
                item[4],
                item[5],
                item[6],
                item[7].toInt(),
                item[8].toInt(),
                item[9].toLong()
            )
            setUI()
        } else {
            val gregorianDate = arrayOf(
                SimpleDateFormat("dd").format(Date()).toInt(),
                SimpleDateFormat("M").format(Date()).toInt(),
                SimpleDateFormat("yyyy").format(Date()).toInt()
            )
            //day , month , year
            val date =
                DateConvertor().gregorianToJalali(
                    gregorianDate[2],
                    gregorianDate[1],
                    gregorianDate[0]
                )

            binding.txtStartDate.text = "${date[0]} ${date[1]} ${date[2]}"
        }
    }

    private fun clickOnOk() {
        val isItemOk = ItemHandler().debtHandler(binding)
        if (isItemOk == "ok") {
            if (mode == 0) addDebt()
            else updateDebt()
        } else Toast.makeText(requireContext(), isItemOk, Toast.LENGTH_SHORT).show()
    }

    private fun updateDebt() {
        val newItem = makeItem()

        Thread {
            debtDao.updateData(newItem)
        }.start()

        dismiss()
        event.onUpdateItem(getString(R.string.debt_changed))
    }

    private fun addDebt() {
        if (isPlanOk(LimitType.Debt, requireContext())) {
            val newItem = makeItem()

            Thread {
                debtDao.insertData(newItem)
            }.start()

            dismiss()
            event.onAddItem(getString(R.string.debt_added))
        } else
            showNeedToSubscriptionDialog(requireContext())
    }

    @SuppressLint("SimpleDateFormat")
    private fun makeItem(): DebtModel {
        val name = binding.edtTxtName.text.toString()
        val totalValue = binding.edtTxtDebtValue.text.toString()
            .replace(",", "").replace("٬", "")
        val paidValue = binding.edtTxtPaidValue.text.toString().ifBlank { "0" }
            .replace(",", "").replace("٬", "")
        val debtTo = binding.edtTxtDebtTo.text.toString()
        val borrowerValue = binding.edtTxtBorrowerValue.text.toString()
            .replace(",", "").replace("٬", "")


        return if (mode == 0) DebtModel(
            name = name,
            totalValue = totalValue.toLong(),
            paidValue = paidValue.toLong(),
            debtTo = debtTo,
            date = binding.txtStartDate.text.toString(),
            dateFinish = binding.txtFinishDate.text.toString(),
            turnsCount = binding.edtTxtBorrowerCount.text.toString().toInt(),
            paidTurns = binding.edtTxtPaidBorrower.text.toString().ifBlank { "0" }.toInt(),
            turnValue = borrowerValue.toLong(),
        ) else DebtModel(
            dataThisDebt.id,
            name,
            totalValue.toLong(),
            paidValue.toLong(),
            debtTo,
            binding.txtStartDate.text.toString(),
            binding.txtFinishDate.text.toString(),
            binding.edtTxtBorrowerCount.text.toString().toInt(),
            binding.edtTxtPaidBorrower.text.toString().ifBlank { "0" }.toInt(),
            borrowerValue.toLong()
        )
    }

    override fun onDateChange(newDate: String, mode: Int) {
        if (mode == 0) {
            binding.txtStartDate.text = newDate
            dataThisDebt.date = newDate
        } else {
            binding.txtFinishDate.text = newDate
            dataThisDebt.dateFinish = newDate
        }
    }
}
