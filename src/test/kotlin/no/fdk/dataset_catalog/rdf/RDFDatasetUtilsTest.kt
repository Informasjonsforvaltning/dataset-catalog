package no.fdk.dataset_catalog.rdf

import no.fdk.dataset_catalog.model.Cost
import no.fdk.dataset_catalog.model.DistributionDBO
import no.fdk.dataset_catalog.model.LocalizedStrings
import no.fdk.dataset_catalog.model.PeriodOfTimeDBO
import no.fdk.dataset_catalog.model.UriWithLabel
import org.apache.jena.datatypes.xsd.XSDDatatype
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
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

    @Test
    fun flexibleDateLiteralIgnoresLexicallyInvalidValue() {
        val model = ModelFactory.createDefaultModel()
        val resource = model.createResource("http://my-dataset-lexical")
        resource.safeAddFlexibleDateLiteral(Schema.startDate, "abcd")
        resource.safeAddFlexibleDateLiteral(Schema.endDate, "2024-13-01")
        assertFalse { resource.hasProperty(Schema.startDate) }
        assertFalse { resource.hasProperty(Schema.endDate) }
    }

    private fun temporalDatatype(period: PeriodOfTimeDBO, property: org.apache.jena.rdf.model.Property): String {
        val model = ModelFactory.createDefaultModel()
        val resource = model.createResource("http://my-dataset-temporal")
        resource.addTemporal(listOf(period))
        val periodResource = resource.getProperty(DCTerms.temporal).`object`.asResource()
        return periodResource.getProperty(property).literal.datatypeURI
    }

    @Test
    fun nullCostsListAddsNothing() {
        val model = ModelFactory.createDefaultModel()
        val resource = model.createResource("http://my-dataset-cost")

        resource.addCosts(null)

        assertFalse { resource.hasProperty(CV.hasCost) }
    }

    @Test
    fun emptyCostsListAddsNothing() {
        val model = ModelFactory.createDefaultModel()
        val resource = model.createResource("http://my-dataset-cost")

        resource.addCosts(emptyList())

        assertFalse { resource.hasProperty(CV.hasCost) }
    }

    @Test
    fun costWithAllFieldsIsSerialized() {
        val model = ModelFactory.createDefaultModel()
        val resource = model.createResource("http://my-dataset-cost")

        resource.addCosts(
            listOf(
                Cost(
                    value = 125.57,
                    description = LocalizedStrings(nb = "med doc", en = "with doc"),
                    documentation = listOf("https://gebyr-doc.no"),
                    currency = "http://publications.europa.eu/resource/authority/currency/EUR",
                )
            )
        )

        val costResource = resource.getProperty(CV.hasCost).`object`.asResource()
        assertTrue { costResource.hasProperty(RDF.type, CV.Cost) }

        val valueLiteral = costResource.getProperty(CV.hasValue).literal
        assertEquals(125.57, valueLiteral.double)
        assertEquals(XSDDatatype.XSDdouble.uri, valueLiteral.datatypeURI)

        assertEquals(
            "http://publications.europa.eu/resource/authority/currency/EUR",
            costResource.getProperty(CV.currency).`object`.asResource().uri
        )

        assertEquals(
            "https://gebyr-doc.no",
            costResource.getProperty(FOAF.page).`object`.asResource().uri
        )

        val descriptions = costResource.listProperties(DCTerms.description).toList()
        assertEquals(2, descriptions.size)
        val descriptionsByLang = descriptions.associate { it.literal.language to it.literal.string }
        assertEquals("med doc", descriptionsByLang["nb"])
        assertEquals("with doc", descriptionsByLang["en"])
    }

    @Test
    fun costWithoutValueIsStillSerialized() {
        val model = ModelFactory.createDefaultModel()
        val resource = model.createResource("http://my-dataset-cost")

        resource.addCosts(
            listOf(
                Cost(
                    value = null,
                    description = LocalizedStrings(nb = "med doc"),
                    documentation = listOf("https://gebyr-doc.no"),
                    currency = null,
                )
            )
        )

        val costResource = resource.getProperty(CV.hasCost).`object`.asResource()
        assertTrue { costResource.hasProperty(RDF.type, CV.Cost) }
        assertNull(costResource.getProperty(CV.hasValue))
        assertNull(costResource.getProperty(CV.currency))
        assertNotNull(costResource.getProperty(DCTerms.description))
        assertNotNull(costResource.getProperty(FOAF.page))
    }

    @Test
    fun multipleCostsAreSerialized() {
        val model = ModelFactory.createDefaultModel()
        val resource = model.createResource("http://my-dataset-cost")

        resource.addCosts(
            listOf(
                Cost(
                    value = 125.57,
                    currency = "http://publications.europa.eu/resource/authority/currency/EUR",
                ),
                Cost(
                    description = LocalizedStrings(nb = "med doc"),
                    documentation = listOf("https://gebyr-doc.no"),
                )
            )
        )

        assertEquals(2, resource.listProperties(CV.hasCost).toList().size)
    }

    @Test
    fun invalidCurrencyUrlIsSkipped() {
        val model = ModelFactory.createDefaultModel()
        val resource = model.createResource("http://my-dataset-cost")

        resource.addCosts(
            listOf(
                Cost(
                    value = 10.0,
                    currency = "not-a-uri",
                )
            )
        )

        val costResource = resource.getProperty(CV.hasCost).`object`.asResource()
        assertNull(costResource.getProperty(CV.currency))
        assertNotNull(costResource.getProperty(CV.hasValue))
    }

    @Test
    fun invalidDocumentationUrlsAreSkipped() {
        val model = ModelFactory.createDefaultModel()
        val resource = model.createResource("http://my-dataset-cost")

        resource.addCosts(
            listOf(
                Cost(
                    documentation = listOf("not-a-uri", "https://gebyr-doc.no"),
                )
            )
        )

        val costResource = resource.getProperty(CV.hasCost).`object`.asResource()
        val pages = costResource.listProperties(FOAF.page).toList()
        assertEquals(1, pages.size)
        assertEquals("https://gebyr-doc.no", pages[0].`object`.asResource().uri)
    }

}