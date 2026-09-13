package ir.androidir.SelfAccounting.utils

import android.content.Context
import androidx.core.content.ContextCompat
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.model.HesabchiDataBase
import ir.androidir.SelfAccounting.model.dao.CardDao
import ir.androidir.SelfAccounting.model.dao.CategoryDao
import ir.androidir.SelfAccounting.model.dao.TransactionDao
import ir.androidir.SelfAccounting.model.dataClasses.CardModel
import ir.androidir.SelfAccounting.model.dataClasses.CategoryModel
import ir.androidir.SelfAccounting.model.dataClasses.TransactionModel
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.ModelsOfTransaction
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.TransactionRadioMode
import ir.androidir.SelfAccounting.model.dataClasses.nonDataBase.Transaction2Item
import java.text.DecimalFormat


val getColor: (Int, Context) -> Int = { color, context ->
    if (color != 0)
        ContextCompat.getColor(context, color)
    else
        0
}


//1.get all categories whose totalValues aren't 0
//2.make an transaction2 item for each category
//3.add transactions to transaction2 items
//4.delete items with 0 transactions
fun convertAllCategories(
    transactionList: ArrayList<TransactionModel>,
    radioMode: TransactionRadioMode,
    context: Context
): ArrayList<Transaction2Item> {
    val hesabchiDataBase = HesabchiDataBase.getDataBase(context)!!
    val categoryDao = hesabchiDataBase.categoryDao
    val cardDao = hesabchiDataBase.cardDao

    return when (radioMode) {
        TransactionRadioMode.Category -> {
            //1.
            val filteredCategories = arrayListOf<CategoryModel>()
            categoryDao.selectData().forEach {
                if (it.valueExpenses != 0L || it.valueIncomes != 0L) {
                    filteredCategories.add(it)
                }
            }

            //2.
            val transaction2Items = arrayListOf<Transaction2Item>()
            filteredCategories.forEach {
                transaction2Items.add(
                    Transaction2Item(
                        it.name, it.icon, it.color, 0, TransactionRadioMode.Category
                    )
                )
            }


            //3.
            val listToBack = arrayListOf<Transaction2Item>()
            transaction2Items.forEach { t2 ->
                var value = 0L
                transactionList.forEach { t1 ->
                    if (t2.name == t1.category) {
                        if (t1.mode == ModelsOfTransaction.Income)
                            value += convertStringValue(t1.value, context)
                        else
                            value -= convertStringValue(t1.value, context)
                    }
                }

                t2.value = value
                listToBack.add(t2)
            }

            //4.
            val listToDelete = arrayListOf<Transaction2Item>()
            listToBack.forEach { t2 ->
                var needToDelete = true
                transactionList.forEach { t1 ->
                    if (t2.name == t1.category)
                        needToDelete = false
                }

                if (needToDelete)
                    listToDelete.add(t2)
            }

            listToDelete.forEach {
                listToBack.remove(it)
            }

            listToBack
        }

        TransactionRadioMode.Card -> {
            //1.
            val allCards = arrayListOf<CardModel>().apply {
                addAll(cardDao.selectData())
            }

            //2.
            val transaction2Items = arrayListOf<Transaction2Item>()
            allCards.forEach {
                transaction2Items.add(
                    Transaction2Item(
                        it.cardNumber,
                        BankSelector().selectBankImage(
                            it.cardNumber.substring2(
                                0,
                                6
                            )
                        ),
                        0, convertStringValue(it.cardValue,context),TransactionRadioMode.Card
                    )
                )
            }

            //3.
            val listToBack = arrayListOf<Transaction2Item>()
            transaction2Items.forEach { t2 ->
                var value = 0L
                transactionList.forEach { t1 ->
                    if (t2.name.substring2(12,16) == t1.card.substring2(0,4)) {
                        if (t1.mode == ModelsOfTransaction.Income)
                            value += convertStringValue(t1.value, context)
                        else
                            value -= convertStringValue(t1.value, context)
                    }
                }

                t2.value = value
                listToBack.add(t2)
            }

            //4.
            val listToDelete = arrayListOf<Transaction2Item>()
            listToBack.forEach { t2 ->
                var needToDelete = true
                transactionList.forEach { t1 ->
                    if (t2.name.substring2(12,16) == t1.card.substring2(0,4))
                        needToDelete = false
                }

                if (needToDelete)
                    listToDelete.add(t2)
            }

            listToDelete.forEach {
                listToBack.remove(it)
            }

            listToBack
        }

        else -> {
            arrayListOf()
        }
    }
}

fun convertDataList(
    totalData: ArrayList<TransactionModel>,
    radioMode: TransactionRadioMode,
    context: Context
): ArrayList<Transaction2Item> {
    val listToBack = convertAllCategories(totalData, radioMode, context)
    when (radioMode) {
        TransactionRadioMode.Category -> {
            val withoutCategory = Transaction2Item(
                context.getString(R.string.without_category), 0, 0, 0, TransactionRadioMode.Category
            )

            totalData.forEach {
                if (it.category == context.getString(R.string.without_category)) {
                    if (it.mode == ModelsOfTransaction.Income)
                        withoutCategory.value += convertStringValue(
                            it.value,
                            context
                        )
                    else
                        withoutCategory.value -= convertStringValue(
                            it.value,
                            context
                        )
                }

            }

            listToBack.add(0, withoutCategory)
        }

        TransactionRadioMode.Card -> {
            val allCards = Transaction2Item(
                context.getString(R.string.all), 0, 0, 0, TransactionRadioMode.Card
            )


            totalData.forEach {
                if (it.card == context.getString(R.string.all)) {
                    if (it.mode == ModelsOfTransaction.Income)
                        allCards.value += convertStringValue(
                            it.value,
                            context
                        )
                    else
                        allCards.value -= convertStringValue(
                            it.value,
                            context
                        )
                }

            }

            listToBack.add(0, allCards)
        }

        else -> {}
    }

    return listToBack
}


//--------------------------------------------------------------------------------------------------


fun setCategoryValues(
    category: CategoryModel,
    transactionDao: TransactionDao,
    categoryDao: CategoryDao
): CategoryModel {
    var transactionValue = 0L
    var transactionCount = 0
    var incomeCount = 0
    var incomeValue = 0L
    var expenseCount = 0
    var expenseValue = 0L
    transactionDao.selectData().forEach {
        if (it.category == category.name) {
            transactionCount++
            if (it.mode == ModelsOfTransaction.Income) {
                transactionValue += it.value.replace(",", "").toLong()
                incomeCount++
                incomeValue += it.value.replace(",", "").toLong()
            } else {
                transactionValue += -(it.value.replace(",", "").toLong())
                expenseCount++
                expenseValue += -(it.value.replace(",", "").toLong())
            }
        }
    }


    val newItem = CategoryModel(
        category.id,
        category.name,
        transactionValue,
        incomeValue,
        expenseValue,
        transactionCount,
        incomeCount,
        expenseCount,
        category.color,
        category.icon,
        category.type
    )
    Thread {
        categoryDao.updateData(newItem)
    }.start()

    return newItem
}

val findCategoryByName: (CategoryDao, String) -> CategoryModel = { dao, name ->
    var valueToBack = CategoryModel()
    dao.selectData().forEach {
        if (it.name == name)
            valueToBack = it
    }
    valueToBack
}


//--------------------------------------------------------------------------------------------------


fun setCardValues(
    card: CardModel,
    transactionDao: TransactionDao,
    cardDao: CardDao
): CardModel {
    var transactionValue = 0L
    val decimalFormat = DecimalFormat("#,###")
    transactionDao.selectData().forEach {

        if (it.card == card.cardNumber.substring2(12, 16) + " ${card.bank}") {
            transactionValue += when (it.mode) {
                ModelsOfTransaction.Income -> it.value.replace(",", "").toLong()
                ModelsOfTransaction.Expense -> -(it.value.replace(",", "").toLong())
                else -> 0
            }

        }


        if (it.mode == ModelsOfTransaction.Transfer) {

            if (it.card == card.cardNumber.substring2(12, 16) + " ${card.bank}")
                transactionValue += it.value.replace(",", "").toLong()

            if (it.category == card.cardNumber.substring2(12, 16) + " ${card.bank}")
                transactionValue += -(it.value.replace(",", "").toLong())

        }
    }


    val newItem = CardModel(
        card.id,
        card.cardNumber,
        card.cardDefaultValue,
        decimalFormat.format(transactionValue),
        card.bank
    )
    Thread {
        cardDao.updateData(newItem)
    }.start()

    return newItem
}

val findCardByName: (CardDao, String) -> CardModel = { dao, name ->
    var valueToBack = CardModel()
    dao.selectData().forEach {
        if (it.cardNumber.substring2(12, 16) == name.substring2(0, 4))
            valueToBack = it
    }
    valueToBack
}
