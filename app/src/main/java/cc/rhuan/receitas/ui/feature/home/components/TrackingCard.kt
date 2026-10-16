package cc.rhuan.receitas.ui.feature.home.components

import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import cc.rhuan.receitas.ui.theme.DesignTokens as T
import cc.rhuan.receitas.ui.components.ActionButton
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import cc.rhuan.receitas.ui.components.AppCard as Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cc.rhuan.receitas.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrackingCard(
    type: String,
    status: TrackingStatus,
    statusLabel: String,
    title: String,
    leadingIcon: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    actions: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    userNote: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(T.padding),
            verticalArrangement = Arrangement.spacedBy(T.gap)
        ) {
            // 1. Header: Tipo do Item e Badge de Status
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(T.small),
                verticalArrangement = Arrangement.spacedBy(T.small)
            ) {
                Text(
                    text = type,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                )

                StatusBadge(
                    status = status,
                    label = statusLabel
                )
            }

            // Top divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            )

            // Icon, title, and content slot
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                leadingIcon()

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,

                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    content()
                }
            }

            // Optional user note
            if (!userNote.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = userNote,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Bottom divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            )

            // Actions
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(T.small)) { actions() }
        }
    }
}

@Composable
fun MedicationTrackingCard(
    item: TrackingItem.Medication,
    onClick: () -> Unit,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    TrackingCard(
        type = stringResource(R.string.label_medication),
        status = item.status,
        statusLabel = item.statusLabel,
        title = item.title,
        userNote = item.userNote,
        onClick = onClick,
        modifier = modifier,
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_bold_pills),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        content = {
            Text(item.formattedDateText, style = MaterialTheme.typography.bodyLarge)
            if (!item.lastPickupDateText.isNullOrBlank()) {
                Text(
                    text = item.lastPickupDateText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
            if (!item.stockText.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_box),
                        contentDescription = stringResource(R.string.pickup_quantity_label),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = item.stockText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        actions = { ActionButton(item.primaryActionText, onPrimaryAction, Modifier.fillMaxWidth()) }
    )
}

@Composable
fun PrescriptionTrackingCard(
    item: TrackingItem.Prescription,
    onClick: () -> Unit,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    TrackingCard(
        type = stringResource(R.string.label_prescription),
        status = item.status,
        statusLabel = item.statusLabel,
        title = item.title,
        userNote = item.userNote,
        onClick = onClick,
        modifier = modifier,
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_bold_document_medicine),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        content = {
            Text(item.formattedDateText, style = MaterialTheme.typography.bodyLarge)
            if (!item.prescriberName.isNullOrBlank()) {
                Column(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        text = stringResource(R.string.prescriber),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = item.prescriberName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        },
        actions = { ActionButton(item.primaryActionText, onPrimaryAction, Modifier.fillMaxWidth()) }
    )
}
