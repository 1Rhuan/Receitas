package cc.rhuan.receitas

import android.Manifest
import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.room.withTransaction
import androidx.test.espresso.Espresso
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
    private fun click(text: String) { compose.onNodeWithText(text, useUnmergedTree = true).performClick(); compose.waitForIdle() }
    private fun clickFormOption(label: String) {
        val node = compose.onNodeWithText(label).performScrollTo()
        val actionTop = compose.onNodeWithTag("wizard-primary-action").fetchSemanticsNode().boundsInRoot.top
        val bounds = node.fetchSemanticsNode().boundsInRoot
        if (bounds.bottom >= actionTop) compose.onNodeWithTag("wizard-content")
            .performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.ScrollBy) { it(0f, bounds.bottom - actionTop + bounds.height) }
        node.performClick()
    }
    private fun field(label: String, value: String) { if (label == "Nome da receita (opcional)" && compose.onAllNodesWithText(label).fetchSemanticsNodes().isEmpty()) clickFormOption("Nome e profissional (opcional)"); compose.onNodeWithText(label).performScrollTo().performTextReplacement(value); closeKeyboard(); compose.waitForIdle() }
    private fun screenshot(name: String) {
        val bitmap = requireNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()) { "Captura de tela indisponível" }
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
            val medicationIds = recipes.flatMap { it.medicationIds }.distinct()
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
            RoomPrescriptionRegistrationRepository(db, RoomPrescriptionRepository(db), RoomMedicationRepository(db), RoomDoctorRepository(db.doctorDao())).save(
                PrescriptionItem(oldId, fixture, "", issueDate = today.minusDays(181), customValidityDays = 180, medicationIds = listOf(medId), notes = "Observação anterior"),
                listOf(MedicationItem(medId, fixture + " medicamento", hasRecurringPickup = true, lastRefillDate = today.minusDays(30), linkedPrescriptionId = oldId)), null)
        }
        compose.onNodeWithContentDescription("Receitas", useUnmergedTree = true).performClick()
        compose.waitForIdle()
        compose.onNodeWithText(fixture).performScrollTo().performClick()
        compose.onNodeWithText("Registrar nova receita").performScrollTo().performClick()
        compose.waitUntil(15000) { compose.onAllNodesWithText("Vamos registrar sua nova receita").fetchSemanticsNodes().isNotEmpty() }
        field("Nome da receita (opcional)", fixture + " renovada")
        click("Continuar para medicamentos")
        click("Continuar para lembretes")
        click("Configurar lembretes")
        clickFormOption("Continuar nesta nova receita")
        click("Revisar cadastro")
        click("Salvar receita")
        compose.waitUntil(15000) { compose.onAllNodesWithText("Receita cadastrada").fetchSemanticsNodes().isNotEmpty() }
        val newRecipe = runBlocking { RoomPrescriptionRepository(db).getPrescriptions().first().single { it.title == fixture + " renovada" } }
        assertNotEquals(oldId, newRecipe.id)
        assertEquals(oldId, newRecipe.previousPrescriptionId)
        assertEquals(RecordState.REPLACED, runBlocking { RoomPrescriptionRepository(db).getPrescriptionById(oldId)!!.state })
        assertEquals("Observação anterior", runBlocking { RoomPrescriptionRepository(db).getPrescriptionById(oldId)!!.notes })
        val med = runBlocking { RoomMedicationRepository(db).getMedicationById(medId)!! }
        assertEquals(newRecipe.id, med.linkedPrescriptionId)
        assertEquals(today, med.nextAvailableRefillDate)
        assertEquals(1, med.pickupHistory.size)
        click("Ver receita")
        screenshot("registration-renewal")
    }
    @Test fun homeToSaveSupportsReviewEditsAndShowsCompletion() {
        compose.onNodeWithContentDescription("Receitas", useUnmergedTree = true).performClick()
        compose.onNodeWithContentDescription("Nova receita").performClick()
        compose.onNodeWithText("Etapa 1 de 4").assertExists()
        screenshot("registration-entry")
        field("Nome da receita (opcional)", fixture)
        field("Validade em dias", "45")
        clickFormOption("Lembrete de renovação")
        clickFormOption("Receber alertas de validade e renovação")
        screenshot("registration-recipe")
        click("Continuar para medicamentos")
        compose.onNodeWithText("Etapa 2 de 4").assertExists()
        field("Nome do medicamento *", fixture + " A")
        field("Concentração (opcional)", "50 mg")
        screenshot("registration-medication")
        compose.onNodeWithText("Adicionar outro medicamento").performScrollTo().performClick()
        field("Nome do medicamento *", fixture + " B")
        click("Continuar para lembretes")
        compose.onNodeWithText("Etapa 3 de 4").assertExists()
        compose.onAllNodesWithText("Quantidade por retirada (opcional)")[0].performScrollTo().performTextReplacement("2 caixas")
        closeKeyboard()
        screenshot("registration-pickup")
        click("Revisar cadastro")
        compose.onNodeWithText("Etapa 4 de 4").assertExists()
        compose.onAllNodesWithText("Editar")[0].performScrollTo().performClick()
        compose.onNodeWithText("Etapa 1 de 4").assertExists()
        field("Nome da receita (opcional)", fixture + " revisada")
        click("Voltar à revisão")
        compose.onAllNodesWithText("Editar")[2].performScrollTo().performClick()
        field("Concentração (opcional)", "25 mg")
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.onNodeWithText("Etapa 4 de 4").assertExists()
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
        assertFalse(saved.renewalReminderEnabled)
        val savedMed = runBlocking { AppDatabase.getDatabase(compose.activity).medicationDao().getMedicationById(saved.medicationIds.first())!! }
        assertEquals("2 caixas", runBlocking { AppDatabase.getDatabase(compose.activity).trackingDao().tracking(savedMed.id)!!.quantity })
        click("Ver receita")
        compose.onNodeWithText(fixture + " revisada").assertExists()
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
