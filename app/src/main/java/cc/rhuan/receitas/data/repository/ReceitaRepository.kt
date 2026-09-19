package cc.rhuan.receitas.data.repository

import cc.rhuan.receitas.domain.model.ReceitaItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface ReceitaRepository {
    fun getReceitas(): Flow<List<ReceitaItem>>
}

class FakeReceitaRepository : ReceitaRepository {
    private val sampleReceitas = listOf(
        ReceitaItem(
            id = "1",
            title = "Receita Cardiologia",
            specialty = "Cardiologista",
            doctorName = "Dr. Carlos Eduardo",
            daysUntilExpiration = 0,
            issueDate = "10/01/2025",
            notes = "Uso contínuo de anti-hipertensivo."
        ),
        ReceitaItem(
            id = "2",
            title = "Tratamento Dermatológico",
            specialty = "Dermatologista",
            doctorName = "Dra. Ana Paula",
            daysUntilExpiration = 2,
            issueDate = "25/01/2025",
            notes = "Pomada dermatológica à noite."
        ),
        ReceitaItem(
            id = "3",
            title = "Checkup Clínico Geral",
            specialty = "Clínico Geral",
            doctorName = "Dr. Marcos Vinicius",
            daysUntilExpiration = 15,
            issueDate = "01/02/2025",
            notes = "Suplementação de Vitamina D."
        ),
        ReceitaItem(
            id = "4",
            title = "Receita Pediatria",
            specialty = "Pediatra",
            doctorName = "Dra. Camila Santos",
            daysUntilExpiration = 56,
            issueDate = "14/02/2025",
            notes = "Xarope para tosse."
        ),
        ReceitaItem(
            id = "5",
            title = "Avaliação Ortopédica",
            specialty = "Ortopedista",
            doctorName = "Dr. Lucas Mendes",
            daysUntilExpiration = -5,
            issueDate = "15/12/2024",
            notes = "Anti-inflamatório para dor no joelho."
        )
    )

    override fun getReceitas(): Flow<List<ReceitaItem>> {
        return flowOf(sampleReceitas)
    }
}
