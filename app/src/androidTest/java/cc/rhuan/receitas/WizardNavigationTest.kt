package cc.rhuan.receitas

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import cc.rhuan.receitas.ui.feature.prescriptions.wizard.*
import cc.rhuan.receitas.ui.feature.prescriptions.*
import cc.rhuan.receitas.domain.model.*
import cc.rhuan.receitas.ui.theme.AppTheme
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class WizardNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private fun show(dark: Boolean = false, scale: Float = 1f, content: @Composable () -> Unit) {
        compose.setContent { AppTheme(darkTheme = dark) {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, scale)) {
                Box(Modifier.requiredSize(320.dp, 640.dp)) { content() }
            }
        } }
    }

    private fun scrollAboveAction(label: String, description: Boolean = false) {
        val node = (if (description) compose.onNodeWithContentDescription(label) else compose.onNodeWithText(label)).performScrollTo()
        val bounds = node.fetchSemanticsNode().boundsInRoot
        val footerTop = compose.onNodeWithTag("wizard-primary-action").fetchSemanticsNode().boundsInRoot.top
        if (bounds.bottom >= footerTop) compose.onNodeWithTag("wizard-content")
            .performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.ScrollBy) { it(0f, bounds.bottom - footerTop + bounds.height) }
        compose.waitForIdle()
        node.assertIsDisplayed()
        assertTrue(node.fetchSemanticsNode().boundsInRoot.bottom <= compose.onNodeWithTag("wizard-primary-action").fetchSemanticsNode().boundsInRoot.top)
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val bitmap = compose.captureActiveRoot()
        val file = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), "$name.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        listOf("mkdir -p /sdcard/Download/wizard-navigation", "cp ${file.absolutePath} /sdcard/Download/wizard-navigation/$name.png").forEach {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(it)).use { stream -> stream.readBytes() }
        }
    }

    @Test fun recipeNameIsRequiredAndIdentityHasNoSkip() {
        show {
            var state by remember { mutableStateOf(PrescriptionWizardUiState()) }
            PrescriptionWizardContent(state, WizardCallbacks(
                update = { state = it(state) },
                next = { if (state.currentStepError == null) state = state.copy(currentStep = 2) }))
        }
        compose.onNodeWithText("Pular").assertDoesNotExist()
        compose.onNodeWithText("Informe um nome para continuar.").assertDoesNotExist()
        compose.onNodeWithText("Avançar").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Nome da receita").performScrollTo().performTextInput("Acompanhamento")
        compose.onNodeWithText("Avançar").performClick()
        compose.onNodeWithText("Data de emissão").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Pular").assertDoesNotExist()
    }
    @Test fun largeFontKeepsSkipAtTopRightAndOpensRecipeReview() {
        var preserved = ""
        val longName = "Profissional com nome extenso para acompanhar o tratamento"
        show(dark = true, scale = 2f) {
            var state by remember { mutableStateOf(PrescriptionWizardUiState(currentStep = PrescriptionWizardUiState().optionalDetailsStep, doctorName = longName)) }
            PrescriptionWizardContent(state, WizardCallbacks(skip = {
                preserved = state.doctorName
                state = state.copy(currentStep = state.maxSteps)
            }))
        }
        val skip = compose.onNodeWithText("Pular").assertIsDisplayed().assertIsEnabled().fetchSemanticsNode().boundsInRoot
        val advance = compose.onNodeWithText("Avançar").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue(skip.bottom < compose.onNodeWithTag("wizard-content").fetchSemanticsNode().boundsInRoot.top)
        assertTrue(skip.left > compose.onRoot().fetchSemanticsNode().boundsInRoot.width / 2)
        scrollAboveAction("Adicionar Observações da receita", description = true)
        compose.onNodeWithContentDescription("Adicionar Observações da receita").performClick()
        compose.onNodeWithContentDescription("Observações da receita").assertExists()
        compose.onNodeWithText("Concluído").performClick()
        capture("navigation-dark-200")
        compose.onNodeWithText("Pular").performClick()
        assertEquals(longName, preserved)
        compose.onNodeWithText("Pular").assertDoesNotExist()
        compose.onNodeWithText("Salvar receita").assertIsDisplayed()
    }

    @Test fun recipeReviewContainsOnlyRecipeDataAtLargeFont() {
        show(dark = true, scale = 2f) {
            val state = PrescriptionWizardUiState(prescriptionTitle = "Receita de acompanhamento com nome longo")
            PrescriptionWizardContent(state.copy(currentStep = state.maxSteps), WizardCallbacks())
        }
        compose.onNodeWithText("Nome do medicamento").assertDoesNotExist()
        compose.onNodeWithText("Pular").assertDoesNotExist()
        compose.onNodeWithText("Salvar receita").assertIsDisplayed()
        compose.onNodeWithText("Medicamentos (0)").assertDoesNotExist()
        compose.onNodeWithText("Nenhum medicamento adicionado. Você pode incluí-los depois em Ver medicamentos.").assertDoesNotExist()
        capture("separate-recipe-review-dark-200")
    }
    @Test fun emptyRecipeListOffersAddAtLargeFont() {
        var added = false
        show(dark = true, scale = 2f) {
            PrescriptionMedicationList(PrescriptionDetailUiState(isLoading = false,
                prescription = PrescriptionItem("p", "Receita com nome longo para acompanhar o tratamento", "")), {}, {},
                onAddMedication = { added = true })
        }
        compose.onNodeWithText("Adicionar medicamento").performScrollTo().assertIsDisplayed().performClick()
        assertTrue(added)
        capture("optional-empty-list-dark-200")
        compose.onNodeWithText("0 medicamentos cadastrados").assertDoesNotExist()
        compose.onNodeWithText("Nenhum medicamento vinculado a esta receita.").performScrollTo().assertIsDisplayed()
    }

    @Test fun populatedRecipeListKeepsAddBeforeRecordsAtLargeFont() {
        var added = false
        val med = MedicationItem("m", "Medicamento com nome muito longo para testar a lista", dosage = "50 mg")
        show(scale = 2f) {
            PrescriptionMedicationList(PrescriptionDetailUiState(isLoading = false,
                prescription = PrescriptionItem("p", "Receita", "", medicationIds = listOf("m")),
                medications = listOf(PrescriptionMedicationDetail(med, RefillStatus.OK))), {}, {},
                onAddMedication = { added = true })
        }
        compose.onNodeWithText("Adicionar medicamento").performScrollTo().assertIsDisplayed().performClick()
        assertTrue(added)
        capture("optional-populated-list-light-200")
        compose.onNodeWithText(med.title).performScrollTo().assertIsDisplayed()
    }

    @Test fun pickupUsesSingleDaysFieldAndPreservesLegacyNextDateAtLargeFont() {
        show(dark = true, scale = 2f) {
            val med = MedicationDraft(title = "Medicamento", hasRecurringPickup = true,
                lastPickupDate = java.time.LocalDate.of(2026, 1, 31), hasPickupHistory = true,
                pickupIntervalDays = 1, intervalUnit = IntervalUnit.MONTHS)
            PrescriptionWizardContent(PrescriptionWizardUiState(entryMode = WizardEntryMode.STANDALONE_MEDICATION,
                currentStep = 2, showMedicationEditor = false, medications = listOf(med)), WizardCallbacks())
        }
        compose.onNodeWithText("Unidade do intervalo").assertDoesNotExist()
        listOf("Dias", "Semanas", "Meses", "Anos").forEach { compose.onNodeWithText(it).assertDoesNotExist() }
        compose.onNodeWithText("Intervalo entre retiradas (dias)").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("28").assertExists()
        scrollAboveAction("Próxima retirada prevista: 28/02/2026")
        capture("pickup-days-dark-200")
    }

    @Test fun contextualPickupShowsAutomaticOriginWithoutAskingAtLargeFont() {
        val title = "Receita de acompanhamento com nome longo para consulta"
        show(dark = true, scale = 2f) {
            var state by remember { mutableStateOf(PrescriptionWizardUiState(entryMode = WizardEntryMode.STANDALONE_MEDICATION,
                currentStep = 4, showMedicationEditor = false, addToPrescriptionId = "p", addToPrescriptionTitle = title,
                medications = listOf(MedicationDraft(title = "Medicamento", linkedPrescriptionId = "p", hasRecurringPickup = true,
                    lastPickupDate = java.time.LocalDate.of(2026, 1, 31))))) }
            PrescriptionWizardContent(state, WizardCallbacks(next = { state = state.copy(currentStep = state.maxSteps) }))
        }
        compose.onNodeWithText("Receita para retiradas").performScrollTo().assertIsDisplayed()
        scrollAboveAction(title)
        compose.onAllNodes(hasText(title) and hasClickAction()).assertCountEquals(0)
        compose.onNodeWithText("Sem receita para retiradas").assertDoesNotExist()
        capture("automatic-origin-context-dark-200")
        compose.onNodeWithText("Avançar").performClick()
        compose.onNodeWithText("Receita do medicamento").performScrollTo().assertIsDisplayed()
        scrollAboveAction("Receita para retiradas: $title")
        capture("automatic-origin-review-dark-200")
    }

    @Test fun reviewAndCompletionKeepTheirFinalActions() {
        show {
            var state by remember { mutableStateOf(PrescriptionWizardUiState(prescriptionTitle = "Receita de teste", currentStep = PrescriptionWizardUiState().maxSteps, showMedicationEditor = false, medications = listOf(MedicationDraft(title = "Medicamento de teste")))) }
            PrescriptionWizardContent(state, WizardCallbacks(save = { state = state.copy(isSaved = true) }))
        }
        compose.onNodeWithText("Salvar receita").assertIsDisplayed()
        compose.onNodeWithText("Avançar").assertDoesNotExist()
        compose.onNodeWithText("Pular").assertDoesNotExist()
        compose.onNodeWithText("Salvar receita").performClick()
        compose.onNodeWithText("Adicionar medicamentos").assertIsDisplayed()
        compose.onNodeWithText("Agora não").assertExists()
        compose.onNodeWithText("Pular").assertDoesNotExist()
    }
    @Test fun postRegistrationSuggestsMedicationAndAllowsDeferringAtLargeFont() {
        var added = false
        var deferred = false
        show(dark = true, scale = 2f) {
            PrescriptionWizardContent(PrescriptionWizardUiState(isSaved = true, savedPrescriptionId = "p",
                prescriptionTitle = "Receita de acompanhamento com nome longo"),
                WizardCallbacks(addSavedMedication = { added = true }, complete = { deferred = true }))
        }
        compose.onNodeWithText("Receita cadastrada").assertIsDisplayed()
        compose.onNodeWithText("Adicionar medicamentos").assertIsDisplayed().performClick()
        assertTrue(added)
        scrollAboveAction("Agora não")
        capture("post-registration-dark-200")
        compose.onNodeWithText("Agora não").performClick()
        assertTrue(deferred)
    }}
