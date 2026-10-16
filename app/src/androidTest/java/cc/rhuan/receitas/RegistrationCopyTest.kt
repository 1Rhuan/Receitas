package cc.rhuan.receitas

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import cc.rhuan.receitas.domain.model.IntervalUnit
import cc.rhuan.receitas.ui.feature.prescriptions.wizard.*
import cc.rhuan.receitas.ui.theme.ReceitasTheme
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test

class RegistrationCopyTest {
    @get:Rule val compose = createComposeRule()

    @Test fun monthlyPickupReviewUsesCalendarMonthAndSingularReminder() {
        val state = PrescriptionWizardUiState(
            entryMode = WizardEntryMode.STANDALONE_MEDICATION,
            currentStep = 5,
            showMedicationEditor = false,
            medications = listOf(MedicationDraft(
                title = "Medicamento de teste", hasRecurringPickup = true,
                lastPickupDate = LocalDate.of(2026, 1, 31),
                pickupIntervalDays = 1, intervalUnit = IntervalUnit.MONTHS,
                pickupReminderDaysBefore = 1,
            )),
        )
        compose.setContent { ReceitasTheme { PrescriptionWizardContent(state, WizardCallbacks()) } }
        compose.onNodeWithText("Próxima retirada prevista: 28/02/2026").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Lembrete: 1 dia antes").performScrollTo().assertIsDisplayed()
    }

    @Test fun editingSavedMedicationShowsUpdatedConfirmation() {
        val state = PrescriptionWizardUiState(
            entryMode = WizardEntryMode.STANDALONE_MEDICATION,
            editingMedicationId = "existing", isSaved = true,
            showMedicationEditor = false,
        )
        compose.setContent { ReceitasTheme { PrescriptionWizardContent(state, WizardCallbacks()) } }
        compose.onNodeWithText("Medicamento atualizado").assertIsDisplayed()
    }
}
