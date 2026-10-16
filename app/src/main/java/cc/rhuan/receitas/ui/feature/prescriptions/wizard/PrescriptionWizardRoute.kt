package cc.rhuan.receitas.ui.feature.prescriptions.wizard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PrescriptionWizardScreen(
    viewModel: PrescriptionWizardViewModel,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    onViewSaved: (String?) -> Unit = { onNavigateUp() },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isSaved) {
        if (state.isSaved && state.addToPrescriptionId != null) onNavigateUp()
    }
    PrescriptionWizardPresentation(
        state = state,
        onNavigateUp = onNavigateUp,
        actions = WizardCallbacks(
            skip = viewModel::skipStep,
            updateIssueDate = viewModel::updateIssueDate,
            openTypeEditor = viewModel::openTypeEditor,
            confirmTypeEditor = viewModel::confirmTypeEditor,
            cancelTypeEditor = viewModel::cancelTypeEditor,
            setValidityDateMode = viewModel::setValidityDateMode,
            selectType = viewModel::selectPrescriptionType,
            updateValidityDays = viewModel::updateValidityDays,
            updateExpirationDate = viewModel::updateExpirationDate,
            selectDoctor = viewModel::selectDoctor, selectMedication = viewModel::selectExistingMedication,
            updatePickupInterval = viewModel::updatePickupInterval,
            update = viewModel::updateForm, next = viewModel::nextStep,
            back = viewModel::previousStep, save = viewModel::savePrescription,
            commitMedication = { viewModel.commitMedicationDraft() },
            addMedication = viewModel::startNewMedication, editMedication = viewModel::editMedication,
            removeMedication = viewModel::removeMedication, cancelMedication = viewModel::cancelMedicationEditor,
            retry = viewModel::retryInitialLoad, goToStep = viewModel::goToStep,
            complete = { onViewSaved(state.savedPrescriptionId) },
        ),
        modifier = modifier,
    )
}
