package ir.androidir.SelfAccounting.ui.homeScreen

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.ActivityMainBinding
import ir.androidir.SelfAccounting.databinding.DialogChangeLanguageBinding
import ir.androidir.SelfAccounting.databinding.DialogChangeThemeBinding
import ir.androidir.SelfAccounting.databinding.DialogQuitAppBinding
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.CategoryType
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.TransactionRadioMode
import ir.androidir.SelfAccounting.ui.backupPage.BackupBottomSheet
import ir.androidir.SelfAccounting.ui.budgetPage.BudgetActivity
import ir.androidir.SelfAccounting.ui.debtPage.DebtActivity
import ir.androidir.SelfAccounting.ui.homeScreen.cardsPage.BottomSheetAddCard
import ir.androidir.SelfAccounting.ui.homeScreen.cardsPage.CardsFragment
import ir.androidir.SelfAccounting.ui.homeScreen.categoiesPage.BottomSheetAddCategory
import ir.androidir.SelfAccounting.ui.homeScreen.categoiesPage.CategoryFragment
import ir.androidir.SelfAccounting.ui.homeScreen.historyPage.HistoryFragment
import ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage.BottomSheetAddTransaction
import ir.androidir.SelfAccounting.ui.homeScreen.transactionsPage.TransactionFragment
import ir.androidir.SelfAccounting.ui.subscriptionPage.SubscriptionActivity
import ir.androidir.SelfAccounting.utils.BottomSheetEvent
import ir.androidir.SelfAccounting.utils.CountActions
import ir.androidir.SelfAccounting.utils.SHARED_PREFERENCES_TAG
import java.util.Locale
import kotlin.system.exitProcess

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), BottomSheetEvent, CountActions {
    private lateinit var binding: ActivityMainBinding
    private var fragmentLoaded = 1
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        //Transaction Fragment =>
        var tFilter: String? = null
        var tMode: TransactionRadioMode? = null

        //History Fragment =>
        var hRadio1: TransactionRadioMode? = null
        var hPacked: Boolean? = null
        var hRadio3: String? = null
        var hFilter: String? = null

        //Category Fragment =>
        var cRadio: CategoryType? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_TAG, MODE_PRIVATE)
        setLanguage()
        setContentView(binding.root)
        addAction(this)



        setFragment(TransactionFragment())
        bottomNavigation()
        navigationDrawer()
        buttonAdd()
    }

    override fun onRestart() {
        super.onRestart()
        setLanguage()
    }

    private fun setLanguage() {
        val languageTag = sharedPreferences.getString("languageTag", "fa")!!

        val locale = Locale(languageTag)        //create locale with language tag
        val configuration = resources.configuration      //create configuration
        configuration.setLocale(locale)     //set locale for configuration
        resources.updateConfiguration(configuration, DisplayMetrics())     //set language
        sharedPreferences.edit().putString("languageTag", languageTag).apply()
    }

    private fun buttonAdd() {
        binding.btnAdd.setOnClickListener {
            when (fragmentLoaded) {
                1, 2 -> {
                    val bottomSheetFragment = BottomSheetAddTransaction(this)

                    bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
                }

                3 -> {
                    val bottomSheetFragment = BottomSheetAddCard(this)
                    bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
                }

                4 -> {
                    val bottomSheetFragment = BottomSheetAddCategory(this)
                    bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
                }
            }
        }
    }

    private fun navigationDrawer() {
        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this, binding.root, binding.toolbar.toolbar, R.string.open_drawer, R.string.close_drawer
        )
        actionBarDrawerToggle.syncState()
        binding.root.addDrawerListener(actionBarDrawerToggle)


        binding.navigationDrawer.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.budget -> {
                    binding.root.closeDrawer(GravityCompat.START)
                    val intent = Intent(this, BudgetActivity::class.java)
                    startActivity(intent)
                }

                R.id.debt -> {
                    binding.root.closeDrawer(GravityCompat.START)
                    val intent = Intent(this, DebtActivity::class.java)
                    startActivity(intent)
                }

                R.id.alarm -> {
                    binding.root.closeDrawer(GravityCompat.START)
                    val alarmIntent = Intent(
                        this, ir.androidir.SelfAccounting.ui.alarmPage.AlarmActivity::class.java
                    )
                    startActivity(alarmIntent)
                }

                R.id.backup -> {
                    binding.root.closeDrawer(GravityCompat.START)
                    val bottomSheet = BackupBottomSheet()
                    bottomSheet.show(supportFragmentManager, bottomSheet.tag)
                }

                R.id.subscription -> {
                    binding.root.closeDrawer(GravityCompat.START)
                    val subscriptionIntent = Intent(this, SubscriptionActivity::class.java)
                    startActivity(subscriptionIntent)
                }

                R.id.language -> {
                    languageDialog()
                }

                R.id.theme -> {
                    changeTheme()
                }

                R.id.exit -> {
                    binding.root.closeDrawer(GravityCompat.START)
                    exitApp()
                }
            }
            true
        }
    }

    private fun languageDialog() {
        binding.root.closeDrawer(GravityCompat.START)
        val dialog = AlertDialog.Builder(this).create()
        val dialogBinding = DialogChangeLanguageBinding.inflate(layoutInflater)

        dialog.setView(dialogBinding.root)
        dialog.show()

        dialogBinding.btnEnglish.setOnClickListener {
            changeLanguage("en")
        }
        dialogBinding.txtEnglish.setOnClickListener {
            changeLanguage("en")
        }

        dialogBinding.btnPersian.setOnClickListener {
            changeLanguage("fa")
        }
        dialogBinding.txtPersian.setOnClickListener {
            changeLanguage("fa")
        }
    }

    private fun changeTheme() {
        binding.root.closeDrawer(GravityCompat.START)
        val dialog = AlertDialog.Builder(this).create()
        val dialogBinding = DialogChangeThemeBinding.inflate(layoutInflater)
        dialog.setView(dialogBinding.root)
        dialog.show()


        dialogBinding.icLight.setOnClickListener {
            setAppTheme(true)
            dialog.dismiss()
        }
        dialogBinding.btnLight.setOnClickListener {
            setAppTheme(true)
            dialog.dismiss()
        }
        dialogBinding.txtLight.setOnClickListener {
            setAppTheme(true)
            dialog.dismiss()
        }


        dialogBinding.icDark.setOnClickListener {
            setAppTheme(false)
            dialog.dismiss()
        }
        dialogBinding.btnDark.setOnClickListener {
            setAppTheme(false)
            dialog.dismiss()
        }
        dialogBinding.txtDark.setOnClickListener {
            setAppTheme(false)
            dialog.dismiss()
        }
    }

    private fun setAppTheme(themeMode: Boolean) {
        if (themeMode) sharedPreferences.edit { putString("theme", "light") }
        else sharedPreferences.edit { putString("theme", "dark") }


        val intent = Intent(this, StartActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        startActivity(intent)
    }

    private fun changeLanguage(tag: String) {
        val locale = Locale(tag)        //create locale with language tag
        val configuration = resources.configuration      //create configuration
        configuration.setLocale(locale)     //set locale for configuration
        resources.updateConfiguration(configuration, DisplayMetrics())     //set language
        sharedPreferences.edit().putString("languageTag", tag).apply()


        //restart activity for set language
        val intent = Intent(this, this::class.java)
        startActivity(intent)
    }

    private fun bottomNavigation() {
        binding.navigationBottomMain.navigationBottomMain.setOnItemReselectedListener {}
        binding.navigationBottomMain.navigationBottomMain.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.transactions -> {
                    setFragment(TransactionFragment(tFilter, tMode))
                    fragmentLoaded = 1
                }

                R.id.history -> {
                    setFragment(HistoryFragment(hRadio1, hPacked, hRadio3, hFilter))
                    fragmentLoaded = 2
                }

                R.id.cards -> {
                    setFragment(CardsFragment())
                    fragmentLoaded = 3
                }

                R.id.category -> {
                    setFragment(CategoryFragment(mode = cRadio))
                    fragmentLoaded = 4
                }
            }
            true
        }
    }

    private fun setFragment(fragment: Fragment) {
        val manager = supportFragmentManager.beginTransaction()
        manager.replace(R.id.frameMain, fragment)
        manager.commit()
    }

    override fun onAddItem(text: String) {
        val snack = Snackbar.make(
            binding.root.context, binding.root, text, Snackbar.LENGTH_SHORT
        )
        snack.show()
        snack.setAction(getString(R.string.submit)) { snack.dismiss() }

        when (fragmentLoaded) {
            1 -> setFragment(TransactionFragment(tFilter, tMode))
            2 -> setFragment(HistoryFragment(hRadio1, hPacked, hRadio3, hFilter))
            3 -> setFragment(CardsFragment())
            4 -> setFragment(CategoryFragment(CategoryFragment.companionType, cRadio))
        }
    }

    override fun onUpdateItem(text: String) {}

    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (fragmentLoaded != 1) {
            setFragment(TransactionFragment())
            binding.navigationBottomMain.navigationBottomMain.selectedItemId = R.id.transactions
            fragmentLoaded = 1
        } else exitApp()
    }

    private fun exitApp() {
        val dialog = AlertDialog.Builder(this).create()
        val dialogBinding = DialogQuitAppBinding.inflate(layoutInflater)

        dialog.setView(dialogBinding.root)
        dialog.show()

        dialogBinding.txtBtnNo.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.txtBtnYes.setOnClickListener {
            exitProcess(0)
        }
    }
}
