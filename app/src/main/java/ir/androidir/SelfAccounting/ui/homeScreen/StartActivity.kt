package ir.androidir.SelfAccounting.ui.homeScreen

import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.model.HesabchiDataBase
import ir.androidir.SelfAccounting.model.dataClasses.CategoryModel
import ir.androidir.SelfAccounting.utils.RSA_KEY
import ir.androidir.SelfAccounting.utils.SHARED_PREFERENCES_TAG
import ir.cafebazaar.poolakey.Connection
import ir.cafebazaar.poolakey.Payment
import ir.cafebazaar.poolakey.config.PaymentConfiguration
import ir.cafebazaar.poolakey.config.SecurityCheck
import java.util.Locale

class StartActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var paymentConnection: Connection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_TAG, MODE_PRIVATE)



        starterFunctions()
    }

    private fun starterFunctions() {
        setAppTheme()
        setLanguage()
        checkFirstRun()
        cancelNotification()
        checkForSubscription()
        goToMainActivity()
    }
    
    private fun checkForSubscription() {
        val localSecurityCheck = SecurityCheck.Enable(RSA_KEY)
        val paymentConfiguration = PaymentConfiguration(localSecurityCheck)
        val payment = Payment(this, paymentConfiguration)

        paymentConnection = payment.connect {
            connectionSucceed {
                Log.e("connection", "succeed")

                payment.getSubscribedProducts {
                    try {
                        querySucceed { purchasedProducts ->
                            Log.e("querySucceed", purchasedProducts.toString())
                            sharedPreferences.edit().putBoolean(
                                "subscriptionIsAvailable",
                                purchasedProducts.isNotEmpty()
                            ).apply()
                        }
                    } catch (_: Exception) {
                        Log.e("data is null", "data is null")
                    }
                    queryFailed { throwable ->
                        Toast.makeText(
                            this@StartActivity,
                            "برای استفاده از اشتراک برنامه وارد حساب بازار خود شوید",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.e("queryFailed", throwable.message.toString())
                    }
                }
            }

            connectionFailed { throwable ->
                Log.e("connection", "failed : ${throwable.message}")
            }

            disconnected {
                Log.e("connection", "disconnected")
            }
        }
    }

    override fun onDestroy() {
        try {
            paymentConnection.disconnect()
        } catch (_: Exception) {
        }
        super.onDestroy()
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        startActivity(intent)
        finishAfterTransition()
    }

    private fun cancelNotification() {
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1)
    }

    private fun checkFirstRun() {
        val isFirstRun = sharedPreferences.getBoolean("firstRun", true)

        if (isFirstRun) {
            Thread {
                val dao = HesabchiDataBase.getDataBase(this)!!.categoryDao

                val list = listOf(
                    CategoryModel(
                        name = "تفریح",
                        icon = R.drawable.gamingpad,
                        color = R.color.blue3,
                        type = "Expense"
                    ),
                    CategoryModel(
                        name = "بدهی",
                        icon = R.drawable.arrow_up,
                        color = R.color.red,
                        type = "Expense"
                    ),
                    CategoryModel(
                        name = "حقوق",
                        icon = R.drawable.arrow_down,
                        color = R.color.green,
                        type = "Income"
                    ),
                    CategoryModel(
                        name = "خرید",
                        icon = R.drawable.list,
                        color = R.color.yellow2,
                        type = "Expense"
                    ),
                    CategoryModel(
                        name = "غذا",
                        icon = R.drawable.fork,
                        color = R.color.orange2,
                        type = "Expense"
                    ),
                    CategoryModel(
                        name = "پس انداز",
                        icon = R.drawable.card,
                        color = R.color.black,
                        type = "All"
                    ),
                    CategoryModel(
                        name = "خرج های متفرقه",
                        icon = R.drawable.chartpie,
                        color = R.color.purple1,
                        type = "Expense"
                    ),
                    CategoryModel(
                        name = "درآمد های متفرقه",
                        icon = R.drawable.cash,
                        color = R.color.orange2,
                        type = "Income"
                    ),
                    CategoryModel(
                        name = "پاداش",
                        icon = R.drawable.gem,
                        color = R.color.blue4,
                        type = "Income"
                    )
                )
                dao.insertAll(list)
            }.start()

            sharedPreferences.edit().putBoolean("firstRun", false).apply()
        }
    }

    private fun setAppTheme() {
        val themeMode = sharedPreferences.getString("theme", "light")!!


        if (themeMode == "light") {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            sharedPreferences.edit().putString("theme", "light").apply()
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            sharedPreferences.edit().putString("theme", "dark").apply()
        }
    }

    private fun setLanguage() {
        val languageTag = sharedPreferences.getString("languageTag", "fa")!!

        val locale = Locale(languageTag)        //create locale with language tag
        val configuration = resources.configuration      //create configuration
        configuration.setLocale(locale)     //set locale for configuration
        resources.updateConfiguration(configuration, DisplayMetrics())     //set language
        sharedPreferences.edit().putString("languageTag", languageTag).apply()
    }
}
