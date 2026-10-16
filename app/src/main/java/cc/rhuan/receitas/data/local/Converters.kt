package cc.rhuan.receitas.data.local

import androidx.room.TypeConverter
import cc.rhuan.receitas.domain.model.MedicationSource
import cc.rhuan.receitas.domain.model.NotificationType
import cc.rhuan.receitas.domain.model.PharmaceuticalForm
import cc.rhuan.receitas.domain.model.PrescriptionType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Converters {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.format(dateFormatter)
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it, dateFormatter) }
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    @TypeConverter
    fun fromPharmaceuticalForm(value: PharmaceuticalForm?): String? {
        return value?.name
    }

    @TypeConverter
    fun toPharmaceuticalForm(value: String?): PharmaceuticalForm? {
        return value?.let { runCatching { PharmaceuticalForm.valueOf(it) }.getOrNull() }
    }

    @TypeConverter
    fun fromMedicationSource(value: MedicationSource?): String? {
        return value?.name
    }

    @TypeConverter
    fun toMedicationSource(value: String?): MedicationSource? {
        // Older database versions used these storage codes. New writes use enum names.
        val code = when (value) {
            "COMPRA_PROPRIA" -> "SELF_PURCHASE"
            "CONVENIO" -> "HEALTH_INSURANCE"
            "FARMACIA_POPULAR" -> "POPULAR_PHARMACY"
            "FARMACIA_BASICA" -> "PUBLIC_PHARMACY"
            "OUTRO" -> "OTHER"
            else -> value
        }
        return code?.let { runCatching { MedicationSource.valueOf(it) }.getOrNull() }
    }

    @TypeConverter
    fun fromPrescriptionType(value: PrescriptionType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toPrescriptionType(value: String?): PrescriptionType? {
        val code = when (value) {
            "CONTINUO" -> "CONTINUOUS_USE"
            "ANTIBIOTICO" -> "ANTIBIOTIC"
            "CONTROLADO" -> "CONTROLLED"
            "FARMACIA_POPULAR" -> "POPULAR_PHARMACY"
            "ANUAL" -> "ANNUAL_USE"
            "PERSONALIZADO" -> "CUSTOM"
            else -> value
        }
        return code?.let { runCatching { PrescriptionType.valueOf(it) }.getOrNull() }
    }

    @TypeConverter
    fun fromNotificationType(value: NotificationType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toNotificationType(value: String?): NotificationType? {
        return value?.let { runCatching { NotificationType.valueOf(it) }.getOrNull() }
    }
}
