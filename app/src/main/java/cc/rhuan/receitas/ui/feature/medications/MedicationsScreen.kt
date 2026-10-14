package cc.rhuan.receitas.ui.feature.medications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.Preview
import cc.rhuan.receitas.R
import cc.rhuan.receitas.domain.model.*
import cc.rhuan.receitas.ui.components.*
import cc.rhuan.receitas.ui.theme.*
import cc.rhuan.receitas.ui.theme.DesignTokens as T
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun MedicationsPresentation(
    modifier: Modifier = Modifier, uiState: MedicationsUiState,
    registerPickup: (String, LocalDate, Boolean, LocalDate?) -> Unit,
    dismissSnackbar: () -> Unit, onSearchQueryChanged: (String) -> Unit,
    onSourceFilterSelected: (String) -> Unit, loadMedications: () -> Unit,
    onMedicationClick: (MedicationItem) -> Unit = {}, onAddMedicationClick: () -> Unit = {},
) {
    DesignTheme {
        var pendingPickup by remember { mutableStateOf<MedicationItem?>(null) }
        pendingPickup?.let { medication ->
            PickupDialog(medication, onDismiss = { pendingPickup = null }) { date, early, cycle ->
                registerPickup(medication.id, date, early, cycle)
            }
        }
        val snackbar = remember { SnackbarHostState() }
        LaunchedEffect(uiState.snackbarMessage) {
            uiState.snackbarMessage?.let { snackbar.showSnackbar(it); dismissSnackbar() }
        }
        MedicationsScreenContent(uiState, onSearchQueryChanged, onSourceFilterSelected,
            onRegisterPickup = { id -> pendingPickup = uiState.medications.find { it.id == id } },
            onMedicationClick = onMedicationClick, onAddMedicationClick = onAddMedicationClick,
            snackbarHostState = snackbar, modifier = modifier, onRetry = loadMedications)
    }
}

@Composable
fun MedicationsScreenContent(
    uiState: MedicationsUiState, onSearchQueryChange: (String) -> Unit,
    onFilterSelected: (String) -> Unit, onRegisterPickup: (String) -> Unit,
    onMedicationClick: (MedicationItem) -> Unit, onAddMedicationClick: () -> Unit,
    snackbarHostState: SnackbarHostState, modifier: Modifier = Modifier, onRetry: () -> Unit = {},
) {
    val emptyLibrary = !uiState.isLoading && uiState.errorMessage == null && uiState.medications.isEmpty() &&
        uiState.searchQuery.isBlank() && uiState.selectedSourceFilter.equals("TODOS", true)
    val sources = listOf("TODOS") + MedicationSource.entries.map { it.label }
    RecordListScaffold(title = stringResource(R.string.design_medications_title),
        subtitle = if (emptyLibrary) "" else pluralStringResource(R.plurals.design_medication_count,
            uiState.medications.size, uiState.medications.size),
        listTag = "medication-list", modifier = modifier,
        createLabel = stringResource(R.string.ui_novo_medicamento), onCreate = onAddMedicationClick,
        snackbarHost = { SnackbarHost(snackbarHostState) }) {
        if (!emptyLibrary) {
            item("search") {
                SearchField(uiState.searchQuery, onSearchQueryChange,
                    stringResource(R.string.design_medication_search), stringResource(R.string.search),
                    stringResource(R.string.clear_search), Modifier.fillMaxWidth().padding(horizontal = T.margin))
            }
            item("sources") {
                androidx.compose.foundation.lazy.LazyRow(modifier = Modifier.testTag("medication-sources"), contentPadding = PaddingValues(horizontal = T.margin),
                    horizontalArrangement = Arrangement.spacedBy(T.small)) {
                    items(sources) { source ->
                        FilterPill(uiState.selectedSourceFilter.equals(source, true),
                            if (source == "TODOS") stringResource(R.string.design_all_medications) else source,
                            { onFilterSelected(source) })
                    }
                }
            }
            item("section") { Column(Modifier.padding(horizontal = T.margin)) {
                SectionHeading(stringResource(R.string.design_your_medications))
            } }
        }
        when {
            uiState.isLoading -> item { Box(Modifier.fillMaxWidth().padding(T.section), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            uiState.errorMessage != null -> item { LoadErrorPanel(uiState.errorMessage, onRetry, Modifier.padding(T.margin)) }
            emptyLibrary -> item {
                EmptyStateCard(stringResource(R.string.ui_nenhum_medicamento_cadastrado),
                    stringResource(R.string.design_first_medication), null,
                    onAddMedicationClick, Modifier.padding(horizontal = T.margin))
            }
            uiState.filteredMedications.isEmpty() -> item { Column(Modifier.padding(horizontal = T.margin)) {
                ContentCard {
                    Text(stringResource(R.string.ui_nenhum_medicamento_encontrado), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.design_medication_no_results), style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = { onSearchQueryChange(""); onFilterSelected("TODOS") }) {
                        Text(stringResource(R.string.design_clear_filters))
                    }
                }
            } }
            else -> items(uiState.filteredMedications, key = { it.id }) { medication ->
                MedicationCardItem(medication, { onMedicationClick(medication) }, { onRegisterPickup(medication.id) },
                    Modifier.padding(horizontal = T.margin))
            }
        }
    }
}

@Composable
fun MedicationCardItem(medication: MedicationItem, onClick: () -> Unit, onRegisterPickup: () -> Unit,
    modifier: Modifier = Modifier) {
    val format = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    ContentCard(modifier.clickable(onClick = onClick)) {
        Text(medication.source.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(medication.title, style = MaterialTheme.typography.titleMedium)
        StatusLabel(medication.dosage.ifBlank { medication.form.label })
        medication.activeIngredient?.takeIf { it.isNotBlank() }?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        medication.sourceName?.takeIf { it.isNotBlank() && !it.equals(medication.source.label, true) }?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (medication.quantity.isNotBlank()) Text(stringResource(R.string.pickup_quantity, medication.quantity), style = MaterialTheme.typography.bodyMedium)
        if (medication.prescribedPosology.isNotBlank()) Text(medication.prescribedPosology, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (medication.hasRecurringPickup && !medication.trackingArchived) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            medication.lastRefillDate?.let { Text(stringResource(R.string.last_pickup_date, it.format(format)),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            medication.nextAvailableRefillDate?.let { Text(stringResource(R.string.next_pickup_date, it.format(format)),
                style = MaterialTheme.typography.bodyMedium) }
            SecondaryActionButton(stringResource(R.string.action_register_pickup), onRegisterPickup, Modifier.fillMaxWidth())
        }
        medication.trackingError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
        if (medication.trackingArchived) StatusLabel(stringResource(R.string.ui_acompanhamento_arquivado))
    }
}

fun medicationPreviewState(): MedicationsUiState {
    val medications = listOf(
        MedicationItem("med-preview-1", "Losartana potássica", activeIngredient = "Losartana", dosage = "50 mg",
            quantity = "30 comprimidos", prescribedPosology = "Tomar 1 comprimido por dia.",
            lastRefillDate = LocalDate.now().minusDays(27)),
        MedicationItem("med-preview-2", "Cloridrato de metformina de liberação prolongada", dosage = "500 mg",
            quantity = "60 comprimidos", source = MedicationSource.FARMACIA_BASICA, sourceName = "Unidade de Saúde",
            lastRefillDate = LocalDate.now().minusDays(35)))
    return MedicationsUiState(medications = medications, filteredMedications = medications)
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun MedicationsPreview() { ReceitasTheme {
    MedicationsScreenContent(medicationPreviewState(), {}, {}, {}, {}, {}, remember { SnackbarHostState() })
} }

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun MedicationsDarkPreview() { ReceitasTheme(darkTheme = true) {
    MedicationsScreenContent(medicationPreviewState(), {}, {}, {}, {}, {}, remember { SnackbarHostState() })
} }

@Preview(showBackground = true, widthDp = 320, heightDp = 640, fontScale = 2f)
@Composable
private fun MedicationsLargeTextPreview() { ReceitasTheme {
    MedicationsScreenContent(medicationPreviewState(), {}, {}, {}, {}, {}, remember { SnackbarHostState() })
} }
