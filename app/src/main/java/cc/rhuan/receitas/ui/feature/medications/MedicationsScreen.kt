package cc.rhuan.receitas.ui.feature.medications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import cc.rhuan.receitas.ui.components.AppCard as Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cc.rhuan.receitas.R
import cc.rhuan.receitas.domain.model.MedicationItem

@Composable
fun MedicationsScreen(
    modifier: Modifier = Modifier,
    viewModel: MedicationsViewModel = viewModel(),
    onMedicationClick: (MedicationItem) -> Unit = {},
    onAddMedicationClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.dismissSnackbar()
        }
    }

    MedicationsScreenContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChanged,
        onFilterSelected = viewModel::onSourceFilterSelected,
        onReplenishStock = viewModel::replenishStock,
        onMedicationClick = onMedicationClick,
        onAddMedicationClick = onAddMedicationClick,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationsScreenContent(
    uiState: MedicationsUiState,
    onSearchQueryChange: (String) -> Unit,
    onFilterSelected: (String) -> Unit,
    onReplenishStock: (String) -> Unit,
    onMedicationClick: (MedicationItem) -> Unit,
    onAddMedicationClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val filterOptions = listOf("TODOS", "Compra Própria", "Farmácia Popular", "Farmácia Básica (SUS)")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            androidx.compose.material3.ExtendedFloatingActionButton(
                onClick = onAddMedicationClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.large,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Novo medicamento") },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Meus Medicamentos",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Consulte seus medicamentos, reposições e datas de retirada",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Busca
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar medicamento ou concentração...", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filtros de origem
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(filterOptions) { filter ->
                    val isSelected = uiState.selectedSourceFilter.equals(filter, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterSelected(filter) },
                        label = {
                            Text(
                                text = filter,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = MaterialTheme.shapes.large,
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (uiState.filteredMedications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_pills),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (uiState.searchQuery.isNotEmpty()) "Nenhum medicamento encontrado" else "Nenhum medicamento cadastrado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Adicione receitas com medicamentos para visualizá-los aqui.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(
                        items = uiState.filteredMedications,
                        key = { it.id }
                    ) { medication ->
                        MedicationCardItem(
                            medication = medication,
                            onClick = { onMedicationClick(medication) },
                            onReplenishStock = { onReplenishStock(medication.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MedicationCardItem(
 medication: MedicationItem, onClick: () -> Unit, onReplenishStock: () -> Unit,
 modifier: Modifier = Modifier,
) {
 var confirmPickup by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
 if (confirmPickup) androidx.compose.material3.AlertDialog(
  onDismissRequest = { confirmPickup = false }, title = { Text("Registrar retirada?") },
  text = { Text("Confirme que você retirou o medicamento. A data da última retirada será atualizada para hoje.") },
  confirmButton = { androidx.compose.material3.TextButton(onClick = { confirmPickup = false; onReplenishStock() }) { Text("Confirmar") } },
  dismissButton = { androidx.compose.material3.TextButton(onClick = { confirmPickup = false }) { Text("Cancelar") } },
 )
 cc.rhuan.receitas.ui.components.RecordCard(
  title = medication.title, category = "Medicamento", iconRes = R.drawable.ic_bold_pills,
  onClick = onClick, modifier = modifier,
  badge = {
   Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.secondaryContainer) {
    Text(medication.dosage.ifBlank { medication.form.label }, Modifier.padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
   }
  },
 ) {
  if (!medication.activeIngredient.isNullOrBlank()) Text(medication.activeIngredient, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
  Text("Origem: " + medication.source.label + (medication.sourceName?.takeIf { it.isNotBlank() && !it.equals(medication.source.label, ignoreCase = true) }?.let { " · " + it } ?: ""), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
  if (medication.quantity.isNotBlank()) Text("Quantidade por reposição: " + medication.quantity, style = MaterialTheme.typography.bodyMedium)
  if (medication.hasRecurringPickup) {
   medication.lastRefillDate?.let { Text("Última retirada: " + it.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")), style = MaterialTheme.typography.bodySmall) }
   medication.nextAvailableRefillDate?.let { Text("Próxima retirada prevista: " + it.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")), style = MaterialTheme.typography.bodySmall) }
   OutlinedButton(onClick = { confirmPickup = true }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) { Text("Registrar retirada") }
  }
  if (medication.prescribedPosology.isNotBlank()) Text(medication.prescribedPosology, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
 }
}
