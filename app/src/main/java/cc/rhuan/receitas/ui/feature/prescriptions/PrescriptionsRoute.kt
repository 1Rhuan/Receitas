package cc.rhuan.receitas.ui.feature.prescriptions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cc.rhuan.receitas.domain.model.PrescriptionItem

@Composable
fun PrescriptionsScreen(
    modifier: Modifier = Modifier,
    viewModel: PrescriptionsViewModel = viewModel(),
    onPrescriptionClick: (PrescriptionItem) -> Unit = {},
    onCreatePrescriptionClick: () -> Unit = {},
    medicationFilterId: String? = null,
    onBack: (() -> Unit)? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var filterApplied by rememberSaveable(medicationFilterId) { mutableStateOf(true) }
    PrescriptionsPresentation(
        uiState = uiState.copy(medicationFilterId = medicationFilterId.takeIf { filterApplied }),
        onBack = onBack, onClearMedicationFilter = { filterApplied = false },
        modifier = modifier,
        onPrescriptionClick = onPrescriptionClick,
        onCreatePrescriptionClick = onCreatePrescriptionClick,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onFilterSelected = viewModel::onFilterSelected,
        onSortOrderSelected = viewModel::onSortOrderSelected,
        loadPrescriptions = viewModel::loadPrescriptions,
    )
}
