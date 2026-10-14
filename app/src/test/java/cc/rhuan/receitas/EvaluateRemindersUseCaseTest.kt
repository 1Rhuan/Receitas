package cc.rhuan.receitas
import cc.rhuan.receitas.domain.model.*
import cc.rhuan.receitas.domain.usecase.EvaluateRemindersUseCase
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test
class EvaluateRemindersUseCaseTest {
    private val today = LocalDate.of(2026, 10, 13)
    private val evaluate = EvaluateRemindersUseCase()
    private fun prescription(days: Long) = PrescriptionItem("p", "Receita", "", customValidityDays = 30, issueDate = today.plusDays(days).minusDays(30))
    private fun medication(days: Long) = MedicationItem("m", "Medicamento", hasRecurringPickup = true, lastRefillDate = today.plusDays(days).minusDays(30))
    @Test fun renewalStartsAtConfiguredBoundary() {
        assertTrue(evaluate(listOf(prescription(6)), emptyList(), today).isEmpty())
        assertEquals(ReminderPhase.RENEWAL, evaluate(listOf(prescription(5)), emptyList(), today).single().phase)
        assertTrue(evaluate(listOf(prescription(5).copy(renewalReminderEnabled = false)), emptyList(), today).isEmpty())
    }
    @Test fun expirationHasSeparatePhases() {
        assertEquals(ReminderPhase.EXPIRES_TODAY, evaluate(listOf(prescription(0)), emptyList(), today).single().phase)
        assertEquals(ReminderPhase.EXPIRED, evaluate(listOf(prescription(-1)), emptyList(), today).single().phase)
    }
    @Test fun pickupUsesConfiguredBoundary() {
        assertTrue(evaluate(emptyList(), listOf(medication(4)), today).isEmpty())
        assertEquals(ReminderPhase.PICKUP_UPCOMING, evaluate(emptyList(), listOf(medication(3)), today).single().phase)
        assertEquals(ReminderPhase.PICKUP_DUE, evaluate(emptyList(), listOf(medication(0)), today).single().phase)
    }
    @Test fun incompleteArchivedAndNonRecurringPickupsAreIgnored() {
        val m = medication(0)
        assertTrue(evaluate(emptyList(), listOf(m.copy(hasRecurringPickup = false), m.copy(lastRefillDate = null), m.copy(refillCycleDays = 0), m.copy(trackingArchived = true)), today).isEmpty())
    }
    @Test fun replacedAndArchivedRecipesDoNotGenerateAlerts() {
        assertTrue(evaluate(listOf(prescription(-1).copy(state = RecordState.REPLACED), prescription(0).copy(state = RecordState.ARCHIVED)), emptyList(), today).isEmpty())
    }
    @Test fun sameCycleDoesNotGenerateNewIdsEveryDay() {
        val p = prescription(-1); val m = medication(-1)
        assertEquals(evaluate(listOf(p), listOf(m), today).map { it.id }, evaluate(listOf(p), listOf(m), today.plusDays(1)).map { it.id })
    }
    @Test fun monthIntervalRespectsCalendar() {
        val m = MedicationItem("m", "Medicamento", lastRefillDate = LocalDate.of(2026, 1, 31), hasRecurringPickup = true, refillCycleDays = 1, intervalUnit = IntervalUnit.MONTHS)
        assertEquals(LocalDate.of(2026, 2, 28), m.nextAvailableRefillDate)
        assertEquals(ReminderPhase.PICKUP_DUE, evaluate(emptyList(), listOf(m), LocalDate.of(2026, 2, 28)).single().phase)
    }
}
