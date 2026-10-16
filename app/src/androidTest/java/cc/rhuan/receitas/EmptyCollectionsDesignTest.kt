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
import cc.rhuan.receitas.ui.feature.home.*
import cc.rhuan.receitas.ui.feature.medications.*
import cc.rhuan.receitas.ui.feature.prescriptions.*
import cc.rhuan.receitas.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.File

class EmptyCollectionsDesignTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private fun show(scale: Float = 1f, content: @Composable () -> Unit) {
        compose.setContent { AppTheme(darkTheme = scale > 1f) {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, scale)) {
                Box(Modifier.requiredSize(320.dp, 640.dp)) { content() }
            }
        } }
    }
    private fun capture(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.getExternalFilesDir(null), "$name.png")
        val bitmap = compose.captureActiveRoot()
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        listOf("mkdir -p /sdcard/Download/empty-collections", "cp ${file.absolutePath} /sdcard/Download/empty-collections/$name.png").forEach {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(it)).use { stream -> stream.readBytes() }
        }
    }
    private fun recipes(scale: Float) {
        var created = 0
        show(scale) { PrescriptionsScreenContent(PrescriptionsUiState(), {}, {},
            onCreatePrescriptionClick = { created++ }) }
        compose.onNodeWithTag("record-search").assertDoesNotExist()
        compose.onAllNodesWithText("Nova receita").assertCountEquals(1)
        compose.onNodeWithText("Nova receita").performScrollTo().assertIsDisplayed().assertHeightIsAtLeast(48.dp)
        capture("recipes-empty-$scale")
        compose.onNodeWithText("Nova receita").performClick()
        assertEquals(1, created)
    }
    @Test fun recipesEmptyLightHasOneWorkingCreationAction() = recipes(1f)
    @Test fun recipesEmptyDoubleFontKeepsCreationReachable() = recipes(2f)
    @Test fun medicationsEmptyDoubleFontKeepsOneWorkingAction() {
        var created = 0
        show(2f) { MedicationsScreenContent(MedicationsUiState(), {}, {}, {}, {}, { created++ }, remember { SnackbarHostState() }) }
        compose.onAllNodesWithText("Novo medicamento").assertCountEquals(1)
        compose.onNodeWithText("Novo medicamento").performScrollTo().assertIsDisplayed().performClick()
        assertEquals(1, created)
        capture("medications-empty-200")
    }
    private fun recipeMembers(scale: Float) {
        var created = 0
        show(scale) { PrescriptionMedicationList(PrescriptionDetailUiState(isLoading = false,
            prescription = PrescriptionItem("recipe", "Receita de acompanhamento de uso contínuo", "")), {}, {},
            onAddMedication = { created++ }) }
        compose.onAllNodesWithText("Adicionar medicamento").assertCountEquals(1)
        compose.onNodeWithText("Adicionar medicamento").performScrollTo().assertIsDisplayed().performClick()
        assertEquals(1, created)
        capture("recipe-members-empty-$scale")
    }
    @Test fun recipeMembersEmptyLightHasOneWorkingAction() = recipeMembers(1f)
    @Test fun recipeMembersEmptyDoubleFontKeepsActionReachable() = recipeMembers(2f)
    @Test fun archivedRecipeEmptyExplainsRestrictionAndKeepsReturn() {
        var returned = 0
        show(2f) { PrescriptionMedicationList(PrescriptionDetailUiState(isLoading = false,
            prescription = PrescriptionItem("recipe", "Receita arquivada", "", state = RecordState.ARCHIVED)),
            { returned++ }, {}) }
        compose.onNodeWithText("Adicionar medicamento").assertDoesNotExist()
        compose.onNodeWithText("reative-a", substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("Voltar").performClick()
        assertEquals(1, returned)
    }
    @Test fun homeEmptyDoubleFontKeepsBothActionsReachable() {
        var recipes = 0
        var medications = 0
        show(2f) { HomeScreenContent(HomeUiState(texts = TestTexts, ), onCreatePrescription = { recipes++ }, onCreateMedication = { medications++ }) }
        compose.onNodeWithText("Adicionar receita").performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithText("Adicionar medicamento").performScrollTo().assertIsDisplayed().performClick()
        assertEquals(1, recipes)
        assertEquals(1, medications)
        capture("home-empty-200")
    }
}
