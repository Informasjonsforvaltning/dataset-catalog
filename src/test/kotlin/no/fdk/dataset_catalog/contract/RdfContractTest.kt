package no.fdk.dataset_catalog.contract

import no.fdk.dataset_catalog.utils.*
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.Lang
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
        fun `Gets all catalogs`() {
            resetDB()

            val getAllTurtle = apiAuthorizedRequest("/catalogs/", method="GET", accept=MediaType("text","turtle"))
            val getAllN3 = apiAuthorizedRequest("/catalogs/", method="GET", accept=MediaType("text","n3"))
            val getAllTrig = apiAuthorizedRequest("/catalogs/", method="GET", accept=MediaType("application","trig"))
            val getAllTrix = apiAuthorizedRequest("/catalogs/", method="GET", accept=MediaType("application","trix"))

            assertEquals(HttpStatus.OK.value(), getAllTurtle["status"])
            assertEquals(HttpStatus.OK.value(), getAllN3["status"])
            assertEquals(HttpStatus.OK.value(), getAllTrig["status"])
            assertEquals(HttpStatus.OK.value(), getAllTrix["status"])

            val expectedGetAll = responseReader.parseFile("getAll.ttl", "TURTLE")

            val actualGetAllTurtle = ModelFactory.createDefaultModel().read(StringReader(getAllTurtle["body"] as String), null, Lang.TURTLE.name)
            val actualGetAllN3 = ModelFactory.createDefaultModel().read(StringReader(getAllN3["body"] as String), null, Lang.N3.name)
            val actualGetAllTrig = ModelFactory.createDefaultModel().read(StringReader(getAllTrig["body"] as String), null, Lang.TRIG.name)
            val actualGetAllTrix = ModelFactory.createDefaultModel().read(StringReader(getAllTrix["body"] as String), null, Lang.TRIX.name)

            assertTrue(checkIfIsomorphicAndPrintDiff(actualGetAllTurtle, expectedGetAll, "Get All turtle result", logger))
            assertTrue(checkIfIsomorphicAndPrintDiff(actualGetAllN3, expectedGetAll, "Get All n3 result", logger))
            assertTrue(checkIfIsomorphicAndPrintDiff(actualGetAllTrig, expectedGetAll, "Get All trig result", logger))
            assertTrue(checkIfIsomorphicAndPrintDiff(actualGetAllTrix, expectedGetAll, "Get All trix result", logger))
        }

        @Test
        fun `Gets single catalog`() {
            resetDB()

            val getOneTurtle = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1", method="GET", accept=MediaType("text","turtle"))
            val getOneXML = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1", method="GET", accept=MediaType("application","rdf+xml"))
            val getOneJSON = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1", method="GET", accept=MediaType("application","rdf+json"))
            val getOneNQuads = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1", method="GET", accept=MediaType("application","n-quads"))

            assertEquals(HttpStatus.OK.value(), getOneTurtle["status"])
            assertEquals(HttpStatus.OK.value(), getOneXML["status"])
            assertEquals(HttpStatus.OK.value(), getOneJSON["status"])
            assertEquals(HttpStatus.OK.value(), getOneNQuads["status"])

            val expectedGetOne = responseReader.parseFile("getOne.ttl", "TURTLE")

            val actualGetOneTurtle = ModelFactory.createDefaultModel().read(StringReader(getOneTurtle["body"] as String), null, Lang.TURTLE.name)
            val actualGetOneXML = ModelFactory.createDefaultModel().read(StringReader(getOneXML["body"] as String), null, Lang.RDFXML.name)
            val actualGetOneJSON = ModelFactory.createDefaultModel().read(StringReader(getOneJSON["body"] as String), null, Lang.RDFJSON.name)
            val actualGetOneNQUADS = ModelFactory.createDefaultModel().read(StringReader(getOneNQuads["body"] as String), null, Lang.NQUADS.name)

            assertTrue(checkIfIsomorphicAndPrintDiff(actualGetOneTurtle, expectedGetOne, "Get One turtle result", logger))
            assertTrue(checkIfIsomorphicAndPrintDiff(actualGetOneXML, expectedGetOne, "Get One xml result", logger))
            assertTrue(checkIfIsomorphicAndPrintDiff(actualGetOneJSON, expectedGetOne, "Get One json result", logger))
            assertTrue(checkIfIsomorphicAndPrintDiff(actualGetOneNQUADS, expectedGetOne, "Get One n-quads result", logger))
        }

        @Test
        fun `Gets Dataset`() {
            resetDB()

            val responseTurtle = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/$DB_DATASET_ID_2", method="GET", accept=MediaType("text","turtle"))
            val responseNTriples = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/$DB_DATASET_ID_2", method="GET", accept=MediaType("application","n-triples"))
            val responseJsonLD = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/$DB_DATASET_ID_2", method="GET", accept=MediaType("application","ld+json"))

            assertEquals(HttpStatus.OK.value(), responseTurtle["status"])
            assertEquals(HttpStatus.OK.value(), responseNTriples["status"])
            assertEquals(HttpStatus.OK.value(), responseJsonLD["status"])

            val expected = responseReader.parseFile("dataset.ttl", "TURTLE")

            val actualTurtle = ModelFactory.createDefaultModel().read(StringReader(responseTurtle["body"] as String), null, Lang.TURTLE.name)
            val actualNTriples = ModelFactory.createDefaultModel().read(StringReader(responseNTriples["body"] as String), null, Lang.NTRIPLES.name)
            val actualJsonLD = ModelFactory.createDefaultModel().read(StringReader(responseJsonLD["body"] as String), null, Lang.JSONLD.name)

            assertTrue(checkIfIsomorphicAndPrintDiff(actualTurtle, expected, "Get Dataset turtle result", logger))
            assertTrue(checkIfIsomorphicAndPrintDiff(actualNTriples, expected, "Get Dataset n-triples result", logger))
            assertTrue(checkIfIsomorphicAndPrintDiff(actualJsonLD, expected, "Get Dataset json-ld result", logger))
        }

        @Test
        fun `Unpublished Dataset not found`() {
            val getOne = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_2/datasets/$DB_DATASET_ID_6", method="GET", accept=MediaType("text","turtle"))

            assertEquals(HttpStatus.NOT_FOUND.value(), getOne["status"])
        }
    }
}
