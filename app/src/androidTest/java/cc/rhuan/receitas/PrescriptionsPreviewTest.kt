package cc.rhuan.receitas

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.test.platform.app.InstrumentationRegistry
import cc.rhuan.receitas.ui.feature.prescriptions.*
import cc.rhuan.receitas.ui.navigation.TextNavigationBar
import cc.rhuan.receitas.ui.navigation.MainScreenContent
import cc.rhuan.receitas.ui.theme.*
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import java.io.File

class PrescriptionsPreviewTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private fun capture(name: String) {
        compose.waitForIdle()
        val bitmap = compose.captureActiveRoot()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.getExternalFilesDir(null), name + ".png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        for (command in listOf("mkdir -p /sdcard/Download/receitas-preview-v3",
            "cp " + file.absolutePath + " /sdcard/Download/receitas-preview-v3/" + name + ".png")) {
            val descriptor = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
            android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
        }
    }
    private fun show(dark: Boolean = false, small: Boolean = false, scale: Float = 1f,
        data: PrescriptionsUiState = designPreviewState(TestTexts), onCreate: () -> Unit = {}) {
        compose.setContent {
            AppTheme(darkTheme = dark) {
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, scale)) {
                DesignTheme {
                    Box(if (small) Modifier.requiredSize(320.dp, 640.dp) else Modifier.fillMaxSize()) {
                        MainScreenContent("prescriptions", true, {}) { padding ->
                            PrescriptionsScreenContent(data, {}, {}, Modifier.padding(padding), onCreatePrescriptionClick = onCreate)
                        }
                    }
                }
                }
            }
        }
        compose.waitForIdle()
    }
    @Test fun lightPreviewAndLongRecipe() {
        val sample = designPreviewState(TestTexts)
        show(data = sample.copy(prescriptions = sample.prescriptions + List(4) { index ->
            sample.prescriptions.last().copy(id = "scroll-extra-$index", title = "Receita adicional $index")
        }))
        compose.onNodeWithText("Próxima compra").assertDoesNotExist()
        capture("receitas-light")
        compose.onNodeWithTag("prescription-list").performScrollToNode(hasText("Tratamento de uso contínuo"))
        compose.onNodeWithText("Tratamento de uso contínuo").assertIsDisplayed()
        compose.onNodeWithTag("prescription-list").performTouchInput { swipeUp() }
        compose.onNodeWithTag("prescription-list").performScrollToIndex(4)
        compose.onNodeWithTag("floating-list-toolbar").assertIsDisplayed()
        capture("receitas-scrolled")
    }
    @Test fun darkPreview() { show(dark = true); capture("receitas-dark") }
    @Test fun smallPreview() {
        show(small = true)
        compose.onNodeWithText("Buscar receitas").assertIsDisplayed()
        capture("receitas-small")
        compose.onNodeWithTag("prescription-list").performScrollToNode(hasText("Acompanhamento e controle da pressão arterial"))
        compose.onNodeWithText("Acompanhamento e controle da pressão arterial", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
    }
    @Test fun doubleFontPreview() {
        show(small = true, scale = 2f)
        capture("receitas-font-200")
        compose.onNodeWithContentDescription("Nova receita").assertDoesNotExist()
        compose.onNodeWithTag("prescription-list").performScrollToNode(hasTestTag("record-search"))
        compose.onNodeWithTag("record-search").assertIsDisplayed()
        capture("receitas-font-200-search")
        compose.onNodeWithTag("prescription-list").performScrollToNode(hasText("Acompanhamento e controle da pressão arterial"))
        compose.onNodeWithText("Acompanhamento e controle da pressão arterial", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
        capture("receitas-font-200-list")
    }
    @Test fun callbacksRemainAvailable() {
        var created = 0
        var purchased = ""
        var opened = ""
        compose.setContent { AppTheme {
            DesignTheme { Scaffold(bottomBar = { TextNavigationBar("prescriptions", {}, { created++ }) }) { padding ->
            PrescriptionsScreenContent(designPreviewState(TestTexts), {}, {}, Modifier.padding(padding),
                onCreatePrescriptionClick = { created++ }, onPurchaseClick = { purchased = it.id },
                onPrescriptionClick = { opened = it.id })
            } }
        } }
        compose.onNodeWithContentDescription("Criar receita").performClick()
        assertEquals(1, created)
        compose.onNodeWithText("Próxima compra").assertDoesNotExist()
        assertEquals("", purchased)
        compose.onNodeWithTag("prescription-list").performScrollToNode(hasText("Tratamento de uso contínuo"))
        compose.onNodeWithText("Tratamento de uso contínuo").performClick()
        assertEquals("preview-1", opened)
    }
    @Test fun searchAndClearRecoverTheList() {
        compose.setContent { AppTheme {
            var state by remember { mutableStateOf(designPreviewState(TestTexts)) }
            PrescriptionsScreenContent(state, { state = state.copy(searchQuery = it) },
                { state = state.copy(selectedFilter = it) })
        } }
        compose.onNodeWithTag("prescription-list").performScrollToNode(hasTestTag("record-search"))
        compose.onNodeWithTag("record-search").performTextInput("termo inexistente")
        compose.runOnIdle {
            (compose.activity.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager)
                .hideSoftInputFromWindow(compose.activity.window.decorView.windowToken, 0)
        }
        compose.waitForIdle()
        compose.onNodeWithTag("prescription-list").performScrollToNode(hasText("Limpar busca e filtros"))
        capture("receitas-no-results")
        compose.onNodeWithText("Limpar busca e filtros").performClick()
        compose.onNodeWithTag("prescription-list").performScrollToNode(hasText("Tratamento de uso contínuo"))
        compose.onNodeWithText("Tratamento de uso contínuo").assertIsDisplayed()
    }
    @Test fun emptyLibraryHasOnePageCreationAction() {
        var created = 0
        show(data = PrescriptionsUiState(), onCreate = { created++ })
        compose.onNodeWithText("Nenhuma receita cadastrada").assertIsDisplayed()
        compose.onNodeWithTag("record-search").assertDoesNotExist()
        compose.onNodeWithText("Próxima compra").assertDoesNotExist()
        compose.onAllNodesWithContentDescription("Nova receita").assertCountEquals(1)
        capture("receitas-empty-light")
        compose.onNodeWithContentDescription("Nova receita").performClick()
        assertEquals(1, created)
    }
    @Test fun emptyLibraryDarkPreview() {
        show(dark = true, data = PrescriptionsUiState())
        capture("receitas-empty-dark")
    }
}
