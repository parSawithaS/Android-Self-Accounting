package ir.androidir.SelfAccounting.ui.homeScreen.categoiesPage.colorBsh

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.FragmentChooseColorBinding
import ir.androidir.SelfAccounting.model.dataClasses.CategoryModel
import ir.androidir.SelfAccounting.ui.homeScreen.categoiesPage.BottomSheetCategoryEvent

class BottomSheetColor(private val event: BottomSheetCategoryEvent) : BottomSheetDialogFragment(),
    ColorEvent {
    private lateinit var binding: FragmentChooseColorBinding
    private lateinit var colorAdapter: ColorAdapter
    private var dataThisCategory = CategoryModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChooseColorBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setBundle()
        setAdapters()
    }

    private fun setBundle() {
        if (arguments != null) {
            dataThisCategory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireArguments().getParcelable("item" , CategoryModel::class.java)!!
            } else {
                requireArguments().getParcelable("item")!!
            }
        }
    }

    private fun setAdapters() {
        val listColor = arrayListOf(
            R.color.blue1,
            R.color.blue2,
            R.color.blue3,
            R.color.blue4,
            R.color.red,
            R.color.purple1,
            R.color.purple2,
            R.color.purple3,
            R.color.orange1,
            R.color.orange2,
            R.color.yellow1,
            R.color.yellow2,
            R.color.yellowAndGreen,
            R.color.green1,
            R.color.green2,
            R.color.greenAndBlue,
            R.color.white,
            R.color.black
        )


        //icon adapter =>
        colorAdapter = ColorAdapter(listColor, this)
        binding.recyclerColor.adapter = colorAdapter
        binding.recyclerColor.layoutManager =
            GridLayoutManager(binding.root.context, 5, GridLayoutManager.VERTICAL, false)
    }

    override fun onClickColor(item: Int) {
        dataThisCategory.color = item
        event.moveItem(dataThisCategory)
        dismiss()
    }
}
