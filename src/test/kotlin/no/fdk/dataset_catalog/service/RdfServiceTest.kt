package no.fdk.dataset_catalog.service

import no.fdk.dataset_catalog.configuration.ApplicationProperties
import no.fdk.dataset_catalog.model.*
import no.fdk.dataset_catalog.utils.TEST_CATALOG_1
import no.fdk.dataset_catalog.utils.TEST_DATASET_1
import no.fdk.dataset_catalog.utils.TestResponseReader
import no.fdk.dataset_catalog.utils.checkIfIsomorphicAndPrintDiff
import org.apache.jena.vocabulary.DCTerms
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.slf4j.LoggerFactory
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private val logger = LoggerFactory.getLogger(RdfServiceTest::class.java)

@Tag("unit")
class RdfServiceTest {
    private val catalogService: CatalogService = mock()
    private val datasetService: DatasetService = mock()
    private val applicationProperties: ApplicationProperties = mock()
    private val rdfService = RDFService(catalogService, datasetService, applicationProperties)
    private val responseReader = TestResponseReader()

    @BeforeEach
    fun setUp() {
        whenever(applicationProperties.catalogUriHost).thenReturn("http://localhost:5050/catalogs")
    }

    @Nested
    internal inner class Serialize {

        @Test
        fun `Empty catalog serializes correctly`() {
            whenever(catalogService.getByID("1")).thenReturn(CatalogCount(id = "1", datasetCount = 0))

            assertNotNull(rdfService.getCatalogById("1"))
        }

        @Test
        fun `Serializes dataset relations`() {
            val dataset = Dataset(
                registrationStatus = REGISTRATION_STATUS.PUBLISH,
                id = "http://catalog/1/dataset/1",
                uri = "http://catalog/1/dataset/1",
                catalogId = "1",
                relations = listOf(
                    SkosConcept(
                        uri = "http://uri-1",
                        prefLabel = mapOf(Pair("nb", "label-1-nb"), Pair("en", "label-1-en"))
                    ),
                    SkosConcept(
                        uri = "http://uri-2",
                        prefLabel = mapOf(Pair("nb", "label-2-nb"), Pair("en", "label-2-en"))
                    ),
                )
            )

            val catalog = CatalogCount(id = "1", datasetCount = 0)

            whenever(catalogService.getByID("1")).thenReturn(catalog)
            whenever(datasetService.getAll("1")).thenReturn(listOf(dataset))
            whenever(applicationProperties.organizationCatalogHost).thenReturn("http://localhost:5050")

            val expected = responseReader.parseFile("catalog_0.ttl", "TURTLE")
            val responseModel = rdfService.getCatalogById("1")

            assertTrue(checkIfIsomorphicAndPrintDiff(responseModel!!, expected, "Serializing dataset relations", logger))

        }

        @Test
        fun `Serializes dataset qualified attributions`() {
            val dataset = Dataset(registrationStatus = REGISTRATION_STATUS.PUBLISH, id = "http://catalog/1/dataset/1", uri = "http://catalog/1/dataset/1", qualifiedAttributions = setOf("123456789", "987654321"))
            val catalog = CatalogCount(id = "1", datasetCount = 1)

            whenever(catalogService.getByID("1")).thenReturn(catalog)
            whenever(datasetService.getAll("1")).thenReturn(listOf(dataset))
            whenever(applicationProperties.organizationCatalogHost).thenReturn("http://localhost:5050")

            val expected = responseReader.parseFile("catalog_1.ttl", "TURTLE")
            val responseModel = rdfService.getCatalogById("1")

            assertTrue(checkIfIsomorphicAndPrintDiff(responseModel!!, expected, "Serializing qualified attributions", logger))
        }

        @Test
        fun `Serializes complete catalog`() {
            val dataset = TEST_DATASET_1
            val catalog = TEST_CATALOG_1
            val references = listOf(Reference(
                    SkosCode(DCTerms.references.uri, "references", mapOf(Pair("nb", "Referanse"))),
                    SkosConcept("http://referenced/dataset/resolved", prefLabel = mapOf(Pair("nb", "Referanse datasett")))))

            whenever(catalogService.getByID(catalog.id!!)).thenReturn(catalog)
            whenever(datasetService.getAll("${catalog.id}")).thenReturn(listOf(dataset))
            whenever(datasetService.resolveReferences(dataset)).thenReturn(references)
            whenever(applicationProperties.organizationCatalogHost).thenReturn("http://localhost:5050")

            val expected = responseReader.parseFile("catalog_2.ttl", "TURTLE")
            val responseModel = rdfService.getCatalogById("${catalog.id}")!!

            assertTrue(checkIfIsomorphicAndPrintDiff(responseModel, expected, "Serializing complete catalog", logger))
        }
    }
}