package cc.rhuan.receitas.ui.feature.medications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MedicationDetailScreen(viewModel: MedicationDetailViewModel, onBack: () -> Unit, onEdit: (String) -> Unit,
    onPrescriptionsClick: (String) -> Unit = {}) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    MedicationDetailContent(
        state = state, message = message, onBack = onBack, onEdit = onEdit,
        onClearMessage = viewModel::clearMessage, onPickup = viewModel::pickup,
        onDelete = viewModel::delete, onArchive = viewModel::archive,
        onRetry = viewModel::loadMedication, onCorrectPickup = viewModel::correct,
        onDeletePickup = viewModel::deleteEvent,
        onPrescriptionsClick = onPrescriptionsClick,
    )
}
