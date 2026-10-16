package cc.rhuan.receitas

import androidx.activity.ComponentActivity
import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.time.LocalDate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import cc.rhuan.receitas.ui.feature.prescriptions.wizard.*
import cc.rhuan.receitas.ui.theme.AppTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class MedicationRegistrationFlowTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private lateinit var parent: MutableState<PrescriptionWizardUiState>

    private fun show(dark: Boolean = false, scale: Float = 1f) {
        parent = mutableStateOf(PrescriptionWizardUiState(currentStep = 4, prescriptionTitle = "Receita de acompanhamento", showMedicationEditor = false))
        compose.setContent {
            AppTheme(darkTheme = dark) {
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, scale)) {
                    Box(Modifier.requiredSize(320.dp, 640.dp)) {
                        PrescriptionWizardContent(parent.value, WizardCallbacks(
                            update = { parent.value = it(parent.value) },
                            goToStep = { parent.value = parent.value.copy(currentStep = it) },
                        ))
                    }
                }
            }
        }
    }

    private fun click(text: String) {
        val node = compose.onNodeWithText(text)
        if (text != "Avançar" && text != "Concluir") node.performScrollTo()
        node.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
    }

    private fun capture(name: String) {
        val bitmap = compose.captureActiveRoot()
        val file = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), "$name.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        listOf("mkdir -p /sdcard/Download/medication-registration", "cp ${file.absolutePath} /sdcard/Download/medication-registration/$name.png").forEach { command ->
            android.os.ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(command)).use { it.readBytes() }
        }
    }

    @Test fun completedChildReturnsToGroupedListAndRecipeDetailsStaySeparate() {
        show()
        click("Adicionar medicamento")
        compose.onNodeWithText("Qual é o nome do medicamento?").assertIsDisplayed()
        compose.onNode(hasSetTextAction()).performTextInput("Medicamento de nome longo para acompanhar")
        click("Avançar")
        compose.onNodeWithText("Quantidade por retirada").assertExists()
        click("Avançar")
        compose.onNodeWithText("Concentração").assertExists()
        click("Avançar")
        click("Concluir")
        compose.runOnIdle {
            assertEquals(1, parent.value.medications.size)
            assertEquals(4, parent.value.currentStep)
        }
        capture("medication-group-light")
        compose.onNodeWithContentDescription("Editar Medicamento de nome longo para acompanhar").performScrollTo().performClick()
        compose.onNode(hasSetTextAction()).assertTextContains("Medicamento de nome longo para acompanhar")
        compose.onNode(hasSetTextAction()).performTextReplacement("Alteração descartada")
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.runOnIdle { assertEquals("Medicamento de nome longo para acompanhar", parent.value.medications.single().title) }
        click("Avançar")
        compose.onNodeWithText("Profissional responsável").assertExists()
        compose.onNodeWithText("Concentração").assertDoesNotExist()
        compose.onNodeWithText("Forma farmacêutica").assertDoesNotExist()
        capture("recipe-details-light")
    }

    @Test fun cancelChildPreservesRecipeInDarkWithLargeFont() {
        show(dark = true, scale = 2f)
        compose.runOnIdle { parent.value = parent.value.copy(medications = listOf(
            MedicationDraft(id = "one", title = "Medicamento com um nome muito longo para conferir a leitura", dosageOverride = "500 mg"),
            MedicationDraft(id = "two", title = "Segundo medicamento", dosageOverride = "10 mg"))) }
        click("Adicionar medicamento")
        compose.onNode(hasSetTextAction()).performTextInput("Rascunho")
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.runOnIdle {
            assertEquals(listOf("one", "two"), parent.value.medications.map { it.id })
            assertEquals("Receita de acompanhamento", parent.value.prescriptionTitle)
        }
        compose.onNodeWithText("Adicionar medicamento").assertExists()
        val lastEdit = compose.onNodeWithContentDescription("Editar Segundo medicamento").performScrollTo()
        val footer = compose.onNodeWithTag("wizard-primary-action").fetchSemanticsNode().boundsInRoot.top
        val bounds = lastEdit.fetchSemanticsNode().boundsInRoot
        if (bounds.bottom >= footer) compose.onNodeWithTag("wizard-content")
            .performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.ScrollBy) { it(0f, bounds.bottom - footer + bounds.height) }
        assertTrue(lastEdit.fetchSemanticsNode().boundsInRoot.bottom < footer)
        capture("medication-group-dark-200")
    }

    @Test fun editingRecurringMedicationKeepsScheduleAndRequiresExplicitRecipeChoice() {
        show()
        compose.runOnIdle { parent.value = parent.value.copy(medications = listOf(MedicationDraft(
            id = "existing", title = "Medicamento", hasRecurringPickup = true, lastPickupDate = LocalDate.now(),
            initialPickupDate = LocalDate.now(), pickupIntervalDays = 30, pickupIntervalInput = "30"))) }
        compose.onNodeWithContentDescription("Editar Medicamento").performScrollTo().performClick()
        click("Avançar")
        compose.onNodeWithContentDescription("Intervalo entre retiradas (dias)").assertTextContains("30")
        click("Avançar")
        click("Avançar")
        compose.onNodeWithText("Sem receita para retiradas").performScrollTo().assertIsSelected()
        click("Usar esta receita")
        click("Avançar")
        click("Avançar")
        click("Concluir")
        compose.runOnIdle {
            val item = parent.value.medications.single()
            assertEquals("existing", item.id)
            assertEquals(30, item.pickupIntervalDays)
            assertTrue(item.linkToCurrentRecipe)
            assertNull(item.linkedPrescriptionId)
        }
    }
}
