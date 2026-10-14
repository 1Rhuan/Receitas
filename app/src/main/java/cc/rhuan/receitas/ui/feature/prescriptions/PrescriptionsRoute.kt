package cc.rhuan.receitas.ui.feature.prescriptions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    onPurchaseClick: (cc.rhuan.receitas.domain.model.MedicationItem) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PrescriptionsPresentation(
        uiState = uiState,
        onPurchaseClick = onPurchaseClick,
        modifier = modifier,
        onPrescriptionClick = onPrescriptionClick,
        onCreatePrescriptionClick = onCreatePrescriptionClick,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onFilterSelected = viewModel::onFilterSelected,
        onSortOrderSelected = viewModel::onSortOrderSelected,
        loadPrescriptions = viewModel::loadPrescriptions,
    )
}
