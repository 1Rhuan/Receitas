package cc.rhuan.receitas.ui.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.domain.model.PrescriptionItem

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    onPrescriptionClick: (PrescriptionItem) -> Unit = {},
    onMedicationClick: (MedicationItem) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onNavigateToPrescriptions: () -> Unit = {},
    onNavigateToMedications: () -> Unit = {},
    onCreatePrescription: () -> Unit = {},
    onCreateMedication: () -> Unit = {},
    onRenewPrescription: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomePresentation(
        uiState = uiState,
        modifier = modifier,
        onPrescriptionClick = onPrescriptionClick,
        onMedicationClick = onMedicationClick,
        onNotificationClick = onNotificationClick,
        onNavigateToPrescriptions = onNavigateToPrescriptions,
        onNavigateToMedications = onNavigateToMedications,
        onCreatePrescription = onCreatePrescription,
        onCreateMedication = onCreateMedication,
        onRenewPrescription = onRenewPrescription,
        onDateSelected = viewModel::onDateSelected,
        registerPickup = viewModel::registerPickup,
        dismissSnackbar = viewModel::dismissSnackbar,
        refreshData = viewModel::refreshData,
    )
}
