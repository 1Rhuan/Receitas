package cc.rhuan.receitas.ui.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.rhuan.receitas.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TrackingList(
    items: List<TrackingItem>,
    onItemClick: (TrackingItem) -> Unit,
    onPrimaryAction: (TrackingItem) -> Unit,
    onSecondaryAction: (TrackingItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items.forEach { item ->
            when (item) {
                is TrackingItem.Medication -> {
                    MedicationTrackingCard(
                        item = item,
                        onClick = { onItemClick(item) },
                        onPrimaryAction = { onPrimaryAction(item) },
                        onSecondaryAction = { onSecondaryAction(item) }
                    )
                }
                is TrackingItem.Prescription -> {
                    PrescriptionTrackingCard(
                        item = item,
                        onClick = { onItemClick(item) },
                        onPrimaryAction = { onPrimaryAction(item) },
                        onSecondaryAction = { onSecondaryAction(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun NextEventHeader(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            tint = Color(0xFF64748B),
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "Próximos eventos",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF334155)
            )
        )
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFE2E8F0)
        ) {
            Text(
                text = "Em breve",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = Color(0xFF475569)
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun NextEventCard(
    item: TrackingItem,
    onClick: () -> Unit,
    onNavigateToDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val iconRes = if (item is TrackingItem.Prescription) R.drawable.ic_bold_document_medicine else R.drawable.ic_bold_pills
    val typeLabel = if (item is TrackingItem.Prescription) "Receita" else "Medicamento"
    val dateStr = item.rawDate?.format(DateTimeFormatter.ofPattern("dd/MM")) ?: ""
    val formattedDateText = when (item) {
        is TrackingItem.Medication -> item.formattedDateText
        is TrackingItem.Prescription -> item.formattedDateText
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = Color(0xFF475569),
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    )

                    if (!item.contextTag.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEFF6FF)
                        ) {
                            Text(
                                text = item.contextTag!!,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2563EB),
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = formattedDateText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                )
            }

            if (item.rawDate != null) {
                Surface(
                    onClick = { onNavigateToDate(item.rawDate!!) },
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF2563EB)
                            )
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Ir para a data",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
