package cc.rhuan.receitas.data.repository

import cc.rhuan.receitas.domain.model.PrescriptionItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface PrescriptionRepository {
    fun getPrescriptions(): Flow<List<PrescriptionItem>>
    suspend fun renewPrescription(id: String?, title: String?, message: String?)
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
                    daysUntilExpiration = 0,
                    issueDate = "10/01/2025",
                    notes = "Uso contínuo de anti-hipertensivo.",
                ),
                PrescriptionItem(
                    id = "2",
                    title = "Tratamento Dermatológico",
                    specialty = "Dermatologista",
                    doctorName = "Dra. Ana Paula",
                    daysUntilExpiration = 2,
                    issueDate = "25/01/2025",
                    notes = "Pomada dermatológica à noite.",
                ),
                PrescriptionItem(
                    id = "3",
                    title = "Checkup Clínico Geral",
                    specialty = "Clínico Geral",
                    doctorName = "Dr. Marcos Vinicius",
                    daysUntilExpiration = 15,
                    issueDate = "01/02/2025",
                    notes = "Suplementação de Vitamina D.",
                ),
                PrescriptionItem(
                    id = "4",
                    title = "Receita Pediatria",
                    specialty = "Pediatra",
                    doctorName = "Dra. Camila Santos",
                    daysUntilExpiration = 56,
                    issueDate = "14/02/2025",
                    notes = "Xarope para tosse.",
                ),
                PrescriptionItem(
                    id = "5",
                    title = "Avaliação Ortopédica",
                    specialty = "Ortopedista",
                    doctorName = "Dr. Lucas Mendes",
                    daysUntilExpiration = -5,
                    issueDate = "15/12/2024",
                    notes = "Anti-inflamatório para dor no joelho.",
                ),
            )
        )
    }

    override fun getPrescriptions(): Flow<List<PrescriptionItem>> = _prescriptions.asStateFlow()

    override suspend fun renewPrescription(id: String?, title: String?, message: String?) {
        _prescriptions.update { list ->
            list.map { prescription ->
                val isMatching = (id != null && prescription.id == id) ||
                    (message?.contains(prescription.title, ignoreCase = true) == true) ||
                    (title?.contains(prescription.title, ignoreCase = true) == true)

                if (isMatching) {
                    prescription.copy(
                        daysUntilExpiration = 60,
                        notes = "Receita renovada recentemente",
                    )
                } else prescription
            }
        }
    }
}
