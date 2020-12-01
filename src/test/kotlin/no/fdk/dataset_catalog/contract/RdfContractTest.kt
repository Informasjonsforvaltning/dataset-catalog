package no.fdk.dataset_catalog.contract

import no.fdk.dataset_catalog.utils.*
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.*
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import java.io.StringReader
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val logger = LoggerFactory.getLogger(RdfContractTest::class.java)

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@SpringBootTest(
    properties = ["spring.profiles.active=contract-test"],
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(initializers = [ApiTestContext.Initializer::class])
@Tag("contract")
class RdfContractTest: ApiTestContext() {
    private val responseReader = TestResponseReader()

    @Nested
    internal inner class GetRDF {
        @Test
        fun `Gets Catalog`() {
            resetDB()

            val getAll = apiAuthorizedRequest("/catalogs/", method="GET", accept=MediaType("text","turtle"))
            val getOne = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1", method="GET", accept=MediaType("text","turtle"))

            assertEquals(HttpStatus.OK.value(), getAll["status"])
            assertEquals(HttpStatus.OK.value(), getOne["status"])

            val expectedGetAll = responseReader.parseFile("getAll.ttl", "TURTLE")
            val expectedGetOne = responseReader.parseFile("getOne.ttl", "TURTLE")

            val actualGetAll = ModelFactory.createDefaultModel().read(StringReader(getAll["body"] as String), null, "TURTLE")
            val actualGetOne = ModelFactory.createDefaultModel().read(StringReader(getOne["body"] as String), null, "TURTLE")

            assertTrue(checkIfIsomorphicAndPrintDiff(actualGetAll, expectedGetAll, "Get All result", logger))
            assertTrue(checkIfIsomorphicAndPrintDiff(actualGetOne, expectedGetOne, "Get One result", logger))
        }
    }
}