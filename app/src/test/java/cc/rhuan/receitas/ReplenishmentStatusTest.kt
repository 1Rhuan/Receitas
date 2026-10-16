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
 @Test fun duePickupUsesExplicitRecipe() {
  val med = MedicationItem("m", "Medicamento", linkedPrescriptionId = "p", hasRecurringPickup = true, lastRefillDate = today.minusDays(30))
  val old = recipe.copy(id = "old", issueDate = today.minusDays(365))
  assertTrue(check(med, listOf(old, recipe)) is RefillStatus.RefillAvailable)
 }
 @Test fun missingLinkNeverSelectsFirstRecipe() {
  val med = MedicationItem("m", "Medicamento", hasRecurringPickup = true, lastRefillDate = today)
  assertEquals(RefillStatus.NoPrescriptionLinked, check(med, listOf(recipe)))
 }
 @Test fun medicationInOneRecipeCanTrackAnotherExplicitRecipe() {
  val med = MedicationItem("m", "Medicamento", linkedPrescriptionId = "tracking", hasRecurringPickup = true, lastRefillDate = today.minusDays(30))
  val tracking = recipe.copy(id = "tracking")
  assertEquals(RefillStatus.NoPrescriptionLinked, check(med, listOf(recipe)))
  assertTrue(check(med, listOf(recipe, tracking)) is RefillStatus.RefillAvailable)
 }
 @Test fun noRecurringPickupDoesNotCreatePendingEvent() {
  val med = MedicationItem("m", "Medicamento", hasRecurringPickup = false)
  assertEquals(RefillStatus.OK, check(med, listOf(recipe)))
  assertNull(med.nextAvailableRefillDate)
  assertFalse(med.needsRefill)
  assertTrue(cc.rhuan.receitas.ui.feature.home.HomeUiState(texts = TestTexts, medications = listOf(med)).mainTrackingItems.isEmpty())
 }
 @Test fun validityStartsAtIssueDateAndIncludesExpiry() {
  assertFalse(recipe.isValidOn(today.minusDays(1)))
  assertTrue(recipe.isValidOn(recipe.expirationDate))
  assertFalse(recipe.isValidOn(recipe.expirationDate.plusDays(1)))
 }
 @Test fun calendarUnitsUseActualCalendar() {
  val date = LocalDate.of(2024, 2, 29)
  assertEquals(LocalDate.of(2025, 2, 28), PickupInterval(1, IntervalUnit.YEARS).nextAfter(date))
  assertEquals(date.plusDays(14), PickupInterval(2, IntervalUnit.WEEKS).nextAfter(date))
 }
}
