package cc.rhuan.receitas

import android.Manifest
import android.graphics.Bitmap
import android.os.Build
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import cc.rhuan.receitas.data.local.AppDatabase
import cc.rhuan.receitas.data.repository.*
import cc.rhuan.receitas.domain.model.*
import cc.rhuan.receitas.notifications.ReminderSynchronizer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import java.io.File
import java.time.LocalDate

/** Real navigation and writes with a dedicated, cleaned-up demonstration record. */
class ExploratoryUseTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val id = "exploratory-${System.currentTimeMillis()}"
    private val title = "Teste de uso $id"
    private val today = LocalDate.now()
    private val db get() = AppDatabase.getDatabase(compose.activity)
    private val medications get() = RoomMedicationRepository(TestTexts, db)
    private val prescriptions get() = RoomPrescriptionRepository(TestTexts, db)

    @Before fun setup() = runBlocking {
        if (Build.VERSION.SDK_INT >= 33) InstrumentationRegistry.getInstrumentation().uiAutomation
            .grantRuntimePermission("cc.rhuan.receitas", Manifest.permission.POST_NOTIFICATIONS)
        RoomPrescriptionRegistrationRepository(TestTexts, db, prescriptions, medications, RoomDoctorRepository(db.doctorDao())).save(
            PrescriptionItem(id, "Receita $title", "", issueDate = today.minusDays(20), customValidityDays = 30, medicationIds = listOf(id)),
            listOf(MedicationItem(id, title, lastRefillDate = today.minusDays(15), refillCycleDays = 30, linkedPrescriptionId = id)), null)
    }

    @After fun cleanup() = runBlocking {
        if (prescriptions.getPrescriptionById(id) != null) prescriptions.deletePrescription(id)
        medications.deleteMedication(id)
        ReminderSynchronizer.sync(compose.activity)
        db.notificationDao().getAllNotifications().first().filter { it.targetId == id }.forEach {
            db.notificationDao().deleteNotification(it.id)
            compose.activity.getSharedPreferences("delivered_reminders", android.content.Context.MODE_PRIVATE)
                .edit().remove(it.id).remove("dismissed:${it.id}").commit()
        }
    }

    private fun click(text: String) { compose.onNodeWithText(text).performClick(); compose.waitForIdle() }
    private fun awaitHistory(count: Int) {
        compose.waitUntil(15000) { runBlocking { medications.getMedicationById(id)?.pickupHistory?.size == count } }
    }
    private fun capture(name: String) {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val bitmap = compose.captureActiveRoot()
        val file = File(compose.activity.getExternalFilesDir(null), "$name.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        listOf("mkdir -p /sdcard/Download/receitas-review", "cp ${file.absolutePath} /sdcard/Download/receitas-review/$name.png").forEach {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(it)).use { stream -> stream.readBytes() }
        }
    }

    @Test fun pickupHistoryAndArchiveRemainConsistentDuringNavigationAndRecreation() {
        compose.onNodeWithContentDescription("Medicamentos", useUnmergedTree = true).performClick()
        compose.onNodeWithText(title).performScrollTo().performClick()
        compose.onNodeWithText("Atenção à validade").performScrollTo().assertIsDisplayed()
        capture("exploratory-validity-conflict")
        compose.onNodeWithText("Registrar retirada").performScrollTo().performClick()
        click("Registrar")
        compose.onNodeWithText("Confirmar retirada antecipada?").assertIsDisplayed()
        click("Confirmar antecipação")
        awaitHistory(2)
        assertEquals(today.plusDays(30), runBlocking { medications.getMedicationById(id)!!.nextAvailableRefillDate })
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Ver histórico de retiradas").performScrollTo().performClick()
        compose.onAllNodesWithText("Ações").onFirst().performClick()
        click("Excluir")
        compose.onNodeWithText("Excluir retirada?").assertIsDisplayed()
        click("Cancelar")
        assertEquals(2, runBlocking { medications.getMedicationById(id)!!.pickupHistory.size })
        compose.onAllNodesWithText("Ações").onFirst().performClick()
        click("Excluir"); click("Excluir")
        awaitHistory(1)
        assertEquals(today.plusDays(15), runBlocking { medications.getMedicationById(id)!!.nextAvailableRefillDate })
        capture("exploratory-history-recalculated")
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.onNodeWithContentDescription("Excluir").performScrollTo().performClick()
        click("Arquivar"); click("Arquivar")
        compose.waitUntil(15000) { runBlocking { medications.getMedicationById(id)!!.trackingArchived } }
        compose.onNodeWithText("Registrar retirada").assertDoesNotExist()
        compose.onNodeWithContentDescription("Reativar").performScrollTo().performClick()
        compose.waitUntil(15000) { runBlocking { !medications.getMedicationById(id)!!.trackingArchived } }
        assertEquals(1, runBlocking { medications.getMedicationById(id)!!.pickupHistory.size })
        compose.onNodeWithText("Registrar retirada").performScrollTo().assertIsDisplayed()
        capture("exploratory-reactivated")
    }

    @Test fun conflictNotificationIsDeliveredAndClosedAfterValidityIsExtended() = runBlocking {
        ReminderSynchronizer.sync(compose.activity)
        val alert = db.notificationDao().getAllNotifications().first().single {
            it.targetId == id && it.phase == ReminderPhase.PRESCRIPTION_PICKUP_CONFLICT.name
        }
        val manager = compose.activity.getSystemService(android.app.NotificationManager::class.java)
        compose.waitUntil(15000) { manager.activeNotifications.any { it.tag == "reminder:${alert.id}" } }
        assertTrue(compose.activity.getSharedPreferences("delivered_reminders", android.content.Context.MODE_PRIVATE).getBoolean(alert.id, false))
        prescriptions.updatePrescription(prescriptions.getPrescriptionById(id)!!.copy(customValidityDays = 90, expiresOn = null))
        ReminderSynchronizer.sync(compose.activity)
        assertNotNull(db.notificationDao().getNotificationById(alert.id)!!.resolvedAt)
        assertTrue(manager.activeNotifications.none { it.tag == "reminder:${alert.id}" })
    }
}
