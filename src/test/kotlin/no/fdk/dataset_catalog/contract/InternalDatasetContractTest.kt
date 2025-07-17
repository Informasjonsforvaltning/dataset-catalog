package no.fdk.dataset_catalog.contract

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.fdk.dataset_catalog.extensions.datasetToDBO
import no.fdk.dataset_catalog.model.DatasetDBO
import no.fdk.dataset_catalog.utils.ApiTestContext
import no.fdk.dataset_catalog.utils.DATASET_ID_1
import no.fdk.dataset_catalog.utils.DB_CATALOG_ID_1
import no.fdk.dataset_catalog.utils.DB_CATALOG_ID_2
import no.fdk.dataset_catalog.utils.DB_DATASET_1
import no.fdk.dataset_catalog.utils.DB_DATASET_ID_1
import no.fdk.dataset_catalog.utils.DB_DATASET_ID_4
import no.fdk.dataset_catalog.utils.DB_DATASET_ID_5
import no.fdk.dataset_catalog.utils.DB_DATASET_ID_6
import no.fdk.dataset_catalog.utils.apiAuthorizedRequest
import no.fdk.dataset_catalog.utils.jwk.Access
import no.fdk.dataset_catalog.utils.jwk.JwtToken
import no.fdk.dataset_catalog.utils.resetDB
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val mapper = jacksonObjectMapper()

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@SpringBootTest(
    properties = ["spring.profiles.active=contract-test"],
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
@ContextConfiguration(initializers = [ApiTestContext.Initializer::class])
@Tag("contract")
class InternalDatasetContractTest : ApiTestContext() {

    @Nested
    internal inner class GetDataset {
        @Test
        fun `Unable to get when not logged in as a user with org access`() {
            resetDB()
            val notLoggedIn = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1/datasets/${DATASET_ID_1}",
                null,
                null,
                "GET"
            )
            val wrongOrg = apiAuthorizedRequest(
                "/internal/catalogs/${DB_CATALOG_ID_2}/datasets/${DB_DATASET_ID_4}",
                null,
                JwtToken(Access.ORG_READ).toString(),
                "GET"
            )

            assertEquals(HttpStatus.UNAUTHORIZED.value(), notLoggedIn["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), wrongOrg["status"])
        }

        @Test
        fun `Both read and write can read`() {
            resetDB()
            val responseRead = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}",
                null,
                JwtToken(Access.ORG_READ).toString(),
                "GET"
            )
            val responseWrite = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}",
                null,
                JwtToken(Access.ORG_WRITE).toString(),
                "GET"
            )

            assertTrue(HttpStatus.OK.value() == responseRead["status"])
            assertTrue(HttpStatus.OK.value() == responseWrite["status"])

            val resultRead: DatasetDBO = mapper.readValue(responseRead["body"] as String)
            val resultWrite: DatasetDBO = mapper.readValue(responseWrite["body"] as String)

            assertEquals(DB_DATASET_1.datasetToDBO(), resultRead)
            assertEquals(DB_DATASET_1.datasetToDBO(), resultWrite)
        }

        @Test
        fun `Get datasets by catalogId only returns datasets in specified catalog`() {
            resetDB()
            val response = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_2/datasets",
                null,
                JwtToken(Access.ROOT).toString(),
                "GET"
            )
            val result: List<DatasetDBO> = mapper.readValue(response["body"] as String)

            assertEquals(
                listOf(DB_DATASET_ID_4, DB_DATASET_ID_5, DB_DATASET_ID_6),
                result.map { it.id }.sorted()
            )
        }
    }
}
