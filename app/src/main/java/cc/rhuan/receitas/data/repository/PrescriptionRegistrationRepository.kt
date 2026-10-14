package cc.rhuan.receitas.data.repository

import androidx.room.withTransaction
import cc.rhuan.receitas.data.local.AppDatabase
import cc.rhuan.receitas.domain.model.Doctor
import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.domain.model.PrescriptionItem
import javax.inject.Inject

interface PrescriptionRegistrationRepository {
    suspend fun save(prescription: PrescriptionItem?, medications: List<MedicationItem>, newDoctor: Doctor?)
}

class RoomPrescriptionRegistrationRepository @Inject constructor(
    private val database: AppDatabase,
    private val prescriptions: PrescriptionRepository,
    private val medications: MedicationRepository,
    private val doctors: DoctorRepository,
) : PrescriptionRegistrationRepository {
    override suspend fun save(prescription: PrescriptionItem?, medications: List<MedicationItem>, newDoctor: Doctor?) {
        database.withTransaction {
            newDoctor?.let { doctors.addDoctor(it) }
            medications.forEach { medication ->
                if (this@RoomPrescriptionRegistrationRepository.medications.getMedicationById(medication.id) == null) {
                    this@RoomPrescriptionRegistrationRepository.medications.addMedication(medication)
                } else {
                    this@RoomPrescriptionRegistrationRepository.medications.updateMedication(medication)
                }
            }
            prescription?.let {
                if (prescriptions.getPrescriptionById(it.id) == null) prescriptions.addPrescription(it)
                else prescriptions.updatePrescription(it)
            }
        }
    }
}
