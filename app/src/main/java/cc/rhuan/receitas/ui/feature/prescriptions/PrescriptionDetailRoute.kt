package cc.rhuan.receitas.ui.feature.prescriptions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PrescriptionDetailScreen(
    viewModel: PrescriptionDetailViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onEditClick: (String) -> Unit = {},
    onRenewClick: (String) -> Unit = {},
    onMedicationClick: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PrescriptionDetailPresentation(
        uiState = uiState,
        onBackClick = onBackClick,
        modifier = modifier,
        onEditClick = onEditClick,
        onRenewClick = onRenewClick,
        onMedicationClick = onMedicationClick,
        registerPickup = viewModel::registerPickup,
        deleteRecipe = viewModel::deleteRecipe,
        archive = viewModel::archive,
        clearActionMessage = viewModel::clearActionMessage,
        correctPickup = viewModel::correctPickup,
        deletePickup = viewModel::deletePickup,
        loadDetail = viewModel::loadDetail,
    )
}
