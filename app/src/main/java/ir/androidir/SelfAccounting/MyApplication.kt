package ir.androidir.SelfAccounting

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel =
				NotificationChannel("MyNotification", "alarm", NotificationManager.IMPORTANCE_HIGH)

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}