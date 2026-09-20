package cc.rhuan.receitas.ui.feature.medications

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
import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.ui.theme.ReceitasTheme

@Composable
fun MedicationsScreen(
    modifier: Modifier = Modifier,
    viewModel: MedicationsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MedicationsScreenContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChanged,
        onFilterSelected = viewModel::onFilterSelected,
        onSortOrderSelected = viewModel::onSortOrderSelected,
        modifier = modifier,
    )
}

@Composable
fun MedicationsScreenContent(
    uiState: MedicationsUiState,
    onSearchQueryChange: (String) -> Unit,
    onFilterSelected: (MedicationFilter) -> Unit,
    modifier: Modifier = Modifier,
    onSortOrderSelected: (MedicationSortOrder) -> Unit = {},
    onMedicationClick: (MedicationItem) -> Unit = {},
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
                    text = "Medicamentos",
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
                            text = "Acompanhe e gerencie a lista dos seus remédios",
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
                                    text = "Buscar medicamento...",
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(MedicationFilter.entries) { filter ->
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

                    Spacer(modifier = Modifier.width(8.dp))

                    var sortMenuExpanded by remember { mutableStateOf(value = false) }

                    Box {
                        FilterChip(
                            selected = uiState.selectedSortOrder != MedicationSortOrder.DEFAULT,
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
                            MedicationSortOrder.entries.forEach { sortOption ->
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

                    uiState.filteredMedications.isEmpty() -> {
                        EmptyMedicationState(
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
                                items = uiState.filteredMedications,
                                key = { it.id },
                            ) { medication ->
                                MedicationCard(
                                    medication = medication,
                                    onClick = { onMedicationClick(medication) },
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
private fun MedicationCard(
    medication: MedicationItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconBg = if (medication.isAvailable) Color(0xFFE3F2FD) else Color(0xFFFFEBEE)
    val iconColor = if (medication.isAvailable) Color(0xFF0288D1) else Color(0xFFD32F2F)
    val iconRes = if (!medication.isAvailable) R.drawable.ic_box else R.drawable.ic_bold_pills

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
                        text = medication.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = iconColor,
                        ),
                    )
                    Text(
                        text = medication.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        ),
                        color = Color(0xFF333846),
                    )
                }

                MedicationStatusBadge(medication = medication)
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
                        text = "${medication.dosage} • ${medication.quantity}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569),
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }

                if (medication.frequency.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9),
                    ) {
                        Text(
                            text = medication.frequency,
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

            if (!medication.instructions.isNullOrBlank()) {
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
                            text = medication.instructions,
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
private fun MedicationStatusBadge(medication: MedicationItem) {
    val (bgColor, textColor, label) = if (medication.isAvailable) {
        Triple(Color(0xFFE6F4EA), Color(0xFF1E8E3E), "Disponível")
    } else {
        if (medication.daysUntilAvailable <= 3) {
            Triple(Color(0xFFFFF3E0), Color(0xFFD97706), "Em ${medication.daysUntilAvailable} dias")
        } else {
            Triple(Color(0xFFE0E4EA), Color(0xFF5C6270), "Em ${medication.daysUntilAvailable} dias")
        }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
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
private fun EmptyMedicationState(
    hasSearchQuery: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_pills),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (hasSearchQuery) "Nenhum medicamento encontrado" else "Nenhum medicamento cadastrado",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF333846),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (hasSearchQuery) "Tente buscar por outro termo ou alterar os filtros." else "Sues medicamentos aparecerão aqui.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6C7280),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MedicationsScreenPreview() {
    ReceitasTheme {
        MedicationsScreenContent(
            uiState = MedicationsUiState(
                medications = listOf(
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
                    MedicationItem(
                        id = "2",
                        title = "Losartana Potássica",
                        dosage = "50 mg",
                        quantity = "1 cx",
                        frequency = "1x ao dia",
                        category = "Uso Contínuo",
                        isAvailable = false,
                        daysUntilAvailable = 3,
                    ),
                ),
            ),
            onSearchQueryChange = {},
            onFilterSelected = {},
        )
    }
}
