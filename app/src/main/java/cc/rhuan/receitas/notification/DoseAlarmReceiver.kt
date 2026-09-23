package cc.rhuan.receitas.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import cc.rhuan.receitas.MainActivity
import cc.rhuan.receitas.R

class DoseAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getStringExtra("SCHEDULE_ID") ?: return
        val medicationId = intent.getStringExtra("MEDICATION_ID") ?: ""
        val medicationTitle = intent.getStringExtra("MEDICATION_TITLE") ?: "Medicamento"
        val dosage = intent.getStringExtra("DOSAGE") ?: ""
        val scheduledDateTime = intent.getStringExtra("SCHEDULED_DATE_TIME") ?: ""

        createNotificationChannel(context)

        val notificationId = scheduleId.hashCode()

        // Open App Intent
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Tomei
        val takeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_TAKE
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("SCHEDULE_ID", scheduleId)
            putExtra("MEDICATION_ID", medicationId)
            putExtra("SCHEDULED_DATE_TIME", scheduledDateTime)
        }
        val takePendingIntent = PendingIntent.getBroadcast(
            context,
            (notificationId.toString() + "_take").hashCode(),
            takeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Pular
        val skipIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SKIP
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("SCHEDULE_ID", scheduleId)
            putExtra("MEDICATION_ID", medicationId)
            putExtra("SCHEDULED_DATE_TIME", scheduledDateTime)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            (notificationId.toString() + "_skip").hashCode(),
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Adiar 15 min
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("SCHEDULE_ID", scheduleId)
            putExtra("MEDICATION_ID", medicationId)
            putExtra("MEDICATION_TITLE", medicationTitle)
            putExtra("DOSAGE", dosage)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (notificationId.toString() + "_snooze").hashCode(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bold_pills)
            .setContentTitle("Hora da dose: $medicationTitle")
            .setContentText(if (dosage.isNotBlank()) "Dosagem: $dosage" else "Hora de tomar seu medicamento.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(0, "Tomei", takePendingIntent)
            .addAction(0, "Pular", skipPendingIntent)
            .addAction(0, "Adiar 15 min", snoozePendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Lembretes de Medicamentos"
            val descriptionText = "Notificações para tomar seus medicamentos no horário exato"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "dose_reminders"
    }
}
