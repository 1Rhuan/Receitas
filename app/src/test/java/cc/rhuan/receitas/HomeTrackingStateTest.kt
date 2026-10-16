package cc.rhuan.receitas

import cc.rhuan.receitas.domain.model.*
import cc.rhuan.receitas.ui.feature.home.HomeUiState
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class HomeTrackingStateTest {
    private val today = LocalDate.now()
    private val recipe = PrescriptionItem("p", "Receita", "", issueDate = today, customValidityDays = 10)

    @Test fun archivedAndReplacedRecipesAreAbsentFromFutureAgendaAndIndicators() {
        val recipes = listOf(recipe.copy(state = RecordState.ARCHIVED), recipe.copy(id = "replaced", state = RecordState.REPLACED))
        val state = HomeUiState(texts = TestTexts, prescriptions = recipes)
        assertTrue(state.futureTrackingItems.isEmpty())
        assertTrue(state.copy(selectedDate = recipe.expirationDate).mainTrackingItems.isEmpty())
        assertNull(state.getEventIndicatorForDate(recipe.expirationDate))
    }

    @Test fun activeRecipeRemainsVisibleInFutureAgendaAndOnItsDate() {
        val state = HomeUiState(texts = TestTexts, prescriptions = listOf(recipe))
        assertEquals("p", state.futureTrackingItems.single().targetId)
        assertEquals("p", state.copy(selectedDate = recipe.expirationDate).mainTrackingItems.single().targetId)
        assertNotNull(state.getEventIndicatorForDate(recipe.expirationDate))
    }

    @Test fun archivedAndDisabledMedicationsDoNotAppearInFutureAgenda() {
        val medication = MedicationItem("m", "Medicamento", lastRefillDate = today, refillCycleDays = 10)
        val state = HomeUiState(texts = TestTexts, medications = listOf(medication.copy(trackingArchived = true), medication.copy(id = "disabled", hasRecurringPickup = false)))
        assertTrue(state.futureTrackingItems.isEmpty())
        assertTrue(state.copy(selectedDate = today.plusDays(10)).mainTrackingItems.isEmpty())
        assertNull(state.getEventIndicatorForDate(today.plusDays(10)))
    }
}
