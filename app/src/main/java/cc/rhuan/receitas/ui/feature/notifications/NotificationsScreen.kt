package cc.rhuan.receitas.ui.feature.notifications

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cc.rhuan.receitas.R
import cc.rhuan.receitas.domain.model.NotificationAction
import cc.rhuan.receitas.domain.model.NotificationItem
import cc.rhuan.receitas.domain.model.NotificationType
import cc.rhuan.receitas.ui.theme.ReceitasTheme

@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier,
    viewModel: NotificationsViewModel = viewModel(),
    onNavigateToPrescription: (String) -> Unit = {},
    onNavigateToMedication: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NotificationsScreenContent(
        uiState = uiState,
        onFilterSelected = viewModel::onFilterSelected,
        onMarkAsRead = viewModel::markAsRead,
        onMarkAllAsRead = viewModel::markAllAsRead,
        onDeleteNotification = viewModel::deleteNotification,
        onClearAll = viewModel::clearAll,
        onPerformAction = viewModel::performAction,
        onDismissSnackbar = viewModel::dismissSnackbar,
        onRefresh = viewModel::refresh,
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
        color = Color(0xFFE8ECEF),
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
                                color = Color(0xFF333846),
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
                                        tint = Color(0xFF6C7280),
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
                            color = Color(0xFF6C7280),
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
                                    (it.type == NotificationType.MEDICATION_REMINDER) || (it.type == NotificationType.MEDICATION_STOCK)
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
                                    containerColor = Color.White,
                                    labelColor = Color(0xFF535A6B),
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color(0xFFD0D5DD),
                                    selectedBorderColor = Color.Transparent,
                                    borderWidth = 1.dp,
                                ),
                                shape = RoundedCornerShape(20.dp),
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
                                                    onPerformAction(notification.id, action)
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
                    containerColor = Color(0xFF333846),
                    contentColor = Color.White,
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
    val (iconRes, iconColor, containerColor, categoryLabel) = when (item.type) {
        NotificationType.MEDICATION_REMINDER -> Quadruple(
            R.drawable.ic_bold_pills,
            Color(0xFF0288D1),
            Color(0xFFE3F2FD),
            stringResource(R.string.category_medication_reminder),
        )
        NotificationType.PRESCRIPTION_EXPIRING -> {
            if (item.title.contains("Vencida", ignoreCase = true)) {
                Quadruple(
                    R.drawable.ic_bold_document_medicine,
                    Color(0xFFD32F2F),
                    Color(0xFFFFEBEE),
                    stringResource(R.string.category_prescription_expired),
                )
            } else {
                Quadruple(
                    R.drawable.ic_bold_document_medicine,
                    Color(0xFFD97706),
                    Color(0xFFFFF3E0),
                    stringResource(R.string.category_prescription_renewal),
                )
            }
        }
        NotificationType.MEDICATION_STOCK -> {
            if (item.title.contains("Liberada", ignoreCase = true) || item.title.contains("Retirar", ignoreCase = true)) {
                Quadruple(
                    R.drawable.ic_box,
                    Color(0xFF1E8E3E),
                    Color(0xFFE6F4EA),
                    stringResource(R.string.category_stock_available),
                )
            } else {
                Quadruple(
                    R.drawable.ic_box,
                    Color(0xFFE65100),
                    Color(0xFFFFF3E0),
                    stringResource(R.string.category_stock_low),
                )
            }
        }
        NotificationType.SYSTEM_ALERT -> Quadruple(
            R.drawable.ic_bold_bell_bing,
            Color(0xFF7B1FA2),
            Color(0xFFF3E5F5),
            stringResource(R.string.category_system_alert),
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!item.isRead) Color(0xFFF8FAFC) else Color.White,
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
                        color = Color(0xFF94A3B8),
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.delete_notification),
                        tint = Color(0xFF94A3B8),
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
                color = Color(0xFF333846),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                ),
                color = Color(0xFF64748B),
            )

            // Status Banner if action taken
            if (item.actionTaken != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.actionTaken,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2E7D32),
                            ),
                        )
                    }
                }
            }

            // Contextual Action Buttons Row
            if (item.actionTaken == null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    when (item.type) {
                        NotificationType.MEDICATION_REMINDER -> {
                            Button(
                                onClick = { onActionClick(NotificationAction.TAKE_MEDICATION) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = stringResource(R.string.action_take_medication), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = { onActionClick(NotificationAction.SNOOZE_MEDICATION) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = stringResource(R.string.action_snooze_medication), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        NotificationType.PRESCRIPTION_EXPIRING -> {
                            Button(
                                onClick = { onActionClick(NotificationAction.RENEW_PRESCRIPTION) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = stringResource(R.string.action_renew_prescription), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        NotificationType.MEDICATION_STOCK -> {
                            Button(
                                onClick = { onActionClick(NotificationAction.REPLENISH_STOCK) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = stringResource(R.string.action_replenish_stock), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        NotificationType.SYSTEM_ALERT -> {
                            if (!item.isRead) {
                                TextButton(
                                    onClick = { onActionClick(NotificationAction.MARK_AS_READ) },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = stringResource(R.string.action_mark_read), fontSize = 13.sp)
                                }
                            }
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
                color = Color(0xFFE2E8F0),
                modifier = Modifier.size(80.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.NotificationsOff,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
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
                color = Color(0xFF333846),
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = when (filter) {
                    NotificationFilter.UNREAD -> stringResource(R.string.empty_notifications_unread_desc)
                    else -> stringResource(R.string.empty_notifications_desc)
                },
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = Color(0xFF64748B),
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
                        id = "1",
                        title = "Hora do Medicamento",
                        message = "Está na hora de tomar Amoxicilina (500mg) - Dose das 14:00.",
                        timestamp = "Há 10 min",
                        type = NotificationType.MEDICATION_REMINDER,
                        isRead = false,
                    ),
                    NotificationItem(
                        id = "2",
                        title = "Receita a Vencer",
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
