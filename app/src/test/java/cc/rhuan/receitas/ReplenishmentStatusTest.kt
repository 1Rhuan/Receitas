package cc.rhuan.receitas

import cc.rhuan.receitas.domain.model.*
import cc.rhuan.receitas.domain.usecase.CheckRefillStatusUseCase
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate

class ReplenishmentStatusTest {
 private val today = LocalDate.now()
 private val recipe = PrescriptionItem("p", "Receita", "", issueDate = today, medicationIds = listOf("m"), customValidityDays = 180)
 private val check = CheckRefillStatusUseCase()
 @Test fun pickupDueDoesNotDependOnStockCount() {
  val med = MedicationItem("m", "Medicamento", quantity = "2 caixas", stockQuantity = 500, hasRecurringPickup = true, lastRefillDate = today.minusDays(30))
  assertTrue(check(med, listOf(recipe)) is RefillStatus.RefillAvailable)
 }
 @Test fun upcomingPickupDoesNotWarnAboutLegacyLowStock() {
  val med = MedicationItem("m", "Medicamento", stockQuantity = 1, hasRecurringPickup = true, lastRefillDate = today)
  assertEquals(RefillStatus.OK, check(med, listOf(recipe)))
 }
 @Test fun noRecurringPickupDoesNotCreateStockReminder() {
  val med = MedicationItem("m", "Medicamento", stockQuantity = 0, hasRecurringPickup = false)
  assertEquals(RefillStatus.OK, check(med, listOf(recipe)))
  assertNull(med.nextAvailableRefillDate)
  assertFalse(med.needsRefill)
 }
}
