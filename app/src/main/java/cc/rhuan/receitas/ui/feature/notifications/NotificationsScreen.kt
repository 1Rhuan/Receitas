package cc.rhuan.receitas.ui.feature.notifications

import cc.rhuan.receitas.ui.theme.AppPalette

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import cc.rhuan.receitas.ui.components.AppCard as Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalMaterial3Api::class)
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
    var pendingAction by remember { mutableStateOf<Pair<String, NotificationAction>?>(null) }
    pendingAction?.let { (id, action) ->
        val renewal = action == NotificationAction.RENEW_PRESCRIPTION
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            title = { Text(if (renewal) stringResource(R.string.ui_registrar_nova_receita) else stringResource(R.string.ui_registrar_retirada)) },
            text = { Text(if (renewal) stringResource(R.string.ui_voce_podera_confirmar_os_dados_da_nova_receita_e_escolher_os_acompanhamento) else stringResource(R.string.ui_confirme_que_o_medicamento_ja_foi_retirado_a_proxima_retirada_sera_calculad)) },
            confirmButton = { TextButton(onClick = { pendingAction = null; onPerformAction(id, action) }) { Text(stringResource(R.string.action_confirm)) } },
            dismissButton = { TextButton(onClick = { pendingAction = null }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            onDismissSnackbar()
            snackbarHostState.showSnackbar(message)
        }
    }

    val lazyListState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf {
            (lazyListState.firstVisibleItemIndex > 0) || (lazyListState.firstVisibleItemScrollOffset > 15)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                // Header Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.notifications_title),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )

                            if (uiState.unreadCount > 0) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                ) {
                                    Text(
                                        text = uiState.unreadCount.toString(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    )
                                }
                            }
                        }

                        Row {
                            if (uiState.unreadCount > 0) {
                                TextButton(
                                    onClick = onMarkAllAsRead,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.mark_as_read),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }

                            if (uiState.notifications.isNotEmpty()) {
                                IconButton(onClick = onClearAll) {
                                    Icon(
                                        imageVector = Icons.Default.ClearAll,
                                        contentDescription = stringResource(R.string.clear_all_notifications),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = !isScrolled,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        Text(
                            text = stringResource(R.string.notifications_subtitle),
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filter Chips Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(NotificationFilter.entries.toTypedArray()) { filter ->
                            val isSelected = uiState.selectedFilter == filter
                            val count = when (filter) {
                                NotificationFilter.ALL -> uiState.notifications.size
                                NotificationFilter.UNREAD -> uiState.notifications.count { !it.isRead }
                                NotificationFilter.MEDICATIONS -> uiState.notifications.count {
                                    (it.type == NotificationType.MEDICATION_PICKUP) || (it.type == NotificationType.MEDICATION_STOCK)
                                }
                                NotificationFilter.PRESCRIPTIONS -> uiState.notifications.count {
                                    it.type == NotificationType.PRESCRIPTION_EXPIRING
                                }
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = { onFilterSelected(filter) },
                                label = {
                                    Text(
                                        text = "${stringResource(filter.labelRes)} ($count)",
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                                    selectedBorderColor = Color.Transparent,
                                    borderWidth = 1.dp,
                                ),
                                shape = MaterialTheme.shapes.large,
                            )
                        }
                    }
                }

                // Body List / States
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    when {
                        uiState.isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        uiState.errorMessage != null -> {
                            cc.rhuan.receitas.ui.components.LoadErrorPanel(uiState.errorMessage, onRefresh, Modifier.fillMaxSize())
                        }
                        uiState.filteredNotifications.isEmpty() -> {
                            EmptyNotificationsState(
                                filter = uiState.selectedFilter,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        else -> {
                            LazyColumn(
                                state = lazyListState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(
                                    items = uiState.filteredNotifications,
                                    key = { it.id },
                                ) { notification ->
                                    NotificationCard(
                                        item = notification,
                                        onCardClick = {
                                            if (!notification.isRead) {
                                                onMarkAsRead(notification.id)
                                            }
                                        },
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
                                                    if (action == NotificationAction.REGISTER_PICKUP || action == NotificationAction.RENEW_PRESCRIPTION) onPerformAction(notification.id, action) else pendingAction = notification.id to action
                                                }
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
            ) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(12.dp),
                )
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
    val (iconRes, iconColor, containerColor, categoryLabel) = when (item.phase) {
        ReminderPhase.EXPIRED -> Quadruple(R.drawable.ic_bold_document_medicine, MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.errorContainer, stringResource(R.string.ui_receita_vencida))
        ReminderPhase.EXPIRES_TODAY -> Quadruple(R.drawable.ic_bold_document_medicine, AppPalette.warning, AppPalette.warningContainer, stringResource(R.string.ui_vence_hoje))
        ReminderPhase.RENEWAL -> Quadruple(R.drawable.ic_bold_document_medicine, AppPalette.warning, AppPalette.warningContainer, stringResource(R.string.ui_renovacao_de_receita))
        ReminderPhase.PICKUP_UPCOMING -> Quadruple(R.drawable.ic_box, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer, stringResource(R.string.ui_retirada_proxima))
        ReminderPhase.PICKUP_DUE -> Quadruple(R.drawable.ic_box, MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.tertiaryContainer, stringResource(R.string.ui_data_de_retirada))
        null -> Quadruple(R.drawable.ic_bold_bell_bing, Color(0xFF7B1FA2), Color(0xFFF3E5F5), "AVISO")
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (!item.isRead) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (!item.isRead) 2.dp else 1.dp,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            // Header row: Category Icon, Label, Timestamp, Unread Indicator, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = containerColor,
                    modifier = Modifier.size(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = categoryLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = iconColor,
                            ),
                        )

                        if (!item.isRead) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                            )
                        }
                    }

                    Text(
                        text = item.timestamp,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.delete_notification),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Notification Title & Message
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Status Banner if action taken
            if (item.actionTaken != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.actionTaken,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.tertiary,
                            ),
                        )
                    }
                }
            }

            if (item.actionTaken == null) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val prescription = item.id.startsWith("prescription:")
                    if (item.targetId != null) {
                        OutlinedButton(
                            onClick = { onActionClick(if (prescription) NotificationAction.VIEW_PRESCRIPTION else NotificationAction.VIEW_MEDICATION) },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                        ) {
                            Text(if (prescription) stringResource(R.string.ui_ver_receita) else stringResource(R.string.ui_ver_medicamentos))
                        }
                        Button(
                            onClick = { onActionClick(if (prescription) NotificationAction.RENEW_PRESCRIPTION else NotificationAction.REGISTER_PICKUP) },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                        ) {
                            Text(if (prescription) stringResource(R.string.action_register_new_prescription) else stringResource(R.string.action_register_pickup))
                        }
                    }
                }
            }

        }
    }
}

@Composable
private fun EmptyNotificationsState(
    filter: NotificationFilter,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(80.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.NotificationsOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when (filter) {
                    NotificationFilter.ALL -> stringResource(R.string.empty_notifications_all)
                    NotificationFilter.UNREAD -> stringResource(R.string.empty_notifications_unread)
                    NotificationFilter.MEDICATIONS -> stringResource(R.string.empty_notifications_medications)
                    NotificationFilter.PRESCRIPTIONS -> stringResource(R.string.empty_notifications_prescriptions)
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = when (filter) {
                    NotificationFilter.UNREAD -> stringResource(R.string.empty_notifications_unread_desc)
                    else -> stringResource(R.string.empty_notifications_desc)
                },
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)

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
