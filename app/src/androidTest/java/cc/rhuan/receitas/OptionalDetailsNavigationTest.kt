package cc.rhuan.receitas

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import cc.rhuan.receitas.ui.feature.prescriptions.wizard.*
import cc.rhuan.receitas.ui.theme.ReceitasTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

class OptionalDetailsNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private fun show(base: PrescriptionWizardUiState, scale: Float = 1f, startAtOptional: Boolean = true) {
        compose.setContent {
            ReceitasTheme(darkTheme = scale > 1f) {
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, scale)) {
                    var state by remember { mutableStateOf(if (startAtOptional) base.copy(currentStep = base.optionalDetailsStep) else base) }
                    Box(Modifier.requiredSize(320.dp, 640.dp)) {
                        PrescriptionWizardContent(state, WizardCallbacks(update = { state = it(state) }))
                    }
                }
            }
        }
    }

    private fun capture(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.getExternalFilesDir(null), "$name.png")
        val bitmap = runCatching { compose.captureActiveRoot() }.getOrElse {
            requireNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        }
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun click(description: String) {
        val node = compose.onNodeWithContentDescription(description).performScrollTo()
        val footer = compose.onNodeWithTag("wizard-primary-action").fetchSemanticsNode().boundsInRoot.top
        val bounds = node.fetchSemanticsNode().boundsInRoot
        if (bounds.bottom >= footer) compose.onNodeWithTag("wizard-content")
            .performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.ScrollBy) { it(0f, bounds.bottom - footer + bounds.height) }
        node.performClick()
    }

    @Test fun recipeProfessionalUsesSeparateScreenAndConfirmsSummary() {
        show(PrescriptionWizardUiState(showMedicationEditor = false))
        capture("optional-final-recipe-light")
        compose.onNodeWithContentDescription("Nome do profissional").assertDoesNotExist()
        click("Adicionar Profissional responsável")
        compose.onNodeWithContentDescription("Adicionar Observações da receita").assertDoesNotExist()
        compose.onNodeWithContentDescription("Nome do profissional").performScrollTo().performTextInput("Dra. Ana")
        compose.onNodeWithText("Concluído").performClick()
        compose.onNodeWithContentDescription("Nome do profissional").assertDoesNotExist()
        compose.onNodeWithText("Dra. Ana").assertExists()
        click("Editar Profissional responsável")
        compose.onNodeWithContentDescription("Nome do profissional").assertTextContains("Dra. Ana")
    }

    @Test fun darkLargeFontMedicationEditorCancelsAndKeepsConfirmedValue() {
        show(PrescriptionWizardUiState(entryMode = WizardEntryMode.STANDALONE_MEDICATION,
            showMedicationEditor = false, medications = listOf(MedicationDraft(title = "Medicamento com nome extenso", activeIngredient = "Original"))), 2f)
        capture("optional-final-medication-dark-200")
        click("Editar Princípio ativo")
        compose.onNodeWithContentDescription("Princípio ativo").performScrollTo().performTextReplacement("Descartar")
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.onNodeWithText("Original").assertExists()
        compose.onNodeWithText("Descartar").assertDoesNotExist()
        click("Editar Princípio ativo")
        compose.onNodeWithContentDescription("Princípio ativo").performScrollTo().performTextReplacement("Confirmado")
        compose.onNodeWithText("Concluído").performClick()
        compose.onNodeWithText("Confirmado").assertExists()
    }

    @Test fun pharmaceuticalFormOpensItsOwnChoiceScreen() {
        show(PrescriptionWizardUiState(entryMode = WizardEntryMode.STANDALONE_MEDICATION,
            showMedicationEditor = false, medications = listOf(MedicationDraft(title = "Medicamento"))))
        click("Editar Forma farmacêutica")
        compose.onNodeWithContentDescription("Adicionar Princípio ativo").assertDoesNotExist()
        compose.onNodeWithText("Cápsula").performScrollTo().performClick()
        compose.onNodeWithText("Concluído").performClick()
        compose.onNodeWithText("Cápsula").assertExists()
    }

    @Test fun pageTitleReplacesRegistrationTitleAndCloseIcon() {
        show(PrescriptionWizardUiState(currentStep = 2), startAtOptional = false)
        compose.onNodeWithText("Nova receita").assertDoesNotExist()
        compose.onNodeWithContentDescription("Sair do cadastro").assertDoesNotExist()
        compose.onNodeWithText("Etapa 2 de 6").assertDoesNotExist()
        val title = compose.onNodeWithText("Emissão e tipo").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue(title.bottom < compose.onNodeWithTag("wizard-content").fetchSemanticsNode().boundsInRoot.top)
        capture("registration-header-title-light")
    }
}
