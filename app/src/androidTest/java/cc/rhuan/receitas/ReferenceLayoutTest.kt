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
import cc.rhuan.receitas.ui.feature.notifications.*
import cc.rhuan.receitas.ui.feature.prescriptions.*
import cc.rhuan.receitas.ui.theme.AppTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class ReferenceLayoutTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private fun show(scale: Float, content: @Composable () -> Unit) {
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
        listOf("mkdir -p /sdcard/Download/reference-layout", "cp ${file.absolutePath} /sdcard/Download/reference-layout/$name.png").forEach {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(it)).use { stream -> stream.readBytes() }
        }
    }
    private fun sorting(scale: Float) {
        var selected by mutableStateOf(PrescriptionSortOrder.EXPIRATION)
        var changes = 0
        show(scale) {
            PrescriptionsScreenContent(PrescriptionsUiState(prescriptions = listOf(PrescriptionItem("r", "Receita de acompanhamento", "")),
                selectedSortOrder = selected), {}, {}, onSortOrderSelected = { selected = it; changes++ })
        }
        compose.onNodeWithContentDescription("Filtros e ordenação de receitas").assertHeightIsAtLeast(48.dp).assertIsDisplayed()
        capture("recipes-header-$scale")
        compose.onNodeWithContentDescription("Filtros e ordenação de receitas").performClick()
        compose.onNodeWithText("Nome (A-Z)").assertIsDisplayed()
        capture("recipes-sort-menu-$scale")
        compose.onNodeWithText("Nome (A-Z)").performScrollTo().performClick()
        assertEquals(0, changes)
        compose.onNodeWithText("Cancelar").performClick()
        assertEquals(PrescriptionSortOrder.EXPIRATION, selected)
        compose.onNodeWithContentDescription("Filtros e ordenação de receitas").performClick()
        compose.onNodeWithText("Nome (A-Z)").performScrollTo().performClick()
        compose.onNodeWithText("Concluir").performClick()
        compose.runOnIdle { assertEquals(PrescriptionSortOrder.NAME_ASC, selected); assertEquals(1, changes) }
        compose.onNodeWithContentDescription("Filtros e ordenação de receitas").performClick()
        compose.onNodeWithText("Nome (A-Z)").assertIsDisplayed()
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK)
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Nome (A-Z)").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithText("Nome (A-Z)").assertDoesNotExist()
        assertEquals(1, changes)
    }
    @Test fun sortMenuLightSelectsAndDismissesWithoutChangingAgain() = sorting(1f)
    @Test fun sortMenuDoubleFontKeepsTitleAndOptionsReachable() = sorting(2f)

    @Test fun orderingAndStatusStayPendingUntilDone() {
        var state by mutableStateOf(PrescriptionsUiState(prescriptions = listOf(PrescriptionItem("r", "Receita", ""))))
        show(2f) { PrescriptionsScreenContent(state, {}, { state = state.copy(selectedFilter = it) },
            onSortOrderSelected = { state = state.copy(selectedSortOrder = it) }) }
        compose.onNodeWithContentDescription("Filtros e ordenação de receitas").performClick()
        compose.onNodeWithTag("list-option-0-1").performScrollTo().performClick()
        compose.onNodeWithTag("list-option-1-3").performScrollTo().performClick()
        assertEquals(PrescriptionFilter.ALL, state.selectedFilter)
        assertEquals(PrescriptionSortOrder.EXPIRATION, state.selectedSortOrder)
        compose.onNodeWithText("Cancelar").performClick()
        compose.onNodeWithContentDescription("Filtros e ordenação de receitas").performClick()
        compose.onNodeWithTag("list-option-1-0").performScrollTo().assertIsSelected()
        compose.onNodeWithTag("list-option-0-0").performScrollTo().assertIsSelected()
        compose.onNodeWithTag("list-option-0-2").performScrollTo().performClick()
        compose.onNodeWithTag("list-option-1-1").performScrollTo().performClick()
        capture("recipes-options-status-dark-200")
        compose.onNodeWithText("Concluir").performClick()
        assertEquals(PrescriptionFilter.VALID, state.selectedFilter)
        assertEquals(PrescriptionSortOrder.NAME_DESC, state.selectedSortOrder)
    }

    private val alert = NotificationItem("conflict", "Receita vence antes da retirada", "Confira a validade antes da próxima retirada.",
        "Hoje", NotificationType.MEDICATION_PICKUP, targetId = "med", targetType = "PICKUP", phase = ReminderPhase.PRESCRIPTION_PICKUP_CONFLICT)

    @Composable
    private fun Notifications(state: NotificationsUiState = NotificationsUiState(), onFilter: (NotificationFilter) -> Unit = {},
        onRead: (String) -> Unit = {}, onReadAll: () -> Unit = {}, onDelete: (String) -> Unit = {},
        onOpen: (String) -> Unit = {}) {
        NotificationsScreenContent(state, onFilter, onRead, onReadAll, onDelete, {}, { _, _ -> }, {}, {},
            onNavigateToMedication = onOpen)
    }
    private fun emptyNotifications(scale: Float) {
        show(scale) { Notifications() }
        compose.onNodeWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.empty_notifications_all)).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Opções de notificações").assertDoesNotExist()
        compose.onNodeWithContentDescription("Opções de notificações").assertDoesNotExist()
        capture("notifications-empty-$scale")
    }
    @Test fun emptyNotificationsLightUsesSharedGuidance() = emptyNotifications(1f)
    @Test fun emptyNotificationsDoubleFontRemainsScrollable() = emptyNotifications(2f)

    @Test fun emptyUnreadFilterCanReturnToAllNotifications() {
        var filter by mutableStateOf(NotificationFilter.UNREAD)
        show(2f) { Notifications(NotificationsUiState(listOf(alert.copy(isRead = true)), selectedFilter = filter),
            onFilter = { filter = it }) }
        compose.onNodeWithText("Mostrar todas as notificações").performScrollTo().assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(NotificationFilter.ALL, filter) }
        compose.onNodeWithTag("notification-list").performScrollToNode(hasText(alert.title))
        compose.onNodeWithText(alert.title).assertIsDisplayed()
    }
    @Test fun notificationMenusKeepReadDeleteAndOpenActionsDistinct() {
        var readAll = 0
        var read = ""
        var deleted = ""
        var opened = ""
        show(1f) { Notifications(NotificationsUiState(listOf(alert)), onRead = { read = it },
            onReadAll = { readAll++ }, onDelete = { deleted = it }, onOpen = { opened = it }) }
        capture("notifications-list-light")
        compose.onNodeWithContentDescription("Opções de notificações").performClick()
        capture("notifications-menu-light")
        compose.onNodeWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.mark_as_read)).performClick()
        assertEquals(1, readAll)
        compose.onNodeWithContentDescription("Ações da notificação").performClick()
        compose.onNodeWithText("Marcar como lida").performClick()
        assertEquals("conflict", read)
        compose.onNodeWithText("Ver medicamento").performScrollTo().performClick()
        assertEquals("med", opened)
        compose.onNodeWithText("Registrar retirada").assertDoesNotExist()
        compose.onNodeWithContentDescription("Ações da notificação").performScrollTo().performClick()
        compose.onNodeWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.delete_notification)).performClick()
        assertEquals("conflict", deleted)
    }
}
