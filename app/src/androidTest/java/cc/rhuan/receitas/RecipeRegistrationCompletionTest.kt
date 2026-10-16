package cc.rhuan.receitas

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import cc.rhuan.receitas.domain.model.RecordState
import cc.rhuan.receitas.ui.feature.prescriptions.wizard.PrescriptionWizardContent
import cc.rhuan.receitas.ui.feature.prescriptions.wizard.PrescriptionWizardUiState
import cc.rhuan.receitas.ui.feature.prescriptions.wizard.WizardCallbacks
import cc.rhuan.receitas.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class RecipeRegistrationCompletionTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private fun show(state: PrescriptionWizardUiState, actions: WizardCallbacks, largeText: Boolean = false) {
        compose.setContent {
            AppTheme(darkTheme = largeText) {
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, if (largeText) 2f else 1f)) {
                    Box(Modifier.requiredSize(320.dp, 640.dp)) { PrescriptionWizardContent(state, actions) }
                }
            }
        }
    }

    @Test fun recipeOptionalDetailsDoNotContainTheRemovedMedicationEditor() {
        val state = PrescriptionWizardUiState(prescriptionTitle = "Receita", showMedicationEditor = false)
        show(state.copy(currentStep = state.optionalDetailsStep), WizardCallbacks())
        compose.onNodeWithText("Profissional responsável").assertExists()
        compose.onNodeWithText("Adicionar medicamento").assertDoesNotExist()
        compose.onNodeWithText("Concentração").assertDoesNotExist()
    }

    @Test fun savedRecipeOffersSeparateMedicationRegistration() {
        var added = 0
        var completed = 0
        show(PrescriptionWizardUiState(isSaved = true, savedPrescriptionId = "recipe", prescriptionTitle = "Receita salva"),
            WizardCallbacks(addSavedMedication = { added++ }, complete = { completed++ }))
        compose.onNodeWithText("Receita salva").assertExists()
        compose.onNodeWithText("Adicionar medicamentos").performClick()
        compose.runOnIdle { assertEquals(1, added); assertEquals(0, completed) }
        compose.onNodeWithText("Agora não").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, completed) }
    }

    @Test fun archivedRecipeCompletionDoesNotOfferMedicationCreation() {
        var completed = 0
        show(PrescriptionWizardUiState(isSaved = true, savedPrescriptionId = "recipe", prescriptionTitle = "Receita arquivada",
            recordState = RecordState.ARCHIVED), WizardCallbacks(complete = { completed++ }), largeText = true)
        compose.onNodeWithText("Adicionar medicamentos").assertDoesNotExist()
        compose.onNodeWithText("Ver receita").performClick()
        compose.runOnIdle { assertEquals(1, completed) }
    }
}
