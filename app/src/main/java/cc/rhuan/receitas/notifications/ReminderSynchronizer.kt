package cc.rhuan.receitas.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.withTransaction
import cc.rhuan.receitas.MainActivity
import cc.rhuan.receitas.R
import cc.rhuan.receitas.data.local.AppDatabase
import cc.rhuan.receitas.data.local.entity.NotificationEntity
import cc.rhuan.receitas.domain.usecase.EvaluateRemindersUseCase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.first
import java.time.LocalDate

object ReminderSynchronizer {
    val mutex = Mutex()
    const val EXTRA_NOTIFICATION_ID = "notification_id"
    private const val CHANNEL = "prescription_pickup_reminders"

    @SuppressLint("MissingPermission")
    suspend fun sync(context: Context) = mutex.withLock {
        val db = AppDatabase.getDatabase(context)
        val reminders = db.withTransaction {
            EvaluateRemindersUseCase()(db.prescriptionDao().getAllPrescriptions().first(), db.medicationDao().getAllMedications().first(), LocalDate.now())
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL, "Validade, renovação e retirada", NotificationManager.IMPORTANCE_DEFAULT))
        val delivered = context.getSharedPreferences("delivered_reminders", Context.MODE_PRIVATE)
        val activeIds = reminders.map { it.id }.toSet()
        // Cancel obsolete system alerts after renewal, pickup, deletion or a phase change.
        manager.activeNotifications.filter { it.tag?.startsWith("reminder:") == true && it.tag?.removePrefix("reminder:") !in activeIds }
            .forEach { manager.cancel(it.tag, it.id) }
        val existing = db.notificationDao().getAllNotifications().first()
        existing.filter { (it.id.startsWith("prescription:") || it.id.startsWith("pickup:")) && it.id !in activeIds && it.actionTaken == null }
            .forEach { db.notificationDao().updateNotification(it.copy(isRead = true, actionTaken = "Lembrete encerrado")) }
        for (reminder in reminders) {
            if (delivered.getBoolean("dismissed:" + reminder.id, false)) continue
            db.notificationDao().getNotificationById(reminder.id)?.let { stored ->
                if (stored.type != reminder.type || stored.title != reminder.title || stored.message != reminder.message) db.notificationDao().updateNotification(stored.copy(type = reminder.type, title = reminder.title, message = reminder.message))
            }
            db.notificationDao().insertNotification(NotificationEntity(
                id = reminder.id, targetId = reminder.targetId, title = reminder.title,
                message = reminder.message, timestamp = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")), type = reminder.type,
            ))
            val stored = db.notificationDao().getNotificationById(reminder.id) ?: continue
            if (stored.isRead || delivered.getBoolean(reminder.id, false) || !NotificationManagerCompat.from(context).areNotificationsEnabled() || manager.getNotificationChannel(CHANNEL).importance == NotificationManager.IMPORTANCE_NONE) continue
            val intent = PendingIntent.getActivity(context, 0,
                Intent(context, MainActivity::class.java)
                    .setData(android.net.Uri.parse("receitas://notifications/" + android.net.Uri.encode(reminder.id)))
                    .putExtra(EXTRA_NOTIFICATION_ID, reminder.id).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val notification = NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_notification_reminder)
                .setContentTitle(reminder.title).setContentText(reminder.message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(reminder.message))
                .setContentIntent(intent).setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).build()
            try {
                manager.notify("reminder:" + reminder.id, 0, notification)
                delivered.edit().putBoolean(reminder.id, true).commit()
            } catch (_: SecurityException) {
                // Permission may be revoked between checking and posting. Retry on the next check.
            }
        }
    }
}
