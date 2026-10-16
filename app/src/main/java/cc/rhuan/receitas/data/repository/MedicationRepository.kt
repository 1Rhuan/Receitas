package cc.rhuan.receitas.data.repository

import cc.rhuan.receitas.text.TextMessage

import cc.rhuan.receitas.text.TextResolver

import cc.rhuan.receitas.R

import androidx.room.withTransaction
import cc.rhuan.receitas.data.local.AppDatabase
import cc.rhuan.receitas.data.local.entity.*
import cc.rhuan.receitas.domain.model.*
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.*

interface MedicationRepository {
    fun getMedications(): Flow<List<MedicationItem>>
    suspend fun getMedicationById(id: String): MedicationItem?
    suspend fun registerPickup(id: String, date: LocalDate = LocalDate.now(), confirmedEarly: Boolean = false, expectedCycle: LocalDate? = null): OperationResult
    suspend fun addMedication(medication: MedicationItem)
    suspend fun updateMedication(medication: MedicationItem)
    suspend fun correctPickup(eventId: String, date: LocalDate): OperationResult
    suspend fun deletePickup(eventId: String): OperationResult = OperationResult.NotFound
    suspend fun archiveTracking(id: String, archived: Boolean): OperationResult = OperationResult.NotFound
    suspend fun deleteMedication(id: String): OperationResult = OperationResult.NotFound
}

class RoomMedicationRepository @Inject constructor(
    private val texts: TextResolver,private val database: AppDatabase) : MedicationRepository {
    private val dao get() = database.medicationDao()
    private val tracking get() = database.trackingDao()
    override fun getMedications(): Flow<List<MedicationItem>> = combine(dao.getAllMedications(), tracking.observeTracking(), tracking.observeEvents()) { medications, tracks, events ->
        medications.map { med -> med.toDomain(tracks.find { it.medicationId == med.id }, events, texts) }
    }
    override suspend fun getMedicationById(id: String): MedicationItem? = database.withTransaction {
        val med = dao.getMedicationById(id) ?: return@withTransaction null
        val track = tracking.tracking(id)
        med.toDomain(track, track?.let { tracking.events(it.id) }.orEmpty(), texts)
    }
    override suspend fun registerPickup(id: String, date: LocalDate, confirmedEarly: Boolean, expectedCycle: LocalDate?): OperationResult = database.withTransaction {
        if (date.isAfter(LocalDate.now())) return@withTransaction OperationResult.InvalidDate
        val med = getMedicationById(id) ?: return@withTransaction OperationResult.NotFound
        val track = tracking.tracking(id) ?: return@withTransaction OperationResult.Inactive
        if (!med.hasRecurringPickup || track.archived) return@withTransaction OperationResult.Inactive
        if (expectedCycle != null && med.nextAvailableRefillDate != expectedCycle) return@withTransaction OperationResult.ObsoleteCycle
        if (med.pickupHistory.any { it.occurredOn == date }) return@withTransaction OperationResult.AlreadyRecorded
        if (med.nextAvailableRefillDate?.let { date.isBefore(it) } == true && med.lastRefillDate?.let { date.isAfter(it) } == true && !confirmedEarly) return@withTransaction OperationResult.ConfirmationRequired
        tracking.insertEvent(PickupEventEntity(UUID.randomUUID().toString(), track.id, date))
        if (med.lastRefillDate == null || date.isAfter(med.lastRefillDate)) closePickupAlerts(id)
        OperationResult.Success
    }
    private suspend fun closePickupAlerts(id: String) = database.resolveReminders(id, "PICKUP", "PICKUP_RECORDED", texts.get(R.string.pickup_recorded))
    override suspend fun correctPickup(eventId: String, date: LocalDate): OperationResult = database.withTransaction {
        if (date.isAfter(LocalDate.now())) return@withTransaction OperationResult.InvalidDate
        val event = tracking.event(eventId) ?: return@withTransaction OperationResult.NotFound
        if (tracking.events(event.trackingId).any { it.id != eventId && it.occurredOn == date }) return@withTransaction OperationResult.AlreadyRecorded
        tracking.updateEvent(event.copy(occurredOn = date))
        OperationResult.Success
    }
    override suspend fun deletePickup(eventId: String): OperationResult = database.withTransaction {
        if (tracking.event(eventId) == null) return@withTransaction OperationResult.NotFound
        tracking.deleteEvent(eventId)
        OperationResult.Success
    }
    override suspend fun archiveTracking(id: String, archived: Boolean): OperationResult = database.withTransaction {
        val track = tracking.tracking(id) ?: return@withTransaction OperationResult.NotFound
        if (!archived && track.prescriptionId?.let { database.prescriptionDao().getPrescriptionById(it)?.state != "ACTIVE" } == true) return@withTransaction OperationResult.InvalidPrescription
        tracking.updateTracking(track.copy(archived = archived))
        if (archived) database.resolveReminders(id, "PICKUP", "OBSOLETE", texts.get(R.string.tracking_archived_message))
        OperationResult.Success
    }
    override suspend fun deleteMedication(id: String): OperationResult = database.withTransaction {
        if (dao.getMedicationById(id) == null) return@withTransaction OperationResult.NotFound
        if (tracking.observeItems().first().any { it.medicationId == id }) return@withTransaction OperationResult.Inactive
        database.resolveReminders(id, "PICKUP", "OBSOLETE", texts.get(R.string.medication_deleted_message))
        dao.deleteMedication(id)
        OperationResult.Success
    }
    override suspend fun addMedication(medication: MedicationItem) = database.withTransaction {
        dao.insertMedication(medication.toEntity())
        saveTracking(medication)
    }
    override suspend fun updateMedication(medication: MedicationItem) = database.withTransaction {
        val existing = dao.getMedicationById(medication.id) ?: throw BusinessRuleViolation(TextMessage(R.string.medication_not_found))
        // Preserve retired columns. All current writes use the normalized tables.
        dao.updateMedication(existing.copy(title = medication.title, activeIngredient = medication.activeIngredient, dosage = medication.dosage, form = medication.form))
        saveTracking(medication)
    }
    private suspend fun saveTracking(med: MedicationItem) {
        val old = tracking.tracking(med.id)
        val history = old?.let { tracking.events(it.id) }.orEmpty()
        val initialDate = med.initialPickupDate ?: if (old == null) med.lastRefillDate else null
        if (med.hasRecurringPickup) {
            val interval = PickupInterval(med.refillCycleDays, med.intervalUnit)
            val date = history.maxOfOrNull { it.occurredOn } ?: initialDate ?: throw BusinessRuleViolation(TextMessage(R.string.pickup_initial_date_required))
            requireBusiness(!date.isAfter(LocalDate.now())) { TextMessage(R.string.pickup_future_date_error) }
            requireBusiness(med.pickupReminderDaysBefore in 0..java.time.temporal.ChronoUnit.DAYS.between(date, interval.nextAfter(date)).toInt()) { TextMessage(R.string.pickup_reminder_interval_error) }
        }
        med.linkedPrescriptionId?.let { id ->
            val prescription = database.prescriptionDao().getPrescriptionById(id) ?: throw BusinessRuleViolation(TextMessage(R.string.prescription_not_found))
            val preservingInactive = old?.prescriptionId == id && (med.trackingArchived || !med.hasRecurringPickup)
            requireBusiness(prescription.state == "ACTIVE" || preservingInactive) { TextMessage(R.string.active_prescription_required) }
            requireBusiness(tracking.items(id).any { it.medicationId == med.id }) { TextMessage(R.string.prescription_missing_medication) }
        }
        val updated = PickupTrackingEntity(old?.id ?: "tracking:" + med.id, med.id, med.linkedPrescriptionId,
            med.hasRecurringPickup, med.trackingArchived, med.refillCycleDays, med.intervalUnit.name,
            med.pickupReminderDaysBefore, med.source, med.sourceName, med.quantity, med.instructions)
        if (old == null) tracking.insertTracking(updated) else tracking.updateTracking(updated)
        // Editing configuration does not write history. Only initialize an empty tracking explicitly.
        if (med.hasRecurringPickup && history.isEmpty() && initialDate != null) {
            requireBusiness(!initialDate.isAfter(LocalDate.now())) { TextMessage(R.string.pickup_future_date_error) }
            tracking.insertEvent(PickupEventEntity(UUID.randomUUID().toString(), updated.id, initialDate))
        }
    }
    private fun MedicationItem.toEntity() = MedicationEntity(id = id, title = title, activeIngredient = activeIngredient, dosage = dosage, form = form, stockAlertEnabled = false)
}

fun MedicationEntity.toDomain(track: PickupTrackingEntity?, events: List<PickupEventEntity>, texts: TextResolver): MedicationItem {
    val unit = IntervalUnit.entries.firstOrNull { it.name == track?.intervalUnit }
    val invalid = track != null && (unit == null || track.intervalAmount !in 1..3650)
    val history = events.filter { it.trackingId == track?.id }.sortedByDescending { it.occurredOn }.map { PickupEvent(it.id, it.occurredOn, it.recordedAt) }
    return MedicationItem(id = id, title = title, activeIngredient = activeIngredient, dosage = dosage, form = form,
        quantity = track?.quantity.orEmpty(), source = track?.source ?: MedicationSource.SELF_PURCHASE,
        sourceName = track?.sourceName, instructions = track?.note,
        lastRefillDate = history.firstOrNull()?.occurredOn, refillCycleDays = track?.intervalAmount ?: 30,
        intervalUnit = unit ?: IntervalUnit.DAYS, hasRecurringPickup = track?.enabled == true && !invalid,
        pickupReminderDaysBefore = track?.reminderDaysBefore ?: 3, linkedPrescriptionId = track?.prescriptionId,
        trackingArchived = track?.archived == true, pickupHistory = history,
        trackingError = if (invalid) texts.get(R.string.tracking_interval_review_required) else null)
}
