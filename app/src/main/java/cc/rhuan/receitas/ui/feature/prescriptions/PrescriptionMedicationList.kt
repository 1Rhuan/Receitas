package cc.rhuan.receitas.ui.feature.prescriptions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import cc.rhuan.receitas.R
import cc.rhuan.receitas.ui.components.*
import cc.rhuan.receitas.ui.theme.DesignTokens as T

/** Recipe membership uses prescription items, independently of the pickup tracking link. */
@Composable
internal fun PrescriptionMedicationList(state: PrescriptionDetailUiState, onBack: () -> Unit,
    onMedicationClick: (String) -> Unit, modifier: Modifier = Modifier,
    snackbarHost: @Composable () -> Unit = {}) {
    Scaffold(modifier.fillMaxSize(), containerColor = MaterialTheme.colorScheme.background,
        topBar = { DetailHeader(stringResource(R.string.design_recipe_medications), onBack) },
        snackbarHost = snackbarHost) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("recipe-medication-list"),
            contentPadding = PaddingValues(horizontal = T.margin, vertical = T.padding),
            verticalArrangement = Arrangement.spacedBy(T.gap)) {
            item("recipe") {
                Text(state.prescription?.title.orEmpty(), style = MaterialTheme.typography.titleLarge)
                Text(pluralStringResource(R.plurals.design_medication_count, state.medications.size, state.medications.size),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (state.medications.isEmpty()) item("empty") {
                ContentCard { Text(stringResource(R.string.ui_nenhum_medicamento_vinculado_a_esta_receita)) }
            }
            items(state.medications, key = { it.medication.id }) { detail ->
                MedicationDetailCard(detail.medication, onMedicationClick = { onMedicationClick(detail.medication.id) })
            }
        }
    }
}
