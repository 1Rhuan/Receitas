package cc.rhuan.receitas.ui.feature.home

import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.domain.model.ReceitaItem

data class AlertItem(
    val id: String,
    val title: String,
    val description: String,
    val isCritical: Boolean,
)

data class HomeUiState(
    val userName: String = "Rhuan",
    val receitas: List<ReceitaItem> = emptyList(),
    val medicamentos: List<MedicationItem> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val dismissedAlerts: Set<String> = emptySet(),
    val errorMessage: String? = null,
) {

    val urgentReceitas: List<ReceitaItem>
        get() = receitas.asSequence().filter { it.daysUntilExpiration <= 30 }
            .sortedBy { it.daysUntilExpiration }.toList()

    val urgentMedications: List<MedicationItem>
        get() = medicamentos.sortedBy { if (it.isAvailable) 999 else it.daysUntilAvailable }

    val activeAlerts: List<AlertItem>
        get() = buildList {
            val expiredCount = receitas.count { it.isExpired }
            if (("receitas" !in dismissedAlerts) && (expiredCount > 0)) {
                add(
                    AlertItem(
                        id = "receitas",
                        title = "Receita Vencida",
                        description = "Você possui receitas ($expiredCount) que necessitam de renovação médica.",
                        isCritical = true,
                    )
                )
            }
            val unavailableCount = medicamentos.count { !it.isAvailable }
            if (("medications" !in dismissedAlerts) && (unavailableCount > 0)) {
                add(
                    AlertItem(
                        id = "medications",
                        title = "Reposição de Remédios",
                        description = "Você possui medicamentos ($unavailableCount) que precisam ser repostos.",
                        isCritical = false,
                    )
                )
            }
        }
}
