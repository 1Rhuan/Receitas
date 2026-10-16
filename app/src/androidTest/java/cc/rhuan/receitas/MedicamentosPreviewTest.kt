package cc.rhuan.receitas

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import cc.rhuan.receitas.domain.model.*
import cc.rhuan.receitas.ui.feature.medications.*
import cc.rhuan.receitas.ui.navigation.TextNavigationBar
import cc.rhuan.receitas.ui.navigation.MainScreenContent
import cc.rhuan.receitas.ui.theme.*
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.time.LocalDate

class MedicamentosPreviewTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private fun capture(name: String) {
        compose.waitForIdle()
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val bitmap = compose.captureActiveRoot()
        val file = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), name + ".png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        for (command in listOf("mkdir -p /sdcard/Download/medicamentos-preview",
            "cp " + file.absolutePath + " /sdcard/Download/medicamentos-preview/" + name + ".png")) {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(command)).use { it.readBytes() }
        }
    }
    private fun show(dark: Boolean = false, small: Boolean = false, scale: Float = 1f,
        data: MedicationsUiState = medicationPreviewState()) {
        compose.setContent { ReceitasTheme(darkTheme = dark) {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, scale)) {
                DesignTheme {
                    Box(if (small) Modifier.requiredSize(320.dp, 640.dp) else Modifier.fillMaxSize()) {
                        MainScreenContent("medications", true, {}, onCreateRecord = {}) { padding ->
                            MedicationsScreenContent(data, {}, {}, {}, {}, {}, remember { SnackbarHostState() }, Modifier.padding(padding))
                        }
                    }
                }
            }
        } }
    }
    @Test fun lightPreview() { show(); capture("medicamentos-light") }
    @Test fun darkPreview() { show(dark = true); capture("medicamentos-dark") }
    @Test fun emptyPreview() {
        show(data = MedicationsUiState())
        compose.onNodeWithText("Nenhum medicamento cadastrado").assertIsDisplayed()
        compose.onNodeWithTag("record-search").assertDoesNotExist()
        compose.onAllNodesWithContentDescription("Novo medicamento").assertCountEquals(1)
        capture("medicamentos-empty")
    }
    @Test fun noResultsCanClearQueryAndSource() = noResults(1f)
    @Test fun noResultsDoubleFontKeepsFiltersAndClearActionReachable() = noResults(2f)
    private fun noResults(scale: Float) {
        var query = "sem resultado"
        var source = MedicationSource.OUTRO.label
        compose.setContent { ReceitasTheme(darkTheme = scale > 1f) {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, scale)) {
                Box(Modifier.requiredSize(320.dp, 640.dp)) {
                    MedicationsScreenContent(medicationPreviewState().copy(searchQuery = query, selectedSourceFilter = source,
                        filteredMedications = emptyList()), { query = it }, { source = it }, {}, {}, {}, remember { SnackbarHostState() })
                }
            }
        } }
        compose.onNodeWithTag("record-search").assertIsDisplayed()
        compose.onNodeWithContentDescription("Filtros de medicamentos").assertIsDisplayed()
        compose.onNodeWithText("Seus medicamentos").assertIsDisplayed()
        compose.onNodeWithText("Novo medicamento").assertDoesNotExist()
        compose.onNodeWithTag("medication-list").performScrollToNode(hasText("Limpar busca e filtros"))
        capture("medicamentos-no-results-$scale")
        compose.onNodeWithText("Limpar busca e filtros").performClick()
        assertEquals("", query)
        assertEquals("TODOS", source)
    }
    @Test fun smallAndLongText() {
        show(small = true, scale = 2f)
        compose.onNodeWithTag("navigation-create").assertIsDisplayed().assertHeightIsAtLeast(48.dp)
        compose.onNodeWithContentDescription("Novo medicamento").assertDoesNotExist()
        capture("medicamentos-font-200")
        compose.onNodeWithTag("medication-list").performScrollToNode(hasText("Cloridrato de metformina de liberação prolongada"))
        compose.onNodeWithText("Cloridrato de metformina de liberação prolongada", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
        capture("medicamentos-font-200-list")
        val lastAction = compose.onAllNodesWithText("Registrar retirada", useUnmergedTree = true).onLast()
        lastAction.performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("medication-list").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.ScrollBy) { it(0f, 10000f) }
        compose.waitForIdle()
        val actionBounds = lastAction.fetchSemanticsNode().boundsInRoot
        val barBounds = compose.onNodeWithTag("floating-navigation").fetchSemanticsNode().boundsInRoot
        assertTrue("A última ação deve ficar acima da barra", actionBounds.bottom <= barBounds.top)
        capture("medicamentos-font-200-end")
    }
    @Test fun searchAndSourceCallbacks() {
        var query by mutableStateOf("")
        var source = ""
        var opened = ""
        var created = 0
        compose.setContent { ReceitasTheme {
            DesignTheme { Scaffold(bottomBar = { TextNavigationBar("medications", {}, { created++ }) }) { padding ->
            MedicationsScreenContent(medicationPreviewState().copy(searchQuery = query), { query = it }, { source = it }, {},
                { opened = it.id }, { created++ }, remember { SnackbarHostState() }, Modifier.padding(padding))
            } }
        } }
        compose.onNodeWithContentDescription("Criar medicamento").performClick()
        assertEquals(1, created)
        compose.onNodeWithTag("record-search").performTextInput("50")
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        compose.waitForIdle()
        assertEquals("50", query)
        compose.onNodeWithContentDescription("Filtros de medicamentos").performClick()
        compose.onNode(hasText("Compra própria") and hasAnyAncestor(hasTestTag("list-options-scroll"))).performScrollTo().performClick()
        assertEquals("", source)
        compose.onNodeWithText("Concluir").performClick()
        assertEquals(MedicationSource.COMPRA_PROPRIA.label, source)
        compose.onNodeWithTag("medication-list").performScrollToNode(hasText("Losartana potássica"))
        compose.onNodeWithText("Losartana potássica").performClick()
        assertEquals("med-preview-1", opened)
    }
    @Test fun pickupKeepsEarlyConfirmationAndCycle() {
        var registeredId = ""
        var registeredCycle: LocalDate? = null
        var confirmedEarly = false
        compose.setContent { ReceitasTheme {
            MedicationsPresentation(uiState = medicationPreviewState(),
                registerPickup = { id, _, early, cycle -> registeredId = id; confirmedEarly = early; registeredCycle = cycle },
                dismissSnackbar = {}, onSearchQueryChanged = {}, onSourceFilterSelected = {}, loadMedications = {})
        } }
        compose.onAllNodesWithText("Registrar retirada", useUnmergedTree = true).onFirst().performScrollTo().performClick()
        compose.onNodeWithText("Registrar", useUnmergedTree = true).performClick()
        assertEquals("", registeredId)
        compose.onNodeWithText("Confirmar antecipação", useUnmergedTree = true).performClick()
        assertEquals("med-preview-1", registeredId)
        assertTrue(confirmedEarly)
        assertEquals(LocalDate.now().plusDays(3), registeredCycle)
    }
}
