package ir.androidir.SelfAccounting.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ir.androidir.SelfAccounting.model.dao.BudgetDao
import ir.androidir.SelfAccounting.model.dao.CardDao
import ir.androidir.SelfAccounting.model.dao.CategoryDao
import ir.androidir.SelfAccounting.model.dao.DebtDao
import ir.androidir.SelfAccounting.model.dao.TransactionDao
import ir.androidir.SelfAccounting.model.dataClasses.BudgetModel
import ir.androidir.SelfAccounting.model.dataClasses.CardModel
import ir.androidir.SelfAccounting.model.dataClasses.CategoryModel
import ir.androidir.SelfAccounting.model.dataClasses.DebtModel
import ir.androidir.SelfAccounting.model.dataClasses.TransactionModel

@Database(
    version = 16,
    exportSchema = false,
    entities = [
        TransactionModel::class,
        CardModel::class,
        CategoryModel::class,
        BudgetModel::class,
        DebtModel::class
    ]
)
abstract class HesabchiDataBase : RoomDatabase() {

    abstract val transactionDao: TransactionDao
    abstract val cardDao: CardDao
    abstract val categoryDao: CategoryDao
    abstract val budgetDao: BudgetDao
    abstract val debtDao: DebtDao

    companion object {
        private var MyHesabchiDataBase: HesabchiDataBase? = null

        private val MIGRATION1 = object : Migration(10, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "create table BudgetModel(" +
                            "name TEXT NOT NULL,\n" +
                            "totalValue INTEGER NOT NULL,\n" +
                            "budgetCategory TEXT NOT NULL,\n" +
                            "budgetCard TEXT NOT NULL,\n" +
                            "date TEXT NOT NULL,\n" +
                            "id INTEGER PRIMARY KEY ,\n" +
                            "usedValue INTEGER NOT NULL" +
                            ")"
                )
            }
        }

        private val MIGRATION2 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                //ALTER TABLE nameTable ADD RowName int(dataType)
                db.execSQL("ALTER TABLE CategoryModel ADD color INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE CategoryModel ADD icon INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION3 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "create table DebtModel(" +
                            "name TEXT NOT NULL,\n" +
                            "totalValue INTEGER NOT NULL,\n" +
                            "paidValue INTEGER NOT NULL,\n" +
                            "debtTo TEXT NOT NULL,\n" +
                            "id INTEGER PRIMARY KEY ,\n" +
                            "date TEXT NOT NULL" +
                            ")"
                )
            }
        }

        private val MIGRATION4 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE DebtModel ADD dateFinish TEXT DEFAULT undefined")
                db.execSQL("ALTER TABLE DebtModel ADD turnsCount INTEGER DEFAULT undefined")
                db.execSQL("ALTER TABLE DebtModel ADD paidTurns INTEGER DEFAULT undefined")
                db.execSQL("ALTER TABLE DebtModel ADD turnValue INTEGER DEFAULT undefined")
                db.execSQL("ALTER TABLE DebtModel ADD registerAutomatically INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE DebtModel ADD intervalBetweenBorrowers INTEGER DEFAULT undefined")
            }
        }

        private val MIGRATION5 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE CategoryModel ADD type TEXT NOT NULL DEFAULT \"All\"")
            }
        }

        fun getDataBase(context: Context): HesabchiDataBase? {
            synchronized(this) {
                if (MyHesabchiDataBase == null) {
                    MyHesabchiDataBase = Room.databaseBuilder(
                        context.applicationContext,
                        HesabchiDataBase::class.java,
                        "DataBase"
                    ).addMigrations(MIGRATION1, MIGRATION2, MIGRATION3, MIGRATION4, MIGRATION5)
                        .allowMainThreadQueries()
                        .build()
                }
                return MyHesabchiDataBase!!
            }
        }

    }

}
