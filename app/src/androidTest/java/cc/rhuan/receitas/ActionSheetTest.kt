package cc.rhuan.receitas

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.ui.components.*
import cc.rhuan.receitas.ui.theme.ReceitasTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.time.LocalDate

class ActionSheetTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private fun show(scale: Float = 1f, content: @Composable () -> Unit) {
        compose.setContent { ReceitasTheme(darkTheme = scale > 1f) {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, scale)) {
                Box(Modifier.fillMaxSize()) { content() }
            }
        } }
    }
    private fun capture(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.getExternalFilesDir(null), "$name.png")
        val bitmap = compose.captureActiveRoot()
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        listOf("mkdir -p /sdcard/Download/action-sheets", "cp ${file.absolutePath} /sdcard/Download/action-sheets/$name.png").forEach {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(it)).use { stream -> stream.readBytes() }
        }
    }
    @Composable
    private fun SheetFixture(title: String, onConfirm: () -> Unit, onDismiss: () -> Unit,
        onView: (android.view.View) -> Unit, onBodyMarker: (android.view.View) -> Unit = {},
        onFooterMarker: (android.view.View) -> Unit = {}) {
        ActionSheet(title, onDismiss, actions = {
            SheetActionButton("Confirmar", onConfirm)
            GuidanceActionButton("Cancelar", onDismiss, Modifier.fillMaxWidth(), outlined = true)
            AndroidView(factory = { context -> android.view.View(context).also(onFooterMarker) }, modifier = Modifier.size(1.dp))
        }) {
            val view = LocalView.current
            SideEffect { onView(view) }
            Text("Os registros serão preservados.")
            AndroidView(factory = { context -> android.view.View(context).also(onBodyMarker) }, modifier = Modifier.size(1.dp))
        }
    }
    @Test fun shortConfirmationIsAnchoredBelowAndCancelDoesNotConfirm() {
        var open by mutableStateOf(true)
        var confirmed = 0
        var sheetView: android.view.View? = null
        var bodyMarker: android.view.View? = null
        var footerMarker: android.view.View? = null
        show {
            Text("Tela de notificações")
            if (open) SheetFixture("Limpar notificações?", { confirmed++; open = false }, { open = false },
                { sheetView = it }, { bodyMarker = it }, { footerMarker = it })
        }
        compose.onNodeWithTag("action-sheet").assertIsDisplayed()
        compose.waitUntil(5_000) { sheetView?.hasWindowFocus() == true }
        capture("confirmation-light")
        val bodyPosition = IntArray(2)
        val footerPosition = IntArray(2)
        compose.runOnIdle {
            requireNotNull(bodyMarker).getLocationOnScreen(bodyPosition)
            requireNotNull(footerMarker).getLocationOnScreen(footerPosition)
        }
        val screenHeight = compose.activity.resources.displayMetrics.heightPixels
        val tolerance = 96f * compose.activity.resources.displayMetrics.density
        assertTrue("Panel content must be in the lower screen: ${bodyPosition[1]}", bodyPosition[1] > screenHeight / 3)
        assertTrue("Actions must reach the bottom edge: ${footerPosition[1]}", footerPosition[1] >= screenHeight - tolerance)
        compose.onNodeWithText("Cancelar").assertHeightIsAtLeast(48.dp).performClick()
        assertEquals(0, confirmed)
        compose.onNodeWithTag("action-sheet").assertDoesNotExist()
    }
    @Test fun backDismissesWithoutExecutingTheAction() {
        var open by mutableStateOf(true)
        var confirmed = 0
        var sheetView: android.view.View? = null
        show { if (open) SheetFixture("Arquivar receita", { confirmed++ }, { open = false }, { sheetView = it }) }
        compose.onNodeWithTag("action-sheet").assertIsDisplayed()
        compose.waitUntil(5_000) { sheetView?.hasWindowFocus() == true }
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK)
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("action-sheet").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithTag("action-sheet").assertDoesNotExist()
        assertEquals(0, confirmed)
    }
    @Test fun longDoubleFontContentKeepsBothButtonsReachableAndConfirmsOnce() {
        var confirmed = 0
        show(2f) { ConfirmationSheet("Excluir medicamento definitivamente?",
            "O acompanhamento e as retiradas serão excluídos. " + "Leia os dados antes de confirmar. ".repeat(15),
            "Excluir", { confirmed++ }, {}, destructive = true) }
        compose.onNodeWithText("Excluir medicamento definitivamente?").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) { getLayout ->
            val results = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
            assertTrue(getLayout(results))
            assertEquals(2f, results.single().layoutInput.density.fontScale, 0.01f)
        }
        compose.onNodeWithText("Cancelar").performScrollTo().assertIsDisplayed().assertHeightIsAtLeast(48.dp)
        assertEquals(0, confirmed)
        compose.onNodeWithText("Excluir").performScrollTo().assertIsDisplayed().assertHeightIsAtLeast(48.dp)
        capture("confirmation-dark-200")
        compose.onNodeWithText("Excluir").performClick()
        assertEquals(1, confirmed)
    }
    @Test fun pickupSheetPreservesEarlyConfirmationAndChosenDate() {
        val today = LocalDate.now()
        var confirmed = 0
        var actualDate: LocalDate? = null
        var wasEarly = false
        show(2f) { PickupDialog(MedicationItem("m", "Losartana potássica", lastRefillDate = today.minusDays(15)),
            onDismiss = {}) { date, early, _ -> actualDate = date; wasEarly = early; confirmed++ } }
        compose.onNodeWithText("Registrar").performScrollTo().performClick()
        assertEquals(0, confirmed)
        compose.onNodeWithText("Confirmar antecipação").performScrollTo().assertIsDisplayed()
        capture("pickup-early-dark-200")
        compose.onNodeWithText("Confirmar antecipação").performClick()
        assertEquals(1, confirmed)
        assertEquals(today, actualDate)
        assertTrue(wasEarly)
    }
}
