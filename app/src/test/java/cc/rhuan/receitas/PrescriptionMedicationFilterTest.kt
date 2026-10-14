package cc.rhuan.receitas

import cc.rhuan.receitas.domain.model.*
import cc.rhuan.receitas.ui.feature.prescriptions.*
import org.junit.Test
import org.junit.Assert.*

class PrescriptionMedicationFilterTest {
    private val member = PrescriptionItem("member", "Receita atual", "", medicationIds = listOf("med"))
    private val historical = PrescriptionItem("history", "Receita anterior", "", state = RecordState.REPLACED,
        items = listOf(PrescriptionMedication("med")))
    private val unrelated = PrescriptionItem("other", "Receita diferente", "")
    @Test fun medicationScopeIncludesMemberRecipesAndHistoryOnly() {
        val state = PrescriptionsUiState(prescriptions = listOf(member, historical, unrelated), medicationFilterId = "med")
        assertEquals(setOf("member", "history"), state.filteredPrescriptions.map { it.id }.toSet())
        assertEquals(3, state.copy(medicationFilterId = null).filteredPrescriptions.size)
        assertTrue(state.copy(medicationFilterId = "missing").filteredPrescriptions.isEmpty())
    }
    @Test fun medicationScopeCombinesWithSearchStatusAndSorting() {
        val state = PrescriptionsUiState(prescriptions = listOf(member, historical, unrelated), medicationFilterId = "med")
        assertEquals(listOf(historical), state.copy(selectedFilter = PrescriptionFilter.HISTORY).filteredPrescriptions)
        assertEquals(listOf(member), state.copy(searchQuery = "ATUAL").filteredPrescriptions)
        assertTrue(state.copy(searchQuery = "diferente").filteredPrescriptions.isEmpty())
        assertEquals(listOf(historical, member), state.copy(selectedSortOrder = PrescriptionSortOrder.NAME_ASC).filteredPrescriptions)
    }
}
