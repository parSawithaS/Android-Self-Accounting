package ir.androidir.SelfAccounting.ui.alarmPage

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.ui.homeScreen.StartActivity
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AlarmWorker(private val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        return try {
            val time = Calendar.getInstance().time
            Log.v("AlarmWorker", "Worker has been started.The worker id is : $id\nThe time is : $time")

            if (isTimeRight()){
                postNotification()
                startWorker()
            }

            Result.success()
        } catch (exception: Exception) {
            Log.e("AlarmWorker", "An exception happened : ${exception.message!!}")
            Result.failure()
        }
    }

    private fun startWorker() {
        val workManager = WorkManager.getInstance(context)
        val worker = OneTimeWorkRequestBuilder<AlarmWorker>()
            .setInitialDelay(24 , TimeUnit.HOURS)
            .build()
        workManager
            .beginUniqueWork("AlarmWorker", ExistingWorkPolicy.REPLACE, worker)
            .enqueue()
    }

    private fun isTimeRight(): Boolean {
        val sharedPreferences =
            context.getSharedPreferences("sharedData", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("isCheckedAlarm", false)
    }

    private fun postNotification() {
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val intentNotification = Intent(context, StartActivity::class.java)
        intentNotification.flags = Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intentNotification,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "MyNotification")
            .setContentText(context.getString(R.string.reminder_text))
            .setSmallIcon(R.drawable.app_icon)
            .setContentTitle(context.getString(R.string.transaction_reminder))
            .setContentIntent(pendingIntent)
            .build()
        notificationManager.notify(1, notification)

        val time = Calendar.getInstance().time

        Log.v("AlarmWorker", "Notification has been sent in $time")
    }
}