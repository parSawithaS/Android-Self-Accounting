package ir.androidir.SelfAccounting.ui.homeScreen.categoiesPage.iconBsh

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.FragmentChooseIconBinding
import ir.androidir.SelfAccounting.model.dataClasses.CategoryModel
import ir.androidir.SelfAccounting.ui.homeScreen.categoiesPage.BottomSheetCategoryEvent

class BottomSheetIcon(private val event: BottomSheetCategoryEvent) : BottomSheetDialogFragment(),
    IconEvent {
    private lateinit var binding: FragmentChooseIconBinding
    private lateinit var iconAdapter: IconAdapter
    private var dataThisCategory = CategoryModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChooseIconBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setAdapters()
        setBundle()
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
        val listIcon = arrayListOf(
            R.drawable.ad,
            R.drawable.bat,
            R.drawable.bed,
            R.drawable.arrow_down,
            R.drawable.arrow_up,
            R.drawable.bell,
            R.drawable.bitcoin,
            R.drawable.candy,
            R.drawable.captal_h,
            R.drawable.card,
            R.drawable.casher,
            R.drawable.categoryshapes,
            R.drawable.chart,
            R.drawable.chartmixed,
            R.drawable.chartpie,
            R.drawable.chartup,
            R.drawable.chinese,
            R.drawable.circletreepie,
            R.drawable.clapperboard,
            R.drawable.clock,
            R.drawable.cloud,
            R.drawable.club,
            R.drawable.code,
            R.drawable.colorpalate,
            R.drawable.dialpad,
            R.drawable.dice,
            R.drawable.document,
            R.drawable.fingerprint,
            R.drawable.flask,
            R.drawable.fork,
            R.drawable.gamingpad,
            R.drawable.gear,
            R.drawable.gem,
            R.drawable.glass,
            R.drawable.globe,
            R.drawable.graduation,
            R.drawable.heart,
            R.drawable.image,
            R.drawable.kit,
            R.drawable.laptop,
            R.drawable.list,
            R.drawable.location,
            R.drawable.map,
            R.drawable.message,
            R.drawable.music,
            R.drawable.partyhorn,
            R.drawable.scoter,
            R.drawable.shield,
            R.drawable.shieldcheck,
            R.drawable.shielddollar,
            R.drawable.signs,
            R.drawable.skull,
            R.drawable.snooze,
            R.drawable.switchkeys,
            R.drawable.terminal,
            R.drawable.user,
            R.drawable.video,
            R.drawable.virus,
            R.drawable.wallet,
            R.drawable.yen,
            R.drawable.baby,
            R.drawable.baby_carriage,
            R.drawable.bag,
            R.drawable.bag_plus,
            R.drawable.bicycle,
            R.drawable.bookmark,
            R.drawable.card_add,
            R.drawable.card_down,
            R.drawable.card_up,
            R.drawable.cash,
            R.drawable.cloud,
            R.drawable.coin_send,
            R.drawable.discount,
            R.drawable.gift,
            R.drawable.headset,
            R.drawable.heart,
            R.drawable.info,
            R.drawable.linkedin,
            R.drawable.money_send,
            R.drawable.music,
            R.drawable.photo,
            R.drawable.shop,
            R.drawable.shopping_cart,
            R.drawable.smoothie,
            R.drawable.star,
            R.drawable.tag_2,
            R.drawable.trophy,
            R.drawable.video2,
            R.drawable.video3,
            R.drawable.wallet2
        )



        //icon adapter =>
        iconAdapter = IconAdapter(listIcon, this)
        binding.recyclerIcon.adapter = iconAdapter
        binding.recyclerIcon.layoutManager =
            GridLayoutManager(binding.root.context, 5, GridLayoutManager.VERTICAL, false)

    }

    override fun onClickIcon(item: Int) {
        dataThisCategory.icon = item
        event.moveItem(dataThisCategory)
        dismiss()
    }
}
