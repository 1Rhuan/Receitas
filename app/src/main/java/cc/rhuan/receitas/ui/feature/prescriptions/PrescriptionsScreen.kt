package cc.rhuan.receitas.ui.feature.prescriptions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun PrescriptionsPresentation(
    modifier: Modifier = Modifier, uiState: PrescriptionsUiState,
    onSearchQueryChanged: (String) -> Unit, onFilterSelected: (PrescriptionFilter) -> Unit,
    onSortOrderSelected: (PrescriptionSortOrder) -> Unit, loadPrescriptions: () -> Unit,
    onPrescriptionClick: (PrescriptionItem) -> Unit = {}, onCreatePrescriptionClick: () -> Unit = {},
    onBack: (() -> Unit)? = null, onClearMedicationFilter: () -> Unit = {},
) = PrescriptionsScreenContent(uiState, onSearchQueryChanged, onFilterSelected, modifier,
    onSortOrderSelected, onPrescriptionClick, onCreatePrescriptionClick, loadPrescriptions,
    onBack, onClearMedicationFilter)

@Composable
fun PrescriptionsScreenContent(
    uiState: PrescriptionsUiState, onSearchQueryChange: (String) -> Unit,
    onFilterSelected: (PrescriptionFilter) -> Unit, modifier: Modifier = Modifier,
    onSortOrderSelected: (PrescriptionSortOrder) -> Unit = {},
    onPrescriptionClick: (PrescriptionItem) -> Unit = {}, onCreatePrescriptionClick: () -> Unit = {},
    onRetry: () -> Unit = {},
    onBack: (() -> Unit)? = null, onClearMedicationFilter: () -> Unit = {},
) {
    DesignTheme {
        val emptyLibrary = !uiState.isLoading && uiState.errorMessage == null && uiState.prescriptions.isEmpty() &&
            uiState.searchQuery.isBlank() && uiState.selectedFilter == PrescriptionFilter.ALL && uiState.medicationFilterId == null
        val count = if (uiState.medicationFilterId != null) uiState.filteredPrescriptions.size else uiState.prescriptions.size
        val tools = RecordListTools(uiState.searchQuery, onSearchQueryChange,
            stringResource(R.string.design_search_short), stringResource(R.string.search), stringResource(R.string.clear_search),
            listOf(
                ListOptionGroup(stringResource(R.string.list_options_sort),
                    PrescriptionSortOrder.entries.map { stringResource(it.labelRes) }, uiState.selectedSortOrder.ordinal),
                ListOptionGroup(stringResource(R.string.list_options_status),
                    PrescriptionFilter.entries.map { stringResource(it.labelRes) }, uiState.selectedFilter.ordinal)),
            stringResource(R.string.list_options_prescriptions), { choices ->
                onSortOrderSelected(PrescriptionSortOrder.entries[choices[0]])
                onFilterSelected(PrescriptionFilter.entries[choices[1]])
            })
        RecordListScaffold(
            title = stringResource(R.string.label_prescription_list),
            subtitle = if (emptyLibrary) "" else pluralStringResource(R.plurals.design_record_count, count, count),
            listTag = "prescription-list", modifier = modifier,
            onBack = onBack, floatingTools = if (emptyLibrary) null else tools,
            floatingAfterItems = if (uiState.medicationFilterId == null) 3 else 4,
        ) {
                if (uiState.medicationFilterId != null) item("medication-filter") {
                    ContentCard(Modifier.padding(horizontal = T.margin)) {
                        Text(stringResource(R.string.design_recipes_for_medication), style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        uiState.medications.find { it.id == uiState.medicationFilterId }?.let {
                            Text(it.title, style = MaterialTheme.typography.titleMedium)
                        }
                        SecondaryActionButton(stringResource(R.string.design_show_all_recipes), onClearMedicationFilter, Modifier.fillMaxWidth())
                    }
                }
                if (!emptyLibrary) item("search") {
                    SearchField(uiState.searchQuery, onSearchQueryChange,
                        label = stringResource(R.string.design_search_short), shortLabel = stringResource(R.string.search),
                        clearLabel = stringResource(R.string.clear_search),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = T.margin))
                }
                if (!emptyLibrary) item("filters") {
                    ListOptionsMenu(tools.optionGroups, tools.optionsLabel, tools.onOptionsApply,
                        Modifier.padding(horizontal = T.margin))
                }
                if (!emptyLibrary) item("list_title") { Column(Modifier.padding(horizontal = T.margin)) {
                    SectionHeading(stringResource(R.string.design_your_prescriptions))
                } }
                when {
                    uiState.isLoading -> item { Box(Modifier.fillMaxWidth().padding(T.section), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                    uiState.errorMessage != null -> item { LoadErrorPanel(uiState.errorMessage, onRetry, Modifier.padding(T.margin)) }
                    emptyLibrary -> item {
                        CenteredListGuidance {
                            EmptyListGuidance(stringResource(R.string.no_prescription_registered),
                                stringResource(R.string.design_first_prescription), stringResource(R.string.action_new_prescription),
                                onCreatePrescriptionClick, iconRes = R.drawable.ic_bold_document_medicine)
                        }
                    }
                    uiState.filteredPrescriptions.isEmpty() -> item {
                        Column(Modifier.padding(horizontal = T.margin)) {
                            ContentCard {
                                Text(stringResource(if (uiState.medicationFilterId != null || uiState.searchQuery.isNotBlank() || uiState.selectedFilter != PrescriptionFilter.ALL)
                                    R.string.no_prescription_found else R.string.no_prescription_registered), style = MaterialTheme.typography.titleMedium)
                                Text(stringResource(if (uiState.medicationFilterId != null || uiState.searchQuery.isNotBlank() || uiState.selectedFilter != PrescriptionFilter.ALL) R.string.design_empty_filtered
                                    else R.string.registered_prescriptions_appear_here), style = MaterialTheme.typography.bodyMedium)
                                if (uiState.searchQuery.isNotBlank() || uiState.selectedFilter != PrescriptionFilter.ALL) {
                                    TextButton(onClick = { onSearchQueryChange(""); onFilterSelected(PrescriptionFilter.ALL) }) {
                                        Text(stringResource(R.string.design_clear_filters))
                                    }
                                }
                            }
                        }
                    }
                    else -> items(uiState.filteredPrescriptions, key = { it.id }) { prescription ->
                        PrescriptionCard(prescription, { onPrescriptionClick(prescription) }, Modifier.padding(horizontal = T.margin))
                    }
                }
        }
    }
}

@Composable
private fun PrescriptionCard(prescription: PrescriptionItem, onClick: () -> Unit, modifier: Modifier) {
    ContentCard(modifier.clickable(onClick = onClick)) {
        Text(prescription.specialty.ifBlank { stringResource(R.string.label_prescription) },
            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(prescription.title, style = MaterialTheme.typography.titleMedium)
        if (prescription.doctorName.isNotBlank()) Text(stringResource(R.string.professional_name, prescription.doctorName),
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(stringResource(R.string.issued_on, prescription.issueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))),
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        val label = when {
            !prescription.isActive -> stringResource(if (prescription.state == RecordState.ARCHIVED) R.string.state_archived else R.string.state_replaced)
            prescription.isExpired -> stringResource(R.string.expired)
            prescription.daysUntilExpiration == 0 -> stringResource(R.string.expires_today_alternate)
            prescription.daysUntilExpiration <= 30 -> pluralStringResource(R.plurals.prescription_expires_in_days,
                prescription.daysUntilExpiration, prescription.daysUntilExpiration)
            else -> stringResource(R.string.valid)
        }
        StatusLabel(label, prescription.isActive && prescription.daysUntilExpiration <= 30)
        prescription.notes?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
    }
}

fun designPreviewState(texts: cc.rhuan.receitas.text.TextResolver) = PrescriptionsUiState(
    prescriptions = listOf(
        PrescriptionItem("preview-1", texts.get(R.string.preview_continuous_treatment), texts.get(R.string.preview_cardiology), doctorName = texts.get(R.string.preview_doctor_name),
            issueDate = LocalDate.now().minusDays(30), notes = texts.get(R.string.preview_prescription_note)),
        PrescriptionItem("preview-2", texts.get(R.string.preview_blood_pressure_treatment), texts.get(R.string.preview_general_practice),
            doctorName = texts.get(R.string.preview_second_doctor), issueDate = LocalDate.now().minusDays(165))),
    medications = listOf(MedicationItem("preview-med", texts.get(R.string.preview_losartan_dosage),
        lastRefillDate = LocalDate.now().minusDays(27))))

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PrescriptionsLightPreview() { AppTheme { PrescriptionsScreenContent(designPreviewState(cc.rhuan.receitas.text.AndroidTextResolver(androidx.compose.ui.platform.LocalContext.current)), {}, {}) } }

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PrescriptionsDarkPreview() { AppTheme(darkTheme = true) { PrescriptionsScreenContent(designPreviewState(cc.rhuan.receitas.text.AndroidTextResolver(androidx.compose.ui.platform.LocalContext.current)), {}, {}) } }

@Preview(showBackground = true, widthDp = 320, heightDp = 640, fontScale = 2f)
@Composable
private fun PrescriptionsSmallPreview() { AppTheme { PrescriptionsScreenContent(designPreviewState(cc.rhuan.receitas.text.AndroidTextResolver(androidx.compose.ui.platform.LocalContext.current)), {}, {}) } }
