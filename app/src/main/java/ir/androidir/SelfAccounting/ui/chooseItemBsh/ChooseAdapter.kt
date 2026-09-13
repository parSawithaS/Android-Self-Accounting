package ir.androidir.SelfAccounting.ui.chooseItemBsh

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.ItemBottomSheetBinding
import ir.androidir.SelfAccounting.model.HesabchiDataBase
import ir.androidir.SelfAccounting.model.dao.CardDao
import ir.androidir.SelfAccounting.model.dao.CategoryDao
import ir.androidir.SelfAccounting.model.dataClasses.CardModel
import ir.androidir.SelfAccounting.model.dataClasses.CategoryModel
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.CategoryType
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.ItemMode
import ir.androidir.SelfAccounting.utils.BankSelector
import java.text.DecimalFormat

class ChooseAdapter(
    private val mode: ItemMode,
    val chooseEvent: ChooseEvent,
    val context: Context,
    private val type: CategoryType? = null
) : RecyclerView.Adapter<ChooseAdapter.ChooseHolder>() {
    private lateinit var categoryDao: CategoryDao
    private lateinit var cardDao: CardDao
    private lateinit var dataList: ArrayList<Array<String>>
    private val categoryMode = ItemMode.Category
    private val cardMode = ItemMode.Card
    private val cardMode2 = ItemMode.Card2
    private val bothMode = ItemMode.Both
    private var categoryDataList = ArrayList<CategoryModel>()

    init {
        fun initDataList() {
            val allData = categoryDao.selectData()

            when (type) {
                CategoryType.Income -> {
                    allData.forEach {
                        if (it.type == "Income" || it.type == "All")
                            categoryDataList.add(it)
                    }
                }

                CategoryType.Expense -> {
                    allData.forEach {
                        if (it.type == "Expense" || it.type == "All")
                            categoryDataList.add(it)
                    }
                }

                else -> {
                    categoryDataList.addAll(allData)
                }
            }


            val sortedList = categoryDataList.sortedBy { it.type }
            categoryDataList.clear()
            categoryDataList.addAll(sortedList)
            categoryDataList.add(CategoryModel(name = context.getString(R.string.without_category), id = -1))
            categoryDataList.reverse()
        }


        val hesabchiDataBase = HesabchiDataBase.getDataBase(context)!!
        if (mode == ItemMode.Category) {
            categoryDao = hesabchiDataBase.categoryDao
            initDataList()
        }


        if (mode == cardMode || mode == cardMode2)
            cardDao = hesabchiDataBase.cardDao

        if (mode == bothMode) {
            categoryDao = hesabchiDataBase.categoryDao
            cardDao = hesabchiDataBase.cardDao
        }

        //make a list with all categories and cards
        if (mode == bothMode) {
            val cardData = cardDao.selectData()
            val categoryData = categoryDao.selectData().sortedBy { it.type }.reversed()

            val categoryArrayList = arrayListOf<Array<String>>()
            val cardArrayList = arrayListOf<Array<String>>()

            categoryData.forEach {
                categoryArrayList.add(
                    arrayOf(
                        it.name,
                        it.icon.toString(),
                        it.color.toString()
                    )
                )
            }
            cardData.forEach {
                val decimalFormat = DecimalFormat("#,####")
                cardArrayList.add(
                    arrayOf(
                        decimalFormat.format(it.cardNumber.toLong())
                            .replace(",", "-")
                            .replace("٬", "-"),
                        BankSelector().selectBankImage(it.cardNumber.substring(0, 6)).toString()
                    )
                )
            }

            val list = ArrayList<Array<String>>()
            list.add(arrayOf(context.getString(R.string.all)))
            list.add(arrayOf(context.getString(R.string.all_cards)))
            list.add(arrayOf(context.getString(R.string.all_categories)))


            categoryArrayList.forEach {
                list.add(it)
            }
            cardArrayList.forEach {
                list.add(it)
            }

            dataList = list
        }
    }

    inner class ChooseHolder(
        private val binding: ItemBottomSheetBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(position: Int) {
            when (mode) {
                categoryMode -> {
                    binding.txtName.text = categoryDataList[position].name
                    binding.img.setImageResource(categoryDataList[position].icon)
                    binding.img.setColorFilter(
                        ContextCompat.getColor(
                            binding.root.context,
                            categoryDataList[position].color
                        )
                    )

                    if (categoryDataList[position].id!! > 0) {
                        val colorAndTxt = when (categoryDataList[position].type) {
                            CategoryType.Income.toString() -> {
                                Pair(R.color.green, context.getString(R.string.income))
                            }

                            CategoryType.Expense.toString() -> {
                                Pair(R.color.red, context.getString(R.string.expense))
                            }

                            CategoryType.All.toString() -> {
                                Pair(R.color.colorPrimaryContainer, context.getString(R.string.all))
                            }

                            else -> Pair(android.R.color.transparent, "")
                        }
                        binding.viewTypeColor.setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                colorAndTxt.first
                            )
                        )
                        binding.txtType.setTextColor(ContextCompat.getColor(context, colorAndTxt.first))
                        binding.txtType.text = colorAndTxt.second
                        binding.txtType.visibility = View.VISIBLE
                    } else
                        binding.img.visibility = View.GONE

                    itemView.setOnClickListener {
                        chooseEvent.onClick(categoryDataList[position].id!!, 0)
                    }
                }

                cardMode, cardMode2 -> {
                    val allData = cardDao.selectData() as ArrayList
                    if (mode == cardMode)
                        allData.add(0,CardModel(-1,context.getString(R.string.all)))
                    val data = allData[position]


                    if (data.id!! > 0){
                        val decimalFormat = DecimalFormat("#,####")
                        binding.txtName.text =
                            decimalFormat.format(data.cardNumber.toLong())
                                .replace(",", "-")
                                .replace("٬", "-")

                        binding.img.setImageResource(
                            BankSelector().selectBankImage(
                                data.cardNumber.substring(
                                    0,
                                    6
                                )
                            )
                        )
                    } else {
                        binding.img.visibility = View.GONE
                        binding.txtName.text = data.cardNumber
                    }



                    itemView.setOnClickListener {
                        chooseEvent.onClick(data.id, 1)
                    }
                }

                bothMode -> {
                    val item = dataList[position]

                    when (item.size) {
                        //item is category =>
                        3 -> {
                            binding.txtName.text = item[0]
                            binding.img.setImageResource(item[1].toInt())
                            binding.img.setColorFilter(
                                ContextCompat.getColor(binding.root.context, item[2].toInt())
                            )

                            var categoryItem = CategoryModel()
                            categoryDao.selectData().forEach {
                                if (it.name == item[0])
                                    categoryItem = it
                            }


                            //set type txt =>
                            run {
                                val colorAndTxt = when (categoryItem.type) {
                                    CategoryType.Income.toString() -> {
                                        Pair(R.color.green, context.getString(R.string.income))
                                    }

                                    CategoryType.Expense.toString() -> {
                                        Pair(R.color.red, context.getString(R.string.expense))
                                    }

                                    CategoryType.All.toString() -> {
                                        Pair(R.color.colorPrimaryContainer, context.getString(R.string.all))
                                    }

                                    else -> Pair(android.R.color.transparent, "")
                                }
                                binding.viewTypeColor.setBackgroundColor(
                                    ContextCompat.getColor(
                                        context,
                                        colorAndTxt.first
                                    )
                                )
                                binding.txtType.setTextColor(ContextCompat.getColor(context, colorAndTxt.first))
                                binding.txtType.text = colorAndTxt.second
                                binding.txtType.visibility = View.VISIBLE
                                binding.txtType.text
                            }


                            //clicker =>
                            itemView.setOnClickListener {
                                chooseEvent.onClick(categoryItem.id!!, 0)
                            }

                        }

                        //item is card =>
                        2 -> {
                            binding.txtName.text = item[0]
                            binding.img.setImageResource(item[1].toInt())

                            var cardItem = CardModel()
                            cardDao.selectData().forEach {
                                if (it.cardNumber.substring(12, 16) == item[0].substring(15, 19))
                                    cardItem = it
                            }

                            itemView.setOnClickListener {
                                chooseEvent.onClick(cardItem.id!!, 1)
                            }

                        }

                        //item is all =>
                        1 -> {
                            binding.txtName.text = item[0]
                            binding.img.visibility = View.GONE

                            val id = when (item[0]) {
                                context.getString(R.string.all) -> 1
                                context.getString(R.string.all_cards) -> 2
                                context.getString(R.string.all_categories) -> 3
                                else -> 0
                            }

                            itemView.setOnClickListener {
                                chooseEvent.onClick(id, -1)
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChooseHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemBottomSheetBinding.inflate(layoutInflater, parent, false)
        return ChooseHolder(binding)
    }

    override fun onBindViewHolder(holder: ChooseHolder, position: Int) {
        holder.bindData(position)
    }

    override fun getItemCount(): Int {
        return when (mode) {
            categoryMode -> {
                categoryDataList.size
            }

            cardMode, cardMode2 -> {
                if (mode == cardMode)
                    cardDao.selectData().size + 1
                else
                    cardDao.selectData().size
            }

            else -> {
                cardDao.selectData().size + categoryDao.selectData().size + 3
            }
        }
    }
}
