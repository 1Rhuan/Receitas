package cc.rhuan.receitas

import cc.rhuan.receitas.domain.model.*
import cc.rhuan.receitas.ui.feature.notifications.*
import org.junit.Assert.*
import org.junit.Test

class NotificationPresentationTest {
    private fun item(id: String, type: NotificationType = NotificationType.PRESCRIPTION_EXPIRING) = NotificationItem(id, "Título editado", "Mensagem", "13/10/2026", type)

    @Test fun phaseUsesAlertIdentityInsteadOfTitle() {
        assertEquals(ReminderPhase.EXPIRED, item("prescription:p:2026-10-13:expired").phase)
        assertEquals(ReminderPhase.EXPIRES_TODAY, item("prescription:p:2026-10-13:today").phase)
        assertEquals(ReminderPhase.PICKUP_DUE, item("pickup:m:2026-10-13:due", NotificationType.MEDICATION_PICKUP).phase)
    }
    @Test fun retiredDoseEventsAreNotScheduledReminders() {
        assertFalse(item("1", NotificationType.MEDICATION_REMINDER).isScheduledReminder)
        assertNull(item("1", NotificationType.MEDICATION_REMINDER).phase)
    }
    @Test fun pickupFilterIncludesNewAndPreviouslySavedPickupCategories() {
        val pickup = item("pickup:m:2026-10-13:due", NotificationType.MEDICATION_PICKUP)
        val previous = item("pickup:n:2026-10-13:due", NotificationType.MEDICATION_STOCK)
        val recipe = item("prescription:p:2026-10-13:renewal")
        val state = NotificationsUiState(listOf(pickup, previous, recipe), selectedFilter = NotificationFilter.MEDICATIONS)
        assertEquals(listOf(pickup, previous), state.filteredNotifications)
    }
    @Test fun closedRemindersDoNotCountAsUnread() {
        val closed = item("prescription:p:2026-10-13:expired").copy(isRead = true, actionTaken = "Lembrete encerrado")
        assertTrue(closed.isObsolete)
        val state = NotificationsUiState(listOf(closed), selectedFilter = NotificationFilter.UNREAD)
        assertEquals(0, state.unreadCount)
        assertTrue(state.filteredNotifications.isEmpty())
    }
}
