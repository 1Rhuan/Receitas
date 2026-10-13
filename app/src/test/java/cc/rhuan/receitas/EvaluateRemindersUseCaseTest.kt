package cc.rhuan.receitas

import cc.rhuan.receitas.data.local.entity.MedicationEntity
import cc.rhuan.receitas.data.local.entity.PrescriptionEntity
import cc.rhuan.receitas.domain.usecase.EvaluateRemindersUseCase
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class EvaluateRemindersUseCaseTest {
    private val today = LocalDate.of(2026, 10, 13)
    private val evaluate = EvaluateRemindersUseCase()
    private fun prescription(daysUntilExpiry: Long) = PrescriptionEntity("p", "Receita", validityDays = 30, issueDate = today.plusDays(daysUntilExpiry).minusDays(30))
    private fun medication(daysUntilPickup: Long) = MedicationEntity("m", "Medicamento", hasRecurringPickup = true, lastPickupDate = today.plusDays(daysUntilPickup).minusDays(30), currentStock = 100)

    @Test fun renewalStartsAtConfiguredBoundary() {
        assertTrue(evaluate(listOf(prescription(6)), emptyList(), today).isEmpty())
        assertTrue(evaluate(listOf(prescription(5)), emptyList(), today).single().id.endsWith(":renewal"))
        assertTrue(evaluate(listOf(prescription(5).copy(renewalReminderEnabled = false)), emptyList(), today).isEmpty())
    }
    @Test fun expirationHasSeparatePhases() {
        assertTrue(evaluate(listOf(prescription(0)), emptyList(), today).single().id.endsWith(":today"))
        assertTrue(evaluate(listOf(prescription(-1)), emptyList(), today).single().id.endsWith(":expired"))
    }
    @Test fun pickupUsesDateRegardlessOfStock() {
        assertTrue(evaluate(emptyList(), listOf(medication(4)), today).isEmpty())
        assertTrue(evaluate(emptyList(), listOf(medication(3)), today).single().id.endsWith(":upcoming"))
        assertTrue(evaluate(emptyList(), listOf(medication(0)), today).single().id.endsWith(":due"))
    }
    @Test fun incompleteAndNonRecurringPickupsAreIgnored() {
        val m = medication(0)
        assertTrue(evaluate(emptyList(), listOf(m.copy(hasRecurringPickup = false), m.copy(lastPickupDate = null), m.copy(pickupIntervalDays = 0)), today).isEmpty())
    }
    @Test fun sameCycleDoesNotGenerateNewIdsEveryDay() {
        val p = prescription(-1)
        val m = medication(-1)
        assertEquals(evaluate(listOf(p), listOf(m), today).map { it.id }, evaluate(listOf(p), listOf(m), today.plusDays(1)).map { it.id })
        assertNotEquals(evaluate(listOf(p), emptyList(), today).single().id, evaluate(listOf(p.copy(issueDate = p.issueDate.plusDays(1))), emptyList(), today).single().id)
    }
}
