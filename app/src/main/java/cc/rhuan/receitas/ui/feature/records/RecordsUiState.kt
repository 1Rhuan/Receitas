package cc.rhuan.receitas.ui.feature.records

import cc.rhuan.receitas.domain.model.ReceitaItem

enum class ReceitaFilter(val label: String) {
    ALL("Todas"),
    VALID("Válidas"),
    EXPIRED("Vencidas")
}

data class RecordsUiState(
    val receitas: List<ReceitaItem> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: ReceitaFilter = ReceitaFilter.ALL,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val filteredReceitas: List<ReceitaItem>
        get() = receitas.filter { receita ->
            val matchesSearch = searchQuery.isBlank() ||
                receita.title.contains(searchQuery, ignoreCase = true) ||
                receita.specialty.contains(searchQuery, ignoreCase = true) ||
                receita.doctorName.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                ReceitaFilter.ALL -> true
                ReceitaFilter.VALID -> !receita.isExpired
                ReceitaFilter.EXPIRED -> receita.isExpired
            }

            matchesSearch && matchesFilter
        }
}
