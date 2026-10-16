package cc.rhuan.receitas.ui.feature.notifications

import cc.rhuan.receitas.ui.theme.AppPalette
import cc.rhuan.receitas.ui.theme.DesignTokens as T
import cc.rhuan.receitas.ui.components.*

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import cc.rhuan.receitas.R
import cc.rhuan.receitas.domain.model.NotificationAction
import cc.rhuan.receitas.domain.model.NotificationItem
import cc.rhuan.receitas.domain.model.ReminderPhase
import cc.rhuan.receitas.domain.model.NotificationType
import cc.rhuan.receitas.ui.theme.ReceitasTheme

@Composable
fun NotificationsPresentation(
    modifier: Modifier = Modifier,
    uiState: NotificationsUiState,
    registerPickup: (String, java.time.LocalDate, Boolean) -> Unit,
    onFilterSelected: (NotificationFilter) -> Unit,
    markAsRead: (String) -> Unit,
    markAllAsRead: () -> Unit,
    deleteNotification: (String) -> Unit,
    clearAll: () -> Unit,
    openRenewal: (String, (String) -> Unit) -> Unit,
    preparePickup: (String, (cc.rhuan.receitas.domain.model.MedicationItem) -> Unit) -> Unit,
    dismissSnackbar: () -> Unit,
    refresh: () -> Unit,
    onNavigateToPrescription: (String) -> Unit = {},
    onRenewPrescription: (String) -> Unit = {},
    onNavigateToMedication: (String) -> Unit = {},
) {
    var pickup by remember { mutableStateOf<Pair<String, cc.rhuan.receitas.domain.model.MedicationItem>?>(null) }
    pickup?.let { (id, med) -> cc.rhuan.receitas.ui.components.PickupDialog(med, onDismiss = { pickup = null }) { date, early, _ -> registerPickup(id, date, early) } }

    NotificationsScreenContent(
        uiState = uiState,
        onFilterSelected = onFilterSelected,
        onMarkAsRead = markAsRead,
        onMarkAllAsRead = markAllAsRead,
        onDeleteNotification = deleteNotification,
        onClearAll = clearAll,
        onPerformAction = { id, action ->
            val notification = uiState.notifications.find { it.id == id }
            if (notification?.resolvedAt == null) when (action) {
                NotificationAction.RENEW_PRESCRIPTION -> openRenewal(id, onRenewPrescription)
                NotificationAction.REGISTER_PICKUP -> preparePickup(id) { pickup = id to it }
                else -> markAsRead(id)
            }
        },
        onDismissSnackbar = dismissSnackbar,
        onRefresh = refresh,
        onNavigateToPrescription = onNavigateToPrescription,
        onNavigateToMedication = onNavigateToMedication,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun NotificationsScreenContent(
    uiState: NotificationsUiState,
    onFilterSelected: (NotificationFilter) -> Unit,
    onMarkAsRead: (String) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onDeleteNotification: (String) -> Unit,
    onClearAll: () -> Unit,
    onPerformAction: (String, NotificationAction) -> Unit,
    onDismissSnackbar: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToPrescription: (String) -> Unit = {},
    onNavigateToMedication: (String) -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var confirmClear by remember { mutableStateOf(false) }
    if (confirmClear) ConfirmationSheet(
        title = stringResource(R.string.clear_notifications_title),
        message = stringResource(R.string.clear_notifications_help),
        confirmLabel = stringResource(R.string.clear_all_notifications),
        onConfirm = { confirmClear = false; onClearAll() }, onDismiss = { confirmClear = false }, destructive = true)
    var pendingAction by remember { mutableStateOf<Pair<String, NotificationAction>?>(null) }
    pendingAction?.let { (id, action) ->
        val renewal = action == NotificationAction.RENEW_PRESCRIPTION
        ConfirmationSheet(
            title = stringResource(if (renewal) R.string.ui_registrar_nova_receita else R.string.ui_registrar_retirada),
            message = stringResource(if (renewal) R.string.ui_voce_podera_confirmar_os_dados_da_nova_receita_e_escolher_os_acompanhamento else R.string.ui_confirme_que_o_medicamento_ja_foi_retirado_a_proxima_retirada_sera_calculad),
            confirmLabel = stringResource(R.string.action_confirm),
            onConfirm = { pendingAction = null; onPerformAction(id, action) }, onDismiss = { pendingAction = null })
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            onDismissSnackbar()
            snackbarHostState.showSnackbar(message)
        }
    }

    val empty = !uiState.isLoading && uiState.errorMessage == null && uiState.notifications.isEmpty()
    PullToRefreshBox(isRefreshing = uiState.isRefreshing, onRefresh = onRefresh, modifier = modifier.fillMaxSize()) {
        RecordListScaffold(title = stringResource(R.string.notifications_title),
            subtitle = if (empty) "" else stringResource(R.string.notifications_subtitle),
            listTag = "notification-list", snackbarHost = { SnackbarHost(snackbarHostState) },
            headerActions = {
                if (uiState.notifications.isNotEmpty()) OverflowActionsButton(
                    stringResource(R.string.notification_list_actions), buildList {
                        if (uiState.unreadCount > 0) add(ContextAction(stringResource(R.string.mark_as_read), onClick = onMarkAllAsRead))
                        add(ContextAction(stringResource(R.string.clear_all_notifications), destructive = true,
                            onClick = { confirmClear = true }))
                    })
            }) {
            if (!empty) item("filters") {
                LazyRow(contentPadding = PaddingValues(horizontal = T.margin),
                    horizontalArrangement = Arrangement.spacedBy(T.small)) {
                    items(NotificationFilter.entries.toTypedArray()) { filter ->
                        val count = when (filter) {
                            NotificationFilter.ALL -> uiState.notifications.size
                            NotificationFilter.UNREAD -> uiState.unreadCount
                            NotificationFilter.MEDICATIONS -> uiState.notifications.count {
                                it.type == NotificationType.MEDICATION_PICKUP || it.type == NotificationType.MEDICATION_STOCK
                            }
                            NotificationFilter.PRESCRIPTIONS -> uiState.notifications.count { it.type == NotificationType.PRESCRIPTION_EXPIRING }
                        }
                        FilterPill(uiState.selectedFilter == filter,
                            stringResource(filter.labelRes) + " ($count)", { onFilterSelected(filter) })
                    }
                }
            }
            when {
                uiState.isLoading -> item("loading") {
                    Box(Modifier.fillMaxWidth().padding(T.section), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                }
                uiState.errorMessage != null -> item("error") {
                    LoadErrorPanel(uiState.errorMessage, onRefresh, Modifier.padding(horizontal = T.margin))
                }
                uiState.filteredNotifications.isEmpty() -> item("empty") {
                    if (empty) CenteredListGuidance { EmptyNotificationsState(uiState.selectedFilter) { onFilterSelected(NotificationFilter.ALL) } }
                    else Box(Modifier.padding(horizontal = T.margin)) {
                        EmptyNotificationsState(uiState.selectedFilter) { onFilterSelected(NotificationFilter.ALL) }
                    }
                }
                else -> items(uiState.filteredNotifications, key = { "alert:" + it.id }) { notification ->
                    NotificationCard(item = notification, modifier = Modifier.padding(horizontal = T.margin),
                        onCardClick = { if (!notification.isRead) onMarkAsRead(notification.id) },
                        onDeleteClick = { onDeleteNotification(notification.id) },
                        onActionClick = { action ->
                            when (action) {
                                NotificationAction.VIEW_PRESCRIPTION -> {
                                    onMarkAsRead(notification.id)
                                    notification.targetId?.let(onNavigateToPrescription)
                                }
                                NotificationAction.VIEW_MEDICATION -> {
                                    onMarkAsRead(notification.id)
                                    notification.targetId?.let(onNavigateToMedication)
                                }
                                else -> {
                                    if (action == NotificationAction.REGISTER_PICKUP || action == NotificationAction.RENEW_PRESCRIPTION)
                                        onPerformAction(notification.id, action)
                                    else pendingAction = notification.id to action
                                }
                            }
                        })
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    item: NotificationItem,
    onCardClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onActionClick: (NotificationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val (iconRes, iconColor, categoryLabel) = when (item.phase) {
        ReminderPhase.EXPIRED -> Triple(R.drawable.ic_bold_document_medicine, MaterialTheme.colorScheme.error, stringResource(R.string.ui_receita_vencida))
        ReminderPhase.EXPIRES_TODAY -> Triple(R.drawable.ic_bold_document_medicine, AppPalette.warning, stringResource(R.string.ui_vence_hoje))
        ReminderPhase.RENEWAL -> Triple(R.drawable.ic_bold_document_medicine, AppPalette.warning, stringResource(R.string.ui_renovacao_de_receita))
        ReminderPhase.PRESCRIPTION_PICKUP_CONFLICT -> Triple(R.drawable.ic_bold_document_medicine, AppPalette.warning, stringResource(R.string.pickup_validity_warning))
        ReminderPhase.PICKUP_UPCOMING -> Triple(R.drawable.ic_box, MaterialTheme.colorScheme.primary, stringResource(R.string.ui_retirada_proxima))
        ReminderPhase.PICKUP_DUE -> Triple(R.drawable.ic_box, MaterialTheme.colorScheme.tertiary, stringResource(R.string.ui_data_de_retirada))
        null -> Triple(R.drawable.ic_bold_bell_bing, MaterialTheme.colorScheme.primary, stringResource(R.string.notification_category_general))
    }

    ContentCard(modifier.clickable(onClick = onCardClick)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(T.small)) {
            Icon(painterResource(iconRes), null, Modifier.size(T.icon), tint = iconColor)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(T.tight)) {
                Text(categoryLabel, style = MaterialTheme.typography.labelMedium, color = iconColor)
                if (!item.isRead) Text(stringResource(R.string.notification_unread),
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            OverflowActionsButton(stringResource(R.string.notification_item_actions), buildList {
                if (!item.isRead) add(ContextAction(stringResource(R.string.mark_notification_read), onClick = onCardClick))
                add(ContextAction(stringResource(R.string.delete_notification), destructive = true, onClick = onDeleteClick))
            })
        }
        Text(item.timestamp, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(item.title, style = MaterialTheme.typography.titleMedium)
        Text(item.message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        item.actionTaken?.let { status ->
            Surface(shape = RoundedCornerShape(T.fieldRadius), color = MaterialTheme.colorScheme.tertiaryContainer) {
                Row(Modifier.fillMaxWidth().padding(T.padding), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(T.small)) {
                    Icon(Icons.Default.CheckCircle, null, Modifier.size(T.icon), tint = MaterialTheme.colorScheme.onTertiaryContainer)
                    Text(status, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
            }
        }
        if (item.actionTaken == null && item.targetId != null) {
            val prescription = item.targetType == "PRESCRIPTION"
            SecondaryActionButton(stringResource(if (prescription) R.string.ui_ver_receita else R.string.ui_ver_medicamentos),
                { onActionClick(if (prescription) NotificationAction.VIEW_PRESCRIPTION else NotificationAction.VIEW_MEDICATION) },
                Modifier.fillMaxWidth())
            if (item.phase != ReminderPhase.PRESCRIPTION_PICKUP_CONFLICT)
                ActionButton(stringResource(if (prescription) R.string.action_register_new_prescription else R.string.action_register_pickup),
                    { onActionClick(if (prescription) NotificationAction.RENEW_PRESCRIPTION else NotificationAction.REGISTER_PICKUP) },
                    Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun EmptyNotificationsState(filter: NotificationFilter, onShowAll: () -> Unit) {
    EmptyListGuidance(title = stringResource(when (filter) {
        NotificationFilter.ALL -> R.string.empty_notifications_all
        NotificationFilter.UNREAD -> R.string.empty_notifications_unread
        NotificationFilter.MEDICATIONS -> R.string.empty_notifications_medications
        NotificationFilter.PRESCRIPTIONS -> R.string.empty_notifications_prescriptions
    }), message = stringResource(if (filter == NotificationFilter.UNREAD) R.string.empty_notifications_unread_desc
        else R.string.empty_notifications_desc),
        actionLabel = if (filter == NotificationFilter.ALL) null else stringResource(R.string.show_all_notifications),
        onAction = onShowAll, iconRes = R.drawable.ic_bold_bell_bing)
}

@Preview(showBackground = true)
@Composable
private fun NotificationsScreenPreview() {
    ReceitasTheme {
        NotificationsScreenContent(
            uiState = NotificationsUiState(
                notifications = listOf(
                    NotificationItem(
                        id = "pickup:1:2026-10-16:upcoming",
                        title = "Lembrete de retirada",
                        message = "Losartana: retirada prevista para 16/10/2026. Confirme a disponibilidade com a farmácia.",
                        timestamp = "Há 10 min",
                        type = NotificationType.MEDICATION_PICKUP,
                        isRead = false,
                    ),
                    NotificationItem(
                        id = "prescription:2:2026-10-15:renewal",
                        title = "Lembrete de renovação",
                        message = "Sua receita de Tratamento Dermatológico vence em 2 dias.",
                        timestamp = "Há 1 hora",
                        type = NotificationType.PRESCRIPTION_EXPIRING,
                        isRead = true,
                        actionTaken = "Renovação solicitada",
                    ),
                ),
            ),
            onFilterSelected = {},
            onMarkAsRead = {},
            onMarkAllAsRead = {},
            onDeleteNotification = {},
            onClearAll = {},
            onPerformAction = { _, _ -> },
            onDismissSnackbar = {},
            onRefresh = {},
        )
    }
}
