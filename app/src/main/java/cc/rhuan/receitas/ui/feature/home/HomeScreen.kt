package cc.rhuan.receitas.ui.feature.home

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cc.rhuan.receitas.R
import cc.rhuan.receitas.domain.model.DailyDoseItem
import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.domain.model.NotificationAction
import cc.rhuan.receitas.domain.model.NotificationItem
import cc.rhuan.receitas.domain.model.NotificationType
import cc.rhuan.receitas.domain.model.PrescriptionItem
import cc.rhuan.receitas.ui.theme.ReceitasTheme

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    onPrescriptionClick: (PrescriptionItem) -> Unit = {},
    onMedicationClick: (MedicationItem) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onNavigateToPrescriptions: () -> Unit = {},
    onNavigateToMedications: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChanged,
        onMarkDoseAsTaken = viewModel::markDoseAsTaken,
        onPrescriptionClick = onPrescriptionClick,
        onMedicationClick = onMedicationClick,
        onNotificationClick = onNotificationClick,
        onNavigateToPrescriptions = onNavigateToPrescriptions,
        onNavigateToMedications = onNavigateToMedications,
        onPerformNotificationAction = viewModel::performNotificationAction,
        onDismissNotification = viewModel::dismissNotification,
        onDismissSnackbar = viewModel::dismissSnackbar,
        onRefresh = viewModel::refreshData,
        notificationCount = uiState.unreadNotificationsCount,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onSearchQueryChange: (String) -> Unit = {},
    onMarkDoseAsTaken: (String) -> Unit = {},
    onPrescriptionClick: (PrescriptionItem) -> Unit = {},
    onMedicationClick: (MedicationItem) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onNavigateToPrescriptions: () -> Unit = {},
    onNavigateToMedications: () -> Unit = {},
    onPerformNotificationAction: (String, NotificationAction) -> Unit = { _, _ -> },
    onDismissNotification: (String) -> Unit = {},
    onDismissSnackbar: () -> Unit = {},
    onRefresh: () -> Unit = {},
    notificationCount: Int = uiState.unreadNotificationsCount,
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
                // Header com Saudação & Busca Rápida
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
                        Column {
                            Text(
                                text = "Olá, ${uiState.userName} 👋",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                ),
                                color = Color(0xFF333846),
                            )
                            AnimatedVisibility(
                                visible = !isScrolled,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically(),
                            ) {
                                Column {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Confira o resumo da sua saúde hoje",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                        color = Color(0xFF6C7280),
                                    )
                                }
                            }
                        }

                        IconButton(onClick = onNotificationClick) {
                            BadgedBox(
                                badge = {
                                    if (notificationCount > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary,
                                        ) {
                                            Text(text = notificationCount.toString())
                                        }
                                    }
                                },
                            ) {
                                Icon(
                                    painter = painterResource(id = if (notificationCount > 0) R.drawable.ic_bell_bing else R.drawable.ic_bell),
                                    contentDescription = "Notificações",
                                    tint = Color(0xFF333846),
                                )
                            }
                        }
                    }

                    // Busca Rápida na Home (Recolhe ao deslizar a tela)
                    AnimatedVisibility(
                        visible = !isScrolled || uiState.searchQuery.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = onSearchQueryChange,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        text = "Buscar receitas, remédios, médicos...",
                                        fontSize = 13.sp,
                                        color = Color(0xFF8B93A7),
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Buscar",
                                        tint = Color(0xFF8B93A7),
                                        modifier = Modifier.size(18.dp),
                                    )
                                },
                                trailingIcon = {
                                    if (uiState.searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { onSearchQueryChange("") }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Limpar busca",
                                                tint = Color(0xFF8B93A7),
                                                modifier = Modifier.size(18.dp),
                                            )
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.Transparent,
                                ),
                                singleLine = true,
                            )
                        }
                    }
                }

                // Body Content com PullToRefresh
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    when {
                        uiState.isLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                            )
                        }

                        uiState.errorMessage != null -> {
                            Text(
                                text = uiState.errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.align(Alignment.Center),
                            )
                        }

                        else -> {
                            PullToRefreshBox(
                                isRefreshing = uiState.isRefreshing,
                                onRefresh = onRefresh,
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                LazyColumn(
                                    state = lazyListState,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp),
                                ) {
                                    // 1. Seção de Avisos & Lembretes em Destaque (Notificações Ativas)
                                    if (uiState.activeNotifications.isNotEmpty()) {
                                        item {
                                            HomeNotificationsSection(
                                                notifications = uiState.activeNotifications,
                                                onActionClick = onPerformNotificationAction,
                                                onDismiss = onDismissNotification,
                                                onSeeAllClick = onNotificationClick,
                                            )
                                        }
                                    }

                                    // 2. Widget de Progresso do Tratamento (Adesão ao Dia)
                                    item {
                                        TreatmentProgressCard(uiState = uiState)
                                    }

                                    // 3. Agenda de Doses do Dia (Linha do Tempo)
                                    if (uiState.dailyDoses.isNotEmpty()) {
                                        item {
                                            DailyDosesTimelineSection(
                                                doses = uiState.dailyDoses,
                                                onMarkTaken = onMarkDoseAsTaken,
                                            )
                                        }
                                    }

                                    // 4. Atalhos / Ações Rápidas (Quick Actions)
                                    item {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                text = "Ações Rápidas",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                ),
                                                color = Color(0xFF535A6B),
                                            )
                                            QuickActionsRow(
                                                onNewPrescriptionClick = onNavigateToPrescriptions,
                                                onNewMedicationClick = onNavigateToMedications,
                                                onNotificationClick = onNotificationClick,
                                            )
                                        }
                                    }

                                    // 5. Seção de Receitas Próximas
                                    item {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            SectionHeader(
                                                title = "Receitas",
                                                onSeeAllClick = onNavigateToPrescriptions,
                                            )
                                            val list = uiState.urgentPrescriptions
                                            if (list.isEmpty()) {
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(16.dp),
                                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                                ) {
                                                    EmptyRowText(text = "Nenhuma receita encontrada.")
                                                }
                                            } else {
                                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    list.take(3).forEach { prescription ->
                                                        PrescriptionItemCard(
                                                            item = prescription,
                                                            onClick = { onPrescriptionClick(prescription) },
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // 6. Seção de Medicações Próximas
                                    item {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            SectionHeader(
                                                title = "Medicações",
                                                onSeeAllClick = onNavigateToMedications,
                                            )
                                            val list = uiState.urgentMedications
                                            if (list.isEmpty()) {
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(16.dp),
                                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                                ) {
                                                    EmptyRowText(text = "Nenhum medicamento encontrado.")
                                                }
                                            } else {
                                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    list.take(3).forEach { medicamento ->
                                                        MedicationItemCard(
                                                            item = medicamento,
                                                            onClick = { onMedicationClick(medicamento) },
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    item {
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
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
private fun TreatmentProgressCard(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
) {
    val percentageInt = (uiState.adherencePercentage * 100).toInt()
    val badgeBg = when {
        percentageInt == 100 -> Color(0xFFE6F4EA)
        percentageInt > 0 -> Color(0xFFE3F2FD)
        else -> Color(0xFFF1F5F9)
    }
    val badgeTextColor = when {
        percentageInt == 100 -> Color(0xFF1E8E3E)
        percentageInt > 0 -> Color(0xFF0288D1)
        else -> Color(0xFF64748B)
    }
    val progressColor = if (percentageInt == 100) Color(0xFF1E8E3E) else MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Progresso de Hoje",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        ),
                        color = Color(0xFF333846),
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${uiState.takenDosesCount} de ${uiState.totalDosesCount} doses tomadas",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = Color(0xFF6C7280),
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = badgeBg,
                ) {
                    Text(
                        text = "$percentageInt%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor,
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { uiState.adherencePercentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = Color(0xFFE2E8F0),
            )
        }
    }
}

@Composable
private fun DailyDosesTimelineSection(
    doses: List<DailyDoseItem>,
    onMarkTaken: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Próximas Doses do Dia",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
            ),
            color = Color(0xFF535A6B),
        )

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                doses.forEachIndexed { index, dose ->
                    DoseTimelineRow(
                        item = dose,
                        onMarkTaken = { onMarkTaken(dose.id) },
                    )
                    if (index < (doses.size - 1)) {
                        Box(
                            modifier = Modifier
                                .padding(start = 68.dp, end = 16.dp)
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFFF1F5F9)),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DoseTimelineRow(
    item: DailyDoseItem,
    onMarkTaken: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (item.isTaken) Color(0xFFE8F5E9) else Color(0xFFE3F2FD),
            modifier = Modifier.width(48.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 6.dp),
            ) {
                Text(
                    text = item.time,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (item.isTaken) Color(0xFF2E7D32) else Color(0xFF0288D1),
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = item.medicationName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                ),
                color = Color(0xFF333846),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.dosage,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = Color(0xFF64748B),
            )
        }

        if (item.isTaken) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFE8F5E9),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Tomado",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                        ),
                    )
                }
            }
        } else {
            Button(
                onClick = onMarkTaken,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Tomar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HomeNotificationsSection(
    notifications: List<NotificationItem>,
    onActionClick: (String, NotificationAction) -> Unit,
    onDismiss: (String) -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState { notifications.size }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Avisos & Lembretes",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                ),
                color = Color(0xFF535A6B),
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onSeeAllClick)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Ver todas",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                    ),
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            val notification = notifications[page]
            HomeNotificationCard(
                item = notification,
                onActionClick = { action -> onActionClick(notification.id, action) },
                onDismissClick = { onDismiss(notification.id) },
            )
        }

        if (notifications.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(notifications.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration)
                        MaterialTheme.colorScheme.primary
                    else
                        Color(0xFFD0D5DD)
                    val width = if (pagerState.currentPage == iteration) 16.dp else 6.dp

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(width = width, height = 6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color),
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeNotificationCard(
    item: NotificationItem,
    onActionClick: (NotificationAction) -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (iconRes, iconColor, containerColor, categoryLabel) = when (item.type) {
        NotificationType.MEDICATION_REMINDER -> Quadruple(
            R.drawable.ic_bold_pills,
            Color(0xFF0288D1),
            Color(0xFFE3F2FD),
            "LEMBRETE DE DOSE",
        )
        NotificationType.PRESCRIPTION_EXPIRING -> Quadruple(
            R.drawable.ic_bold_document_medicine,
            Color(0xFFF57C00),
            Color(0xFFFFF3E0),
            "RECEITA",
        )
        NotificationType.MEDICATION_STOCK -> Quadruple(
            R.drawable.ic_box,
            Color(0xFFD32F2F),
            Color(0xFFFFEBEE),
            "ESTOQUE DE REMÉDIO",
        )
        NotificationType.SYSTEM_ALERT -> Quadruple(
            R.drawable.ic_bold_bell_bing,
            Color(0xFF7B1FA2),
            Color(0xFFF3E5F5),
            "AVISO DO SISTEMA",
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
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
                    Text(
                        text = categoryLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = iconColor,
                        ),
                    )
                    Text(
                        text = item.timestamp,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color(0xFF94A3B8),
                    )
                }

                IconButton(
                    onClick = onDismissClick,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Descartar aviso",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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

            // Contextual Actions
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
                                Text(text = "Tomar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
                                Text(text = "Adiar 15m", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
                                Text(text = "Marcar como Renovada", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
                                Text(text = "Marcar como Reposto", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        NotificationType.SYSTEM_ALERT -> {
                            TextButton(
                                onClick = { onActionClick(NotificationAction.MARK_AS_READ) },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Marcar como lida", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onNewPrescriptionClick: () -> Unit,
    onNewMedicationClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            QuickActionButton(
                icon = Icons.Default.Add,
                label = "Nova Receita",
                onClick = onNewPrescriptionClick,
            )
        }
        item {
            QuickActionButton(
                icon = Icons.Default.Add,
                label = "Novo Remedio",
                onClick = onNewMedicationClick,
            )
        }
        item {
            QuickActionButton(
                icon = Icons.Default.Notifications,
                label = "Notificações",
                onClick = onNotificationClick,
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        tonalElevation = 1.dp,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                ),
                color = Color(0xFF333846),
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
            ),
            color = Color(0xFF535A6B),
        )
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onSeeAllClick)
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Ver todos",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                ),
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun EmptyRowText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF6C7280),
        modifier = modifier.padding(horizontal = 16.dp, vertical = 16.dp),
    )
}

@Composable
private fun ExpirationBadge(daysUntilExpiration: Int) {
    val isExpired = daysUntilExpiration <= 0
    val (bgColor, textColor, labelText) = when {
        isExpired -> Triple(Color(0xFFFFDADE), Color(0xFFC01535), "Vencida")
        daysUntilExpiration <= 30 -> {
            val daysText = if (daysUntilExpiration == 1) "dia" else "dias"
            Triple(Color(0xFFFFF3E0), Color(0xFFD97706), "Venc. $daysUntilExpiration $daysText")
        }
        else -> Triple(Color(0xFFE6F4EA), Color(0xFF1E8E3E), "Válida")
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = Modifier.padding(start = 8.dp),
    ) {
        Text(
            text = labelText,
            color = textColor,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
            ),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun MedicationStatusBadge(item: MedicationItem) {
    val (bgColor, textColor, label) = if (item.isAvailable) {
        Triple(Color(0xFFE6F4EA), Color(0xFF1E8E3E), "Disponível")
    } else {
        if (item.daysUntilAvailable <= 3) {
            Triple(Color(0xFFFFF3E0), Color(0xFFD97706), "Em ${item.daysUntilAvailable} dias")
        } else {
            Triple(Color(0xFFE0E4EA), Color(0xFF5C6270), "Em ${item.daysUntilAvailable} dias")
        }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = Modifier.padding(start = 8.dp),
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
            ),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun PrescriptionItemCard(
    item: PrescriptionItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isExpired = item.isExpired
    val containerBg = if (isExpired) Color(0xFFFFF8F8) else Color.White
    val iconBg = if (isExpired) Color(0xFFFFEBEE) else Color(0xFFFFF3E0)
    val iconColor = if (isExpired) Color(0xFFD32F2F) else Color(0xFFF57C00)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = iconBg,
                    modifier = Modifier.size(38.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_bold_document_medicine),
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = item.specialty.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = iconColor,
                        ),
                    )
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        ),
                        color = Color(0xFF333846),
                    )
                }

                ExpirationBadge(daysUntilExpiration = item.daysUntilExpiration)
            }

            if (item.doctorName.isNotEmpty() || item.issueDate.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    if (item.doctorName.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF6C7280),
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = item.doctorName,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = Color(0xFF6C7280),
                            )
                        }
                    }

                    if (item.issueDate.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color(0xFF6C7280),
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = "Emitida em ${item.issueDate}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = Color(0xFF6C7280),
                            )
                        }
                    }
                }
            }

            if (!item.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_info_circle),
                            contentDescription = null,
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = item.notes,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF333846),
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicationItemCard(
    item: MedicationItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconBg = if (item.isAvailable) Color(0xFFE3F2FD) else Color(0xFFFFEBEE)
    val iconColor = if (item.isAvailable) Color(0xFF0288D1) else Color(0xFFD32F2F)
    val iconRes = if (!item.isAvailable) R.drawable.ic_box else R.drawable.ic_bold_pills

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = iconBg,
                    modifier = Modifier.size(38.dp),
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

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = item.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = iconColor,
                        ),
                    )
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        ),
                        color = Color(0xFF333846),
                    )
                }

                MedicationStatusBadge(item = item)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF1F5F9),
                ) {
                    Text(
                        text = "${item.dosage} • ${item.quantity}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569),
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }

                if (item.frequency.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9),
                    ) {
                        Text(
                            text = item.frequency,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF475569),
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            if (!item.instructions.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_info_circle),
                            contentDescription = null,
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = item.instructions,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF333846),
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
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
private fun HomeScreenPreview() {
    ReceitasTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                prescriptions = listOf(
                    PrescriptionItem(
                        id = "1",
                        title = "Receita Cardiologia",
                        specialty = "Cardiologista",
                        doctorName = "Dr. Carlos Eduardo",
                        daysUntilExpiration = 0,
                    ),
                ),
                medicamentos = listOf(
                    MedicationItem(
                        id = "1",
                        title = "Amoxicilina",
                        dosage = "500 mg",
                        quantity = "2 cx",
                        frequency = "8 em 8 horas",
                        category = "Antibiótico",
                        isAvailable = true,
                        instructions = "Tomar após as refeições.",
                    ),
                ),
                dailyDoses = listOf(
                    DailyDoseItem(
                        id = "d1",
                        medicationId = "1",
                        medicationName = "Omeprazol",
                        dosage = "20 mg",
                        time = "07:30",
                        isTaken = true,
                    ),
                    DailyDoseItem(
                        id = "d2",
                        medicationId = "2",
                        medicationName = "Amoxicilina",
                        dosage = "500 mg",
                        time = "14:00",
                        isTaken = false,
                    ),
                ),
            ),
        )
    }
}
