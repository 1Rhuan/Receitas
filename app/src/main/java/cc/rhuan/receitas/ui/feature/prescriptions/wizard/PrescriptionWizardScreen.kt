package cc.rhuan.receitas.ui.feature.prescriptions.wizard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun PrescriptionWizardScreen(
    viewModel: PrescriptionWizardViewModel,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateUp()
        }
    }

    val stepTitles = listOf(
        "1. O Medicamento",
        "2. Posologia & Horários",
        "3. Reposição & Estoque",
        "4. Tipo & Validade da Receita",
        "5. Profissional Prescritor",
        "6. Revisão & Confirmação"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFE8ECEF),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = if (uiState.editingPrescriptionId != null) "Editar Receita" else "Nova Receita",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333846)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (uiState.currentStep > 1) {
                                viewModel.previousStep()
                            } else {
                                onNavigateUp()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar",
                                tint = Color(0xFF333846)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFE8ECEF)
                    )
                )

                // Progress Bar
                LinearProgressIndicator(
                    progress = { uiState.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color(0xFFCBD5E1)
                )

                // Stepper Title Header
                Surface(
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "PASSO ${uiState.currentStep} DE 6",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp
                                )
                            )
                            // Sub-indicator for multiple medications in steps 1 & 2
                            if (uiState.currentStep in 1..2 && uiState.hasMultipleMedications) {
                                Text(
                                    text = "Medicamento ${uiState.currentMedicationIndex} (já adicionados: ${uiState.medications.size})",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF2563EB),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                        Text(
                            text = stepTitles.getOrElse(uiState.currentStep - 1) { "" },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.currentStep > 1) {
                        OutlinedButton(
                            onClick = { viewModel.previousStep() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Voltar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Visible "Pular esta etapa" button for optional steps 3, 4, 5
                    if (uiState.currentStep in 3..5) {
                        TextButton(
                            onClick = { viewModel.skipStep() },
                            modifier = Modifier.weight(0.8f)
                        ) {
                            Text("Pular etapa", color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (uiState.currentStep < 6) {
                        val forwardEnabled = when (uiState.currentStep) {
                            1 -> uiState.canAdvanceFromStep1
                            2 -> uiState.canAdvanceFromStep2
                            else -> true
                        }
                        val forwardLabel = when (uiState.currentStep) {
                            1 -> "Avançar"
                            2 -> "Avançar"
                            3 -> "Ir p/ Receita"
                            4 -> "Ir p/ Médico"
                            5 -> "Revisar"
                            else -> "Avançar"
                        }

                        Button(
                            onClick = { viewModel.nextStep() },
                            enabled = forwardEnabled,
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text(forwardLabel)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.savePrescription() },
                            enabled = uiState.canSave && !uiState.isLoading,
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (uiState.isLoading) "Salvando..." else "Salvar Receita")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            AnimatedContent(
                targetState = uiState.currentStep,
                label = "WizardStepTransition"
            ) { step ->
                when (step) {
                    1 -> WizardStepMedication(uiState = uiState, viewModel = viewModel)
                    2 -> WizardStepPosology(uiState = uiState, viewModel = viewModel)
                    3 -> WizardStepReplenishment(uiState = uiState, viewModel = viewModel)
                    4 -> WizardStepPrescriptionType(uiState = uiState, viewModel = viewModel)
                    5 -> WizardStepDoctor(uiState = uiState, viewModel = viewModel)
                    6 -> WizardStepReview(uiState = uiState, viewModel = viewModel)
                }
            }
        }
    }
}
