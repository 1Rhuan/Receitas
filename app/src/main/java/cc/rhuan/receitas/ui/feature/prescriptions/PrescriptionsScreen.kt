package cc.rhuan.receitas.ui.feature.prescriptions

import cc.rhuan.receitas.ui.theme.AppPalette

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import cc.rhuan.receitas.ui.components.AppCard as Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun PrescriptionsScreen(
    modifier: Modifier = Modifier,
    viewModel: PrescriptionsViewModel = viewModel(),
    onPrescriptionClick: (PrescriptionItem) -> Unit = {},
    onCreatePrescriptionClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PrescriptionsScreenContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChanged,
        onFilterSelected = viewModel::onFilterSelected,
        onSortOrderSelected = viewModel::onSortOrderSelected,
        onPrescriptionClick = onPrescriptionClick,
        onCreatePrescriptionClick = onCreatePrescriptionClick,
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
    onCreatePrescriptionClick: () -> Unit = {},
) {
    val lazyListState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf {
            (lazyListState.firstVisibleItemIndex > 0) || (lazyListState.firstVisibleItemScrollOffset > 15)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreatePrescriptionClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nova receita") },
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            color = MaterialTheme.colorScheme.background,
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
                    text = stringResource(R.string.medical_prescriptions),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                AnimatedVisibility(
                    visible = !isScrolled,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.track_and_consult_prescriptions),
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Search Bar
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.search_prescription_or_doctor),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = stringResource(R.string.search),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchQueryChange("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = stringResource(R.string.clear_search),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
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
                                        text = stringResource(uiState.selectedSortOrder.labelRes),
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_sort_vertical),
                                        contentDescription = stringResource(R.string.sort),
                                        modifier = Modifier.size(16.dp),
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface,
                                    iconColor = MaterialTheme.colorScheme.onSurface,
                                ),
                                shape = MaterialTheme.shapes.large,
                            )

                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                            ) {
                                Text(
                                    text = stringResource(R.string.sort_by),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                )
                                PrescriptionSortOrder.entries.forEach { sortOption ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = stringResource(sortOption.labelRes),
                                                fontWeight = if (uiState.selectedSortOrder == sortOption) FontWeight.Bold else FontWeight.Normal,
                                                color = if (uiState.selectedSortOrder == sortOption) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
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
                            label = { Text(stringResource(filter.labelRes)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface,
                            ),
                            shape = MaterialTheme.shapes.large,
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
}

@Composable
private fun PrescriptionCard(prescription: PrescriptionItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
 val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
 cc.rhuan.receitas.ui.components.RecordCard(
  title = prescription.title, category = prescription.specialty.takeIf { it.isNotBlank() } ?: "Receita",
  iconRes = R.drawable.ic_bold_document_medicine, onClick = onClick, modifier = modifier,
  badge = { ExpirationBadge(prescription.daysUntilExpiration) },
 ) {
  if (prescription.doctorName.isNotBlank()) Text("Profissional: " + prescription.doctorName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
  Text(stringResource(R.string.issued_on, prescription.issueDate.format(dateFormatter)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
  prescription.notes?.takeIf { it.isNotBlank() }?.let { note ->
   Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
    Text(note, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
   }
  }
 }
}

@Composable
private fun ExpirationBadge(daysUntilExpiration: Int) {
    val isExpired = daysUntilExpiration < 0
    val (bgColor, textColor, labelText) = when {
        isExpired -> Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error, stringResource(R.string.expired))
        daysUntilExpiration == 0 -> Triple(AppPalette.warningContainer, AppPalette.warning, "Vence hoje")
        daysUntilExpiration <= 30 -> {
            val daysText = if (daysUntilExpiration == 1) stringResource(R.string.day) else stringResource(R.string.days)
            Triple(AppPalette.warningContainer, AppPalette.warning, stringResource(R.string.expires_in, daysUntilExpiration, daysText))
        }
        else -> Triple(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.tertiary, stringResource(R.string.valid))
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
            text = if (hasSearchQuery) stringResource(R.string.no_prescription_found) else stringResource(R.string.no_prescription_registered),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (hasSearchQuery) stringResource(R.string.try_searching_different_term) else stringResource(R.string.registered_prescriptions_appear_here),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        issueDate = LocalDate.of(2025, 1, 10),
                        notes = "Uso contínuo de anti-hipertensivo.",
                    ),
                    PrescriptionItem(
                        id = "2",
                        title = "Tratamento Dermatológico",
                        specialty = "Dermatologista",
                        doctorName = "Dra. Ana Paula",
                        issueDate = LocalDate.of(2025, 1, 25),
                    ),
                ),
            ),
            onSearchQueryChange = {},
            onFilterSelected = {},
        )
    }
}
