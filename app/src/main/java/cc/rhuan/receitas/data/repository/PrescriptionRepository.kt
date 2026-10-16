package cc.rhuan.receitas.data.repository

import cc.rhuan.receitas.text.TextMessage

import cc.rhuan.receitas.text.TextResolver

import cc.rhuan.receitas.R

import androidx.room.withTransaction
import cc.rhuan.receitas.data.local.AppDatabase
import cc.rhuan.receitas.data.local.entity.PrescriptionEntity
import cc.rhuan.receitas.domain.model.*
import javax.inject.Inject
import kotlinx.coroutines.flow.*

interface PrescriptionRepository {
    fun getPrescriptions(): Flow<List<PrescriptionItem>>
    suspend fun getPrescriptionById(id: String): PrescriptionItem?
    suspend fun addPrescription(prescription: PrescriptionItem)
    suspend fun updatePrescription(prescription: PrescriptionItem)
    suspend fun deletePrescription(id: String)
    suspend fun archivePrescription(id: String, archived: Boolean)
}
class RoomPrescriptionRepository @Inject constructor(
    private val texts: TextResolver,private val database: AppDatabase) : PrescriptionRepository {
    private val dao get() = database.prescriptionDao()
    override fun getPrescriptions(): Flow<List<PrescriptionItem>> = combine(dao.getAllPrescriptions(), database.trackingDao().observeItems()) { recipes, items ->
        recipes.map { it.toDomain(items.filter { item -> item.prescriptionId == it.id }.map { item -> PrescriptionMedication(item.medicationId, item.posology, item.quantity, item.titleSnapshot, item.dosageSnapshot, PharmaceuticalForm.valueOf(item.formSnapshot)) }) }
    }
    override suspend fun getPrescriptionById(id: String): PrescriptionItem? = database.withTransaction {
        dao.getPrescriptionById(id)?.toDomain(database.trackingDao().items(id).map { PrescriptionMedication(it.medicationId, it.posology, it.quantity, it.titleSnapshot, it.dosageSnapshot, PharmaceuticalForm.valueOf(it.formSnapshot)) })
    }
    override suspend fun addPrescription(prescription: PrescriptionItem) { validate(prescription); dao.insertPrescription(prescription.toEntity()) }
    override suspend fun updatePrescription(prescription: PrescriptionItem) {
        validate(prescription)
        val old = dao.getPrescriptionById(prescription.id) ?: throw BusinessRuleViolation(TextMessage(R.string.prescription_not_found))
        requireBusiness(old.state != "REPLACED") { TextMessage(R.string.replaced_prescription_readonly) }
        dao.updatePrescription(prescription.toEntity(old))
    }
    override suspend fun archivePrescription(id: String, archived: Boolean) = database.withTransaction {
        val p = dao.getPrescriptionById(id) ?: throw BusinessRuleViolation(TextMessage(R.string.prescription_not_found))
        requireBusiness(p.state != "REPLACED") { TextMessage(R.string.prescription_already_replaced) }
        dao.updatePrescription(p.copy(state = if (archived) "ARCHIVED" else "ACTIVE"))
        database.trackingDao().archiveForPrescription(id, archived)
        if (archived) {
            database.resolveReminders(id, "PRESCRIPTION", "OBSOLETE", texts.get(R.string.prescription_archived_message))
            database.trackingDao().observeTracking().first().filter { it.prescriptionId == id }.forEach { database.resolveReminders(it.medicationId, "PICKUP", "OBSOLETE", texts.get(R.string.tracking_archived_message)) }
        }
    }
    override suspend fun deletePrescription(id: String) = database.withTransaction {
        requireBusiness(dao.getPrescriptionById(id) != null) { TextMessage(R.string.prescription_not_found) }
        database.resolveReminders(id, "PRESCRIPTION", "OBSOLETE", texts.get(R.string.prescription_deleted_message))
        database.trackingDao().unlink(id)
        dao.deletePrescription(id)
    }
    private fun validate(p: PrescriptionItem) {
        requireBusiness(p.title.isNotBlank())
        requireBusiness(!p.issueDate.isAfter(java.time.LocalDate.now())) { TextMessage(R.string.issue_date_future_error) }
        requireBusiness(!p.expirationDate.isBefore(p.issueDate)) { TextMessage(R.string.expiration_before_issue_error) }
        requireBusiness(p.validityDurationDays in 1..3650)
        requireBusiness(!p.renewalReminderEnabled || p.renewalReminderDaysBefore in 0..p.validityDurationDays)
    }
    private fun PrescriptionItem.toEntity(old: PrescriptionEntity? = null) = PrescriptionEntity(
        id = id, title = title, type = type, validityDays = validityDurationDays, issueDate = issueDate,
        renewalReminderEnabled = renewalReminderEnabled, renewalReminderDaysBefore = renewalReminderDaysBefore,
        doctorId = doctorId, medicationIds = medicationIds, notes = notes, photoUri = old?.photoUri,
        state = state.name, previousPrescriptionId = previousPrescriptionId, doctorNameSnapshot = doctorName,
        doctorRegistrationSnapshot = doctorRegistration, doctorSpecialtySnapshot = specialty,
        validityOrigin = validityOrigin, expiresOn = expirationDate)
}
fun PrescriptionEntity.toDomain(items: List<PrescriptionMedication> = emptyList()) = PrescriptionItem(
    id = id, title = title, specialty = doctorSpecialtySnapshot, doctorId = doctorId,
    doctorName = doctorNameSnapshot, doctorRegistration = doctorRegistrationSnapshot, issueDate = issueDate,
    type = type, customValidityDays = validityDays, notes = notes, medicationIds = items.map { it.medicationId },
    items = items, renewalReminderEnabled = renewalReminderEnabled, renewalReminderDaysBefore = renewalReminderDaysBefore,
    state = RecordState.valueOf(state), previousPrescriptionId = previousPrescriptionId, expiresOn = expiresOn, validityOrigin = validityOrigin)
