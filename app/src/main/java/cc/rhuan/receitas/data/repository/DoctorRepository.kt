package cc.rhuan.receitas.data.repository

import cc.rhuan.receitas.data.local.dao.DoctorDao
import cc.rhuan.receitas.data.local.entity.DoctorEntity
import cc.rhuan.receitas.domain.model.Doctor
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
