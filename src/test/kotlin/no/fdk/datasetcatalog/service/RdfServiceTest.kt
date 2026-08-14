package no.fdk.datasetcatalog.service

import no.fdk.datasetcatalog.configuration.ApplicationProperties
import no.fdk.datasetcatalog.model.ApplicationProfile
import no.fdk.datasetcatalog.model.CatalogCount
import no.fdk.datasetcatalog.model.Cost
import no.fdk.datasetcatalog.model.DatasetDBO
import no.fdk.datasetcatalog.model.DistributionDBO
import no.fdk.datasetcatalog.model.LocalizedStrings
import no.fdk.datasetcatalog.model.ReferenceDBO
import no.fdk.datasetcatalog.model.RightsDBO
import no.fdk.datasetcatalog.model.UriWithLabel
import no.fdk.datasetcatalog.utils.TEST_CATALOG_1
import no.fdk.datasetcatalog.utils.TEST_DATASET_1
import no.fdk.datasetcatalog.utils.TestResponseReader
import no.fdk.datasetcatalog.utils.checkIfIsomorphicAndPrintDiff
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
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
            val dataset =
                DatasetDBO(
                    catalogId = "1",
                    published = true,
                    approved = true,
                    lastModified = LocalDateTime.now(),
                    id = "http://catalog/1/dataset/1",
                    uri = "http://catalog/1/dataset/1",
                    relatedResources =
                    listOf(
                        UriWithLabel(
                            uri = "http://uri-1",
                            prefLabel = LocalizedStrings(nb = "label-1-nb", en = "label-1-en"),
                        ),
                        UriWithLabel(
                            uri = "http://uri-2",
                            prefLabel = LocalizedStrings(nb = "label-2-nb", en = "label-2-en"),
                        ),
                    ),
                )

            val catalog = CatalogCount(id = "1", datasetCount = 0)

            whenever(catalogService.getByID("1")).thenReturn(catalog)
            whenever(datasetService.getAllDatasets("1")).thenReturn(listOf(dataset))
            whenever(applicationProperties.organizationCatalogHost).thenReturn("http://localhost:5050")

            val expected = responseReader.parseFile("catalog_0.ttl", "TURTLE")
            val responseModel = rdfService.getCatalogById("1")

            assertTrue(
                checkIfIsomorphicAndPrintDiff(
                    responseModel!!,
                    expected,
                    "Serializing dataset relations",
                    logger,
                ),
            )
        }

        @Test
        fun `Serializes dataset qualified attributions`() {
            val dataset =
                DatasetDBO(
                    catalogId = "1",
                    published = true,
                    approved = true,
                    id = "http://catalog/1/dataset/1",
                    uri = "http://catalog/1/dataset/1",
                    qualifiedAttributions = setOf("123456789", "987654321"),
                    lastModified = LocalDateTime.now(),
                )
            val catalog = CatalogCount(id = "1", datasetCount = 1)

            whenever(catalogService.getByID("1")).thenReturn(catalog)
            whenever(datasetService.getAllDatasets("1")).thenReturn(listOf(dataset))
            whenever(applicationProperties.organizationCatalogHost).thenReturn("http://localhost:5050")

            val expected = responseReader.parseFile("catalog_1.ttl", "TURTLE")
            val responseModel = rdfService.getCatalogById("1")

            assertTrue(
                checkIfIsomorphicAndPrintDiff(
                    responseModel!!,
                    expected,
                    "Serializing qualified attributions",
                    logger,
                ),
            )
        }

        @Test
        fun `Serializes dataset costs`() {
            val dataset =
                DatasetDBO(
                    catalogId = "1",
                    published = true,
                    approved = true,
                    lastModified = LocalDateTime.now(),
                    id = "http://catalog/dataset",
                    uri = "http://catalog/dataset",
                    costs =
                    listOf(
                        Cost(
                            value = 125.57,
                            currency = "http://publications.europa.eu/resource/authority/currency/EUR",
                        ),
                        Cost(
                            description = LocalizedStrings(nb = "med doc"),
                            documentation = listOf("https://gebyr-doc.no"),
                        ),
                    ),
                )

            val catalog = CatalogCount(id = "1", datasetCount = 1)

            whenever(catalogService.getByID("1")).thenReturn(catalog)
            whenever(datasetService.getAllDatasets("1")).thenReturn(listOf(dataset))
            whenever(applicationProperties.organizationCatalogHost).thenReturn("http://localhost:5050")

            val expected = responseReader.parseFile("dataset_with_costs.ttl", "TURTLE")
            val responseModel = rdfService.getCatalogById("1")

            assertTrue(
                checkIfIsomorphicAndPrintDiff(
                    responseModel!!,
                    expected,
                    "Serializing dataset costs",
                    logger,
                ),
            )
        }

        @Test
        fun `Serializes mobilityDCAT dataset`() {
            val dataset =
                DatasetDBO(
                    catalogId = "1",
                    published = true,
                    approved = true,
                    lastModified = LocalDateTime.now(),
                    id = "http://catalog/mobility-dataset",
                    uri = "http://catalog/mobility-dataset",
                    applicationProfile = ApplicationProfile.MOBILITYDCAT_AP,
                    mobilityTheme = setOf("http://mobility-theme-1", "http://mobility-theme-2"),
                    distribution =
                    listOf(
                        DistributionDBO(
                            accessURL = listOf("http://access-url"),
                            mobilityDataStandard = "http://mobility-data-standard",
                            rights = RightsDBO(type = "http://rights-type"),
                        ),
                    ),
                )

            val catalog = CatalogCount(id = "1", datasetCount = 1)

            whenever(catalogService.getByID("1")).thenReturn(catalog)
            whenever(datasetService.getAllDatasets("1")).thenReturn(listOf(dataset))
            whenever(applicationProperties.organizationCatalogHost).thenReturn("http://localhost:5050")

            val expected = responseReader.parseFile("mobility_dataset.ttl", "TURTLE")
            val responseModel = rdfService.getCatalogById("1")

            assertTrue(
                checkIfIsomorphicAndPrintDiff(
                    responseModel!!,
                    expected,
                    "Serializing mobilityDCAT dataset",
                    logger,
                ),
            )
        }

        @Test
        fun `Serializes complete catalog`() {
            val dataset = TEST_DATASET_1
            val catalog = TEST_CATALOG_1
            val references =
                listOf(
                    ReferenceDBO(
                        referenceType = "http://purl.org/dc/terms/references",
                        source = "http://referenced/dataset/resolved",
                    ),
                    ReferenceDBO(
                        referenceType = "http://purl.org/dc/terms/hasPart",
                        source = "http://has-part.no",
                    ),
                )

            whenever(catalogService.getByID(catalog.id)).thenReturn(catalog)
            whenever(datasetService.getAllDatasets(catalog.id)).thenReturn(listOf(dataset))
            whenever(datasetService.resolveDatasetReferences(dataset)).thenReturn(references)
            whenever(applicationProperties.organizationCatalogHost).thenReturn("http://localhost:5050")

            val expected = responseReader.parseFile("catalog_2.ttl", "TURTLE")
            val responseModel = rdfService.getCatalogById(catalog.id)!!

            assertTrue(checkIfIsomorphicAndPrintDiff(responseModel, expected, "Serializing complete catalog", logger))
        }
    }
}
