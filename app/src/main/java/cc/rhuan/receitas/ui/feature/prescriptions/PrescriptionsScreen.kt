package cc.rhuan.receitas.ui.feature.prescriptions

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cc.rhuan.receitas.R
import cc.rhuan.receitas.domain.model.PrescriptionItem
import cc.rhuan.receitas.ui.theme.ReceitasTheme

@Composable
fun PrescriptionsScreen(
    modifier: Modifier = Modifier,
    viewModel: PrescriptionsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PrescriptionsScreenContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChanged,
        onFilterSelected = viewModel::onFilterSelected,
        onSortOrderSelected = viewModel::onSortOrderSelected,
        modifier = modifier,
    )
}

@Composable
fun PrescriptionsScreenContent(
    uiState: PrescriptionsUiState,
    onSearchQueryChange: (String) -> Unit,
    onFilterSelected: (PrescriptionFilter) -> Unit,
    modifier: Modifier = Modifier,
    onSortOrderSelected: (PrescriptionSortOrder) -> Unit = {},
    onPrescriptionClick: (PrescriptionItem) -> Unit = {},
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
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                Text(
                    text = "Receitas Médicas",
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
                            text = "Acompanhe e consulte suas receitas e prescrições",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = Color(0xFF6C7280),
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Search Bar
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = "Buscar receita ou médico...",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Buscar",
                                    tint = Color(0xFF6C7280),
                                )
                            },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchQueryChange("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Limpar busca",
                                            tint = Color(0xFF6C7280),
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color(0xFFD0D5DD),
                            ),
                            singleLine = true,
                            maxLines = 1,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Chips & Sort
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    item {
                        var sortMenuExpanded by remember { mutableStateOf(value = false) }

                        Box {
                            FilterChip(
                                selected = uiState.selectedSortOrder != PrescriptionSortOrder.EXPIRATION,
                                onClick = { sortMenuExpanded = true },
                                label = {
                                    Text(
                                        text = uiState.selectedSortOrder.label,
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_sort_vertical),
                                        contentDescription = "Ordenar",
                                        modifier = Modifier.size(16.dp),
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = Color(0xFF4A4E5A),
                                    iconColor = Color(0xFF4A4E5A),
                                ),
                                shape = RoundedCornerShape(20.dp),
                            )

                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false },
                                modifier = Modifier.background(Color.White),
                            ) {
                                Text(
                                    text = "ORDENAR POR",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6C7280),
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                )
                                PrescriptionSortOrder.entries.forEach { sortOption ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = sortOption.label,
                                                fontWeight = if (uiState.selectedSortOrder == sortOption) FontWeight.Bold else FontWeight.Normal,
                                                color = if (uiState.selectedSortOrder == sortOption) MaterialTheme.colorScheme.primary else Color(0xFF333846),
                                            )
                                        },
                                        trailingIcon = if (uiState.selectedSortOrder == sortOption) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp),
                                                )
                                            }
                                        } else null,
                                        onClick = {
                                            onSortOrderSelected(sortOption)
                                            sortMenuExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                    }

                    items(PrescriptionFilter.entries) { filter ->
                        FilterChip(
                            selected = uiState.selectedFilter == filter,
                            onClick = { onFilterSelected(filter) },
                            label = { Text(filter.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF4A4E5A),
                            ),
                            shape = RoundedCornerShape(20.dp),
                        )
                    }
                }
            }

            // Body content: Loading, Error, Empty, or List
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

                    uiState.filteredPrescriptions.isEmpty() -> {
                        EmptyPrescriptionsState(
                            hasSearchQuery = uiState.searchQuery.isNotEmpty(),
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }

                    else -> {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(
                                items = uiState.filteredPrescriptions,
                                key = { it.id },
                            ) { prescription ->
                                PrescriptionCard(
                                    prescription = prescription,
                                    onClick = { onPrescriptionClick(prescription) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrescriptionCard(
    prescription: PrescriptionItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isExpired = prescription.isExpired
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
                        text = prescription.specialty.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = iconColor,
                        ),
                    )
                    Text(
                        text = prescription.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        ),
                        color = Color(0xFF333846),
                    )
                }

                ExpirationBadge(daysUntilExpiration = prescription.daysUntilExpiration)
            }

            if (prescription.doctorName.isNotEmpty() || prescription.issueDate.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    if (prescription.doctorName.isNotEmpty()) {
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
                                text = prescription.doctorName,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = Color(0xFF6C7280),
                            )
                        }
                    }

                    if (prescription.issueDate.isNotEmpty()) {
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
                                text = "Emitida em ${prescription.issueDate}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = Color(0xFF6C7280),
                            )
                        }
                    }
                }
            }

            prescription.notes?.let { note ->
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
                            text = note,
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
private fun EmptyPrescriptionsState(
    hasSearchQuery: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_document_medicine),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (hasSearchQuery) "Nenhuma receita encontrada" else "Nenhuma receita cadastrada",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF333846),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (hasSearchQuery) "Tente buscar por outro termo ou alterar os filtros." else "Suas receitas cadastradas aparecerão aqui.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6C7280),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PrescriptionsScreenPreview() {
    ReceitasTheme {
        PrescriptionsScreenContent(
            uiState = PrescriptionsUiState(
                prescriptions = listOf(
                    PrescriptionItem(
                        id = "1",
                        title = "Receita Cardiologia",
                        specialty = "Cardiologista",
                        doctorName = "Dr. Carlos Eduardo",
                        daysUntilExpiration = 0,
                        issueDate = "10/01/2025",
                        notes = "Uso contínuo de anti-hipertensivo.",
                    ),
                    PrescriptionItem(
                        id = "2",
                        title = "Tratamento Dermatológico",
                        specialty = "Dermatologista",
                        doctorName = "Dra. Ana Paula",
                        daysUntilExpiration = 15,
                        issueDate = "25/01/2025",
                    ),
                ),
            ),
            onSearchQueryChange = {},
            onFilterSelected = {},
        )
    }
}
