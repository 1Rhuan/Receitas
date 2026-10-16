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
        val node = compose.onNodeWithText(label).performScrollTo()
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
        compose.waitUntil(15000) { compose.onAllNodesWithText("Vamos registrar sua nova receita").fetchSemanticsNodes().isNotEmpty() }
        field("Nome da receita", fixture + " renovada")
        click("Pular")
        click("Avançar")
        click("Avançar")
        click("Avançar")
        click("Avançar")
        click("Avançar")
        click("Avançar")
        clickFormOption("Continuar nesta nova receita")
        click("Pular")
        click("Salvar receita")
        compose.waitUntil(15000) { compose.onAllNodesWithText("Receita cadastrada").fetchSemanticsNodes().isNotEmpty() }
        val newRecipe = runBlocking { RoomPrescriptionRepository(TestTexts, db).getPrescriptions().first().single { it.title == fixture + " renovada" } }
        assertNotEquals(oldId, newRecipe.id)
        assertEquals(oldId, newRecipe.previousPrescriptionId)
        assertEquals("Observação anterior", newRecipe.notes)
        assertEquals(RecordState.REPLACED, runBlocking { RoomPrescriptionRepository(TestTexts, db).getPrescriptionById(oldId)!!.state })
        assertEquals("Observação anterior", runBlocking { RoomPrescriptionRepository(TestTexts, db).getPrescriptionById(oldId)!!.notes })
        val med = runBlocking { RoomMedicationRepository(TestTexts, db).getMedicationById(medId)!! }
        assertEquals(newRecipe.id, med.linkedPrescriptionId)
        assertEquals(today, med.nextAvailableRefillDate)
        assertEquals(1, med.pickupHistory.size)
        click("Ver receita")
        screenshot("registration-renewal")
    }
    @Test fun emptyRecipeCanAddMedicationsFromItsListAndCancelWithoutChanges() {
        compose.onNodeWithContentDescription("Receitas", useUnmergedTree = true).performClick()
        compose.onNodeWithContentDescription("Nova receita").performClick()
        field("Nome da receita", fixture)
        click("Pular"); click("Avançar"); click("Pular"); click("Pular")
        compose.onNodeWithText("Nome do medicamento").assertExists()
        click("Pular")
        compose.onNodeWithText("Etapa 6 de 6").assertExists()
        compose.onNodeWithText("Retiradas e lembretes").assertDoesNotExist()
        click("Salvar receita")
        compose.waitUntil(15000) { compose.onAllNodesWithText("Receita cadastrada").fetchSemanticsNodes().isNotEmpty() }
        val db = AppDatabase.getDatabase(compose.activity)
        val recipes = RoomPrescriptionRepository(TestTexts, db)
        val recipeId = runBlocking { recipes.getPrescriptions().first().single { it.title == fixture }.id }
        assertTrue(runBlocking { recipes.getPrescriptionById(recipeId)!!.medicationIds.isEmpty() })
        click("Ver receita")
        compose.onNodeWithText("Adicionar medicamento").assertDoesNotExist()
        compose.onNodeWithText("Ver medicamentos").performScrollTo().performClick()
        compose.onNodeWithText("Adicionar medicamento").assertIsDisplayed()
        screenshot("optional-empty-list")
        click("Adicionar medicamento")
        compose.waitUntil(15000) { compose.onAllNodesWithText("Nome do medicamento").fetchSemanticsNodes().isNotEmpty() }
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
            compose.waitUntil(15000) { compose.onAllNodesWithText("Nome do medicamento").fetchSemanticsNodes().isNotEmpty() }
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
                compose.onNodeWithText("Receita do medicamento").performScrollTo().assertIsDisplayed()
                compose.onNodeWithText("Receita para retiradas: " + fixture).performScrollTo().assertIsDisplayed()
                screenshot("optional-context-review")
            } else click("Pular")
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
        compose.waitUntil(15000) { compose.onAllNodesWithText("Nome do medicamento").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.onNodeWithTag("recipe-medication-list").assertIsDisplayed()
        assertEquals(2, runBlocking { recipes.getPrescriptionById(recipeId)!!.medicationIds.size })
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.onNodeWithText("Detalhes da receita").assertIsDisplayed()
    }

    @Test fun homeToSaveSupportsReviewEditsAndShowsCompletion() {
        compose.onNodeWithContentDescription("Receitas", useUnmergedTree = true).performClick()
        compose.onNodeWithContentDescription("Nova receita").performClick()
        compose.onNodeWithText("Etapa 1 de 6").assertExists()
        screenshot("registration-entry")
        field("Nome da receita", fixture)
        click("Pular")
        clickFormOption("Tipo de receita")
        clickFormOption("Personalizado")
        field("Validade em dias", "45")
        compose.activityRule.scenario.recreate()
        compose.waitForIdle()
        compose.onNodeWithText("Tipo e validade da receita").assertExists()
        compose.onNodeWithText("45").assertExists()
        click("Usar validade personalizada")
        clickFormOption("Receber alertas de validade e renovação")
        screenshot("registration-recipe")
        click("Avançar")
        field("Observações da receita", fixture + " observação da receita")
        click("Pular")
        compose.onNodeWithText("Etapa 5 de 6").assertExists()
        field("Nome do medicamento", fixture + " A")
        field("Concentração", "50 mg")
        screenshot("registration-medication")
        compose.onNodeWithText("Adicionar outro medicamento").performScrollTo().performClick()
        field("Nome do medicamento", fixture + " B")
        click("Avançar")
        compose.onNodeWithText("Etapa 6 de 7").assertExists()
        compose.onAllNodesWithContentDescription("Quantidade por retirada")[0].performScrollTo().performTextReplacement("2 caixas")
        closeKeyboard()
        screenshot("registration-pickup")
        click("Pular")
        compose.onNodeWithText("Etapa 7 de 7").assertExists()
        compose.onNodeWithContentDescription("Editar Tipo e validade da receita").performScrollTo().performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
        compose.onNodeWithText("Tipo e validade da receita").assertExists()
        compose.onNodeWithText("45").assertExists()
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.onNodeWithText("Etapa 7 de 7").assertExists()
        compose.onNodeWithContentDescription("Editar Nome da receita").performScrollTo().performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
        compose.onNodeWithText("Etapa 1 de 7").assertExists()
        field("Nome da receita", fixture + " revisada")
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.onNodeWithText("Etapa 7 de 7").assertExists()
        compose.onNodeWithContentDescription("Editar medicamento " + fixture + " A").performScrollTo().performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
        field("Concentração", "25 mg")
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.onNodeWithText("Etapa 7 de 7").assertExists()
        compose.activityRule.scenario.recreate()
        compose.waitForIdle()
        compose.onNodeWithText("Medicamentos (2)").assertExists()
        compose.onNodeWithText("25 mg").assertExists()
        screenshot("registration-review")
        click("Salvar receita")
        compose.waitUntil(timeoutMillis = 15000) { compose.onAllNodesWithText("Receita cadastrada").fetchSemanticsNodes().isNotEmpty() }
        screenshot("registration-complete")
        val saved = runBlocking { AppDatabase.getDatabase(compose.activity).prescriptionDao().getAllPrescriptions().first().single { it.title == fixture + " revisada" } }
        assertEquals(2, saved.medicationIds.size)
        assertEquals(45, saved.validityDays)
        assertEquals(fixture + " observação da receita", saved.notes)
        assertEquals(PrescriptionType.CUSTOM, saved.type)
        assertFalse(saved.renewalReminderEnabled)
        val savedMed = runBlocking { AppDatabase.getDatabase(compose.activity).medicationDao().getMedicationById(saved.medicationIds.first())!! }
        assertEquals("2 caixas", runBlocking { AppDatabase.getDatabase(compose.activity).trackingDao().tracking(savedMed.id)!!.quantity })
        click("Ver receita")
        compose.onNodeWithText(fixture + " revisada").assertExists()
        compose.onNodeWithText("Observações da receita").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(fixture + " observação da receita").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("prescription-detail-list").performScrollToNode(hasText("Ver medicamentos"))
        compose.onNodeWithText("Ver medicamentos").performClick()
        compose.onNodeWithTag("recipe-medication-list").performScrollToNode(hasText("Ver medicamento"))
        compose.onAllNodesWithText("Ver medicamento").onFirst().performClick()
        compose.onNodeWithText("Detalhes do medicamento").assertIsDisplayed()
        compose.onNodeWithText("Receitas vinculadas").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("1 receita").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(fixture + " revisada").assertDoesNotExist()
        compose.onNodeWithText("Ver receitas vinculadas").performScrollTo().performClick()
        compose.onNodeWithText("Filtrado por medicamento").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("prescription-list").performScrollToNode(hasText(fixture + " revisada"))
        compose.onNodeWithText(fixture + " revisada").performClick()
        compose.onNodeWithText("Detalhes da receita").assertIsDisplayed()
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.onNodeWithTag("prescription-list").assertIsDisplayed()
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.onNodeWithText("Detalhes do medicamento").assertIsDisplayed()
        compose.onNodeWithText("Sem vínculo").assertDoesNotExist()
        compose.onNodeWithText("Ver histórico de retiradas").performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.onNodeWithTag("recipe-medication-list").assertIsDisplayed()
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.onNodeWithText(fixture + " revisada").assertExists()
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.onNodeWithContentDescription("Receitas", useUnmergedTree = true).performClick()
        compose.waitForIdle()
        screenshot("records-prescriptions")
        compose.onNodeWithContentDescription("Medicamentos", useUnmergedTree = true).performClick()
        compose.waitForIdle()
        screenshot("records-medications")
    }
}
