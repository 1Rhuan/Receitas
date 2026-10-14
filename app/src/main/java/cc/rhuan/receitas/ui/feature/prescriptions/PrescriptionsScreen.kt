package cc.rhuan.receitas.ui.feature.prescriptions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
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
    onPurchaseClick: (MedicationItem) -> Unit = {},
) = PrescriptionsScreenContent(uiState, onSearchQueryChanged, onFilterSelected, modifier,
    onSortOrderSelected, onPrescriptionClick, onCreatePrescriptionClick, loadPrescriptions, onPurchaseClick)

@Composable
fun PrescriptionsScreenContent(
    uiState: PrescriptionsUiState, onSearchQueryChange: (String) -> Unit,
    onFilterSelected: (PrescriptionFilter) -> Unit, modifier: Modifier = Modifier,
    onSortOrderSelected: (PrescriptionSortOrder) -> Unit = {},
    onPrescriptionClick: (PrescriptionItem) -> Unit = {}, onCreatePrescriptionClick: () -> Unit = {},
    onRetry: () -> Unit = {}, onPurchaseClick: (MedicationItem) -> Unit = {},
) {
    DesignTheme {
        val emptyLibrary = !uiState.isLoading && uiState.errorMessage == null && uiState.prescriptions.isEmpty() &&
            uiState.searchQuery.isBlank() && uiState.selectedFilter == PrescriptionFilter.ALL
        RecordListScaffold(
            title = stringResource(R.string.label_prescription_list),
            subtitle = if (emptyLibrary) "" else pluralStringResource(R.plurals.design_record_count, uiState.prescriptions.size, uiState.prescriptions.size),
            listTag = "prescription-list", modifier = modifier,
        ) {
                if (!uiState.isLoading && uiState.errorMessage == null && (uiState.nextPurchase != null || uiState.purchaseError)) {
                    item("purchase") {
                        Column(Modifier.padding(horizontal = T.margin)) {
                            val purchase = uiState.nextPurchase
                            val openLabel = stringResource(R.string.design_purchase_open)
                            ContentCard(if (purchase != null && !uiState.purchaseError) Modifier.clickable(onClickLabel = openLabel) { onPurchaseClick(purchase) } else Modifier) {
                                Text(stringResource(R.string.design_next_purchase), style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                when {
                                    uiState.purchaseError -> {
                                        Text(stringResource(R.string.design_purchase_error))
                                        TextButton(onClick = onRetry) { Text(stringResource(R.string.design_retry)) }
                                    }
                                    purchase == null -> Text(stringResource(R.string.design_no_purchase),
                                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    else -> {
                                        Text(purchase.title, style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                                        Text(stringResource(R.string.design_purchase_date,
                                            purchase.nextAvailableRefillDate!!.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))),
                                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
                if (!emptyLibrary) item("search") {
                    SearchField(uiState.searchQuery, onSearchQueryChange,
                        label = stringResource(R.string.design_search_short), shortLabel = stringResource(R.string.search),
                        clearLabel = stringResource(R.string.clear_search),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = T.margin))
                }
                if (!emptyLibrary) item("filters") {
                    LazyRow(contentPadding = PaddingValues(horizontal = T.margin), horizontalArrangement = Arrangement.spacedBy(T.small)) {
                        item {
                            var menu by remember { mutableStateOf(false) }
                            Box {
                                FilterPill(selected = uiState.selectedSortOrder != PrescriptionSortOrder.EXPIRATION,
                                    onClick = { menu = true }, label = stringResource(uiState.selectedSortOrder.labelRes))
                                DropdownMenu(menu, { menu = false }) {
                                    PrescriptionSortOrder.entries.forEach { order ->
                                        DropdownMenuItem(text = { Text(stringResource(order.labelRes)) },
                                            trailingIcon = { if (order == uiState.selectedSortOrder) Icon(Icons.Default.Check, null) },
                                            onClick = { onSortOrderSelected(order); menu = false })
                                    }
                                }
                            }
                        }
                        items(PrescriptionFilter.entries) { filter ->
                            FilterPill(uiState.selectedFilter == filter, stringResource(filter.labelRes), { onFilterSelected(filter) })
                        }
                    }
                }
                if (!emptyLibrary) item("list_title") { Column(Modifier.padding(horizontal = T.margin)) {
                    SectionHeading(stringResource(R.string.design_your_prescriptions))
                } }
                when {
                    uiState.isLoading -> item { Box(Modifier.fillMaxWidth().padding(T.section), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                    uiState.errorMessage != null -> item { LoadErrorPanel(uiState.errorMessage, onRetry, Modifier.padding(T.margin)) }
                    emptyLibrary -> item {
                        EmptyStateCard(stringResource(R.string.no_prescription_registered),
                            stringResource(R.string.design_first_prescription), null,
                            onCreatePrescriptionClick, Modifier.padding(horizontal = T.margin))
                    }
                    uiState.filteredPrescriptions.isEmpty() -> item {
                        Column(Modifier.padding(horizontal = T.margin)) {
                            ContentCard {
                                Text(stringResource(if (uiState.searchQuery.isNotBlank() || uiState.selectedFilter != PrescriptionFilter.ALL)
                                    R.string.no_prescription_found else R.string.no_prescription_registered), style = MaterialTheme.typography.titleMedium)
                                Text(stringResource(if (uiState.searchQuery.isNotBlank() || uiState.selectedFilter != PrescriptionFilter.ALL) R.string.design_empty_filtered
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
            !prescription.isActive -> stringResource(if (prescription.state == RecordState.ARCHIVED) R.string.ui_arquivada else R.string.ui_substituida)
            prescription.isExpired -> stringResource(R.string.expired)
            prescription.daysUntilExpiration == 0 -> stringResource(R.string.ui_vence_hoje_2)
            prescription.daysUntilExpiration <= 30 -> pluralStringResource(R.plurals.prescription_expires_in_days,
                prescription.daysUntilExpiration, prescription.daysUntilExpiration)
            else -> stringResource(R.string.valid)
        }
        StatusLabel(label, prescription.isActive && prescription.daysUntilExpiration <= 30)
        prescription.notes?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
    }
}

fun designPreviewState() = PrescriptionsUiState(
    prescriptions = listOf(
        PrescriptionItem("preview-1", "Tratamento de uso contínuo", "Cardiologia", doctorName = "Dra. Ana Paula",
            issueDate = LocalDate.now().minusDays(30), notes = "Apresentar a receita na próxima compra."),
        PrescriptionItem("preview-2", "Acompanhamento e controle da pressão arterial", "Clínica médica",
            doctorName = "Dr. Carlos Eduardo", issueDate = LocalDate.now().minusDays(165))),
    medications = listOf(MedicationItem("preview-med", "Losartana potássica · 50 mg",
        lastRefillDate = LocalDate.now().minusDays(27))))

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PrescriptionsLightPreview() { ReceitasTheme { PrescriptionsScreenContent(designPreviewState(), {}, {}) } }

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PrescriptionsDarkPreview() { ReceitasTheme(darkTheme = true) { PrescriptionsScreenContent(designPreviewState(), {}, {}) } }

@Preview(showBackground = true, widthDp = 320, heightDp = 640, fontScale = 2f)
@Composable
private fun PrescriptionsSmallPreview() { ReceitasTheme { PrescriptionsScreenContent(designPreviewState(), {}, {}) } }
