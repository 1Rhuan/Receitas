package cc.rhuan.receitas.ui.components.wizard

import cc.rhuan.receitas.R
import cc.rhuan.receitas.ui.theme.DesignTokens as T
import cc.rhuan.receitas.ui.components.PageHeader
import androidx.compose.foundation.layout.BoxWithConstraints

import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun WizardQuestionScreen(
    question: String,
    onBack: () -> Unit,
    progress: Float,
    primaryButtonText: String,
    primaryButtonEnabled: Boolean,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    contextLabel: String? = null,
    stepNumber: Int? = null,
    stepCount: Int? = null,
    description: String? = null,
    onClose: (() -> Unit)? = null,
    isBusy: Boolean = false,
    errorMessage: String? = null,
    prominentHeader: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    val largeText = LocalDensity.current.fontScale >= T.compactFontScale
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) scrollState.animateScrollTo(0)
    }
    BoxWithConstraints(modifier.fillMaxSize().imePadding()) {
    val actionHeight = remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val expandedHeader = prominentHeader && maxHeight >= T.compactHeight && !largeText
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(Modifier.fillMaxWidth().padding(horizontal = T.gap, vertical = T.small)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, enabled = !isBusy, modifier = Modifier.size(T.control)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                    Column(Modifier.weight(1f).padding(horizontal = T.small)) {
                        if (!largeText || stepNumber == null) Text(contextLabel ?: stringResource(R.string.ui_cadastro_de_receita), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        if (stepNumber != null && stepCount != null) Text(stringResource(R.string.wizard_step, stepNumber, stepCount), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    onClose?.let { close ->
                        IconButton(onClick = close, enabled = !isBusy, modifier = Modifier.size(T.control)) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.ui_sair_do_cadastro))
                        }
                    }
                }
                LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().padding(horizontal = T.small), color = MaterialTheme.colorScheme.primary)
            }
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(scrollState).testTag("wizard-content").padding(vertical = T.padding),
            verticalArrangement = Arrangement.spacedBy(T.padding),
        ) {
            PageHeader(question, description.orEmpty(), expandedHeader)
            Column(Modifier.fillMaxWidth().padding(horizontal = T.margin), verticalArrangement = Arrangement.spacedBy(T.padding)) {
            if (!expandedHeader) description?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            errorMessage?.let { message ->
                Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(T.gap), modifier = Modifier.fillMaxWidth()) {
                    Text(message, modifier = Modifier.padding(T.padding).semantics { liveRegion = LiveRegionMode.Polite }, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
            content()
            Spacer(Modifier.height(T.small))
            Spacer(Modifier.height(actionHeight.value))
            }
        }
    }
    Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()
        .testTag("wizard-primary-action")
        .onSizeChanged { actionHeight.value = with(density) { it.height.toDp() } }
        .padding(horizontal = T.margin, vertical = T.gap)) {
        PrimaryButton(primaryButtonText, primaryButtonEnabled && !isBusy, onPrimaryClick, isBusy = isBusy)
    }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isBusy: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(T.buttonRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.outlineVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = T.control),
    ) {
        if (isBusy) {
            CircularProgressIndicator(modifier = Modifier.size(T.icon), color = MaterialTheme.colorScheme.onPrimary)
            Spacer(Modifier.width(T.small))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
