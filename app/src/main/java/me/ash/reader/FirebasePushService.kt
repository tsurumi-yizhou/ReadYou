package me.ash.reader

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.messaging
import me.ash.reader.infrastructure.android.MainActivity

class FirebasePushService: FirebaseMessagingService() {
    private val manager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }
    private val channel by lazy {
        NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
    }

    private fun notify(title: String?, body: String?, link: String?) {
        val intent = link?.let {
            Intent(this, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse(link)
                addCategory(Intent.CATEGORY_BROWSABLE)
            }
        }
        val pendingIntent = intent?.let {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }
        val notification = Notification.Builder(this, CHANNEL_ID).apply {
            setSmallIcon(R.mipmap.ic_launcher)
            setContentTitle(title)
            setContentText(body)
            pendingIntent?.let {
                setContentIntent(pendingIntent)
            }
            setAutoCancel(true)
        }
        manager.notify(System.currentTimeMillis().toInt(), notification.build())
    }

    override fun onCreate() {
        super.onCreate()
        manager.createNotificationChannel(channel)
        Firebase.messaging.subscribeToTopic("notifications")
            .addOnSuccessListener {
                notify("onCreate", "subscribed to notifications", null)
            }
            .addOnFailureListener {
                notify("onCreate", it.message, null)
            }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        notify(message.notification?.title, message.notification?.body, message.notification?.link?.toString())
    }

    companion object {
        const val CHANNEL_ID = "NotificationChannel"
        const val CHANNEL_NAME = "NotificationChannel"
    }
}