package cc.rhuan.receitas.ui.feature.home

import cc.rhuan.receitas.R

import androidx.compose.ui.res.stringResource

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.domain.model.PrescriptionItem
import cc.rhuan.receitas.ui.feature.home.components.DateSlider
import cc.rhuan.receitas.ui.feature.home.components.EmptyDayState
import cc.rhuan.receitas.ui.feature.home.components.EmptyHomeState
import cc.rhuan.receitas.ui.feature.home.components.HomeHeader
import cc.rhuan.receitas.ui.feature.home.components.NextEventCard
import cc.rhuan.receitas.ui.feature.home.components.NextEventHeader
import cc.rhuan.receitas.ui.feature.home.components.TrackingItem
import cc.rhuan.receitas.ui.feature.home.components.TrackingList
import cc.rhuan.receitas.ui.theme.ReceitasTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomePresentation(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onDateSelected: (LocalDate) -> Unit,
    registerPickup: (String, LocalDate, Boolean, LocalDate?) -> Unit,
    dismissSnackbar: () -> Unit,
    refreshData: () -> Unit,
    onPrescriptionClick: (PrescriptionItem) -> Unit = {},
    onMedicationClick: (MedicationItem) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onNavigateToPrescriptions: () -> Unit = {},
    onNavigateToMedications: () -> Unit = {},
    onCreatePrescription: () -> Unit = {},
    onCreateMedication: () -> Unit = {},
    onRenewPrescription: (String) -> Unit = {},
) {
    var pendingPickup by remember { mutableStateOf<Pair<MedicationItem, LocalDate>?>(null) }
    pendingPickup?.let { (med, date) ->
        cc.rhuan.receitas.ui.components.PickupDialog(med, date, onDismiss = { pendingPickup = null }) { chosen, early, cycle -> registerPickup(med.id, chosen, early, cycle) }
    }

    HomeScreenContent(
        uiState = uiState,
        onPrescriptionClick = onPrescriptionClick,
        onMedicationClick = onMedicationClick,
        onNotificationClick = onNotificationClick,
        onNavigateToPrescriptions = onNavigateToPrescriptions,
        onNavigateToMedications = onNavigateToMedications,
        onCreatePrescription = onCreatePrescription,
        onCreateMedication = onCreateMedication,
        onDateSelected = onDateSelected,
        onQuickAction = { item -> when (item) {
            is TrackingItem.Prescription -> onRenewPrescription(item.targetId)
            is TrackingItem.Medication -> uiState.medicamentos.find { it.id == item.targetId }?.let { pendingPickup = it to LocalDate.now() }
        } },
        onSecondaryActionWithDate = { item, date -> when (item) {
            is TrackingItem.Prescription -> onRenewPrescription(item.targetId)
            is TrackingItem.Medication -> uiState.medicamentos.find { it.id == item.targetId }?.let { pendingPickup = it to date }
        } },
        onDismissSnackbar = dismissSnackbar,
        onRefresh = refreshData,
        notificationCount = uiState.unreadNotificationsCount,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onPrescriptionClick: (PrescriptionItem) -> Unit = {},
    onMedicationClick: (MedicationItem) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onNavigateToPrescriptions: () -> Unit = {},
    onNavigateToMedications: () -> Unit = {},
    onCreatePrescription: () -> Unit = {},
    onCreateMedication: () -> Unit = {},
    onDateSelected: (LocalDate) -> Unit = {},
    onQuickAction: (TrackingItem) -> Unit = {},
    onSecondaryActionWithDate: (TrackingItem, LocalDate) -> Unit = { _, _ -> },
    onDismissSnackbar: () -> Unit = {},
    onRefresh: () -> Unit = {},
    notificationCount: Int = uiState.unreadNotificationsCount,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var itemForDatePicker by remember { mutableStateOf<TrackingItem?>(null) }
    var showHeaderDatePicker by remember { mutableStateOf(false) }

    val portugueseLocale = remember { Locale.forLanguageTag("pt-BR") }
    val todayLabel = stringResource(R.string.today)
    val headerTitle = remember(uiState.selectedDate, todayLabel) {
        val today = LocalDate.now()
        if (uiState.selectedDate == today) {
            todayLabel
        } else if (uiState.selectedDate.year == today.year) {
            uiState.selectedDate.format(DateTimeFormatter.ofPattern("d 'de' MMMM", portugueseLocale))
        } else {
            uiState.selectedDate.format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", portugueseLocale))
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            onDismissSnackbar()
            snackbarHostState.showSnackbar(message)
        }
    }

    if (showHeaderDatePicker) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val chosenDate = LocalDate.of(year, month + 1, dayOfMonth)
                onDateSelected(chosenDate)
                showHeaderDatePicker = false
            },
            uiState.selectedDate.year,
            uiState.selectedDate.monthValue - 1,
            uiState.selectedDate.dayOfMonth
        ).apply {
            setOnCancelListener { showHeaderDatePicker = false }
            setOnDismissListener { showHeaderDatePicker = false }
        }.show()
    }

    if (itemForDatePicker != null) {
        val activeItem = itemForDatePicker!!
        val now = LocalDate.now()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val chosenDate = LocalDate.of(year, month + 1, dayOfMonth)
                onSecondaryActionWithDate(activeItem, chosenDate)
                itemForDatePicker = null
            },
            now.year,
            now.monthValue - 1,
            now.dayOfMonth
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
            setOnCancelListener { itemForDatePicker = null }
            setOnDismissListener { itemForDatePicker = null }
        }.show()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreatePrescription,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.large,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.action_new_prescription)
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.action_new_prescription),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 16.dp)
            ) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Cabeçalho da Home
            HomeHeader(
                title = headerTitle,
                notificationCount = notificationCount,
                onNotificationClick = onNotificationClick,
                onCalendarClick = { showHeaderDatePicker = true }
            )

            // Slider horizontal de datas
            DateSlider(
                selectedDate = uiState.selectedDate,
                onDateSelected = onDateSelected,
                getEventIndicator = { date -> uiState.getEventIndicatorForDate(date) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Conteúdo principal
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    uiState.errorMessage != null -> {
                        cc.rhuan.receitas.ui.components.LoadErrorPanel(uiState.errorMessage, onRefresh, Modifier.align(Alignment.Center))
                    }

                    uiState.isAppEmpty -> {
                        // Estado totalmente vazio do aplicativo
                        EmptyHomeState(
                            onNavigateToPrescriptions = onCreatePrescription,
                            onNavigateToMedications = onCreateMedication,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        )
                    }

                    else -> {
                        PullToRefreshBox(
                            isRefreshing = uiState.isRefreshing,
                            onRefresh = onRefresh,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // 1. Lista de acompanhamentos da data selecionada
                                if (uiState.mainTrackingItems.isEmpty()) {
                                    item(key = "empty_date_state") {
                                        EmptyDayState()
                                    }
                                } else {
                                    item(key = "main_tracking_list") {
                                        TrackingList(
                                            items = uiState.mainTrackingItems,
                                            onItemClick = { item ->
                                                when (item) {
                                                    is TrackingItem.Prescription -> {
                                                        val p = uiState.prescriptions.find { it.id == item.targetId }
                                                        if (p != null) onPrescriptionClick(p) else onNavigateToPrescriptions()
                                                    }
                                                    is TrackingItem.Medication -> {
                                                        val m = uiState.medicamentos.find { it.id == item.targetId }
                                                        if (m != null) onMedicationClick(m) else onNavigateToMedications()
                                                    }
                                                }
                                            },
                                            onPrimaryAction = { item -> onQuickAction(item) },
                                            onSecondaryAction = { item -> if (item is TrackingItem.Prescription) onQuickAction(item) else itemForDatePicker = item }
                                        )
                                    }
                                }

                                // 2. Próximos eventos (Contexto futuro)
                                if (uiState.futureTrackingItems.isNotEmpty()) {
                                    item(key = "future_header") {
                                        NextEventHeader()
                                    }

                                    items(
                                        items = uiState.futureTrackingItems,
                                        key = { it.id }
                                    ) { futureItem ->
                                        NextEventCard(
                                            item = futureItem,
                                            onClick = {
                                                when (futureItem) {
                                                    is TrackingItem.Prescription -> {
                                                        val p = uiState.prescriptions.find { it.id == futureItem.targetId }
                                                        if (p != null) onPrescriptionClick(p) else onNavigateToPrescriptions()
                                                    }
                                                    is TrackingItem.Medication -> {
                                                        val m = uiState.medicamentos.find { it.id == futureItem.targetId }
                                                        if (m != null) onMedicationClick(m) else onNavigateToMedications()
                                                    }
                                                }
                                            },
                                            onNavigateToDate = { targetDate ->
                                                onDateSelected(targetDate)
                                            }
                                        )
                                    }
                                }

                                item(key = "bottom_spacer") {
                                    Spacer(modifier = Modifier.height(80.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    ReceitasTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                prescriptions = listOf(
                    PrescriptionItem(
                        id = "1",
                        title = "Metformina 850 mg",
                        specialty = "Endocrinologia",
                        doctorName = "Carlos Eduardo",
                        notes = "Uso contínuo."
                    ),
                ),
                medicamentos = listOf(
                    MedicationItem(
                        id = "1",
                        title = "Metformina",
                        dosage = "850 mg",
                        quantity = "2 cx",
                        prescribedPosology = "1 comprimido ao dia",
                        lastRefillDate = LocalDate.now().minusDays(30),
                        instructions = "Tomar após o almoço."
                    ),
                ),
            )
        )
    }
}
