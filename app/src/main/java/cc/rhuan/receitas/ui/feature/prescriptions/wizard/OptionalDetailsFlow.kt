package cc.rhuan.receitas.ui.feature.prescriptions.wizard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cc.rhuan.receitas.R
import cc.rhuan.receitas.domain.model.PharmaceuticalForm
import cc.rhuan.receitas.ui.components.*
import cc.rhuan.receitas.ui.components.wizard.WizardQuestionScreen
import java.io.Serializable

internal enum class OptionalDetailKind { PROFESSIONAL, NOTES, DOSAGE, FORM, INGREDIENT, QUANTITY, INSTRUCTIONS }

/** A separate, restorable transaction: only confirmation applies these values to registration. */
internal data class OptionalDetailDraft(
    val kind: OptionalDetailKind,
    val medicationIndex: Int? = null,
    val value: String = "",
    val registration: String = "",
    val specialty: String = "",
    val selectedDoctorId: String? = null,
    val healthFacility: String = "",
    val phone: String = "",
    val form: PharmaceuticalForm = PharmaceuticalForm.TABLET,
) : Serializable {
    fun applyTo(state: PrescriptionWizardUiState): PrescriptionWizardUiState = when (kind) {
        OptionalDetailKind.PROFESSIONAL -> state.copy(doctorName = value, doctorRegistration = registration,
            doctorSpecialty = specialty, selectedDoctorId = selectedDoctorId, healthFacility = healthFacility, doctorPhone = phone)
        OptionalDetailKind.NOTES -> state.copy(notes = value)
        else -> {
            fun update(item: MedicationDraft) = when (kind) {
                OptionalDetailKind.DOSAGE -> item.copy(dosageOverride = value)
                OptionalDetailKind.FORM -> item.copy(form = form)
                OptionalDetailKind.INGREDIENT -> item.copy(activeIngredient = value)
                OptionalDetailKind.QUANTITY -> item.copy(prescribedQuantity = value)
                OptionalDetailKind.INSTRUCTIONS -> item.copy(prescribedPosology = value)
            }
            if (state.showMedicationEditor && (state.editingMedicationIndex ?: state.medications.size) == medicationIndex)
                state.copy(currentMedicationDraft = update(state.currentMedicationDraft))
            else state.copy(medications = state.medications.mapIndexed { index, item -> if (index == medicationIndex) update(item) else item })
        }
    }
}

@Composable
private fun detailLabel(kind: OptionalDetailKind): String = stringResource(when (kind) {
    OptionalDetailKind.PROFESSIONAL -> R.string.responsible_professional
    OptionalDetailKind.NOTES -> R.string.recipe_notes_group
    OptionalDetailKind.DOSAGE -> R.string.optional_concentration
    OptionalDetailKind.FORM -> R.string.pharmaceutical_form
    OptionalDetailKind.INGREDIENT -> R.string.active_ingredient
    OptionalDetailKind.QUANTITY -> R.string.optional_prescribed_quantity
    OptionalDetailKind.INSTRUCTIONS -> R.string.prescribed_instructions
})

@Composable
internal fun OptionalDetailsSection(state: PrescriptionWizardUiState, onOpen: (OptionalDetailDraft) -> Unit) {
    if (state.entryMode == WizardEntryMode.FULL_PRESCRIPTION) GroupedSection(stringResource(R.string.label_prescription)) {
        OptionalDetailRow(detailLabel(OptionalDetailKind.PROFESSIONAL),
            listOf(state.doctorName, state.doctorRegistration, state.doctorSpecialty).filter { it.isNotBlank() }.joinToString(" · ")) {
            onOpen(OptionalDetailDraft(OptionalDetailKind.PROFESSIONAL, value = state.doctorName,
                registration = state.doctorRegistration, specialty = state.doctorSpecialty,
                selectedDoctorId = state.selectedDoctorId, healthFacility = state.healthFacility, phone = state.doctorPhone))
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        OptionalDetailRow(detailLabel(OptionalDetailKind.NOTES), state.notes) {
            onOpen(OptionalDetailDraft(OptionalDetailKind.NOTES, value = state.notes))
        }
    }
    if (state.entryMode == WizardEntryMode.FULL_PRESCRIPTION) return
    state.allMedications.forEachIndexed { index, item ->
        GroupedSection(item.title) {
            OptionalDetailRow(detailLabel(OptionalDetailKind.DOSAGE), item.dosage) {
                onOpen(OptionalDetailDraft(OptionalDetailKind.DOSAGE, index, value = item.dosage))
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            OptionalDetailRow(detailLabel(OptionalDetailKind.FORM), stringResource(item.form.labelRes)) {
                onOpen(OptionalDetailDraft(OptionalDetailKind.FORM, index, form = item.form))
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            OptionalDetailRow(detailLabel(OptionalDetailKind.INGREDIENT), item.activeIngredient) {
                onOpen(OptionalDetailDraft(OptionalDetailKind.INGREDIENT, index, value = item.activeIngredient))
            }
            if (state.entryMode == WizardEntryMode.FULL_PRESCRIPTION || state.addToPrescriptionId != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                OptionalDetailRow(detailLabel(OptionalDetailKind.QUANTITY), item.prescribedQuantity) {
                    onOpen(OptionalDetailDraft(OptionalDetailKind.QUANTITY, index, value = item.prescribedQuantity))
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                OptionalDetailRow(detailLabel(OptionalDetailKind.INSTRUCTIONS), item.prescribedPosology) {
                    onOpen(OptionalDetailDraft(OptionalDetailKind.INSTRUCTIONS, index, value = item.prescribedPosology))
                }
            }
        }
    }
}

@Composable
internal fun OptionalDetailEditor(state: PrescriptionWizardUiState, draft: OptionalDetailDraft,
    onChange: (OptionalDetailDraft) -> Unit, onCancel: () -> Unit, onConfirm: () -> Unit, modifier: Modifier) {
    WizardQuestionScreen(question = detailLabel(draft.kind),
        onBack = onCancel, progress = state.progress,
        primaryButtonText = stringResource(R.string.optional_detail_done), primaryButtonEnabled = !state.isLoading,
        onPrimaryClick = onConfirm, modifier = modifier) {
        draft.medicationIndex?.let { index -> state.allMedications.getOrNull(index)?.let { Text(it.title, style = MaterialTheme.typography.titleMedium) } }
        when (draft.kind) {
            OptionalDetailKind.PROFESSIONAL -> {
                FormField(draft.value, { onChange(draft.copy(value = it, selectedDoctorId = null)) }, stringResource(R.string.professional_name_label))
                state.availableDoctors.filter { draft.value.isNotBlank() && it.name.contains(draft.value, true) }.take(3).forEach { doctor ->
                    SecondaryActionButton(doctor.name, { onChange(draft.copy(value = doctor.name,
                        registration = doctor.registration.orEmpty(), specialty = doctor.specialty.orEmpty(),
                        selectedDoctorId = doctor.id, healthFacility = doctor.healthFacility.orEmpty(), phone = doctor.phone.orEmpty())) })
                }
                FormField(draft.registration, { onChange(draft.copy(registration = it, selectedDoctorId = null)) }, stringResource(R.string.optional_professional_registration))
                FormField(draft.specialty, { onChange(draft.copy(specialty = it)) }, stringResource(R.string.optional_specialty))
            }
            OptionalDetailKind.FORM -> RadioChoiceList(PharmaceuticalForm.entries.map { stringResource(it.labelRes) },
                PharmaceuticalForm.entries.indexOf(draft.form), { onChange(draft.copy(form = PharmaceuticalForm.entries[it])) })
            else -> FormField(draft.value, { onChange(draft.copy(value = it)) }, detailLabel(draft.kind),
                singleLine = draft.kind != OptionalDetailKind.NOTES && draft.kind != OptionalDetailKind.INSTRUCTIONS)
        }
    }
}
