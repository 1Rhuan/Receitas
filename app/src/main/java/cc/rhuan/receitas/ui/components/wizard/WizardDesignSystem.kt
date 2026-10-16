package cc.rhuan.receitas.ui.components.wizard

import cc.rhuan.receitas.R
import cc.rhuan.receitas.ui.theme.DesignTokens as T

import androidx.compose.foundation.layout.Box

import androidx.compose.ui.res.stringResource

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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
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
    isBusy: Boolean = false,
    errorMessage: String? = null,
    onSkip: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) scrollState.animateScrollTo(0)
    }
    Box(modifier.fillMaxSize().imePadding()) {
    val actionHeight = remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(Modifier.fillMaxWidth().padding(horizontal = T.gap, vertical = T.padding)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, enabled = !isBusy, modifier = Modifier.size(T.control)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                    Spacer(Modifier.weight(1f))
                    onSkip?.let { skip ->
                        TextButton(onClick = skip, enabled = primaryButtonEnabled && !isBusy,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                            modifier = Modifier.heightIn(min = T.control).testTag("wizard-skip-action")) {
                            Text(stringResource(R.string.action_skip))
                        }
                    }
                }
                Text(question, Modifier.fillMaxWidth().padding(
                    horizontal = if (LocalDensity.current.fontScale >= T.compactFontScale) T.small else T.margin,
                    vertical = T.section),
                    style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold)
            }
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(scrollState).testTag("wizard-content").padding(vertical = T.padding),
            verticalArrangement = Arrangement.spacedBy(T.padding),
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = T.margin), verticalArrangement = Arrangement.spacedBy(T.padding)) {
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
        .padding(horizontal = T.margin, vertical = T.gap), verticalArrangement = Arrangement.spacedBy(T.small)) {
        PrimaryButton(primaryButtonText, primaryButtonEnabled && !isBusy, onPrimaryClick, isBusy = isBusy)
        LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.outlineVariant)
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
