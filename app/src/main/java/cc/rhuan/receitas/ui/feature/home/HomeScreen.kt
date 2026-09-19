package cc.rhuan.receitas.ui.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cc.rhuan.receitas.R
import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.domain.model.ReceitaItem
import cc.rhuan.receitas.ui.theme.ReceitasTheme

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    onReceitaClick: (ReceitaItem) -> Unit = {},
    onMedicationClick: (MedicationItem) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onNavigateToRecords: () -> Unit = {},
    onNavigateToMedications: () -> Unit = {},
    notificationCount: Int = 2,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        uiState = uiState,
        onReceitaClick = onReceitaClick,
        onMedicationClick = onMedicationClick,
        onNotificationClick = onNotificationClick,
        onNavigateToRecords = onNavigateToRecords,
        onNavigateToMedications = onNavigateToMedications,
        onDismissAlert = viewModel::dismissAlert,
        onRefresh = viewModel::refreshData,
        notificationCount = notificationCount,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onReceitaClick: (ReceitaItem) -> Unit = {},
    onMedicationClick: (MedicationItem) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onNavigateToRecords: () -> Unit = {},
    onNavigateToMedications: () -> Unit = {},
    onDismissAlert: (String) -> Unit = {},
    onRefresh: () -> Unit = {},
    notificationCount: Int = 2,
) {
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
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Header com Saudação Personalizada
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
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
                                painter = painterResource(id = R.drawable.ic_bell_bing),
                                contentDescription = "Notificações",
                                tint = Color(0xFF333846),
                            )
                        }
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
                                // 1. Banner de Alerta Crítico / Resumo do Dia
                                if (uiState.activeAlerts.isNotEmpty()) {
                                    item {
                                        AlertCarouselSection(
                                            alerts = uiState.activeAlerts,
                                            onAlertClick = { id ->
                                                if (id == "receitas") onNavigateToRecords()
                                                else if (id == "medications") onNavigateToMedications()
                                            },
                                            onDismissAlert = onDismissAlert,
                                        )
                                    }
                                }

                                // 2. Atalhos / Ações Rápidas (Quick Actions)
                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "Ações Rápidas",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                            ),
                                            color = Color(0xFF535A6B),
                                        )
                                        QuickActionsRow(
                                            onNewRecordClick = onNavigateToRecords,
                                            onNewMedicationClick = onNavigateToMedications,
                                            onNotificationClick = onNotificationClick,
                                        )
                                    }
                                }

                                // 3. Seção de Receitas Próximas
                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        SectionHeader(
                                            title = "Receitas",
                                            onSeeAllClick = onNavigateToRecords,
                                        )
                                        val list = uiState.urgentReceitas
                                        if (list.isEmpty()) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(16.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                            ) {
                                                EmptyRowText(text = "Nenhuma receita exige atenção imediata.")
                                            }
                                        } else {
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                list.take(3).forEach { receita ->
                                                    ReceitaItemCard(
                                                        item = receita,
                                                        onClick = { onReceitaClick(receita) },
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // 4. Seção de Medicações Próximas
                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                                EmptyRowText(text = "Nenhum medicamento exige atenção imediata.")
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
    }
}

@Composable
private fun AlertCarouselSection(
    alerts: List<AlertItem>,
    onAlertClick: (String) -> Unit,
    onDismissAlert: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState { alerts.size }

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 16.dp,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            val alert = alerts[page]
            AlertCard(
                alert = alert,
                onClick = { onAlertClick(alert.id) },
                onCloseClick = { onDismissAlert(alert.id) },
            )
        }

        if (alerts.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(alerts.size) { iteration ->
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
private fun AlertCard(
    alert: AlertItem,
    onClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardBg = if (alert.isCritical) Color(0xFFFFF3F2) else Color(0xFFFFF8E1)
    val iconBg = if (alert.isCritical) Color(0xFFFFE4E2) else Color(0xFFFFECB3)
    val iconTint = if (alert.isCritical) Color(0xFFC01535) else Color(0xFFE65100)
    val iconImage = if (alert.isCritical) Icons.Default.Warning else Icons.Default.MedicalServices

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Surface(
                    shape = CircleShape,
                    color = iconBg,
                    modifier = Modifier.size(48.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = iconImage,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        ),
                        color = Color(0xFF333846),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = alert.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                        ),
                        color = Color(0xFF535A6B),
                    )
                }

                IconButton(
                    onClick = onCloseClick,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(start = 4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar aviso",
                        tint = Color(0xFF8B93A7),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Resolver agora",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        ),
                        color = iconTint,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onNewRecordClick: () -> Unit,
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
                onClick = onNewRecordClick,
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
                fontSize = 18.sp,
            ),
            color = Color(0xFF535A6B),
        )
        TextButton(
            onClick = onSeeAllClick,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
        ) {
            Text(
                text = "Ver todos",
                style = MaterialTheme.typography.labelLarge.copy(
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
private fun ReceitaItemCard(
    item: ReceitaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isExpired = item.isExpired
    val containerBg = if (isExpired) Color(0xFFFFF8F8) else Color.White

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_medical_records),
                contentDescription = null,
                modifier = Modifier.size(42.dp),
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    ),
                    color = if (isExpired) Color(0xFFC01535) else Color(0xFF333846),
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.specialty,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                    ),
                    color = Color(0xFF6C7280),
                )
            }

            ExpirationBadge(daysUntilExpiration = item.daysUntilExpiration)
        }
    }
}

@Composable
private fun MedicationItemCard(
    item: MedicationItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_medical_pills),
                contentDescription = null,
                modifier = Modifier.size(42.dp),
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    ),
                    color = Color(0xFF333846),
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${item.dosage} • ${item.quantity}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                    ),
                    color = Color(0xFF6C7280),
                )
            }

            MedicationStatusBadge(item = item)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    ReceitasTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                receitas = listOf(
                    ReceitaItem(
                        id = "1",
                        title = "Receita Cardiologia",
                        specialty = "Cardiologista",
                        daysUntilExpiration = 0,
                    ),
                    ReceitaItem(
                        id = "2",
                        title = "Tratamento Dermatológico",
                        specialty = "Dermatologista",
                        daysUntilExpiration = 2,
                    ),
                ),
                medicamentos = listOf(
                    MedicationItem(
                        id = "1",
                        title = "Amoxicilina",
                        dosage = "500 mg",
                        quantity = "2 cx",
                        isAvailable = true,
                    ),
                    MedicationItem(
                        id = "2",
                        title = "Losartana Potássica",
                        dosage = "50 mg",
                        quantity = "1 cx",
                        isAvailable = false,
                        daysUntilAvailable = 3,
                    ),
                ),
            ),
        )
    }
}
