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
    private val fixture = "FluxoTeste" + System.currentTimeMillis()
    private fun click(text: String) { compose.onNodeWithText(text, useUnmergedTree = true).performClick(); compose.waitForIdle() }
    private fun field(label: String, value: String) { compose.onNodeWithText(label).performScrollTo().performTextReplacement(value); Espresso.closeSoftKeyboard(); compose.waitForIdle() }
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
        click("Continuar")
        click("Continuar")
        compose.onNodeWithText("Continuar nesta nova receita").performScrollTo().performClick()
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
        click("Nova receita")
        compose.onNodeWithText("Etapa 1 de 4").assertExists()
        screenshot("registration-entry")
        field("Nome da receita (opcional)", fixture)
        field("Validade em dias", "45")
        compose.onAllNodes(isToggleable())[0].performScrollTo().performClick()
        screenshot("registration-recipe")
        click("Continuar")
        compose.onNodeWithText("Etapa 2 de 4").assertExists()
        field("Nome do medicamento *", fixture + " A")
        field("Concentração (opcional)", "50 mg")
        screenshot("registration-medication")
        compose.onNodeWithText("Adicionar à receita").performScrollTo().performClick()
        compose.onNodeWithText("Adicionar outro medicamento").performScrollTo().performClick()
        field("Nome do medicamento *", fixture + " B")
        click("Continuar")
        compose.onNodeWithText("Etapa 3 de 4").assertExists()
        compose.onAllNodesWithText("Quantidade por retirada (opcional)")[0].performScrollTo().performTextReplacement("2 caixas")
        Espresso.closeSoftKeyboard()
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
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.onNodeWithContentDescription("Receitas", useUnmergedTree = true).performClick()
        compose.waitForIdle()
        screenshot("records-prescriptions")
        compose.onNodeWithContentDescription("Medicamentos", useUnmergedTree = true).performClick()
        compose.waitForIdle()
        screenshot("records-medications")
    }
}
