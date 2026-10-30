package cc.rhuan.receitas

import android.Manifest
import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.room.withTransaction
import androidx.test.platform.app.InstrumentationRegistry
import android.os.Build
import cc.rhuan.receitas.data.local.AppDatabase
import cc.rhuan.receitas.data.repository.*
import cc.rhuan.receitas.domain.model.*
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import java.io.File

class PrescriptionRegistrationUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Before fun grantNotifications() {
        if (Build.VERSION.SDK_INT >= 33) InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission("cc.rhuan.receitas", Manifest.permission.POST_NOTIFICATIONS)
    }
    private fun closeKeyboard() { compose.runOnIdle { (compose.activity.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager).hideSoftInputFromWindow(compose.activity.window.decorView.windowToken, 0) } }
    private val fixture = "FluxoTeste" + System.currentTimeMillis()
    private fun click(text: String) { compose.onNodeWithText(text).performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }; compose.waitForIdle() }
    private fun clickFormOption(label: String) {
        clickVisible(compose.onNodeWithText(label))
    }
    private fun clickVisible(target: SemanticsNodeInteraction) {
        val node = target.performScrollTo()
        val actionTop = compose.onNodeWithTag("wizard-primary-action").fetchSemanticsNode().boundsInRoot.top
        val bounds = node.fetchSemanticsNode().boundsInRoot
        if (bounds.bottom >= actionTop) compose.onNodeWithTag("wizard-content")
            .performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.ScrollBy) { it(0f, bounds.bottom - actionTop + bounds.height) }
        node.performClick()
    }
    private fun field(label: String, value: String) { compose.onNodeWithContentDescription(label).performScrollTo().performTextReplacement(value); closeKeyboard(); compose.waitForIdle() }
    private fun screenshot(name: String) {
        compose.waitForIdle()
        val bitmap = compose.captureActiveRoot()
        val file = File(compose.activity.getExternalFilesDir(null), name + ".png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        // Preserve these generated test captures when Gradle removes the test installation.
        listOf("mkdir -p /sdcard/Download/receitas-cadastro-preview", "cp " + file.absolutePath + " /sdcard/Download/receitas-cadastro-preview/" + name + ".png").forEach { command ->
            val descriptor = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
            android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
        }
    }
    @After fun cleanFixture() = runBlocking {
        val db = AppDatabase.getDatabase(compose.activity)
        db.withTransaction {
            val recipes = db.prescriptionDao().getAllPrescriptions().first().filter { it.title.startsWith(fixture) }
            val medicationIds = (recipes.flatMap { it.medicationIds } + db.medicationDao().getAllMedications().first().filter { it.title.startsWith(fixture) }.map { it.id }).distinct()
            recipes.forEach { db.prescriptionDao().deletePrescription(it.id) }
            medicationIds.forEach { id ->
                if (db.medicationDao().getMedicationById(id)?.title?.startsWith(fixture) == true) db.medicationDao().deleteMedication(id)
            }

        }
    }
    @Test fun renewalCreatesNewRecipeAndKeepsPickupHistory() {
        val today = LocalDate.now()
        val oldId = fixture + "-old"
        val medId = fixture + "-med"
        val db = AppDatabase.getDatabase(compose.activity)
        runBlocking {
            RoomPrescriptionRegistrationRepository(TestTexts, db, RoomPrescriptionRepository(TestTexts, db), RoomMedicationRepository(TestTexts, db), RoomDoctorRepository(db.doctorDao())).save(
                PrescriptionItem(oldId, fixture, "", issueDate = today.minusDays(181), customValidityDays = 180, medicationIds = listOf(medId), notes = "Observação anterior"),
                listOf(MedicationItem(medId, fixture + " medicamento", hasRecurringPickup = true, lastRefillDate = today.minusDays(30), linkedPrescriptionId = oldId)), null)
        }
        compose.onNodeWithContentDescription("Receitas", useUnmergedTree = true).performClick()
        compose.waitForIdle()
        compose.onNodeWithText(fixture).performScrollTo().performClick()
        compose.onNodeWithText("Registrar nova receita").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Nome da receita").performScrollTo().assertTextContains(fixture)
        field("Nome da receita", fixture + " renovada")
        click("Avançar")
        click("Avançar")
        click("Avançar")
        click("Pular")
        compose.onNodeWithText("Confira antes de salvar").assertExists()
        click("Salvar receita")
        compose.waitUntil(15000) { compose.onAllNodesWithText("Receita cadastrada").fetchSemanticsNodes().isNotEmpty() }
        val newRecipe = runBlocking { RoomPrescriptionRepository(TestTexts, db).getPrescriptions().first().single { it.title == fixture + " renovada" } }
        assertNotEquals(oldId, newRecipe.id)
        assertEquals(oldId, newRecipe.previousPrescriptionId)
        assertEquals("Observação anterior", newRecipe.notes)
        assertEquals(RecordState.REPLACED, runBlocking { RoomPrescriptionRepository(TestTexts, db).getPrescriptionById(oldId)!!.state })
        assertEquals("Observação anterior", runBlocking { RoomPrescriptionRepository(TestTexts, db).getPrescriptionById(oldId)!!.notes })
        val med = runBlocking { RoomMedicationRepository(TestTexts, db).getMedicationById(medId)!! }
        assertEquals(oldId, med.linkedPrescriptionId)
        assertTrue(med.trackingArchived)
        assertNull(med.nextAvailableRefillDate)
        assertEquals(1, med.pickupHistory.size)
        // Tracking is now edited separately from recipe registration. Continue explicitly.
        click("Agora não")
        compose.onNodeWithText("Ver medicamentos").performScrollTo().performClick()
        compose.onNodeWithText(fixture + " medicamento").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Editar acompanhamento").performScrollTo().performClick()
        click("Avançar"); click("Avançar"); click("Avançar")
        clickFormOption(newRecipe.title)
        click("Avançar"); click("Pular")
        click("Salvar alterações")
        compose.waitUntil(15000) { runBlocking { RoomMedicationRepository(TestTexts, db).getMedicationById(medId)?.linkedPrescriptionId == newRecipe.id } }
        val continued = runBlocking { RoomMedicationRepository(TestTexts, db).getMedicationById(medId)!! }
        assertFalse(continued.trackingArchived)
        assertEquals(med.pickupHistory, continued.pickupHistory)
        assertEquals(today, continued.nextAvailableRefillDate)
        screenshot("registration-renewal")
    }
    @Test fun emptyRecipeCanAddMedicationsFromItsListAndCancelWithoutChanges() {
        compose.onNodeWithContentDescription("Receitas", useUnmergedTree = true).performClick()
        compose.onNodeWithContentDescription("Nova receita").performClick()
        field("Nome da receita", fixture)
        click("Avançar"); click("Avançar"); click("Pular"); click("Pular")
        compose.onNodeWithContentDescription("Nome do medicamento").assertDoesNotExist()
        compose.onNodeWithText("Confira antes de salvar").assertExists()
        compose.onNodeWithText("Retiradas e lembretes").assertDoesNotExist()
        click("Salvar receita")
        compose.waitUntil(15000) { compose.onAllNodesWithText("Receita cadastrada").fetchSemanticsNodes().isNotEmpty() }
        val db = AppDatabase.getDatabase(compose.activity)
        val recipes = RoomPrescriptionRepository(TestTexts, db)
        val recipeId = runBlocking { recipes.getPrescriptions().first().single { it.title == fixture }.id }
        assertTrue(runBlocking { recipes.getPrescriptionById(recipeId)!!.medicationIds.isEmpty() })
        click("Agora não")
        compose.onNodeWithText("Adicionar medicamento").assertDoesNotExist()
        compose.onNodeWithText("Ver medicamentos").performScrollTo().performClick()
        compose.onNodeWithText("Adicionar medicamento").assertIsDisplayed()
        screenshot("optional-empty-list")
        click("Adicionar medicamento")
        compose.onNodeWithContentDescription("Nome do medicamento").assertExists()
        field("Nome do medicamento", fixture + " cancelar")
        compose.onNodeWithContentDescription("Voltar").performClick()
        click("Descartar e sair")
        compose.onNodeWithTag("recipe-medication-list").assertIsDisplayed()
        assertTrue(runBlocking { recipes.getPrescriptionById(recipeId)!!.medicationIds.isEmpty() })
        listOf(" A", " B").forEachIndexed { index, suffix ->
            if (index == 1) runBlocking {
                RoomMedicationRepository(TestTexts, db).addMedication(MedicationItem(fixture + "-B", fixture + suffix,
                    hasRecurringPickup = true, lastRefillDate = LocalDate.now().minusDays(30),
                    refillCycleDays = 1, intervalUnit = IntervalUnit.MONTHS))
            }
            compose.onNodeWithText("Adicionar medicamento").performScrollTo().performClick()
            compose.onNodeWithContentDescription("Nome do medicamento").assertExists()
            field("Nome do medicamento", fixture + suffix)
            if (index == 1) clickFormOption("Usar cadastrado: " + fixture + suffix)
            click("Avançar")
            if (index == 1) {
                compose.onNodeWithText("Unidade do intervalo").assertDoesNotExist()
                field("Intervalo entre retiradas (dias)", "45")
                click("Avançar"); click("Pular")
                compose.onNodeWithText("Receita para retiradas").performScrollTo().assertIsDisplayed()
                compose.onNodeWithText("Sem receita vinculada").assertDoesNotExist()
                compose.onNodeWithText("Sem receita para retiradas").assertDoesNotExist()
                compose.onNodeWithText(fixture).performScrollTo().assertIsDisplayed()
                click("Pular")
                click("Pular")
                compose.onNodeWithText("Receita do medicamento").performScrollTo().assertIsDisplayed()
                compose.onNodeWithText("Receita para retiradas: " + fixture).performScrollTo().assertIsDisplayed()
                screenshot("optional-context-review")
            } else { click("Pular"); click("Pular") }
            click("Salvar medicamento")
            compose.waitUntil(15000) { compose.onAllNodesWithTag("recipe-medication-list").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Adicionar medicamento").performScrollTo().assertIsDisplayed()
            val recipe = runBlocking { recipes.getPrescriptionById(recipeId)!! }
            assertEquals(index + 1, recipe.medicationIds.size)
            assertEquals(index + 1, recipe.items.size)
            runBlocking {
                val savedMeds = recipe.medicationIds.map { RoomMedicationRepository(TestTexts, db).getMedicationById(it)!! }
                assertEquals(recipeId, savedMeds.single { it.title == fixture + " A" }.linkedPrescriptionId)
                if (index == 1) {
                    val selected = savedMeds.single { it.title == fixture + " B" }
                    assertEquals(recipeId, selected.linkedPrescriptionId)
                    assertEquals(IntervalUnit.DAYS, selected.intervalUnit)
                    assertEquals(45, selected.refillCycleDays)
                    assertEquals(1, selected.pickupHistory.size)
                    assertEquals(LocalDate.now().plusDays(15), selected.nextAvailableRefillDate)
                }
            }
        }
        screenshot("optional-populated-list")
        compose.onNodeWithText("Adicionar medicamento").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Nome do medicamento").assertExists()
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.onNodeWithTag("recipe-medication-list").assertIsDisplayed()
        assertEquals(2, runBlocking { recipes.getPrescriptionById(recipeId)!!.medicationIds.size })
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.onNodeWithText("Detalhes da receita").assertIsDisplayed()
    }

    @Test fun homeToSaveSupportsReviewEditsAndShowsCompletion() {
        compose.onNodeWithContentDescription("Receitas", useUnmergedTree = true).performClick()
        compose.onNodeWithContentDescription("Nova receita").performClick()
        compose.onNodeWithText("Como você quer chamar esta receita?").assertExists()
        screenshot("registration-entry")
        field("Nome da receita", fixture)
        click("Avançar")
        clickFormOption("Validade")
        clickVisible(compose.onNodeWithTag("form-choice-option-${PrescriptionType.CUSTOM.ordinal}"))
        click("Avançar")
        field("Validade em dias", "45")
        compose.activityRule.scenario.recreate()
        compose.waitForIdle()
        compose.onNodeWithContentDescription("Validade em dias").assertTextContains("45")
        click("Usar validade personalizada")
        clickFormOption("Receber alertas de validade e renovação")
        screenshot("registration-recipe")
        click("Avançar")
        clickVisible(compose.onNodeWithContentDescription("Adicionar Observações da receita"))
        field("Observações da receita", fixture + " observação da receita")
        click("Concluído")
        click("Avançar")
        compose.onNodeWithText("Confira antes de salvar").assertExists()
        compose.onNodeWithContentDescription("Editar Tipo e validade da receita").performScrollTo().performClick()
        click("Avançar")
        compose.onNodeWithContentDescription("Validade em dias").assertTextContains("45")
        click("Usar validade personalizada")
        compose.onNodeWithContentDescription("Editar Nome da receita").performScrollTo().performClick()
        field("Nome da receita", fixture + " revisada")
        click("Voltar à revisão")
        compose.activityRule.scenario.recreate()
        compose.waitForIdle()
        compose.onNodeWithText(fixture + " revisada").assertExists()
        screenshot("registration-review")
        click("Salvar receita")
        compose.waitUntil(15000) { compose.onAllNodesWithText("Receita cadastrada").fetchSemanticsNodes().isNotEmpty() }
        screenshot("registration-complete")
        val db = AppDatabase.getDatabase(compose.activity)
        val recipes = RoomPrescriptionRepository(TestTexts, db)
        val saved = runBlocking { recipes.getPrescriptions().first().single { it.title == fixture + " revisada" } }
        assertEquals(45, saved.validityDurationDays)
        assertEquals(fixture + " observação da receita", saved.notes)
        assertEquals(PrescriptionType.CUSTOM, saved.type)
        assertFalse(saved.renewalReminderEnabled)
        assertTrue(saved.medicationIds.isEmpty())
        // Add each medication through the current contextual flow after saving the recipe.
        click("Agora não")
        compose.onNodeWithText("Ver medicamentos").performScrollTo().performClick()
        listOf(" A", " B").forEachIndexed { index, suffix ->
            compose.onNodeWithText("Adicionar medicamento").performScrollTo().performClick()
            field("Nome do medicamento", fixture + suffix)
            click("Avançar")
            field("Quantidade por retirada", "2 caixas")
            click("Pular")
            clickVisible(compose.onNodeWithContentDescription("Adicionar Concentração"))
            field("Concentração", if (index == 0) "25 mg" else "50 mg")
            click("Concluído")
            click("Avançar")
            compose.onNodeWithText("Confira antes de salvar").assertExists()
            compose.activityRule.scenario.recreate()
            compose.waitForIdle()
            screenshot("registration-medication-$index")
            click("Salvar medicamento")
            compose.waitUntil(15000) { compose.onAllNodesWithTag("recipe-medication-list").fetchSemanticsNodes().isNotEmpty() }
            assertEquals(index + 1, runBlocking { recipes.getPrescriptionById(saved.id)!!.medicationIds.size })
        }
        val completed = runBlocking { recipes.getPrescriptionById(saved.id)!! }
        val medications = runBlocking { completed.medicationIds.map { RoomMedicationRepository(TestTexts, db).getMedicationById(it)!! } }
        assertEquals(setOf(fixture + " A", fixture + " B"), medications.map { it.title }.toSet())
        assertEquals("25 mg", medications.single { it.title.endsWith(" A") }.dosage)
        medications.forEach { assertEquals(saved.id, it.linkedPrescriptionId); assertEquals("2 caixas", it.quantity) }
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.onNodeWithText("Observações da receita").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(fixture + " observação da receita").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Ver medicamentos").performScrollTo().performClick()
        compose.onNodeWithText(fixture + " A").performScrollTo().performClick()
        compose.onNodeWithText("Receitas vinculadas").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Ver receitas vinculadas").performScrollTo().performClick()
        compose.onNodeWithText("Filtrado por medicamento").assertExists()
        compose.onNodeWithText(saved.title).performScrollTo().performClick()
        compose.onNodeWithText("Detalhes da receita").assertExists()
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.onNodeWithTag("prescription-list").assertIsDisplayed()
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.onNodeWithText("Detalhes do medicamento").assertExists()
        screenshot("registration-medication-detail")
    }
}
