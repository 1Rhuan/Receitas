package cc.rhuan.receitas.ui.feature.prescriptions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import cc.rhuan.receitas.R
import cc.rhuan.receitas.domain.model.RecordState
import cc.rhuan.receitas.ui.components.*
import cc.rhuan.receitas.ui.theme.DesignTokens as T

/** Recipe membership uses prescription items, independently of the pickup tracking link. */
@Composable
internal fun PrescriptionMedicationList(state: PrescriptionDetailUiState, onBack: () -> Unit,
    onMedicationClick: (String) -> Unit, modifier: Modifier = Modifier,
    snackbarHost: @Composable () -> Unit = {}, onAddMedication: () -> Unit = {}) {
    val prescription = state.prescription
    val canAdd = prescription?.state == RecordState.ACTIVE
    val empty = state.medications.isEmpty()
    RecordListScaffold(
        title = stringResource(R.string.design_recipe_medications),
        subtitle = "", listTag = "recipe-medication-list", modifier = modifier,
        onBack = onBack, snackbarHost = snackbarHost,
        createLabel = if (canAdd && !empty) stringResource(R.string.ui_adicionar_medicamento) else null,
        onCreate = onAddMedication,
    ) {
        if (empty) item("empty") {
            CenteredListGuidance {
                EmptyListGuidance(stringResource(R.string.ui_nenhum_medicamento_vinculado_a_esta_receita),
                    stringResource(when (prescription?.state) {
                        RecordState.ARCHIVED -> R.string.recipe_empty_medications_archived
                        RecordState.REPLACED -> R.string.recipe_empty_medications_readonly
                        else -> R.string.recipe_empty_medications_guidance
                    }, prescription?.title.orEmpty()),
                    if (canAdd) stringResource(R.string.ui_adicionar_medicamento) else null,
                    onAddMedication, iconRes = R.drawable.ic_bold_pills)
            }
        } else {
            item("recipe") {
                Column(Modifier.padding(horizontal = T.margin), verticalArrangement = Arrangement.spacedBy(T.small)) {
                    Text(prescription?.title.orEmpty(), style = MaterialTheme.typography.titleMedium)
                    Text(pluralStringResource(R.plurals.design_medication_count, state.medications.size, state.medications.size),
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (prescription?.state == RecordState.ARCHIVED)
                        Text(stringResource(R.string.recipe_add_medication_requires_active), style = MaterialTheme.typography.bodyMedium)
                }
            }
            items(state.medications, key = { it.medication.id }) { detail ->
                MedicationDetailCard(detail.medication, Modifier.padding(horizontal = T.margin),
                    onMedicationClick = { onMedicationClick(detail.medication.id) })
            }
        }
    }
}
