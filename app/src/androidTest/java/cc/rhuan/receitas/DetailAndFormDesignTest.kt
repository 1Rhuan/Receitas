package cc.rhuan.receitas

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import cc.rhuan.receitas.domain.model.*
import cc.rhuan.receitas.ui.feature.prescriptions.*
import cc.rhuan.receitas.ui.feature.prescriptions.wizard.*
import cc.rhuan.receitas.ui.feature.medications.*
import cc.rhuan.receitas.ui.theme.ReceitasTheme
import java.io.File
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

class DetailAndFormDesignTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val longTitle = "Cloridrato de metformina de liberação prolongada para acompanhamento"
    private fun clickFormOption(label: String) {
        val node = compose.onNodeWithText(label).performScrollTo()
        val actionTop = compose.onNodeWithTag("wizard-primary-action").fetchSemanticsNode().boundsInRoot.top
        val bounds = node.fetchSemanticsNode().boundsInRoot
        if (bounds.bottom >= actionTop) {
            compose.onNodeWithTag("wizard-content").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.ScrollBy) {
                it(0f, bounds.bottom - actionTop + bounds.height)
            }
        }
        node.performClick()
    }
    private fun show(dark: Boolean = false, scale: Float = 1f, content: @Composable () -> Unit) {
        compose.setContent { ReceitasTheme(darkTheme = dark) {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, scale)) {
                Box(Modifier.requiredSize(320.dp, 640.dp)) { content() }
            }
        } }
    }
    private fun capture(name: String) {
        compose.waitForIdle()
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val file = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), "$name.png")
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        listOf("mkdir -p /sdcard/Download/design-migration", "cp ${file.absolutePath} /sdcard/Download/design-migration/$name.png").forEach {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(it)).use { stream -> stream.readBytes() }
        }
    }
    @Test fun recipeEntryShowsEssentialsAndOptionalFieldsRemainAvailable() {
        show { PrescriptionWizardContent(PrescriptionWizardUiState(), WizardCallbacks()) }
        compose.onNodeWithText("Nome da receita (opcional)").assertDoesNotExist()
        compose.onNodeWithText("Continuar para medicamentos").assertIsDisplayed()
        compose.onNodeWithText("Emissão e tipo").assertIsDisplayed()
        compose.onNodeWithText("Validade da receita").performScrollTo().assertIsDisplayed()
        capture("recipe-entry-light")
        clickFormOption("Nome e profissional (opcional)")
        compose.onNodeWithText("Nome da receita (opcional)").performScrollTo().assertIsDisplayed()
    }
    @Test fun recipeEntryDarkLargeFontKeepsValidityAndContinueReachable() {
        var continued = false
        show(dark = true, scale = 2f) {
            PrescriptionWizardContent(PrescriptionWizardUiState(), WizardCallbacks(next = { continued = true }))
        }
        compose.onNodeWithText("Etapa 1 de 4").assertIsDisplayed()
        compose.onNodeWithText("Continuar para medicamentos").assertIsDisplayed()
        compose.onNodeWithText("Data de emissão").performScrollTo().assertIsDisplayed()
        capture("recipe-entry-dark-200")
        compose.onNodeWithText("Validade em dias").performScrollTo().assertIsDisplayed()
        clickFormOption("Nome e profissional (opcional)")
        compose.onNodeWithText("Nome da receita (opcional)").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Continuar para medicamentos").performClick()
        assertTrue(continued)
    }
    @Test fun standaloneLargeFontHasOnePrimaryActionAndReachableOptionalDetails() {
        var continued = false
        show(dark = true, scale = 2f) {
            PrescriptionWizardContent(PrescriptionWizardUiState(entryMode = WizardEntryMode.STANDALONE_MEDICATION),
                WizardCallbacks(next = { continued = true }))
        }
        compose.onNodeWithText("Continuar para lembretes").assertIsDisplayed()
        compose.onNodeWithText("Nome do medicamento *").performScrollTo().assertIsDisplayed()
        capture("medication-form-dark-200")
        clickFormOption("Mais detalhes (opcional)")
        compose.onNodeWithText("Princípio ativo").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Continuar para lembretes").performClick()
        assertTrue(continued)
    }
    @Test fun reviewLargeFontKeepsEditAndSaveReachable() {
        var edited = 0
        var saved = false
        show(scale = 2f) {
            PrescriptionWizardContent(PrescriptionWizardUiState(currentStep = 4, showMedicationEditor = false,
                medications = listOf(MedicationDraft(title = longTitle))),
                WizardCallbacks(goToStep = { edited = it }, save = { saved = true }))
        }
        compose.onNodeWithText("Salvar receita").assertIsDisplayed()
        compose.onAllNodesWithText("Editar")[0].performScrollTo().performClick()
        assertEquals(1, edited)
        compose.onAllNodesWithText(longTitle)[0].performScrollTo().assertIsDisplayed()
        capture("review-light-200")
        compose.onNodeWithText("Salvar receita").performClick()
        assertTrue(saved)
    }
    @Test fun prescriptionLargeFontKeepsRenewAndArchiveReachable() {
        var renewed = false
        var archived = false
        show(scale = 2f) {
            PrescriptionDetailContent(PrescriptionDetailUiState(isLoading = false,
                prescription = PrescriptionItem(id = "preview", title = longTitle, specialty = "Clínica geral",
                    doctorName = "Profissional com nome extenso para verificar a leitura",
                    notes = "Observação longa preservada integralmente para consulta no detalhe.")),
                onBackClick = {}, onRenewClick = { renewed = true }, onReplenishClick = {}, onArchive = { archived = true })
        }
        compose.onNodeWithText(longTitle).performScrollTo().assertIsDisplayed()
        capture("prescription-detail-light-200")
        compose.onNodeWithText("Registrar nova receita").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Excluir").performScrollTo().performClick()
        compose.onNodeWithText("Arquivar").assertIsDisplayed()
        capture("prescription-detail-actions-200")
        compose.onNodeWithText("Arquivar").performClick()
        compose.waitForIdle()
        assertTrue(renewed)
        assertTrue(archived)
    }
    @Test fun medicationDarkDetailKeepsPickupAndHistory() {
        show(dark = true) {
            MedicationDetailContent(MedicationDetailUiState(isLoading = false, medication = MedicationItem(
                id = "preview", title = longTitle, hasRecurringPickup = true, lastRefillDate = LocalDate.now().minusDays(40))),
                null, {}, {}, {}, { _, _, _ -> }, {}, {}, {}, { _, _ -> }, {})
        }
        compose.onNodeWithText("Registrar retirada").performScrollTo().assertIsDisplayed()
        capture("medication-detail-dark")
        compose.onNodeWithText("Ver histórico de retiradas").performScrollTo().performClick()
        compose.onNodeWithText("Histórico de retiradas").assertIsDisplayed()
        compose.onNodeWithTag("pickup-history-list").performScrollToNode(hasText("Nenhuma retirada registrada."))
        compose.onNodeWithText("Nenhuma retirada registrada.").assertIsDisplayed()
    }
    @Test fun historyLargeFontKeepsDeleteConfirmationAndReturnsToMedication() {
        var deleted = ""
        show(dark = true, scale = 2f) {
            MedicationDetailContent(MedicationDetailUiState(isLoading = false, medication = MedicationItem(
                id = "preview", title = longTitle, pickupHistory = listOf(PickupEvent("pickup", LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 8).atTime(9, 30).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())))),
                null, {}, {}, {}, { _, _, _ -> }, {}, {}, {}, { _, _ -> }, { deleted = it })
        }
        compose.onNodeWithText("Ver histórico de retiradas").performScrollTo().performClick()
        compose.onNodeWithTag("pickup-history-list").performScrollToNode(hasText("08/10/2026"))
        compose.onNodeWithText("08/10/2026").assertIsDisplayed()
        compose.onNodeWithText("Ações").performScrollTo().performClick()
        compose.onNodeWithText("Corrigir").assertIsDisplayed()
        compose.onNodeWithText("Excluir").performClick()
        assertEquals("", deleted)
        compose.onNodeWithText("Cancelar").performClick()
        compose.onNodeWithText("Ações").performScrollTo().performClick()
        compose.onNodeWithText("Excluir").performClick()
        compose.onNode(hasText("Excluir") and hasAnyAncestor(isDialog())).performClick()
        assertEquals("pickup", deleted)
        capture("pickup-history-dark-200")
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.onNodeWithText("Ver histórico de retiradas").performScrollTo().assertIsDisplayed()
    }
    @Test fun prescriptionSummaryOpensSelectedMedicationWithoutTrackingTable() {
        var opened = ""
        show(dark = true, scale = 2f) {
            PrescriptionDetailContent(PrescriptionDetailUiState(isLoading = false,
                prescription = PrescriptionItem(id = "preview", title = "Receita", specialty = ""),
                medications = listOf(PrescriptionMedicationDetail(MedicationItem(id = "med", title = longTitle,
                    dosage = "50 mg", prescribedPosology = "Tomar conforme a receita."), RefillStatus.NoPrescriptionLinked))),
                {}, {}, {}, onMedicationClick = { opened = it })
        }
        compose.onNodeWithTag("prescription-detail-list").performScrollToNode(hasText("Ver medicamentos"))
        compose.onNodeWithText(longTitle).assertDoesNotExist()
        compose.onNodeWithText("Ver medicamentos").performClick()
        compose.onNodeWithTag("recipe-medication-list").performScrollToNode(hasText("Ver medicamento"))
        compose.onNodeWithText("Sem vínculo").assertDoesNotExist()
        compose.onNodeWithText("Acompanhamento de retiradas").assertDoesNotExist()
        compose.onNodeWithText("Ver medicamento").performClick()
        assertEquals("med", opened)
        compose.onNodeWithText(longTitle).performScrollTo().assertIsDisplayed()
        capture("prescription-medication-summary-dark-200")
        compose.onNodeWithText(longTitle).performClick()
        assertEquals("med", opened)
    }
    @Test fun medicationGroupedDetailKeepsEditArchiveAndHistory() {
        var edited = ""
        var archived = false
        show {
            MedicationDetailContent(MedicationDetailUiState(isLoading = false, medication = MedicationItem(
                id = "med", title = "Losartana", dosage = "50 mg", hasRecurringPickup = true,
                lastRefillDate = LocalDate.of(2026, 10, 14), refillCycleDays = 156)),
                null, {}, { edited = it }, {}, { _, _, _ -> }, {}, { archived = it }, {}, { _, _ -> }, {})
        }
        compose.onNodeWithText("Última retirada").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("19/03/2027").performScrollTo().assertIsDisplayed()
        capture("medication-grouped-detail-light")
        compose.onNodeWithContentDescription("Editar acompanhamento").performScrollTo().performClick()
        assertEquals("med", edited)
        compose.onNodeWithText("Arquivar").assertDoesNotExist()
        compose.onNodeWithContentDescription("Excluir").performScrollTo().performClick()
        compose.onNodeWithText("Arquivar").performClick()
        assertFalse(archived)
        compose.onNodeWithText("Cancelar").performClick()
        compose.onNodeWithContentDescription("Excluir").performScrollTo().performClick()
        compose.onNodeWithText("Arquivar").performClick()
        compose.onNodeWithText("Arquivar").performClick()
        assertTrue(archived)
        compose.onNodeWithText("Ver histórico de retiradas").performScrollTo().performClick()
        compose.onNodeWithText("Histórico de retiradas").assertIsDisplayed()
    }
    @Test fun medicationHeaderRemovalChoicePreservesConfirmationAtLargeFont() {
        var deleted = false
        show(dark = true, scale = 2f) {
            MedicationDetailContent(MedicationDetailUiState(isLoading = false,
                medication = MedicationItem("med", longTitle)), null, {}, {}, {}, { _, _, _ -> },
                { deleted = true }, {}, {}, { _, _ -> }, {})
        }
        compose.onNodeWithContentDescription("Editar acompanhamento").assertIsDisplayed()
        compose.onNodeWithContentDescription("Excluir").assertIsDisplayed().performClick()
        compose.onNodeWithText("Arquivar").assertIsDisplayed()
        compose.onNodeWithText("Excluir definitivamente").performClick()
        assertFalse(deleted)
        compose.onNodeWithText("Cancelar").performClick()
        assertFalse(deleted)
        compose.onNodeWithContentDescription("Excluir").performClick()
        compose.onNodeWithText("Excluir definitivamente").performClick()
        compose.onNodeWithText("Excluir").performClick()
        assertTrue(deleted)
        capture("medication-header-dark-200")
    }
    @Test fun archivedMedicationHeaderKeepsReactivationAndAvoidsDuplicateArchive() {
        var reactivated = false
        show {
            MedicationDetailContent(MedicationDetailUiState(isLoading = false,
                medication = MedicationItem("med", "Losartana", trackingArchived = true)),
                null, {}, {}, {}, { _, _, _ -> }, {}, { reactivated = !it }, {}, { _, _ -> }, {})
        }
        compose.onNodeWithContentDescription("Reativar").performClick()
        assertTrue(reactivated)
        compose.onNodeWithContentDescription("Excluir").performClick()
        compose.onNodeWithText("Arquivar").assertDoesNotExist()
        compose.onNodeWithText("Excluir definitivamente").assertIsDisplayed()
    }
    @Test fun emptyRecipeMedicationListKeepsReturnAvailable() {
        show(dark = true) { PrescriptionDetailContent(PrescriptionDetailUiState(isLoading = false,
            prescription = PrescriptionItem("recipe", "Receita vazia", "")), {}, {}, {}) }
        compose.onNodeWithTag("prescription-detail-list").performScrollToNode(hasText("Ver medicamentos"))
        compose.onNodeWithText("Ver medicamentos").performClick()
        compose.onNodeWithText("Medicamentos da receita").assertIsDisplayed()
        compose.onNodeWithTag("recipe-medication-list").performScrollToNode(hasText("Nenhum medicamento vinculado a esta receita."))
        compose.onNodeWithText("Nenhum medicamento vinculado a esta receita.").assertIsDisplayed()
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.onNodeWithTag("prescription-detail-list").assertIsDisplayed()
    }
    @Test fun medicationDetailsShowInformationAndCompleteTimeline() {
        val events = (1..4).map { day -> PickupEvent("pickup-$day", LocalDate.of(2026, 10, day),
            LocalDate.of(2026, 10, day).atTime(9, 30).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()) }
        show {
            MedicationDetailContent(MedicationDetailUiState(isLoading = false, medication = MedicationItem(
                id = "med", title = "Losartana", dosage = "50 mg", activeIngredient = "Losartana potássica",
                instructions = "Orientação registrada para consulta.", pickupHistory = events)),
                null, {}, {}, {}, { _, _, _ -> }, {}, {}, {}, { _, _ -> }, {})
        }
        compose.onNodeWithText("Losartana potássica").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Orientação registrada para consulta.").performScrollTo().assertIsDisplayed()
        capture("medication-detailed-light")
        compose.onNodeWithText("Últimas retiradas").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("04/10/2026").performScrollTo().assertIsDisplayed()
        capture("medication-timeline-preview-light")
        compose.onNodeWithText("01/10/2026").assertDoesNotExist()
        compose.onNodeWithText("Ver histórico de retiradas").performScrollTo().performClick()
        compose.onNodeWithTag("pickup-history-list").performScrollToNode(hasText("04/10/2026"))
        capture("medication-timeline-full-light")
        compose.onNodeWithTag("pickup-history-list").performScrollToNode(hasText("01/10/2026"))
        compose.onNodeWithText("01/10/2026").assertIsDisplayed()
    }
    @Test fun medicationShowsMemberRecipeWithoutInventingPickupLink() {
        val title = "Receita de acompanhamento com nome extenso para consulta"
        var opened = false
        show(dark = true, scale = 2f) {
            MedicationDetailContent(MedicationDetailUiState(isLoading = false,
                medication = MedicationItem("med", "Losartana"),
                prescriptions = listOf(PrescriptionItem("recipe", title, "", medicationIds = listOf("med")))),
                null, {}, {}, {}, { _, _, _ -> }, {}, {}, {}, { _, _ -> }, {},
                onPrescriptionsClick = { opened = it == "med" })
        }
        compose.onNodeWithText("Receitas vinculadas").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("1 receita").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(title).assertDoesNotExist()
        compose.onNodeWithText("Ver receitas vinculadas").performScrollTo().performClick()
        assertTrue(opened)
        compose.onNodeWithText("Sem vínculo").assertDoesNotExist()
        compose.onNodeWithText("Receita para retiradas").assertDoesNotExist()
        capture("medication-member-recipe-dark-200")
    }
    @Test fun filteredRecipeListKeepsScopeVisibleAndCanShowAll() {
        val matching = PrescriptionItem("matching", "Receita deste medicamento", "", medicationIds = listOf("med"))
        val other = PrescriptionItem("other", "Receita de outro medicamento", "")
        show(dark = true, scale = 2f) {
            var scoped by remember { mutableStateOf(true) }
            cc.rhuan.receitas.ui.feature.prescriptions.PrescriptionsScreenContent(
                PrescriptionsUiState(prescriptions = listOf(matching, other), medications = listOf(MedicationItem("med", longTitle)),
                    medicationFilterId = if (scoped) "med" else null), {}, {}, onBack = {},
                onClearMedicationFilter = { scoped = false })
        }
        compose.onNodeWithText("Filtrado por medicamento").performScrollTo().assertIsDisplayed()
        capture("recipes-medication-filter-dark-200")
        compose.onNodeWithTag("prescription-list").performScrollToNode(hasText(matching.title))
        compose.onNodeWithText(matching.title).assertIsDisplayed()
        compose.onNodeWithText(other.title).assertDoesNotExist()
        compose.onNodeWithTag("prescription-list").performScrollToNode(hasText("Mostrar todas as receitas"))
        compose.onNodeWithText("Mostrar todas as receitas").performClick()
        compose.onNodeWithTag("prescription-list").performScrollToNode(hasText(other.title))
        compose.onNodeWithText(other.title).assertIsDisplayed()
        compose.onNodeWithText("Filtrado por medicamento").assertDoesNotExist()
    }
    @Test fun medicationWithoutRecipesKeepsCountAndDisablesListAccess() {
        show {
            MedicationDetailContent(MedicationDetailUiState(isLoading = false, medication = MedicationItem("med", "Losartana")),
                null, {}, {}, {}, { _, _, _ -> }, {}, {}, {}, { _, _ -> }, {})
        }
        compose.onNodeWithText("0 receitas").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Ver receitas vinculadas").performScrollTo().assertIsNotEnabled()
    }
    @Test fun prescriptionGroupedDataAndHeaderActionsRemainReachable() {
        show {
            PrescriptionDetailContent(PrescriptionDetailUiState(isLoading = false,
                prescription = PrescriptionItem(id = "preview", title = "Losartana — 14/10/2026", specialty = "")),
                {}, {}, {})
        }
        compose.onNodeWithText("Dados da receita").assertIsDisplayed()
        compose.onNodeWithText("Vencimento").performScrollTo().assertIsDisplayed()
        capture("prescription-organized-data-light")
        compose.onNodeWithText("Gerenciar receita").assertDoesNotExist()
        compose.onNodeWithText("Situação").performScrollTo().assertIsDisplayed()
        capture("prescription-organized-actions-light")
        compose.onNodeWithContentDescription("Excluir").performScrollTo().performClick()
        compose.onNodeWithText("Arquivar").assertIsDisplayed()
    }
    @Test fun recipeHeaderRemovalKeepsConfirmationAndCancellation() {
        var deleted = false
        var archived = false
        var edited = false
        show(dark = true) {
            PrescriptionDetailPresentation(
                uiState = PrescriptionDetailUiState(isLoading = false,
                    prescription = PrescriptionItem("recipe", "Receita de acompanhamento", "")),
                registerPickup = { _, _, _, _ -> }, deleteRecipe = { deleted = true },
                archive = { archived = it }, clearActionMessage = {}, correctPickup = { _, _ -> },
                deletePickup = {}, loadDetail = {}, onBackClick = {}, onEditClick = { edited = true })
        }
        compose.onNodeWithContentDescription("Editar receita").performClick()
        assertTrue(edited)
        capture("recipe-header-dark")
        compose.onNodeWithContentDescription("Excluir").performClick()
        compose.onNodeWithText("Arquivar").performClick()
        assertFalse(archived)
        compose.onNodeWithText("Cancelar").performClick()
        assertFalse(archived)
        compose.onNodeWithContentDescription("Excluir").performClick()
        compose.onNodeWithText("Arquivar").performClick()
        compose.onNodeWithText("Arquivar").performClick()
        assertTrue(archived)
        compose.onNodeWithContentDescription("Excluir").performClick()
        compose.onNodeWithText("Excluir definitivamente").performClick()
        compose.onNodeWithText("Cancelar").performClick()
        assertFalse(deleted)
        compose.onNodeWithContentDescription("Excluir").performClick()
        compose.onNodeWithText("Excluir definitivamente").performClick()
        compose.onNodeWithText("Excluir").performClick()
        assertTrue(deleted)
    }
    @Test fun archivedRecipeHeaderOffersReactivationWithoutArchiveChoice() {
        var reactivated = false
        show {
            PrescriptionDetailContent(PrescriptionDetailUiState(isLoading = false,
                prescription = PrescriptionItem("recipe", "Receita arquivada", "", state = RecordState.ARCHIVED)),
                {}, {}, {}, onArchive = { reactivated = true })
        }
        compose.onNodeWithContentDescription("Reativar").performClick()
        assertTrue(reactivated)
        compose.onNodeWithContentDescription("Excluir").performClick()
        compose.onNodeWithText("Arquivar").assertDoesNotExist()
        compose.onNodeWithText("Excluir definitivamente").assertIsDisplayed()
    }
    @Test fun recurringPreferencesRemainSeparateAndReachableAtLargeFont() {
        show(scale = 2f) {
            PrescriptionWizardContent(PrescriptionWizardUiState(entryMode = WizardEntryMode.STANDALONE_MEDICATION,
                currentStep = 3, showMedicationEditor = false, medications = listOf(MedicationDraft(
                    title = "Losartana", hasRecurringPickup = true, lastPickupDate = LocalDate.now()))), WizardCallbacks())
        }
        compose.onNodeWithText("Etapa 3 de 4").assertIsDisplayed()
        compose.onNodeWithText("Revisar cadastro").assertIsDisplayed()
        compose.onNodeWithText("Avisar com antecedência").performScrollTo().assertIsDisplayed()
        capture("pickup-preferences-light-200")
        compose.onNodeWithText("Sem receita vinculada").performScrollTo().assertIsDisplayed()
    }
}
