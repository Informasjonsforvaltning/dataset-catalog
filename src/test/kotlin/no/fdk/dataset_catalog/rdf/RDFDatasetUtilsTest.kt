package no.fdk.dataset_catalog.rdf

import no.fdk.dataset_catalog.model.Distribution
import no.fdk.dataset_catalog.model.SkosConcept
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RDFDatasetUtilsTest {
    @Test
    fun testIfEmptyDistributionIsNotIncluded() {
        val model = ModelFactory.createDefaultModel()
        val resource = model.createResource("http://my-dataset1")

        resource.addDistribution(ADMS.sample, listOf(Distribution()), "")

        assertFalse { resource.hasProperty(ADMS.sample) }
    }

    @Test
    fun testIfDistributionWithEmptyValuesIsNotIncluded() {
        val model = ModelFactory.createDefaultModel()
        val resource = model.createResource("http://my-dataset1")

        resource.addDistribution(ADMS.sample, listOf(
                Distribution(
                    title = mapOf("nb" to ""),
                    description = mapOf("nb" to ""),
                    uri = "",
                    accessURL = listOf(""),
                    conformsTo = listOf(SkosConcept(uri = "", prefLabel = mapOf("nb" to ""))),
                    format = listOf(""),
                    license = SkosConcept(uri = "", prefLabel = mapOf()),
                    page = listOf(SkosConcept(uri = "", prefLabel = mapOf("nb" to "")))
        ),
                Distribution(
                    title = null,
                    description = mapOf(),
                    uri = "",
                    accessURL = listOf(""),
                    conformsTo = null,
                    format = listOf(),
                    license = null,
                    page = null
                )), "")

        assertFalse { resource.hasProperty(ADMS.sample) }
    }

    @Test
    fun testIfNonEmptyDistributionIsIncluded() {
        val model = ModelFactory.createDefaultModel()
        val resource = model.createResource("http://my-dataset1")

        resource.addDistribution(ADMS.sample, listOf(Distribution(accessURL = listOf("http://access-url"))), "")

        assertTrue { resource.hasProperty(ADMS.sample) }
    }

}