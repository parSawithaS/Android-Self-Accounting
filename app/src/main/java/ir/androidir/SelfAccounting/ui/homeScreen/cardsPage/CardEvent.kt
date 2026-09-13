package ir.androidir.SelfAccounting.ui.homeScreen.cardsPage

import ir.androidir.SelfAccounting.model.dataClasses.CardModel

interface CardEvent {
    fun clickShort(item: CardModel ,mode: Int = 0)
    fun clickLong(viewOld: CardModel, position: Int)
}