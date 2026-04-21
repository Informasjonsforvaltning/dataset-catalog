package no.fdk.dataset_catalog.rdf

import no.fdk.dataset_catalog.model.DistributionDBO
import no.fdk.dataset_catalog.model.LocalizedStrings
import no.fdk.dataset_catalog.model.PeriodOfTimeDBO
import no.fdk.dataset_catalog.model.UriWithLabel
import org.apache.jena.datatypes.xsd.XSDDatatype
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.DCTerms
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@Tag("unit")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RDFDatasetUtilsTest {
    @Test
    fun testIfEmptyDistributionIsNotIncluded() {
        val model = ModelFactory.createDefaultModel()
        val resource = model.createResource("http://my-dataset1")

        resource.addDatasetDistribution(ADMS.sample, listOf(DistributionDBO()))

        assertFalse { resource.hasProperty(ADMS.sample) }
    }

    @Test
    fun testIfDistributionWithEmptyValuesIsNotIncluded() {
        val model = ModelFactory.createDefaultModel()
        val resource = model.createResource("http://my-dataset1")

        resource.addDatasetDistribution(ADMS.sample, listOf(
            DistributionDBO(
                title = LocalizedStrings(nb = ""),
                description = LocalizedStrings(nb = ""),
                accessURL = listOf(""),
                conformsTo = listOf(UriWithLabel(uri = "", prefLabel = LocalizedStrings(nb = ""))),
                format = listOf(""),
                license = "",
                page = listOf("")
            ),
            DistributionDBO(
                    title = null,
                    description = LocalizedStrings(),
                    accessURL = listOf(""),
                    conformsTo = null,
                    format = listOf(),
                    license = null,
                    page = null
                )))

        assertFalse { resource.hasProperty(ADMS.sample) }
    }

    @Test
    fun testIfNonEmptyDistributionIsIncluded() {
        val model = ModelFactory.createDefaultModel()
        val resource = model.createResource("http://my-dataset1")

        resource.addDatasetDistribution(ADMS.sample, listOf(DistributionDBO(accessURL = listOf("http://access-url"))))

        assertTrue { resource.hasProperty(ADMS.sample) }
    }

    @Test
    fun yearOnlyTemporalEmitsXsdGYear() {
        val datatype = temporalDatatype(PeriodOfTimeDBO(startDate = "2024"), Schema.startDate)
        assertEquals(XSDDatatype.XSDgYear.uri, datatype)
    }

    @Test
    fun yearMonthTemporalEmitsXsdGYearMonth() {
        val datatype = temporalDatatype(PeriodOfTimeDBO(startDate = "2024-06"), Schema.startDate)
        assertEquals(XSDDatatype.XSDgYearMonth.uri, datatype)
    }

    @Test
    fun fullDateTemporalEmitsXsdDate() {
        val datatype = temporalDatatype(PeriodOfTimeDBO(startDate = "2024-06-15"), Schema.startDate)
        assertEquals(XSDDatatype.XSDdate.uri, datatype)
    }

    @Test
    fun mixedPrecisionStartAndEndEmitDistinctTypes() {
        val model = ModelFactory.createDefaultModel()
        val resource = model.createResource("http://my-dataset-mixed")
        resource.addTemporal(listOf(PeriodOfTimeDBO(startDate = "2024", endDate = "2024-06")))

        val period = resource.getProperty(DCTerms.temporal).`object`.asResource()
        assertEquals(XSDDatatype.XSDgYear.uri, period.getProperty(Schema.startDate).literal.datatypeURI)
        assertEquals(XSDDatatype.XSDgYearMonth.uri, period.getProperty(Schema.endDate).literal.datatypeURI)
    }

    @Test
    fun emptyTemporalPeriodNotEmitted() {
        val model = ModelFactory.createDefaultModel()
        val resource = model.createResource("http://my-dataset-empty")
        resource.addTemporal(listOf(PeriodOfTimeDBO(startDate = "", endDate = null)))
        assertFalse { resource.hasProperty(DCTerms.temporal) }
    }

    @Test
    fun flexibleDateLiteralIgnoresMalformedLength() {
        val model = ModelFactory.createDefaultModel()
        val resource = model.createResource("http://my-dataset-malformed")
        resource.safeAddFlexibleDateLiteral(Schema.startDate, "2024-06-1")
        assertFalse { resource.hasProperty(Schema.startDate) }
    }

    private fun temporalDatatype(period: PeriodOfTimeDBO, property: org.apache.jena.rdf.model.Property): String {
        val model = ModelFactory.createDefaultModel()
        val resource = model.createResource("http://my-dataset-temporal")
        resource.addTemporal(listOf(period))
        val periodResource = resource.getProperty(DCTerms.temporal).`object`.asResource()
        return periodResource.getProperty(property).literal.datatypeURI
    }

}