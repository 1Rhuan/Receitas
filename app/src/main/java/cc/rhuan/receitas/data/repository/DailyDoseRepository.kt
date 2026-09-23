package cc.rhuan.receitas.data.repository

import cc.rhuan.receitas.domain.model.DailyDoseItem
import cc.rhuan.receitas.domain.model.DoseStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface DailyDoseRepository {
    fun getDailyDoses(): Flow<List<DailyDoseItem>>
    suspend fun markDoseAsTaken(doseId: String)
    suspend fun updateDoseTime(doseId: String, newTime: String)
    suspend fun markMatchingDoseAsTaken(targetId: String?, notificationId: String, title: String?, message: String?)
    suspend fun snoozeMatchingDose(
        targetId: String?,
        notificationId: String,
        title: String?,
        message: String?,
        newTimeCalculator: (String) -> String,
    )
}

class FakeDailyDoseRepository : DailyDoseRepository {
    companion object {
        private val _doses = MutableStateFlow(
            listOf(
                DailyDoseItem(
                    id = "d1",
                    medicationId = "4",
                    medicationName = "Omeprazol",
                    dosage = "20 mg",
                    time = "07:30",
                    isTaken = true,
                    takenAtTime = "07:30",
                    status = DoseStatus.TAKEN,
                    treatmentId = "t1",
                ),
                DailyDoseItem(
                    id = "d2",
                    medicationId = "1",
                    medicationName = "Amoxicilina",
                    dosage = "500 mg",
                    time = "14:00",
                    isTaken = false,
                    status = DoseStatus.PENDING,
                    treatmentId = "t2",
                ),
                DailyDoseItem(
                    id = "d3",
                    medicationId = "5",
                    medicationName = "Sinvastatina",
                    dosage = "20 mg",
                    time = "22:00",
                    isTaken = false,
                    status = DoseStatus.PENDING,
                    treatmentId = "t3",
                ),
            )
        )
    }

    override fun getDailyDoses(): Flow<List<DailyDoseItem>> = _doses.asStateFlow()

    override suspend fun markDoseAsTaken(doseId: String) {
        _doses.update { list ->
            list.map { dose ->
                if (dose.id == doseId) {
                    dose.copy(isTaken = true, takenAtTime = "Agora", status = DoseStatus.TAKEN)
                } else dose
            }
        }
    }

    override suspend fun updateDoseTime(doseId: String, newTime: String) {
        _doses.update { list ->
            list.map { dose ->
                if (dose.id == doseId) {
                    dose.copy(time = newTime)
                } else dose
            }
        }
    }

    override suspend fun markMatchingDoseAsTaken(
        targetId: String?,
        notificationId: String,
        title: String?,
        message: String?
    ) {
        _doses.update { list ->
            list.map { dose ->
                val isMatchingDose = (targetId != null && (dose.medicationId == targetId || dose.id == targetId)) ||
                    (dose.id == notificationId) ||
                    (message?.contains(dose.medicationName, ignoreCase = true) == true) ||
                    (title?.contains(dose.medicationName, ignoreCase = true) == true)

                if (isMatchingDose && !dose.isTaken) {
                    dose.copy(isTaken = true, takenAtTime = "Agora", status = DoseStatus.TAKEN)
                } else dose
            }
        }
    }

    override suspend fun snoozeMatchingDose(
        targetId: String?,
        notificationId: String,
        title: String?,
        message: String?,
        newTimeCalculator: (String) -> String
    ) {
        _doses.update { list ->
            list.map { dose ->
                val isMatchingDose = (targetId != null && (dose.medicationId == targetId || dose.id == targetId)) ||
                    (dose.id == notificationId) ||
                    (message?.contains(dose.medicationName, ignoreCase = true) == true) ||
                    (title?.contains(dose.medicationName, ignoreCase = true) == true)

                if (isMatchingDose && !dose.isTaken) {
                    dose.copy(time = newTimeCalculator(dose.time), status = DoseStatus.SNOOZED)
                } else dose
            }
        }
    }
}
