package cc.rhuan.receitas

import cc.rhuan.receitas.data.local.Converters
import cc.rhuan.receitas.domain.model.MedicationSource
import cc.rhuan.receitas.domain.model.PrescriptionType
import cc.rhuan.receitas.text.TextMessage
import org.junit.Assert.assertEquals
import org.junit.Test

class TextResourcesTest {
    @Test fun validationMessagesResolveNestedResourcesAndRecordNames() {
        val message = TextMessage(R.string.named_validation_error,
            listOf("Losartana", TextMessage(R.string.last_pickup_date_required)))
        assertEquals("Losartana: Informe a data da última retirada.", TestTexts.resolve(message))
    }

    @Test fun renamedEnumsRoundTripThroughDatabaseConverters() {
        val converters = Converters()
        MedicationSource.entries.forEach { source ->
            assertEquals(source.name, converters.fromMedicationSource(source))
            assertEquals(source, converters.toMedicationSource(converters.fromMedicationSource(source)))
        }
        PrescriptionType.entries.forEach { type ->
            assertEquals(type.name, converters.fromPrescriptionType(type))
            assertEquals(type, converters.toPrescriptionType(converters.fromPrescriptionType(type)))
        }
    }
}
