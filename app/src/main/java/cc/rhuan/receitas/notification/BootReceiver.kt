package cc.rhuan.receitas.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cc.rhuan.receitas.data.local.AppDatabase
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            val db = AppDatabase.getDatabase(context)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val activeSchedules = db.doseScheduleDao().getActiveSchedules().firstOrNull() ?: emptyList()
                    for (schedule in activeSchedules) {
                        val medication = db.medicationDao().getMedicationById(schedule.medicationId)
                        if (medication != null) {
                            var scheduledDateTime = LocalDateTime.of(LocalDate.now(), schedule.scheduledTime)
                            if (scheduledDateTime.isBefore(LocalDateTime.now())) {
                                scheduledDateTime = scheduledDateTime.plusDays(1)
                            }

                            AlarmScheduler.scheduleDoseAlarm(
                                context = context,
                                scheduleId = schedule.id,
                                medicationId = schedule.medicationId,
                                medicationTitle = medication.title,
                                dosage = medication.dosage,
                                scheduledDateTime = scheduledDateTime
                            )
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
