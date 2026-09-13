package ir.androidir.SelfAccounting.ui.chooseItemBsh

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.BottomSheetChooseItemBinding
import ir.androidir.SelfAccounting.model.HesabchiDataBase
import ir.androidir.SelfAccounting.model.dao.CardDao
import ir.androidir.SelfAccounting.model.dao.CategoryDao
import ir.androidir.SelfAccounting.model.dataClasses.CardModel
import ir.androidir.SelfAccounting.model.dataClasses.CategoryModel
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.CategoryType
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.ItemMode
import ir.androidir.SelfAccounting.ui.homeScreen.cardsPage.BottomSheetAddCard
import ir.androidir.SelfAccounting.ui.homeScreen.cardsPage.CardEvent
import ir.androidir.SelfAccounting.ui.homeScreen.categoiesPage.BottomSheetAddCategory
import ir.androidir.SelfAccounting.ui.homeScreen.categoiesPage.CategoryEvent
import ir.androidir.SelfAccounting.utils.BottomSheetEvent

class BottomSheetChooseItem(
    val mode: ItemMode,
    private val categoryEvent: CategoryEvent,
    private val cardEvent: CardEvent,
    private val type: CategoryType? = null
) : BottomSheetDialogFragment(), ChooseEvent, BottomSheetEvent {
    private lateinit var binding: BottomSheetChooseItemBinding
    private lateinit var categoryDao: CategoryDao
    private lateinit var cardDao: CardDao
    private val categoryMode = ItemMode.Category
    private val cardMode = ItemMode.Card
    private val cardMode2 = ItemMode.Card2
    private val bothMode = ItemMode.Both

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetChooseItemBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val hesabchiDataBase = HesabchiDataBase.getDataBase(binding.root.context)!!
        categoryDao = hesabchiDataBase.categoryDao
        cardDao = hesabchiDataBase.cardDao



        setTitle()
        setAdapter()
        setAddButton()
    }

    private fun setAddButton() {
        binding.txtName.text = when (mode) {
            categoryMode -> {
                getString(R.string.add_category)
            }

            cardMode, cardMode2 -> {
                getString(R.string.add_card)
            }

            else -> {
                ""
            }
        }

        if (mode == bothMode)
            binding.layoutAdd.visibility = View.GONE

        binding.layoutAdd.setOnClickListener {
            if (mode == categoryMode) {
                val bottomSheet = BottomSheetAddCategory(this)
                bottomSheet.show(childFragmentManager, bottomSheet.tag)
            } else {
                val bottomSheet = BottomSheetAddCard(this)
                bottomSheet.show(childFragmentManager, bottomSheet.tag)
            }
        }
    }

    private fun setTitle() {
        binding.txtTitle.text = when (mode) {
            categoryMode -> {
                getString(R.string.categories)
            }

            cardMode, cardMode2 -> {
                getString(R.string.cards)
            }

            bothMode -> {
                getString(R.string.cardsAndCategories)
            }

            else -> {
                ""
            }
        }
    }

    private fun setAdapter() {
        val myAdapter = ChooseAdapter(mode, this, binding.root.context,type)
        binding.recyclerView.adapter = myAdapter
        binding.recyclerView.layoutManager =
            LinearLayoutManager(binding.root.context, RecyclerView.VERTICAL, false)
    }

    override fun onClick(itemId: Int, itemMode: Int) {
        dismiss()

        when (mode) {
            categoryMode -> {
                if (itemId > 0) {
                    var item = CategoryModel()
                    categoryDao.selectData().forEach {
                        if (it.id!! == itemId) item = it
                    }
                    categoryEvent.clickShort(item)
                } else {
                    val item = CategoryModel(-1,getString(R.string.all))
                    categoryEvent.clickShort(item)
                }
            }

            cardMode, cardMode2 -> {
                if (itemId > 0) {
                    var item = CardModel()
                    cardDao.selectData().forEach {
                        if (it.id!! == itemId) item = it
                    }
                    if (mode == cardMode)
                        cardEvent.clickShort(item)
                    else
                        cardEvent.clickShort(item, 1)
                } else {
                    val item = CardModel(-1,getString(R.string.all))
                    cardEvent.clickLong(item ,0)
                }
            }

            bothMode -> {
                when (itemMode) {
                    0 -> {
                        var item = CategoryModel()

                        categoryDao.selectData().forEach {
                            if (it.id == itemId)
                                item = it
                        }

                        categoryEvent.clickShort(item)
                    }

                    1 -> {
                        var item = CardModel()

                        cardDao.selectData().forEach {
                            if (it.id == itemId)
                                item = it
                        }

                        cardEvent.clickShort(item)
                    }

                    else -> {
                        //item is all =>
                        categoryEvent.clickLong(CategoryModel(), itemId)
                    }
                }
            }

            else -> {}
        }
    }

    override fun onAddItem(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
        setAdapter()
    }

    override fun onUpdateItem(text: String) {}
}