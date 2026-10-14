package cc.rhuan.receitas.data.repository

import cc.rhuan.receitas.data.local.dao.MedicationDao
import cc.rhuan.receitas.data.local.entity.MedicationEntity
import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.domain.model.MedicationSource
import cc.rhuan.receitas.domain.model.PharmaceuticalForm
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

interface MedicationRepository {
    fun getMedications(): Flow<List<MedicationItem>>
    suspend fun getMedicationById(id: String): MedicationItem?
    suspend fun replenishStock(id: String?, title: String?, message: String?, customDate: LocalDate = LocalDate.now())
    suspend fun addMedication(medication: MedicationItem)
    suspend fun updateMedication(medication: MedicationItem)
}

class RoomMedicationRepository @Inject constructor(
    private val medicationDao: MedicationDao
) : MedicationRepository {

    override fun getMedications(): Flow<List<MedicationItem>> {
        return medicationDao.getAllMedications().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getMedicationById(id: String): MedicationItem? {
        return medicationDao.getMedicationById(id)?.toDomain()
    }

    override suspend fun replenishStock(id: String?, title: String?, message: String?, customDate: LocalDate) {
        val medications = medicationDao.getAllMedications().firstOrNull() ?: emptyList()
        medications.filter { medication ->
            if (id != null) medication.id == id
            else message?.contains(medication.title, ignoreCase = true) == true || title?.contains(medication.title, ignoreCase = true) == true
        }.forEach { medication ->
            medicationDao.updateMedication(medication.copy(lastPickupDate = customDate))
        }
    }

    override suspend fun addMedication(medication: MedicationItem) {
        medicationDao.insertMedication(medication.toEntity())
    }

    override suspend fun updateMedication(medication: MedicationItem) {
        val existingEntity = medicationDao.getMedicationById(medication.id)
        medicationDao.updateMedication(medication.toEntity(existingEntity))
    }

    private fun MedicationEntity.toDomain(): MedicationItem {
        val calculatedAvailable = !hasRecurringPickup || isAvailableForRefill(lastPickupDate, pickupIntervalDays ?: 30)
        val formattedQuantity = prescribedQuantity

        return MedicationItem(
            id = id,
            title = title,
            activeIngredient = activeIngredient,
            dosage = dosage,
            quantity = formattedQuantity,
            prescribedPosology = prescribedPosology,
            category = source.label,
            source = source,
            sourceName = sourceName,
            isAvailable = calculatedAvailable,
            instructions = availabilityNote,
            form = form,
            stockQuantity = currentStock ?: 0,
            minStockThreshold = minStockThreshold,
            lastRefillDate = lastPickupDate,
            refillCycleDays = pickupIntervalDays ?: 30,
            hasRecurringPickup = hasRecurringPickup,
            pickupReminderDaysBefore = pickupReminderDaysBefore
        )
    }

    private fun isAvailableForRefill(lastRefillDate: LocalDate?, refillCycleDays: Int): Boolean {
        if (lastRefillDate == null) return true
        val nextAvailable = lastRefillDate.plusDays(refillCycleDays.toLong())
        return !LocalDate.now().isBefore(nextAvailable)
    }

    private fun MedicationItem.toEntity(existingEntity: MedicationEntity? = null): MedicationEntity {
        return MedicationEntity(
            id = id,
            title = title,
            activeIngredient = activeIngredient,
            form = form,
            dosage = dosage,
            prescribedPosology = prescribedPosology,
            prescribedQuantity = quantity,
            source = source,
            sourceName = sourceName,
            hasRecurringPickup = hasRecurringPickup,
            lastPickupDate = if (hasRecurringPickup) lastRefillDate else null,
            pickupIntervalDays = refillCycleDays,
            pickupReminderDaysBefore = pickupReminderDaysBefore,
            currentStock = stockQuantity,
            stockAlertEnabled = false,
            minStockThreshold = minStockThreshold,
            availabilityNote = instructions
        )
    }
}

class FakeMedicationRepository : MedicationRepository {
    companion object {
        private val _medications = MutableStateFlow(
            listOf(
                MedicationItem(
                    id = "1",
                    title = "Amoxicilina",
                    activeIngredient = "Amoxicilina Tri-hidratada",
                    dosage = "500 mg",
                    form = PharmaceuticalForm.CAPSULE,
                    quantity = "2 cx",
                    prescribedPosology = "Tomar 1 cápsula de 8 em 8 horas por 7 dias",
                    stockQuantity = 24,
                    minStockThreshold = 10,
                    category = "Antibiótico",
                    source = MedicationSource.COMPRA_PROPRIA,
                    isAvailable = true,
                    instructions = "Tomar após as refeições.",
                    lastRefillDate = LocalDate.now().minusDays(5),
                    refillCycleDays = 10,
                ),
                MedicationItem(
                    id = "2",
                    title = "Losartana Potássica",
                    activeIngredient = "Losartana Potássica",
                    dosage = "50 mg",
                    form = PharmaceuticalForm.TABLET,
                    quantity = "0 cx",
                    prescribedPosology = "1 comprimido ao dia pela manhã",
                    stockQuantity = 2,
                    minStockThreshold = 15,
                    category = "Uso Contínuo",
                    source = MedicationSource.FARMACIA_POPULAR,
                    isAvailable = true,
                    lastRefillDate = LocalDate.now().minusDays(15),
                    refillCycleDays = 30,
                    instructions = "Tomar pela manhã com água.",
                ),
                MedicationItem(
                    id = "3",
                    title = "Dipirona Sódica",
                    activeIngredient = "Dipirona Monoidratada",
                    dosage = "500 mg",
                    form = PharmaceuticalForm.DROPS,
                    quantity = "0 uni",
                    prescribedPosology = "20 gotas se houver dor ou febre de 6 em 6h",
                    stockQuantity = 0,
                    minStockThreshold = 1,
                    category = "Analgésico",
                    source = MedicationSource.COMPRA_PROPRIA,
                    isAvailable = true,
                    lastRefillDate = LocalDate.now().minusDays(28),
                    refillCycleDays = 30,
                    instructions = "Máximo 4 vezes ao dia.",
                ),
                MedicationItem(
                    id = "4",
                    title = "Omeprazol",
                    activeIngredient = "Omeprazol Magnésico",
                    dosage = "20 mg",
                    form = PharmaceuticalForm.CAPSULE,
                    quantity = "2 cx",
                    prescribedPosology = "1 cápsula ao dia em jejum",
                    stockQuantity = 28,
                    minStockThreshold = 7,
                    category = "Gastroprotetor",
                    source = MedicationSource.FARMACIA_BASICA,
                    isAvailable = true,
                    lastRefillDate = LocalDate.now().minusDays(10),
                    refillCycleDays = 30,
                    instructions = "Tomar 30 minutos antes do café.",
                ),
                MedicationItem(
                    id = "5",
                    title = "Sinvastatina",
                    activeIngredient = "Sinvastatina",
                    dosage = "20 mg",
                    form = PharmaceuticalForm.TABLET,
                    quantity = "1 cx",
                    prescribedPosology = "1 comprimido à noite antes de dormir",
                    stockQuantity = 14,
                    minStockThreshold = 10,
                    category = "Uso Contínuo",
                    source = MedicationSource.FARMACIA_POPULAR,
                    isAvailable = true,
                    lastRefillDate = LocalDate.now().minusDays(20),
                    refillCycleDays = 30,
                    instructions = "Tomar antes de dormir.",
                ),
            )
        )
    }

    override fun getMedications(): Flow<List<MedicationItem>> = _medications.asStateFlow()

    override suspend fun getMedicationById(id: String): MedicationItem? {
        return _medications.value.find { it.id == id }
    }

    override suspend fun replenishStock(id: String?, title: String?, message: String?, customDate: LocalDate) {
        _medications.update { list ->
            list.map { medication ->
                val isMatching = (id != null && medication.id == id) ||
                    (message?.contains(medication.title, ignoreCase = true) == true) ||
                    (title?.contains(medication.title, ignoreCase = true) == true)

                if (isMatching) {
                    medication.copy(
                        isAvailable = true,
                        lastRefillDate = customDate,
                    )
                } else medication
            }
        }
    }

    override suspend fun addMedication(medication: MedicationItem) {
        _medications.update { it + medication }
    }

    override suspend fun updateMedication(medication: MedicationItem) {
        _medications.update { list -> list.map { if (it.id == medication.id) medication else it } }
    }
}
