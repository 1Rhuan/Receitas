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
import cc.rhuan.receitas.domain.model.PrescriptionType
import cc.rhuan.receitas.ui.feature.prescriptions.wizard.*
import cc.rhuan.receitas.ui.theme.AppTheme
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class PrescriptionTypeChoiceTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val issueDate = LocalDate.of(2026, 10, 15)

    private fun show(dark: Boolean = false, scale: Float = 1f, onSelect: (PrescriptionType) -> Unit) {
        compose.setContent {
            AppTheme(darkTheme = dark) {
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, scale)) {
                    var state by remember { mutableStateOf(PrescriptionWizardUiState(currentStep = 2, issueDate = issueDate)) }
                    Box(Modifier.requiredSize(320.dp, 640.dp)) {
                        PrescriptionWizardPresentation(state, WizardCallbacks(
                            openTypeEditor = { state = state.copy(typeEditor = PrescriptionTypeDraft(state.type, state.validityDays, state.validityDaysInput, state.validityOrigin, state.validityDateMode)) },
                            cancelTypeEditor = { state = state.copy(typeEditor = null) },
                            selectType = { type ->
                                state = state.copy(typeEditor = state.typeEditor!!.copy(type = type,
                                    validityDays = type.defaultValidityDays, validityDaysInput = type.defaultValidityDays?.toString().orEmpty()))
                            },
                            update = { state = it(state) },
                            updateValidityDays = { value -> state = state.copy(typeEditor = state.typeEditor!!.copy(validityDaysInput = value, validityDays = value.toIntOrNull(), validityOrigin = "USER_ENTERED")) },
                            setValidityDateMode = { state = state.copy(typeEditor = state.typeEditor!!.copy(useExpirationDate = it)) },
                            confirmTypeEditor = {
                                val draft = state.typeEditor!!
                                onSelect(requireNotNull(draft.type))
                                state = state.copy(type = draft.type, validityDays = draft.validityDays, validityDaysInput = draft.validityDaysInput, validityOrigin = draft.validityOrigin, validityDateMode = draft.useExpirationDate, typeEditor = null, currentStep = 3)
                            },
                        ), onNavigateUp = {})
                    }
                }
            }
        }
    }

    private fun capture(name: String) {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val bitmap = runCatching { compose.captureActiveRoot() }.getOrElse {
            requireNotNull(automation.takeScreenshot())
        }
        val file = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), "$name.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        listOf("mkdir -p /sdcard/Download/recipe-type-choice", "cp ${file.absolutePath} /sdcard/Download/recipe-type-choice/$name.png").forEach {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(it)).use { stream -> stream.readBytes() }
        }
    }

    private fun openChoices() = clickFormOption("Validade")

    private fun scrollToEnd() {
        compose.onNodeWithTag("wizard-content").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.ScrollBy) { it(0f, 10000f) }
    }

    private fun assertAboveAction(text: String) {
        val node = compose.onNodeWithText(text).performScrollTo()
        val bounds = node.fetchSemanticsNode().boundsInRoot
        val actionTop = compose.onNodeWithTag("wizard-primary-action").fetchSemanticsNode().boundsInRoot.top
        if (bounds.bottom >= actionTop) compose.onNodeWithTag("wizard-content")
            .performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.ScrollBy) { it(0f, bounds.bottom - actionTop + bounds.height) }
        node.assertIsDisplayed()
        assertTrue("O conteúdo precisa ficar acima da ação flutuante", node.fetchSemanticsNode().boundsInRoot.bottom <= actionTop)
    }

    private fun clickFormOption(label: String) = clickVisible(compose.onNodeWithText(label))

    private fun chooseType(index: Int) = clickVisible(compose.onNodeWithTag("form-choice-option-$index"))

    private fun clickVisible(target: SemanticsNodeInteraction) {
        val node = target.performScrollTo()
        val bounds = node.fetchSemanticsNode().boundsInRoot
        val actionTop = compose.onNodeWithTag("wizard-primary-action").fetchSemanticsNode().boundsInRoot.top
        if (bounds.bottom >= actionTop) {
            compose.onNodeWithTag("wizard-content").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.ScrollBy) {
                it(0f, bounds.bottom - actionTop + bounds.height)
            }
        }
        node.performClick()
    }

    @Test fun presetTypesRequireAdvanceToApplyValidity() {
        var selected: PrescriptionType? = null
        show { selected = it }
        openChoices()
        compose.onNodeWithText("Qual é a validade da receita?").assertIsDisplayed()
        compose.onNodeWithTag("form-choice-option-0").assertIsSelected()
        compose.onNodeWithText("Para medicamentos usados de forma contínua." + "\n" + "Validade: 180 dias.").assertIsDisplayed()
        compose.onNodeWithText("Validade em dias").assertDoesNotExist()
        capture("type-page-light")
        chooseType(1)
        assertNull(selected)
        compose.onNodeWithTag("form-choice-option-1").assertIsSelected()
        compose.onNodeWithText("Avançar").performClick()
        assertEquals(PrescriptionType.ANTIBIOTIC, selected)
        compose.onNodeWithTag("form-choice-option-1").assertDoesNotExist()
        compose.onNodeWithText("Alertas de vencimento").assertIsDisplayed()
        compose.onNodeWithText("Receber alertas de validade e renovação").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("5 dias antes").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Validade em dias").assertDoesNotExist()
        capture("renewal-options-light")
    }

    @Test fun annualTypeContinuesToAlerts() {
        var selected: PrescriptionType? = null
        show { selected = it }
        openChoices()
        chooseType(PrescriptionType.ANNUAL_USE.ordinal)
        assertNull(selected)
        compose.onNodeWithText("Avançar").performClick()
        assertEquals(PrescriptionType.ANNUAL_USE, selected)
        compose.onNodeWithText("Alertas de vencimento").assertIsDisplayed()
        capture("annual-type-alerts")
    }

    @Test fun leavingCustomPageKeepsOriginalChoice() {
        var selected: PrescriptionType? = null
        show { selected = it }
        openChoices()
        chooseType(PrescriptionType.CUSTOM.ordinal)
        compose.onNodeWithContentDescription("Validade em dias").assertDoesNotExist()
        compose.onNodeWithText("Avançar").performClick()
        compose.onNodeWithContentDescription("Validade em dias").performScrollTo().performTextReplacement("45")
        compose.onNodeWithContentDescription("Voltar").performClick()
        assertNull(selected)
        compose.onNodeWithTag("form-choice-option-${PrescriptionType.CUSTOM.ordinal}").assertIsSelected()
        compose.onNodeWithContentDescription("Validade em dias").assertDoesNotExist()
        compose.onNodeWithText("Avançar").performClick()
        compose.onNodeWithContentDescription("Validade em dias").performScrollTo().assertTextContains("45")
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.onNodeWithText("180 dias").performScrollTo().assertIsDisplayed()
        openChoices()
        compose.onNodeWithTag("form-choice-option-0").assertIsSelected()
        compose.onNodeWithText("Validade em dias").assertDoesNotExist()
    }
    @Test fun darkLargeFontKeepsLastOptionAndConfirmationReachable() {
        var selected: PrescriptionType? = null
        show(dark = true, scale = 2f) { selected = it }
        openChoices()
        chooseType(PrescriptionType.CUSTOM.ordinal)
        compose.onNodeWithContentDescription("Validade em dias").assertDoesNotExist()
        compose.onNodeWithText("Avançar").performClick()
        compose.onNodeWithTag("form-choice-option-${PrescriptionType.CUSTOM.ordinal}").assertDoesNotExist()
        compose.onNodeWithText("Como definir a validade?").assertIsDisplayed()
        compose.onNodeWithText("Usar validade personalizada").assertIsDisplayed()
        compose.onNodeWithContentDescription("Validade em dias").performScrollTo().performTextReplacement("45")
        compose.runOnIdle { (compose.activity.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager).hideSoftInputFromWindow(compose.activity.window.decorView.windowToken, 0) }
        clickFormOption("Data de vencimento")
        compose.onNodeWithText("Vencimento personalizado").performScrollTo().assertIsDisplayed()
        scrollToEnd()
        assertAboveAction(issueDate.plusDays(45).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
        capture("type-page-dark-200")
        compose.onNodeWithText("Usar validade personalizada").performClick()
        assertEquals(PrescriptionType.CUSTOM, selected)
        compose.onNodeWithText("Alertas de vencimento").assertIsDisplayed()
        compose.onNodeWithText("Receber alertas de validade e renovação").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("5 dias antes").performScrollTo().assertIsDisplayed()
        scrollToEnd()
        assertAboveAction("15 dias antes")
        capture("renewal-options-dark-200")
        compose.onNodeWithText("Validade em dias").assertDoesNotExist()
    }
}
