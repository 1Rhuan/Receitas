package cc.rhuan.receitas.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cc.rhuan.receitas.data.local.AppDatabase
import cc.rhuan.receitas.data.local.entity.DoseLogEntity
import cc.rhuan.receitas.domain.model.DoseStatus
import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", -1)
        val scheduleId = intent.getStringExtra("SCHEDULE_ID") ?: ""
        val medicationId = intent.getStringExtra("MEDICATION_ID") ?: ""
        val scheduledDateTimeStr = intent.getStringExtra("SCHEDULED_DATE_TIME") ?: ""

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationId != -1) {
            notificationManager.cancel(notificationId)
        }

        val db = AppDatabase.getDatabase(context)
        val scheduledDateTime = runCatching { LocalDateTime.parse(scheduledDateTimeStr) }.getOrDefault(LocalDateTime.now())

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    ACTION_TAKE -> {
                        val log = DoseLogEntity(
                            id = UUID.randomUUID().toString(),
                            doseScheduleId = scheduleId,
                            medicationId = medicationId,
                            scheduledDateTime = scheduledDateTime,
                            status = DoseStatus.TAKEN,
                            confirmedAt = LocalDateTime.now()
                        )
                        db.doseLogDao().insertDoseLog(log)

                        // Decrement stock
                        if (medicationId.isNotBlank()) {
                            val med = db.medicationDao().getMedicationById(medicationId)
                            if (med != null && med.currentStock != null && med.currentStock > 0) {
                                db.medicationDao().updateStock(medicationId, med.currentStock - 1)
                            }
                        }
                    }

                    ACTION_SKIP -> {
                        val log = DoseLogEntity(
                            id = UUID.randomUUID().toString(),
                            doseScheduleId = scheduleId,
                            medicationId = medicationId,
                            scheduledDateTime = scheduledDateTime,
                            status = DoseStatus.SKIPPED,
                            confirmedAt = LocalDateTime.now()
                        )
                        db.doseLogDao().insertDoseLog(log)
                    }

                    ACTION_SNOOZE -> {
                        val title = intent.getStringExtra("MEDICATION_TITLE") ?: "Medicamento"
                        val dosage = intent.getStringExtra("DOSAGE") ?: ""
                        AlarmScheduler.scheduleSnoozeAlarm(
                            context = context,
                            scheduleId = scheduleId,
                            medicationId = medicationId,
                            medicationTitle = title,
                            dosage = dosage
                        )
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_TAKE = "cc.rhuan.receitas.ACTION_TAKE"
        const val ACTION_SKIP = "cc.rhuan.receitas.ACTION_SKIP"
        const val ACTION_SNOOZE = "cc.rhuan.receitas.ACTION_SNOOZE"
    }
}
