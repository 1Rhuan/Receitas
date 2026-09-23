package cc.rhuan.receitas.data.repository

import cc.rhuan.receitas.data.local.dao.DoctorDao
import cc.rhuan.receitas.data.local.entity.DoctorEntity
import cc.rhuan.receitas.domain.model.Doctor
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

interface DoctorRepository {
    fun getDoctors(): Flow<List<Doctor>>
    suspend fun getDoctorById(id: String): Doctor?
    suspend fun addDoctor(doctor: Doctor)
    suspend fun updateDoctor(doctor: Doctor)
}

class RoomDoctorRepository @Inject constructor(
    private val doctorDao: DoctorDao
) : DoctorRepository {
    override fun getDoctors(): Flow<List<Doctor>> {
        return doctorDao.getAllDoctors().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getDoctorById(id: String): Doctor? {
        return doctorDao.getDoctorById(id)?.toDomain()
    }

    override suspend fun addDoctor(doctor: Doctor) {
        doctorDao.insertDoctor(doctor.toEntity())
    }

    override suspend fun updateDoctor(doctor: Doctor) {
        doctorDao.updateDoctor(doctor.toEntity())
    }

    private fun DoctorEntity.toDomain() = Doctor(
        id = id,
        name = name,
        registration = registration,
        specialty = specialty,
        healthFacility = healthFacility,
        phone = phone
    )

    private fun Doctor.toEntity() = DoctorEntity(
        id = id,
        name = name,
        registration = registration,
        specialty = specialty,
        healthFacility = healthFacility,
        phone = phone
    )
}

class FakeDoctorRepository : DoctorRepository {
    companion object {
        private val _doctors = MutableStateFlow(
            listOf(
                Doctor("1", "Dr. Carlos Eduardo", "CRM/SP 123456", "Cardiologia", "UBS Vila Mariana", "(11) 98765-4321"),
                Doctor("2", "Dra. Ana Paula", "CRM/SP 654321", "Dermatologia", "Hospital das Clínicas", "(11) 91234-5678"),
                Doctor("3", "Dr. Marcos Vinicius", "CRM/RJ 987654", "Clínico Geral", "Posto de Saúde Central", "(21) 99876-5432")
            )
        )
    }

    override fun getDoctors(): Flow<List<Doctor>> = _doctors.asStateFlow()

    override suspend fun getDoctorById(id: String): Doctor? {
        return _doctors.value.find { it.id == id }
    }

    override suspend fun addDoctor(doctor: Doctor) {
        _doctors.update { it + doctor }
    }

    override suspend fun updateDoctor(doctor: Doctor) {
        _doctors.update { list -> list.map { if (it.id == doctor.id) doctor else it } }
    }
}
