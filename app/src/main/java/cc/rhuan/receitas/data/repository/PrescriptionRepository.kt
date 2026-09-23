package cc.rhuan.receitas.data.repository

import cc.rhuan.receitas.data.local.dao.DoctorDao
import cc.rhuan.receitas.data.local.dao.PrescriptionDao
import cc.rhuan.receitas.data.local.entity.DoctorEntity
import cc.rhuan.receitas.data.local.entity.PrescriptionEntity
import cc.rhuan.receitas.domain.model.PrescriptionItem
import cc.rhuan.receitas.domain.model.PrescriptionType
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

interface PrescriptionRepository {
    fun getPrescriptions(): Flow<List<PrescriptionItem>>
    suspend fun getPrescriptionById(id: String): PrescriptionItem?
    suspend fun renewPrescription(id: String?, title: String?, message: String?)
    suspend fun addPrescription(prescription: PrescriptionItem)
    suspend fun updatePrescription(prescription: PrescriptionItem)
    suspend fun deletePrescription(id: String)
}

class RoomPrescriptionRepository @Inject constructor(
    private val prescriptionDao: PrescriptionDao,
    private val doctorDao: DoctorDao
) : PrescriptionRepository {

    override fun getPrescriptions(): Flow<List<PrescriptionItem>> {
        return combine(
            prescriptionDao.getAllPrescriptions(),
            doctorDao.getAllDoctors()
        ) { prescriptions, doctors ->
            val doctorMapById = doctors.associateBy { it.id }
            val doctorMapByReg = doctors.filter { !it.registration.isNull_or_blank() }.associateBy { it.registration!! }

            prescriptions.map { entity ->
                val doctor = entity.doctorId?.let { idOrReg ->
                    doctorMapById[idOrReg] ?: doctorMapByReg[idOrReg]
                }
                entity.toDomain(doctor)
            }
        }
    }

    override suspend fun getPrescriptionById(id: String): PrescriptionItem? {
        val entity = prescriptionDao.getPrescriptionById(id) ?: return null
        val doctor = entity.doctorId?.let { idOrReg ->
            doctorDao.getDoctorById(idOrReg) ?: doctorDao.getAllDoctors().firstOrNull()?.find { it.registration == idOrReg }
        }
        return entity.toDomain(doctor)
    }

    override suspend fun renewPrescription(id: String?, title: String?, message: String?) {
        val prescriptions = getPrescriptions().firstOrNull() ?: emptyList()
        prescriptions.forEach { prescription ->
            val isMatching = (id != null && prescription.id == id) ||
                (message?.contains(prescription.title, ignoreCase = true) == true) ||
                (title?.contains(prescription.title, ignoreCase = true) == true)

            if (isMatching) {
                val updated = prescription.copy(
                    issueDate = LocalDate.now(),
                    notes = "Receita renovada recentemente em ${LocalDate.now()}"
                )
                updatePrescription(updated)
            }
        }
    }

    override suspend fun addPrescription(prescription: PrescriptionItem) {
        prescriptionDao.insertPrescription(prescription.toEntity())
    }

    override suspend fun updatePrescription(prescription: PrescriptionItem) {
        prescriptionDao.updatePrescription(prescription.toEntity())
    }

    override suspend fun deletePrescription(id: String) {
        prescriptionDao.deletePrescription(id)
    }

    private fun PrescriptionEntity.toDomain(doctor: DoctorEntity?) = PrescriptionItem(
        id = id,
        title = title,
        specialty = doctor?.specialty ?: "",
        doctorId = doctor?.id ?: doctorId,
        doctorName = doctor?.name ?: "",
        doctorRegistration = doctor?.registration,
        issueDate = issueDate,
        type = type,
        customValidityDays = validityDays,
        notes = notes,
        medicationIds = medicationIds
    )

    private fun PrescriptionItem.toEntity() = PrescriptionEntity(
        id = id,
        title = title,
        type = type,
        validityDays = customValidityDays ?: type?.defaultValidityDays ?: 180,
        issueDate = issueDate,
        renewalReminderEnabled = true,
        renewalReminderDaysBefore = 5,
        doctorId = doctorId,
        medicationIds = medicationIds,
        notes = notes,
        photoUri = null
    )
}

private fun String?.isNull_or_blank(): Boolean {
    return this == null || this.trim().isEmpty()
}

class FakePrescriptionRepository : PrescriptionRepository {
    companion object {
        private val _prescriptions = MutableStateFlow(
            listOf(
                PrescriptionItem(
                    id = "1",
                    title = "Receita Cardiologia",
                    specialty = "Cardiologista",
                    doctorName = "Dr. Carlos Eduardo",
                    doctorRegistration = "CRM/SP 123456",
                    type = PrescriptionType.PFPB,
                    issueDate = LocalDate.now().minusDays(175),
                    notes = "Uso contínuo de anti-hipertensivo e estatina.",
                    medicationIds = listOf("2", "5"),
                ),
                PrescriptionItem(
                    id = "2",
                    title = "Tratamento Dermatológico",
                    specialty = "Dermatologista",
                    doctorName = "Dra. Ana Paula",
                    doctorRegistration = "CRM/SP 654321",
                    type = PrescriptionType.SPECIAL_CONTROL,
                    issueDate = LocalDate.now().minusDays(28),
                    notes = "Pomada dermatológica à noite.",
                    medicationIds = listOf("3"),
                ),
                PrescriptionItem(
                    id = "3",
                    title = "Tratamento de Infecção",
                    specialty = "Clínico Geral",
                    doctorName = "Dr. Marcos Vinicius",
                    doctorRegistration = "CRM/RJ 987654",
                    type = PrescriptionType.ANTIMICROBIAL,
                    issueDate = LocalDate.now().minusDays(2),
                    notes = "Tratamento para gastrite e antibiótico.",
                    medicationIds = listOf("1", "4"),
                ),
                PrescriptionItem(
                    id = "4",
                    title = "Receita Pediatria",
                    specialty = "Pediatra",
                    doctorName = "Dra. Camila Santos",
                    doctorRegistration = "CRM/MG 456789",
                    type = PrescriptionType.SUS_BASIC,
                    issueDate = LocalDate.now().minusDays(30),
                    notes = "Xarope para tosse.",
                    medicationIds = emptyList(),
                ),
                PrescriptionItem(
                    id = "5",
                    title = "Avaliação Psiquiátrica",
                    specialty = "Psiquiatra",
                    doctorName = "Dr. Lucas Mendes",
                    doctorRegistration = "CRM/SP 112233",
                    type = PrescriptionType.SPECIAL_CONTROL,
                    issueDate = LocalDate.now().minusDays(35),
                    notes = "Controle de ansiedade.",
                    medicationIds = emptyList(),
                ),
            )
        )
    }

    override fun getPrescriptions(): Flow<List<PrescriptionItem>> = _prescriptions.asStateFlow()

    override suspend fun getPrescriptionById(id: String): PrescriptionItem? {
        return _prescriptions.value.find { it.id == id }
    }

    override suspend fun renewPrescription(id: String?, title: String?, message: String?) {
        _prescriptions.update { list ->
            list.map { prescription ->
                val isMatching = (id != null && prescription.id == id) ||
                    (message?.contains(prescription.title, ignoreCase = true) == true) ||
                    (title?.contains(prescription.title, ignoreCase = true) == true)

                if (isMatching) {
                    prescription.copy(
                        issueDate = LocalDate.now(),
                        notes = "Receita renovada recentemente em ${LocalDate.now()}",
                    )
                } else prescription
            }
        }
    }

    override suspend fun addPrescription(prescription: PrescriptionItem) {
        _prescriptions.update { it + prescription }
    }

    override suspend fun updatePrescription(prescription: PrescriptionItem) {
        _prescriptions.update { list ->
            list.map { if (it.id == prescription.id) prescription else it }
        }
    }

    override suspend fun deletePrescription(id: String) {
        _prescriptions.update { list -> list.filter { it.id != id } }
    }
}
