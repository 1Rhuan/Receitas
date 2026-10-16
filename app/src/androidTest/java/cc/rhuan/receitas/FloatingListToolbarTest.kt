package cc.rhuan.receitas

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import cc.rhuan.receitas.domain.model.*
import cc.rhuan.receitas.ui.feature.medications.*
import cc.rhuan.receitas.ui.feature.prescriptions.*
import cc.rhuan.receitas.ui.theme.ReceitasTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.File

class FloatingListToolbarTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private fun capture(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.getExternalFilesDir(null), "$name.png")
        file.outputStream().use { compose.captureActiveRoot().compress(Bitmap.CompressFormat.PNG, 100, it) }
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        listOf("mkdir -p /sdcard/Download/floating-list-tools", "cp ${file.absolutePath} /sdcard/Download/floating-list-tools/$name.png").forEach {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(it)).use { stream -> stream.readBytes() }
        }
    }
    private fun exercise(recipes: Boolean, scale: Float) {
        var query by mutableStateOf("")
        var source by mutableStateOf("TODOS")
        var status by mutableStateOf(PrescriptionFilter.ALL)
        var sort by mutableStateOf(PrescriptionSortOrder.EXPIRATION)
        val medications = List(12) { MedicationItem("m$it", "Medicamento de acompanhamento $it") }
        val prescriptions = List(12) { PrescriptionItem("r$it", "Receita de acompanhamento $it", "") }
        compose.setContent { ReceitasTheme(darkTheme = scale > 1f) {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, scale)) {
                Box(Modifier.requiredSize(320.dp, 640.dp)) {
                    if (recipes) PrescriptionsScreenContent(PrescriptionsUiState(prescriptions = prescriptions,
                        searchQuery = query, selectedFilter = status, selectedSortOrder = sort), { query = it }, { status = it },
                        onSortOrderSelected = { sort = it })
                    else MedicationsScreenContent(MedicationsUiState(medications, medications,
                        searchQuery = query, selectedSourceFilter = source), { query = it }, { source = it }, {}, {}, {},
                        remember { SnackbarHostState() })
                }
            }
        } }
        val list = if (recipes) "prescription-list" else "medication-list"
        val searchLabel = if (recipes) "Buscar receitas" else "Buscar medicamentos"
        val filterLabel = if (recipes) "Filtros e ordenação de receitas" else "Filtros de medicamentos"
        val headerTag = if (scale >= 1.5f) "page-app-bar" else "page-header"
        compose.onNodeWithTag(headerTag).assertIsDisplayed()
        compose.onNodeWithTag("floating-list-toolbar").assertDoesNotExist()
        compose.onNodeWithTag(list).performTouchInput { swipeUp() }
        compose.onNodeWithTag(list).performScrollToIndex(4)
        compose.onNodeWithTag("floating-list-toolbar").assertIsDisplayed()
        compose.onNodeWithTag(headerTag).assertIsNotDisplayed()
        capture("toolbar-$recipes-$scale")
        compose.onNode(hasContentDescription(searchLabel) and hasAnyAncestor(hasTestTag("floating-list-toolbar")))
            .assertHeightIsAtLeast(48.dp).performClick()
        compose.onNodeWithTag("floating-list-search").performTextInput(if (recipes) "Receita" else "Medicamento")
        assertEquals(if (recipes) "Receita" else "Medicamento", query)
        compose.onNodeWithContentDescription("Fechar busca").performClick()
        compose.onNode(hasContentDescription(filterLabel) and hasAnyAncestor(hasTestTag("floating-list-toolbar"))).performClick()
        compose.onNodeWithTag("list-option-0-1").performScrollTo().performClick()
        compose.onNodeWithText("Cancelar").performClick()
        assertEquals("TODOS", source)
        assertEquals(PrescriptionSortOrder.EXPIRATION, sort)
        compose.onNode(hasContentDescription(filterLabel) and hasAnyAncestor(hasTestTag("floating-list-toolbar"))).performClick()
        compose.onNodeWithTag("list-option-0-1").performScrollTo().performClick()
        compose.onNodeWithText("Concluir").performClick()
        if (recipes) assertEquals(PrescriptionSortOrder.NAME_ASC, sort)
        else assertEquals(MedicationSource.COMPRA_PROPRIA.label, source)
        compose.onNodeWithTag(list).performScrollToIndex(0)
        compose.onNodeWithTag(headerTag).assertIsDisplayed()
        compose.onNodeWithTag("floating-list-toolbar").assertDoesNotExist()
        compose.onNodeWithTag("record-search").assertTextContains(query)
    }
    @Test fun medicationScrollKeepsSearchAndFiltersAvailable() = exercise(false, 1f)
    @Test fun prescriptionDoubleFontScrollKeepsSearchAndFiltersAvailable() = exercise(true, 2f)
    @Test fun medicationDoubleFontScrollKeepsSearchAndFiltersAvailable() = exercise(false, 2f)
}
