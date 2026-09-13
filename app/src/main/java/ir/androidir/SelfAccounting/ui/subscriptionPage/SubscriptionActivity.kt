package ir.androidir.SelfAccounting.ui.subscriptionPage

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.ActivityPurchaseSubscriptionBinding
import ir.androidir.SelfAccounting.model.dataClasses.enumClasses.Plans
import ir.androidir.SelfAccounting.utils.PRODUCTION_ID_30
import ir.androidir.SelfAccounting.utils.PRODUCTION_ID_365
import ir.androidir.SelfAccounting.utils.PRODUCTION_ID_90
import ir.androidir.SelfAccounting.utils.RSA_KEY
import ir.androidir.SelfAccounting.utils.SHARED_PREFERENCES_TAG
import ir.cafebazaar.poolakey.Connection
import ir.cafebazaar.poolakey.Payment
import ir.cafebazaar.poolakey.config.PaymentConfiguration
import ir.cafebazaar.poolakey.config.SecurityCheck
import ir.cafebazaar.poolakey.request.PurchaseRequest
import kotlin.properties.Delegates

class SubscriptionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPurchaseSubscriptionBinding
    private lateinit var paymentConnection: Connection
    private lateinit var payment: Payment
    private var isConnectedToBazar: Boolean by Delegates.observable(false) { _, _, _ ->
        getCurrentPlan()
    }
    private var currentPlan: Plans by Delegates.observable(Plans.Free) { _, _, newValue ->
        if (newValue != Plans.Free) {
            setRadioGroupUI()
            buttonPurchase()
        }
    }
    private lateinit var sharedPreferences: SharedPreferences
    private var theme = "light"
    private var selectedPlan: Plans by Delegates.observable(Plans.Free) { _, _, _ ->
        buttonPurchase()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPurchaseSubscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences =
            getSharedPreferences(SHARED_PREFERENCES_TAG, MODE_PRIVATE)
        theme = sharedPreferences.getString("theme", "light") ?: "light"


        checkItem(binding.cardFree, binding.txtPriceFree, binding.txtNameNormal)
        binding.txtUsingPlan.text = getString(R.string.using_free_subscription)


        connectToBazar()
        setActionBar()
        setRadioGroupUI()
        buttonPurchase()
    }

    private fun setActionBar() {
        binding.toolBar.toolbar.title = getString(R.string.purchase_subscription)

        setSupportActionBar(binding.toolBar.toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            onBackPressedDispatcher.onBackPressed()

        return true
    }

    @SuppressLint("SetTextI18n")
    private fun getCurrentPlan() {
        if (isConnectedToBazar) {
            payment.getSubscribedProducts {
                querySucceed { purchasedProducts ->
                    try {
                        currentPlan =
                            when (purchasedProducts[purchasedProducts.lastIndex].productId) {
                                PRODUCTION_ID_30 -> Plans.OneMonth
                                PRODUCTION_ID_90 -> Plans.ThreeMonth
                                PRODUCTION_ID_365 -> Plans.OneYear
                                else -> Plans.Free
                            }

                        val usingPlan: String
                        when (currentPlan) {
                            Plans.OneMonth -> {
                                usingPlan = getString(R.string.month1)
                                checkItem(
                                    binding.card1Month,
                                    binding.txtPrice1,
                                    binding.txtPlanName1
                                )
                            }

                            Plans.ThreeMonth -> {
                                usingPlan = getString(R.string.month3)
                                checkItem(
                                    binding.card3Month,
                                    binding.txtPrice3,
                                    binding.txtPlanName3
                                )
                            }

                            Plans.OneYear -> {
                                usingPlan = getString(R.string.year1)
                                checkItem(
                                    binding.card12Month,
                                    binding.txtPrice12,
                                    binding.txtPlanName12
                                )
                            }

                            Plans.Free -> {
                                usingPlan = getString(R.string.free)
                                checkItem(
                                    binding.cardFree,
                                    binding.txtPriceFree,
                                    binding.txtNameNormal
                                )
                            }
                        }
                        if (sharedPreferences.getString("language", "fa") == "fa")
                            binding.txtUsingPlan.text = "شما از اشتراک $usingPlan استفاده میکنید."
                        else
                            binding.txtUsingPlan.text = "You're using the $usingPlan plan"

                    } catch (_: Exception) {
                        Log.e("data is null", "data is null")

                        currentPlan = Plans.Free
                        buttonPurchase()
                        unCheckItems()
                        checkItem(binding.cardFree, binding.txtPriceFree, binding.txtNameNormal)
                        binding.txtUsingPlan.text = getString(R.string.using_free_subscription)
                    }
                }

                queryFailed { throwable ->
                    Log.e("purchase failed", throwable.message.toString())
                }
            }
        } else
            Log.e("connectionProblem", "Problem in getting subscribed products")
    }

    private fun buttonPurchase() {
        if (selectedPlan == Plans.Free)
            binding.btnPurchase.visibility = View.GONE
        else {
            binding.root.smoothScrollTo(binding.root.width,binding.root.height)
            binding.btnPurchase.visibility = View.VISIBLE
        }


        binding.btnPurchase.setOnClickListener {
            val productionID = when (selectedPlan) {
                Plans.OneMonth -> PRODUCTION_ID_30
                Plans.ThreeMonth -> PRODUCTION_ID_90
                Plans.OneYear -> PRODUCTION_ID_365
                else -> ""
            }

            purchaseSubscription(productionID)
        }
    }

    private fun purchaseSubscription(productID: String) {
        if (isConnectedToBazar) {
            val purchaseRequest = PurchaseRequest(productID)

            payment.subscribeProduct(activityResultRegistry, purchaseRequest) {
                failedToBeginFlow { throwable ->
                    Toast.makeText(
                        this@SubscriptionActivity,
                        getString(R.string.try_again),
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("subscribeFlowFailed", throwable.message.toString())
                }

                purchaseSucceed { purchaseEntity ->
                    Toast.makeText(
                        this@SubscriptionActivity,
                        getString(R.string.purchase_success),
                        Toast.LENGTH_LONG
                    )
                        .show()

                    Log.e("purchaseSucceed", purchaseEntity.productId)
                    sharedPreferences.edit().putBoolean("subscriptionIsAvailable", true).apply()
                    onBackPressedDispatcher.onBackPressed()
                }

                purchaseFailed { throwable ->
                    Toast.makeText(
                        this@SubscriptionActivity,
                        getString(R.string.purchase_failed_try_again),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("subscribe", "failed : ${throwable.message}")
                }
            }
        }
    }

    private fun setRadioGroupUI() {
        if (currentPlan != Plans.Free) {
            binding.horizontalScrollView.visibility = View.GONE
            binding.btnPurchase.visibility = View.GONE
            binding.textView67.visibility = View.GONE
            binding.view9.visibility = View.GONE
        }


        binding.cardFree.setOnClickListener {
            selectedPlan = Plans.Free
            unCheckItems()
            checkItem(binding.cardFree, binding.txtPriceFree, binding.txtNameNormal)
        }
        binding.card1Month.setOnClickListener {
            selectedPlan = Plans.OneMonth
            unCheckItems()
            checkItem(binding.card1Month, binding.txtPrice1, binding.txtPlanName1)
        }
        binding.card3Month.setOnClickListener {
            selectedPlan = Plans.ThreeMonth
            unCheckItems()
            checkItem(binding.card3Month, binding.txtPrice3, binding.txtPlanName3)
        }
        binding.card12Month.setOnClickListener {
            selectedPlan = Plans.OneYear
            unCheckItems()
            checkItem(binding.card12Month, binding.txtPrice12, binding.txtPlanName12)
        }
    }

    private fun checkItem(cardFree: CardView, txt1: TextView, txt2: TextView) {
        txt1.setTextColor(ContextCompat.getColor(this, R.color.white))
        txt2.setTextColor(ContextCompat.getColor(this, R.color.white))

        if (theme == "light")
            cardFree.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
        else
            cardFree.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
    }

    private fun unCheckItems() {
        val mColor = if (theme == "light")
            R.color.white
        else
            R.color.gray_dark

        binding.cardFree.setCardBackgroundColor(ContextCompat.getColor(this, mColor))
        binding.card1Month.setCardBackgroundColor(ContextCompat.getColor(this, mColor))
        binding.card3Month.setCardBackgroundColor(ContextCompat.getColor(this, mColor))
        binding.card12Month.setCardBackgroundColor(ContextCompat.getColor(this, mColor))

        val txtNameNormal = binding.txtNameNormal
        val txtPriceFree = binding.txtPriceFree
        val txtName1 = binding.txtPlanName1
        val txtPrice1 = binding.txtPrice1
        val txtName3 = binding.txtPlanName3
        val txtPrice3 = binding.txtPrice3
        val txtName12 = binding.txtPlanName12
        val txtPrice12 = binding.txtPrice12

        val color = if (theme == "light") {
            R.color.black
        } else {
            R.color.white
        }

        txtNameNormal.setTextColor(ContextCompat.getColor(this, color))
        txtPriceFree.setTextColor(ContextCompat.getColor(this, color))
        txtName1.setTextColor(ContextCompat.getColor(this, color))
        txtPrice1.setTextColor(ContextCompat.getColor(this, color))
        txtName3.setTextColor(ContextCompat.getColor(this, color))
        txtPrice3.setTextColor(ContextCompat.getColor(this, color))
        txtName12.setTextColor(ContextCompat.getColor(this, color))
        txtPrice12.setTextColor(ContextCompat.getColor(this, color))
    }

    private fun connectToBazar() {
        val localSecurityCheck = SecurityCheck.Enable(RSA_KEY)
        val paymentConfiguration = PaymentConfiguration(localSecurityCheck)
        payment = Payment(this, paymentConfiguration)

        paymentConnection = payment.connect {
            connectionSucceed {
                isConnectedToBazar = true
            }

            connectionFailed {
                isConnectedToBazar = false
                Toast.makeText(
                    this@SubscriptionActivity,
                    getString(R.string.problem_with_bazaar),
                    Toast.LENGTH_LONG
                ).show()
            }

            disconnected {
                isConnectedToBazar = false
            }
        }
    }

    override fun onDestroy() {
        paymentConnection.disconnect()
        super.onDestroy()
    }
}