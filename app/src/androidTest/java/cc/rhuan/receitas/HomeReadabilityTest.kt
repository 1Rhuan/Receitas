package cc.rhuan.receitas

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import cc.rhuan.receitas.domain.model.*
import cc.rhuan.receitas.ui.feature.home.*
import cc.rhuan.receitas.ui.feature.home.components.DateSlider
import cc.rhuan.receitas.ui.theme.AppTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.time.LocalDate

class HomeReadabilityTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val today = LocalDate.now()
    private val name = "Losartana potássica para acompanhamento de uso contínuo"
    private fun show(dark: Boolean, scale: Float, content: @Composable () -> Unit) {
        compose.setContent { AppTheme(darkTheme = dark) {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, scale)) {
                Box(Modifier.requiredSize(320.dp, 640.dp)) { content() }
            }
        } }
    }
    private fun capture(name: String) {
        val bitmap = compose.captureActiveRoot()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.getExternalFilesDir(null), "$name.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        listOf("mkdir -p /sdcard/Download/receitas-review", "cp ${file.absolutePath} /sdcard/Download/receitas-review/$name.png").forEach {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(it)).use { stream -> stream.readBytes() }
        }
    }
    private fun home(dark: Boolean, scale: Float, captureName: String) {
        var actions = 0
        show(dark, scale) {
            HomeScreenContent(HomeUiState(texts = TestTexts, medications = listOf(MedicationItem("m", name, dosage = "50 mg", lastRefillDate = today.minusDays(30)))),
                onQuickAction = { actions++ })
        }
        compose.onAllNodesWithText("Registrar retirada").assertCountEquals(1)
        compose.onNodeWithText("Registrar retirada").performScrollTo().assertHeightIsAtLeast(48.dp).assertIsDisplayed()
        capture(captureName)
        compose.onNodeWithText("Registrar retirada").performClick()
        assertEquals(1, actions)
        compose.onNodeWithText("Retirada prevista para hoje").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("$name 50 mg").performScrollTo().assertIsDisplayed()
    }
    @Test fun homeLightKeepsOneReadableActionAndImportantDate() { home(false, 1f, "home-readable-light") }
    @Test fun homeDarkDoubleFontKeepsActionAndLongNameReachable() { home(true, 2f, "home-readable-dark-200") }

    @Test fun smallCalendarKeepsDaysSeparateAndSupportsSelectionAndPaging() {
        // Keep Sunday centered in a three-day page regardless of the execution date.
        val today = generateSequence(LocalDate.now()) { it.plusDays(3) }
            .first { it.dayOfWeek == java.time.DayOfWeek.SUNDAY }
        var selected by mutableStateOf(today)
        show(false, 2f) { DateSlider(selected, { selected = it }, { null }) }
        val nodes = (-1L..1L).map { offset ->
            compose.onNodeWithTag("calendar-date-${today.plusDays(offset)}").assertIsDisplayed().assertWidthIsAtLeast(48.dp)
                .fetchSemanticsNode().boundsInRoot
        }
        nodes.zipWithNext().forEach { (left, right) -> assertTrue(left.right <= right.left) }
        compose.onNodeWithText("dom").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) { getLayout ->
            val results = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
            assertTrue(getLayout(results))
            assertEquals(1, results.single().lineCount)
        }
        capture("calendar-sunday-readable-200")
        compose.onNodeWithTag("calendar-date-${today.plusDays(1)}").performClick()
        compose.runOnIdle { assertEquals(today.plusDays(1), selected) }
        compose.onNodeWithTag("date-pager").performTouchInput { swipeLeft() }
        compose.waitForIdle()
        compose.runOnIdle { assertTrue(selected.isAfter(today.plusDays(1))) }
        capture("calendar-readable-200")
    }

    @Test fun calendarStartsOnPreviouslySelectedDateWithoutChangingIt() {
        val historical = LocalDate.of(1900, 1, 1)
        var selected by mutableStateOf(historical)
        show(false, 1f) { DateSlider(selected, { selected = it }, { null }) }
        compose.onNodeWithTag("calendar-date-$historical").assertIsDisplayed()
        compose.runOnIdle { assertEquals(historical, selected) }
    }
}
