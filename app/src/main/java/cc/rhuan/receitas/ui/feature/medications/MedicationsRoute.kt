package cc.rhuan.receitas.ui.feature.medications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cc.rhuan.receitas.domain.model.MedicationItem

@Composable
fun MedicationsScreen(
    modifier: Modifier = Modifier,
    viewModel: MedicationsViewModel = viewModel(),
    onMedicationClick: (MedicationItem) -> Unit = {},
    onAddMedicationClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MedicationsPresentation(
        uiState = uiState,
        modifier = modifier,
        onMedicationClick = onMedicationClick,
        onAddMedicationClick = onAddMedicationClick,
        registerPickup = viewModel::registerPickup,
        dismissSnackbar = viewModel::dismissSnackbar,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onSourceFilterSelected = viewModel::onSourceFilterSelected,
        loadMedications = viewModel::loadMedications,
    )
}
