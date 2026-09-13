package ir.androidir.SelfAccounting.ui.alarmPage

import android.Manifest
import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.ActivityAlarmBinding
import ir.androidir.SelfAccounting.utils.CountActions
import ir.androidir.SelfAccounting.utils.SHARED_PREFERENCES_TAG
import java.util.Calendar
import java.util.concurrent.TimeUnit

@SuppressLint("SetTextI18n")
class AlarmActivity : AppCompatActivity(), CountActions {
    private lateinit var binding: ActivityAlarmBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_TAG, MODE_PRIVATE)
        addAction(this)


        timeSwitch()
        setActionBar()
        onTimeClick()
        checkForAlarm()
    }

    private fun checkForAlarm() {
        val alarmTime = sharedPreferences.getString("alarmTime", "")!!
        val isChecked = sharedPreferences.getBoolean("isCheckedAlarm", false)

        if (alarmTime.isNotEmpty()) {
            binding.txtTime.text = alarmTime
            binding.txtTime.visibility = View.VISIBLE
            binding.txtDaily.visibility = View.VISIBLE
            binding.switchTurnOnOff.isChecked = isChecked

            if (isChecked) {
                binding.txtTurnOnOff.text = getString(R.string.turn_on)
                checkedSwitch()
            } else {
                binding.txtTurnOnOff.text = getString(R.string.turn_off)
                notCheckedSwitch()
            }
        }
    }

    private fun onTimeClick() {
        binding.txtTime.setOnClickListener {
            val isSwitchChecked = binding.switchTurnOnOff.isChecked
            val timeTxt = binding.txtTime.text.toString()

            if (isSwitchChecked && timeTxt.isNotEmpty()) {
                val hour = timeTxt.split(":")[0]
                val minute = timeTxt.split(":")[1]
                openPickTimeDialog(minute.toInt(), hour.toInt())
            }
        }
    }

    private fun timeSwitch() {
        binding.switchTurnOnOff.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked)
                notCheckedSwitch()
            else
                checkedSwitch()
        }
    }

    private fun checkedSwitch() {
        if (checkNotificationPermissions()) {
            //views =>
            val txtTurnOffOn = binding.txtTurnOnOff
            val txtTime = binding.txtTime
            val sharedPreferences = getSharedPreferences("sharedData", MODE_PRIVATE)
            val theme = sharedPreferences.getString("theme", "light")!!
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            //if time hasn't been checked,show dialog to pick time
            if (txtTime.text.isEmpty())
                openPickTimeDialog(minute, hour)


            //set text colors =>
            if (theme == "light") {
                txtTime.setTextColor(
                    ContextCompat.getColor(this, R.color.black)
                )
                txtTurnOffOn.setTextColor(
                    ContextCompat.getColor(this, R.color.black)
                )
            } else {
                txtTime.setTextColor(
                    ContextCompat.getColor(this, R.color.white)
                )
                txtTurnOffOn.setTextColor(
                    ContextCompat.getColor(this, R.color.white)
                )
            }
        } else
            grantNotificationPermission()
    }

    private fun openPickTimeDialog(min: Int, hour: Int) {
        val txtTurnOffOn = binding.txtTurnOnOff
        val txtTime = binding.txtTime
        val txtDaily = binding.txtDaily

        val timeDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                txtTime.visibility = View.VISIBLE
                txtDaily.visibility = View.VISIBLE
                txtTime.text = "$hourOfDay:$minute"
                txtTurnOffOn.text = getString(R.string.turn_on)
            },
            hour,
            min,
            true
        )
        timeDialog.show()
    }

    private fun notCheckedSwitch() {
        val txtTurnOffOn = binding.txtTurnOnOff
        val txtTime = binding.txtTime

        //change text colors =>
        txtTurnOffOn.text = getString(R.string.turn_off)
        txtTime.setTextColor(
            ContextCompat.getColor(this, R.color.gray)
        )
        txtTurnOffOn.setTextColor(
            ContextCompat.getColor(this, R.color.gray)
        )
    }

    private fun setActionBar() {
        binding.include4.toolbar.title = getString(R.string.alarm)

        setSupportActionBar(binding.include4.toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            onBackPressedDispatcher.onBackPressed()

        return true
    }

    override fun onDestroy() {
        val isChecked = binding.switchTurnOnOff.isChecked

        sharedPreferences.edit().putString("alarmTime", binding.txtTime.text.toString()).apply()
        sharedPreferences.edit().putBoolean("isCheckedAlarm", isChecked)
            .apply()
        if (isChecked)
            startWorker()
        super.onDestroy()
    }

    private fun startWorker() {
        val workManager = WorkManager.getInstance(this)
        workManager.cancelAllWork()
        val worker = OneTimeWorkRequestBuilder<ir.androidir.SelfAccounting.ui.alarmPage.AlarmWorker>()
            .setInitialDelay(getInterval(), TimeUnit.SECONDS)
            .addTag("worker")
            .build()
        workManager
            .beginUniqueWork("AlarmWorker", ExistingWorkPolicy.REPLACE, worker)
            .enqueue()
    }

    private fun getInterval(): Long {
        val chosenTime = binding.txtTime.text.toString()

        val chosenMinute = chosenTime.split(":")[1].toInt()
        var chosenHour = chosenTime.split(":")[0].toInt()
        val currentMinute = Calendar.getInstance().get(Calendar.MINUTE)
        var currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        if (chosenHour == 0)
            chosenHour = 24
        if (currentHour == 0)
            currentHour = 24

        val chosenHourToMinute = chosenHour * 60
        val currentHourToMinute = currentHour * 60

        val totalMinutesOfChosenTime = chosenHourToMinute + chosenMinute
        val totalMinutesOfCurrentTime = currentHourToMinute + currentMinute

        return if (totalMinutesOfChosenTime >= totalMinutesOfCurrentTime)
            (totalMinutesOfChosenTime - totalMinutesOfCurrentTime).toLong() * 60
        else
            ((totalMinutesOfCurrentTime - totalMinutesOfChosenTime) + 1440).toLong() * 60

    }

    private fun grantNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS
                ),
                1
            )
        }
    }

    private fun checkNotificationPermissions(): Boolean {
        val notificationGranted =  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else
            true

        return notificationGranted
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        // Permission granted,continue process
            checkedSwitch()
        else
        // Permission denied,close the app
            notCheckedSwitch()
    }
}
