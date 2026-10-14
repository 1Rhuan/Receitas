package cc.rhuan.receitas.ui.feature.notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier,
    viewModel: NotificationsViewModel = viewModel(),
    onNavigateToPrescription: (String) -> Unit = {},
    onRenewPrescription: (String) -> Unit = {},
    onNavigateToMedication: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NotificationsPresentation(
        uiState = uiState,
        modifier = modifier,
        onNavigateToPrescription = onNavigateToPrescription,
        onRenewPrescription = onRenewPrescription,
        onNavigateToMedication = onNavigateToMedication,
        registerPickup = viewModel::registerPickup,
        onFilterSelected = viewModel::onFilterSelected,
        markAsRead = viewModel::markAsRead,
        markAllAsRead = viewModel::markAllAsRead,
        deleteNotification = viewModel::deleteNotification,
        clearAll = viewModel::clearAll,
        openRenewal = viewModel::openRenewal,
        preparePickup = viewModel::preparePickup,
        dismissSnackbar = viewModel::dismissSnackbar,
        refresh = viewModel::refresh,
    )
}
