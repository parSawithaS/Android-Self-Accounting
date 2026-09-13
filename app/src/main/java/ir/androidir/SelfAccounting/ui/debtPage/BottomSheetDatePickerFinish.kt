package ir.androidir.SelfAccounting.ui.debtPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.DatePickerBottomSheetBinding

class BottomSheetDatePickerFinish(private val event: DateEvent) : BottomSheetDialogFragment() {
    private lateinit var binding: DatePickerBottomSheetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DatePickerBottomSheetBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setUI()
        binding.button.setOnClickListener {
            changeDate()
        }
    }

    private fun changeDate() {
        val year = binding.edtTxtYear.text.toString()
        val month = binding.edtTxtMonth.text.toString()
        val day = binding.edtTxtDay.text.toString()
        if (year.isNotEmpty() && month.isNotEmpty() && day.isNotEmpty()) {
            if (month.toInt() > 12 || day.toInt() > 31 || day.toInt() == 0)
                Toast.makeText(requireContext(), getString(R.string.please_enter_values_right), Toast.LENGTH_SHORT).show()
            else {
                dismiss()
                val monthName = when (month.toInt()) {
                    1 -> "فروردین"
                    2 -> "اردیبهشت"
                    3 -> "خرداد"
                    4 -> "تیر"
                    5 -> "مرداد"
                    6 -> "شهریور"
                    7 -> "مهر"
                    8 -> "آبان"
                    9 -> "آذر"
                    10 -> "دی"
                    11 -> "بهمن"
                    12 -> "اسفند"
                    else -> ""
                }.toString()
                event.onDateChange("$day $monthName $year", 1)
            }
        } else
            Toast.makeText(
                requireContext(),
                getString(R.string.please_enter_all_values),
                Toast.LENGTH_SHORT
            ).show()

    }

    private fun setUI() {
        val rawDate = requireArguments().getStringArray("dates")!!
        if (rawDate[0] != null && rawDate[0] != getString(R.string.unknown)) {
            val splitDate = rawDate[0].split(" ")
            val year = splitDate[2]
            val monthName = splitDate[1]
            val day = splitDate[0]
            val month = when (monthName) {
                "فروردین" -> 1
                "اردیبهشت" -> 2
                "خرداد" -> 3
                "تیر" -> 4
                "مرداد" -> 5
                "شهریور" -> 6
                "مهر" -> 7
                "آبان" -> 8
                "آذر" -> 9
                "دی" -> 10
                "بهمن" -> 11
                "اسفند" -> 12
                else -> 1
            }.toString()

            binding.edtTxtYear.setText(year)
            binding.edtTxtMonth.setText(month)
            binding.edtTxtDay.setText(day)
        }
    }
}
