package pl.marta.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import pl.marta.R
import pl.marta.view.activities.MainActivity

class FCMService : FirebaseMessagingService() {

    companion object {
        private const val mNewOrderNotificationAction = "NEW_ORDER"
        private const val NOTIFICATION_CHANNEL_ID = "MartaNewOrder"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (!remoteMessage.data.isNullOrEmpty() && remoteMessage.data["action"] == mNewOrderNotificationAction)
            sendNotification(remoteMessage.data["martaName"]!!)
    }

    private fun sendNotification(martaName: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent, PendingIntent.FLAG_ONE_SHOT
        )
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.new_order_channel_name),
                NotificationManager.IMPORTANCE_MAX
            )
            notificationChannel.description = getString(R.string.new_order_channel_desc)
            notificationChannel.enableLights(true)
            notificationChannel.vibrationPattern = longArrayOf(500, 1000, 500)
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setColor(ContextCompat.getColor(this, R.color.colorAccent))
            .setLargeIcon(
                BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_round)
            )
            .setContentTitle(getString(R.string.new_order_notification_title))
            .setContentText(getString(R.string.new_order_notification_message, martaName))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent) as NotificationCompat.Builder
        notificationManager.notify(0, notificationBuilder.build())
    }
}