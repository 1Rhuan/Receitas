package cc.rhuan.receitas.data.repository

import cc.rhuan.receitas.data.local.dao.DoseLogDao
import cc.rhuan.receitas.data.local.dao.DoseScheduleDao
import cc.rhuan.receitas.data.local.dao.MedicationDao
import cc.rhuan.receitas.data.local.entity.DoseLogEntity
import cc.rhuan.receitas.domain.model.DailyDoseItem
import cc.rhuan.receitas.domain.model.DoseStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull

class RoomDailyDoseRepository @Inject constructor(
    private val doseScheduleDao: DoseScheduleDao,
    private val doseLogDao: DoseLogDao,
    private val medicationDao: MedicationDao
) : DailyDoseRepository {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun getDailyDoses(): Flow<List<DailyDoseItem>> {
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay()
        val endOfDay = today.atTime(23, 59, 59)

        return combine(
            doseScheduleDao.getActiveSchedules(),
            doseLogDao.getDoseLogsForDay(startOfDay, endOfDay),
            medicationDao.getAllMedications()
        ) { schedules, logs, medications ->
            val medMap = medications.associateBy { it.id }
            val logMap = logs.associateBy { it.doseScheduleId }

            schedules.map { schedule ->
                val med = medMap[schedule.medicationId]
                val log = logMap[schedule.id]

                val isTaken = log?.status == DoseStatus.TAKEN
                val status = log?.status ?: DoseStatus.PENDING

                DailyDoseItem(
                    id = schedule.id,
                    medicationId = schedule.medicationId,
                    medicationName = med?.title ?: "Medicamento",
                    dosage = med?.dosage ?: "",
                    time = schedule.scheduledTime.format(timeFormatter),
                    isTaken = isTaken,
                    takenAtTime = log?.confirmedAt?.format(timeFormatter),
                    status = status,
                    treatmentId = null
                )
            }
        }
    }

    override suspend fun markDoseAsTaken(doseId: String) {
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay()
        val endOfDay = today.atTime(23, 59, 59)

        val activeSchedules = doseScheduleDao.getActiveSchedules().firstOrNull() ?: emptyList()
        val targetSchedule = activeSchedules.find { it.id == doseId || it.medicationId == doseId }

        val scheduleId = targetSchedule?.id ?: doseId
        val medicationId = targetSchedule?.medicationId ?: doseId

        val todayLogs = doseLogDao.getDoseLogsForDay(startOfDay, endOfDay).firstOrNull() ?: emptyList()
        val existingLog = todayLogs.find { it.doseScheduleId == scheduleId }

        val now = LocalDateTime.now()
        if (existingLog != null) {
            doseLogDao.updateDoseLog(
                existingLog.copy(
                    status = DoseStatus.TAKEN,
                    confirmedAt = now
                )
            )
        } else {
            val newLog = DoseLogEntity(
                id = UUID.randomUUID().toString(),
                doseScheduleId = scheduleId,
                medicationId = medicationId,
                scheduledDateTime = targetSchedule?.scheduledTime?.let { LocalDateTime.of(today, it) } ?: now,
                status = DoseStatus.TAKEN,
                confirmedAt = now
            )
            doseLogDao.insertDoseLog(newLog)
        }

        // Decrement stock
        if (medicationId.isNotBlank()) {
            val med = medicationDao.getMedicationById(medicationId)
            if (med != null && med.currentStock != null && med.currentStock > 0) {
                medicationDao.updateStock(medicationId, med.currentStock - 1)
            }
        }
    }

    override suspend fun updateDoseTime(doseId: String, newTime: String) {
        val parsedTime = runCatching { LocalTime.parse(newTime, timeFormatter) }
            .getOrElse { runCatching { LocalTime.parse(newTime) }.getOrNull() } ?: return

        val activeSchedules = doseScheduleDao.getActiveSchedules().firstOrNull() ?: emptyList()
        val targetSchedule = activeSchedules.find { it.id == doseId } ?: return

        doseScheduleDao.updateSchedule(targetSchedule.copy(scheduledTime = parsedTime))
    }

    override suspend fun markMatchingDoseAsTaken(
        targetId: String?,
        notificationId: String,
        title: String?,
        message: String?
    ) {
        val activeSchedules = doseScheduleDao.getActiveSchedules().firstOrNull() ?: emptyList()
        val medications = medicationDao.getAllMedications().firstOrNull() ?: emptyList()
        val medMap = medications.associateBy { it.id }

        val matchingSchedule = activeSchedules.find { schedule ->
            val med = medMap[schedule.medicationId]
            val medTitle = med?.title ?: ""

            (targetId != null && (schedule.id == targetId || schedule.medicationId == targetId)) ||
                (schedule.id == notificationId) ||
                (message?.contains(medTitle, ignoreCase = true) == true && medTitle.isNotBlank()) ||
                (title?.contains(medTitle, ignoreCase = true) == true && medTitle.isNotBlank())
        }

        if (matchingSchedule != null) {
            markDoseAsTaken(matchingSchedule.id)
        }
    }

    override suspend fun snoozeMatchingDose(
        targetId: String?,
        notificationId: String,
        title: String?,
        message: String?,
        newTimeCalculator: (String) -> String
    ) {
        val activeSchedules = doseScheduleDao.getActiveSchedules().firstOrNull() ?: emptyList()
        val medications = medicationDao.getAllMedications().firstOrNull() ?: emptyList()
        val medMap = medications.associateBy { it.id }

        val matchingSchedule = activeSchedules.find { schedule ->
            val med = medMap[schedule.medicationId]
            val medTitle = med?.title ?: ""

            (targetId != null && (schedule.id == targetId || schedule.medicationId == targetId)) ||
                (schedule.id == notificationId) ||
                (message?.contains(medTitle, ignoreCase = true) == true && medTitle.isNotBlank()) ||
                (title?.contains(medTitle, ignoreCase = true) == true && medTitle.isNotBlank())
        }

        if (matchingSchedule != null) {
            val currentTimeStr = matchingSchedule.scheduledTime.format(timeFormatter)
            val newTimeStr = newTimeCalculator(currentTimeStr)
            updateDoseTime(matchingSchedule.id, newTimeStr)
        }
    }
}
