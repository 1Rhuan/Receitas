package cc.rhuan.receitas.data.repository

import cc.rhuan.receitas.domain.model.MedicationItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface MedicationRepository {
    fun getMedications(): Flow<List<MedicationItem>>
    suspend fun replenishStock(id: String?, title: String?, message: String?)
}

class FakeMedicationRepository : MedicationRepository {
    companion object {
        private val _medications = MutableStateFlow(
            listOf(
                MedicationItem(
                    id = "1",
                    title = "Amoxicilina",
                    dosage = "500 mg",
                    quantity = "2 cx",
                    frequency = "8 em 8 horas",
                    category = "Antibiótico",
                    isAvailable = true,
                    instructions = "Tomar após as refeições.",
                ),
                MedicationItem(
                    id = "2",
                    title = "Losartana Potássica",
                    dosage = "50 mg",
                    quantity = "1 cx",
                    frequency = "1x ao dia",
                    category = "Uso Contínuo",
                    isAvailable = false,
                    daysUntilAvailable = 3,
                    instructions = "Tomar pela manhã com água.",
                ),
                MedicationItem(
                    id = "3",
                    title = "Dipirona Sódica",
                    dosage = "500 mg",
                    quantity = "3 uni",
                    frequency = "Se houver dor ou febre",
                    category = "Analgesico",
                    isAvailable = false,
                    daysUntilAvailable = 4,
                    instructions = "Máximo 4 vezes ao dia.",
                ),
                MedicationItem(
                    id = "4",
                    title = "Omeprazol",
                    dosage = "20 mg",
                    quantity = "2 cx",
                    frequency = "1x ao dia em jejum",
                    category = "Gastroprotetor",
                    isAvailable = true,
                    instructions = "Tomar 30 minutos antes do café.",
                ),
                MedicationItem(
                    id = "5",
                    title = "Sinvastatina",
                    dosage = "20 mg",
                    quantity = "1 cx",
                    frequency = "1x à noite",
                    category = "Uso Contínuo",
                    isAvailable = true,
                    instructions = "Tomar antes de dormir.",
                ),
            )
        )
    }

    override fun getMedications(): Flow<List<MedicationItem>> = _medications.asStateFlow()

    override suspend fun replenishStock(id: String?, title: String?, message: String?) {
        _medications.update { list ->
            list.map { medication ->
                val isMatching = (id != null && medication.id == id) ||
                    (message?.contains(medication.title, ignoreCase = true) == true) ||
                    (title?.contains(medication.title, ignoreCase = true) == true)

                if (isMatching) {
                    medication.copy(
                        isAvailable = true,
                        daysUntilAvailable = 0,
                        quantity = medication.quantity.ifBlank { "2 cx" },
                    )
                } else medication
            }
        }
    }
}
