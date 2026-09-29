package cc.rhuan.receitas.ui.feature.home.components

import java.time.LocalDate

sealed interface TrackingItem {
    val id: String
    val targetId: String
    val title: String
    val status: TrackingStatus
    val statusLabel: String
    val userNote: String?
    val primaryActionText: String
    val secondaryActionText: String
    val rawDate: LocalDate?
    val isFutureContext: Boolean
    val contextTag: String?

    data class Medication(
        override val id: String,
        override val targetId: String,
        override val title: String,
        override val status: TrackingStatus,
        override val statusLabel: String,
        val formattedDateText: String,
        val lastPickupDateText: String? = null,
        val stockText: String? = null,
        override val userNote: String? = null,
        override val primaryActionText: String,
        override val secondaryActionText: String,
        override val rawDate: LocalDate? = null,
        override val isFutureContext: Boolean = false,
        override val contextTag: String? = null,
    ) : TrackingItem

    data class Prescription(
        override val id: String,
        override val targetId: String,
        override val title: String,
        override val status: TrackingStatus,
        override val statusLabel: String,
        val formattedDateText: String,
        val prescriberName: String? = null,
        override val userNote: String? = null,
        override val primaryActionText: String,
        override val secondaryActionText: String,
        override val rawDate: LocalDate? = null,
        override val isFutureContext: Boolean = false,
        override val contextTag: String? = null,
    ) : TrackingItem
}
