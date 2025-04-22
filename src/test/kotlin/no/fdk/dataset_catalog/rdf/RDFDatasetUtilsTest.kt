package no.fdk.dataset_catalog.rdf

import no.fdk.dataset_catalog.model.DistributionDBO
import no.fdk.dataset_catalog.model.LocalizedStrings
import no.fdk.dataset_catalog.model.UriWithLabel
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

}