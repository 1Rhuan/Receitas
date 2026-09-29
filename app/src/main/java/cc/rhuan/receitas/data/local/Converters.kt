package cc.rhuan.receitas.data.local

import androidx.room.TypeConverter
import cc.rhuan.receitas.domain.model.MedicationSource
import cc.rhuan.receitas.domain.model.NotificationType
import cc.rhuan.receitas.domain.model.PharmaceuticalForm
import cc.rhuan.receitas.domain.model.PrescriptionType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Converters {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.format(dateFormatter)
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it, dateFormatter) }
    }

    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? {
        return value?.format(timeFormatter)
    }

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it, timeFormatter) }
    }

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.format(dateTimeFormatter)
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, dateTimeFormatter) }
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
        return value?.let { runCatching { MedicationSource.valueOf(it) }.getOrNull() }
    }

    @TypeConverter
    fun fromPrescriptionType(value: PrescriptionType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toPrescriptionType(value: String?): PrescriptionType? {
        return value?.let { runCatching { PrescriptionType.valueOf(it) }.getOrNull() }
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
