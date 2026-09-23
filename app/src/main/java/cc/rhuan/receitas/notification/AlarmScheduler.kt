package cc.rhuan.receitas.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import java.time.LocalDateTime
import java.time.ZoneId

object AlarmScheduler {

    fun scheduleDoseAlarm(
        context: Context,
        scheduleId: String,
        medicationId: String,
        medicationTitle: String,
        dosage: String,
        scheduledDateTime: LocalDateTime
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Cannot schedule exact alarms, fallback or open settings if needed
            }
        }

        val intent = Intent(context, DoseAlarmReceiver::class.java).apply {
            putExtra("SCHEDULE_ID", scheduleId)
            putExtra("MEDICATION_ID", medicationId)
            putExtra("MEDICATION_TITLE", medicationTitle)
            putExtra("DOSAGE", dosage)
            putExtra("SCHEDULED_DATE_TIME", scheduledDateTime.toString())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = scheduledDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val nowMillis = System.currentTimeMillis()

        if (triggerAtMillis > nowMillis) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        }
    }

    fun scheduleSnoozeAlarm(
        context: Context,
        scheduleId: String,
        medicationId: String,
        medicationTitle: String,
        dosage: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, DoseAlarmReceiver::class.java).apply {
            putExtra("SCHEDULE_ID", scheduleId)
            putExtra("MEDICATION_ID", medicationId)
            putExtra("MEDICATION_TITLE", medicationTitle)
            putExtra("DOSAGE", dosage)
            putExtra("SCHEDULED_DATE_TIME", LocalDateTime.now().plusMinutes(15).toString())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (scheduleId + "_snooze").hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = System.currentTimeMillis() + (15 * 60 * 1000) // 15 minutes

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancelAlarm(context: Context, scheduleId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DoseAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
