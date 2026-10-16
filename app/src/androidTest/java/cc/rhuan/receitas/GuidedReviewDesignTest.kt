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
import androidx.test.platform.app.InstrumentationRegistry
import cc.rhuan.receitas.domain.model.*
import cc.rhuan.receitas.ui.feature.prescriptions.wizard.*
import cc.rhuan.receitas.ui.theme.ReceitasTheme
import java.io.File
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class GuidedReviewDesignTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val firstName = "Losartana potássica de uso contínuo"
    private val secondName = "Medicamento sem concentração informada"
    private fun state(step: Int = 9) = PrescriptionWizardUiState(currentStep = step, prescriptionTitle = "Receita de acompanhamento",
        notes = "Observações pertencentes à receita, mesmo sem profissional cadastrado.", showMedicationEditor = false,
        medications = listOf(
            MedicationDraft(id = "a", title = firstName, dosageOverride = "50 mg", hasRecurringPickup = true,
                lastPickupDate = LocalDate.of(2026, 10, 1), pickupIntervalDays = 30, pickupReminderDaysBefore = 1,
                replenishmentQuantity = "2 caixas", sourceName = "UBS Central", linkToCurrentRecipe = true),
            MedicationDraft(id = "b", title = secondName, hasRecurringPickup = true,
                lastPickupDate = LocalDate.of(2026, 10, 2), pickupIntervalDays = 45, pickupReminderDaysBefore = 7,
                replenishmentQuantity = "1 frasco", sourceName = "Farmácia do bairro")))

    private fun show(dark: Boolean = false, scale: Float = 1f, content: @Composable () -> Unit) {
        compose.setContent { ReceitasTheme(darkTheme = dark) {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, scale)) {
                Box(Modifier.requiredSize(320.dp, 640.dp)) { content() }
            }
        } }
    }

    private fun reveal(node: SemanticsNodeInteraction) {
        node.performScrollTo()
        val bounds = node.fetchSemanticsNode().boundsInRoot
        val actionTop = compose.onNodeWithTag("wizard-primary-action").fetchSemanticsNode().boundsInRoot.top
        if (bounds.bottom > actionTop) compose.onNodeWithTag("wizard-content")
            .performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.ScrollBy) { it(0f, bounds.bottom - actionTop + bounds.height) }
        compose.waitForIdle()
        node.assertIsDisplayed()
        assertTrue(node.fetchSemanticsNode().boundsInRoot.bottom <= compose.onNodeWithTag("wizard-primary-action").fetchSemanticsNode().boundsInRoot.top)
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val root = compose.captureActiveRoot()
        val image = root
        val file = File(compose.activity.getExternalFilesDir(null), "$name.png")
        file.outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
        listOf("mkdir -p /sdcard/Download/guided-review", "cp ${file.absolutePath} /sdcard/Download/guided-review/$name.png").forEach {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(it)).use { stream -> stream.readBytes() }
        }
    }

    @Test fun reviewKeepsEveryEditAttachedToItsContent() {
        var editedStep = 0
        var editedMedication = -1
        var typeOpened = false
        show { PrescriptionWizardContent(state(), WizardCallbacks(goToStep = { editedStep = it },
            editMedication = { editedMedication = it }, openTypeEditor = { typeOpened = true })) }
        listOf("Nome da receita" to 1, "Data de emissão" to 2, "Alertas de vencimento" to 3,
            "Profissional responsável" to 4, "Observações da receita" to 4, "Medicamentos (2)" to 5,
            "Retiradas e lembretes" to 6).forEach { (label, step) ->
            val button = compose.onNodeWithContentDescription("Editar $label")
            reveal(button); button.performClick(); assertEquals(step, editedStep)
        }
        val typeButton = compose.onNodeWithContentDescription("Editar Tipo e validade da receita")
        reveal(typeButton); typeButton.performClick(); assertTrue(typeOpened)
        compose.onNodeWithTag("review-medication-0").assert(hasAnyDescendant(hasText(firstName)))
            .assert(hasAnyDescendant(hasText("50 mg")))
            .assert(hasAnyDescendant(hasContentDescription("Editar medicamento $firstName")))
        compose.onNodeWithTag("review-medication-1").assert(hasAnyDescendant(hasText(secondName)))
            .assert(hasAnyDescendant(hasContentDescription("Editar medicamento $secondName")))
            .assert(!hasAnyDescendant(hasText("50 mg")))
        val medicationButton = compose.onNodeWithContentDescription("Editar medicamento $secondName")
        reveal(medicationButton); medicationButton.performClick(); assertEquals(1, editedMedication)
        capture("review-medications-light")
        compose.onNodeWithTag("review-pickup-0").assert(hasAnyDescendant(hasText("Quantidade por retirada: 2 caixas")))
            .assert(hasAnyDescendant(hasText("Lembrete: 1 dia antes")))
            .assert(!hasAnyDescendant(hasText(secondName)))
        compose.onNodeWithTag("review-pickup-1").assert(hasAnyDescendant(hasText("Quantidade por retirada: 1 frasco")))
            .assert(hasAnyDescendant(hasText("Lembrete: 7 dias antes")))
            .assert(!hasAnyDescendant(hasText(firstName)))
        val context = compose.onNodeWithText("Editar local e receita para retiradas")
        reveal(context); context.performClick(); assertEquals(8, editedStep)
        val reminders = compose.onNodeWithText("Editar lembretes de retirada")
        reveal(reminders); reminders.performClick(); assertEquals(7, editedStep)
        capture("review-pickups-light")
    }

    @Test fun largeReviewSeparatesRecordsWithoutInventingDosage() {
        show(dark = true, scale = 2f) { PrescriptionWizardContent(state(), WizardCallbacks()) }
        reveal(compose.onNodeWithText(state().notes))
        capture("review-recipe-dark-200")
        reveal(compose.onNode(hasText(firstName) and hasAnyAncestor(hasTestTag("review-medication-0"))))
        capture("review-medication-dark-200")
        compose.onNodeWithTag("review-medication-1").assert(!hasAnyDescendant(hasText("Concentração não informada")))
        reveal(compose.onNode(hasText(firstName) and hasAnyAncestor(hasTestTag("review-pickup-0"))))
        capture("review-pickup-first-dark-200")
        reveal(compose.onNode(hasText(secondName) and hasAnyAncestor(hasTestTag("review-pickup-1"))))
        capture("review-pickup-second-dark-200")
        compose.onNodeWithText("Salvar receita").assertIsDisplayed()
    }

    @Test fun stepsSevenAndEightKeepEachMedicationAndItsSettingsTogether() {
        var latest = state(7)
        show(dark = true, scale = 2f) {
            var current by remember { mutableStateOf(latest) }
            PrescriptionWizardContent(current, WizardCallbacks(next = { current = current.copy(currentStep = current.currentStep + 1); latest = current },
                update = { transform -> current = transform(current); latest = current }))
        }
        compose.onNodeWithTag("pickup-options-0").assert(hasAnyDescendant(hasText(firstName)))
            .assert(!hasAnyDescendant(hasText(secondName)))
        compose.onNodeWithTag("pickup-options-1").assert(hasAnyDescendant(hasText(secondName)))
            .assert(!hasAnyDescendant(hasText(firstName)))
        val onDay = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.reminder_on_day)
        val reminder = compose.onNode(hasText(onDay) and hasAnyAncestor(hasTestTag("pickup-options-0")))
        reveal(reminder); reminder.performClick(); reminder.assertIsSelected()
        assertEquals(0, latest.medications[0].pickupReminderDaysBefore)
        assertEquals(7, latest.medications[1].pickupReminderDaysBefore)
        capture("reminder-radio-dark-200")
        reveal(compose.onNode(hasText(secondName) and hasAnyAncestor(hasTestTag("pickup-options-1"))))
        capture("step-seven-dark-200")
        compose.onNodeWithText("Avançar").performClick()
        compose.onNodeWithTag("pickup-context-0").assert(hasAnyDescendant(hasText("UBS Central")))
            .assert(!hasAnyDescendant(hasText("Farmácia do bairro")))
        compose.onNodeWithTag("pickup-context-1").assert(hasAnyDescendant(hasText("Farmácia do bairro")))
            .assert(!hasAnyDescendant(hasText("UBS Central")))
        val source = compose.onNode(hasText("Farmácia Popular") and hasAnyAncestor(hasTestTag("pickup-context-0")))
        reveal(source); source.performClick(); source.assertIsSelected()
        assertEquals(MedicationSource.FARMACIA_POPULAR, latest.medications[0].source)
        assertEquals(MedicationSource.COMPRA_PROPRIA, latest.medications[1].source)
        capture("source-radio-dark-200")
        val firstPlace = compose.onNode(hasText("UBS Central") and hasAnyAncestor(hasTestTag("pickup-context-0")))
        reveal(firstPlace); firstPlace.performTextReplacement("UBS Nova")
        compose.runOnIdle { (compose.activity.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager)
            .hideSoftInputFromWindow(compose.activity.window.decorView.windowToken, 0) }
        assertEquals("UBS Nova", latest.medications[0].sourceName)
        assertEquals("Farmácia do bairro", latest.medications[1].sourceName)
        reveal(compose.onNode(hasText(secondName) and hasAnyAncestor(hasTestTag("pickup-context-1"))))
        capture("step-eight-dark-200")
        compose.onNodeWithText("Avançar").performClick()
        assertEquals(9, latest.currentStep)
        compose.onNodeWithTag("review-pickup-0").assert(hasAnyDescendant(hasText("Local: UBS Nova")))
        compose.onNodeWithTag("review-pickup-1").assert(hasAnyDescendant(hasText("Local: Farmácia do bairro")))
    }
}
