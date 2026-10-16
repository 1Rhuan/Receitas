package cc.rhuan.receitas.data.repository

import androidx.room.withTransaction
import cc.rhuan.receitas.data.local.AppDatabase
import cc.rhuan.receitas.data.local.entity.*
import cc.rhuan.receitas.domain.model.*
import javax.inject.Inject
import kotlinx.coroutines.flow.first

interface PrescriptionRegistrationRepository {
    suspend fun save(prescription: PrescriptionItem?, medications: List<MedicationItem>, newDoctor: Doctor?)
    suspend fun addToPrescription(prescriptionId: String, medications: List<MedicationItem>)
}
class RoomPrescriptionRegistrationRepository @Inject constructor(
    private val database: AppDatabase, private val prescriptions: PrescriptionRepository,
    private val medications: MedicationRepository, private val doctors: DoctorRepository,
) : PrescriptionRegistrationRepository {
    override suspend fun save(prescription: PrescriptionItem?, medications: List<MedicationItem>, newDoctor: Doctor?) {
        requireBusiness((prescription != null || medications.isNotEmpty()) && medications.all { it.title.isNotBlank() })
        requireBusiness(medications.map { it.id }.distinct().size == medications.size)
        database.withTransaction {
            prescription?.let { requireBusiness(it.medicationIds.toSet() == medications.map { med -> med.id }.toSet()) { "Os medicamentos não correspondem aos itens da receita" } }
            val tracking = database.trackingDao()
            val existingRecipe = prescription?.let { database.prescriptionDao().getPrescriptionById(it.id) }
            val previous = if (existingRecipe == null) prescription?.previousPrescriptionId?.let { database.prescriptionDao().getPrescriptionById(it) ?: throw BusinessRuleViolation("Receita anterior não encontrada") } else null
            requireBusiness(previous == null || previous.state == "ACTIVE") { "A receita anterior já foi encerrada" }
            newDoctor?.let { if (doctors.getDoctorById(it.id) == null) doctors.addDoctor(it) else doctors.updateDoctor(it) }
            // Insert products first; tracking is saved only after the recipe and its items exist.
            medications.forEach { med ->
                val old = database.medicationDao().getMedicationById(med.id)
                if (old == null) database.medicationDao().insertMedication(MedicationEntity(id = med.id, title = med.title, activeIngredient = med.activeIngredient, form = med.form, dosage = med.dosage, stockAlertEnabled = false))
                else database.medicationDao().updateMedication(old.copy(title = med.title, activeIngredient = med.activeIngredient, form = med.form, dosage = med.dosage))
            }
            prescription?.let { p ->
                if (prescriptions.getPrescriptionById(p.id) == null) prescriptions.addPrescription(p) else prescriptions.updatePrescription(p)
                tracking.clearItems(p.id)
                tracking.putItems(medications.map { PrescriptionMedicationEntity(p.id, it.id, it.prescribedPosology, it.prescribedQuantity, it.title, it.dosage, it.form.name) })
                // Removed items must not leave a tracking linked to a recipe without that medication.
                tracking.observeTracking().first().filter { it.prescriptionId == p.id && it.medicationId !in p.medicationIds }.forEach { tracking.updateTracking(it.copy(prescriptionId = null)) }
            }
            medications.forEach { this@RoomPrescriptionRegistrationRepository.medications.updateMedication(it) }
            if (previous != null) {
                database.prescriptionDao().updatePrescription(previous.copy(state = "REPLACED"))
                database.resolveReminders(previous.id, "PRESCRIPTION", "RENEWED", "Nova receita registrada")
                // Remaining tracks not explicitly continued are archived; their events stay intact.
                tracking.archiveForPrescription(previous.id, true)
            }
        }
    }

    override suspend fun addToPrescription(prescriptionId: String, medications: List<MedicationItem>) {
        requireBusiness(medications.isNotEmpty() && medications.all { it.title.isNotBlank() })
        requireBusiness(medications.map { it.id }.distinct().size == medications.size)
        database.withTransaction {
            val recipe = database.prescriptionDao().getPrescriptionById(prescriptionId)
                ?: throw BusinessRuleViolation("Receita não encontrada")
            requireBusiness(recipe.state == "ACTIVE") { "Reative a receita antes de adicionar medicamentos" }
            val tracking = database.trackingDao()
            val existingItems = tracking.items(prescriptionId)
            requireBusiness(medications.none { med -> existingItems.any { it.medicationId == med.id } }) {
                "Esse medicamento já está na receita. Edite o item existente."
            }
            medications.forEach { med ->
                val old = database.medicationDao().getMedicationById(med.id)
                if (old == null) database.medicationDao().insertMedication(MedicationEntity(id = med.id, title = med.title, activeIngredient = med.activeIngredient, form = med.form, dosage = med.dosage, stockAlertEnabled = false))
            }
            tracking.putItems(medications.map { PrescriptionMedicationEntity(prescriptionId, it.id, it.prescribedPosology, it.prescribedQuantity, it.title, it.dosage, it.form.name) })
            database.prescriptionDao().updatePrescription(recipe.copy(medicationIds = existingItems.map { it.medicationId } + medications.map { it.id }))
            medications.forEach { this@RoomPrescriptionRegistrationRepository.medications.updateMedication(it.copy(linkedPrescriptionId = prescriptionId)) }
        }
    }
}
