package no.fdk.dataset_catalog.contract

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.fdk.dataset_catalog.model.DatasetDBO
import no.fdk.dataset_catalog.utils.ApiTestContext
import no.fdk.dataset_catalog.utils.DATASET_1
import no.fdk.dataset_catalog.utils.DATASET_2
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
import org.springframework.http.HttpHeaders
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

            assertEquals(DB_DATASET_1, resultRead)
            assertEquals(DB_DATASET_1, resultWrite)
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

    @Nested
    internal inner class CreateDataset {
        @Test
        fun `Illegal create`() {
            val notLoggedIn = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1/datasets",
                mapper.writeValueAsString(DATASET_1),
                null,
                "POST"
            )
            val readAccess = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1/datasets",
                mapper.writeValueAsString(DATASET_1),
                JwtToken(Access.ORG_READ).toString(),
                "POST"
            )
            val wrongOrg = apiAuthorizedRequest(
                "/internal/catalogs/1/datasets",
                mapper.writeValueAsString(DATASET_1),
                JwtToken(Access.ORG_WRITE).toString(),
                "POST"
            )

            assertEquals(HttpStatus.UNAUTHORIZED.value(), notLoggedIn["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), readAccess["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), wrongOrg["status"])
        }


        @Test
        fun `Invalid create`() {
            val emptyBody = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1/datasets",
                "",
                JwtToken(Access.ORG_WRITE).toString(),
                "POST"
            )

            assertEquals(HttpStatus.BAD_REQUEST.value(), emptyBody["status"])
        }

        @Test
        fun `Able to get after create`() {
            val responseCreate = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1/datasets",
                mapper.writeValueAsString(DATASET_1),
                JwtToken(Access.ORG_WRITE).toString(),
                "POST"
            )
            assertTrue(HttpStatus.CREATED.value() == responseCreate["status"])

            val headers = responseCreate["header"] as HttpHeaders

            val responseGet = apiAuthorizedRequest(
                headers.location.toString(),
                null,
                JwtToken(Access.ORG_WRITE).toString(),
                "GET"
            )
            assertTrue(HttpStatus.OK.value() == responseGet["status"])

            val resultGet: DatasetDBO = mapper.readValue(responseGet["body"] as String)

            assertEquals(
                expected = DATASET_1.copy(
                    id = resultGet.id,
                    lastModified = resultGet.lastModified,
                    uri = resultGet.uri,
                    published = false
                ),
                actual = resultGet
            )

        }

        @Test
        fun `All fields are persisted`() {
            val responseCreate = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1/datasets",
                mapper.writeValueAsString(DATASET_2),
                JwtToken(Access.ORG_WRITE).toString(),
                "POST"
            )
            assertTrue(HttpStatus.CREATED.value() == responseCreate["status"])
            val headers = responseCreate["header"] as HttpHeaders

            val responseGet = apiAuthorizedRequest(
                headers.location.toString(),
                null,
                JwtToken(Access.ORG_WRITE).toString(),
                "GET"
            )
            assertTrue(HttpStatus.OK.value() == responseGet["status"])

            val resultGet: DatasetDBO = mapper.readValue(responseGet["body"] as String)

            assertEquals(
                expected = DATASET_2.copy(
                    id = resultGet.id,
                    lastModified = resultGet.lastModified,
                    uri = resultGet.uri,
                    published = false
                ),
                actual = resultGet
            )
        }
    }

    @Nested
    internal inner class DeleteDataset {
        @Test
        fun `Illegal delete`() {
            val notLoggedIn = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}",
                mapper.writeValueAsString(DATASET_1),
                null,
                "DELETE"
            )
            val readAccess = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}",
                mapper.writeValueAsString(DATASET_1),
                JwtToken(Access.ORG_READ).toString(),
                "DELETE"
            )
            val wrongOrg = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_2/datasets/${DB_DATASET_ID_1}",
                mapper.writeValueAsString(DATASET_1),
                JwtToken(Access.ORG_WRITE).toString(),
                "DELETE"
            )

            assertEquals(HttpStatus.UNAUTHORIZED.value(), notLoggedIn["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), readAccess["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), wrongOrg["status"])
        }

        @Test
        fun `Invalid delete of non existing dataset`() {
            val doesNotExist = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_4}",
                mapper.writeValueAsString(DATASET_1),
                JwtToken(Access.ORG_WRITE).toString(),
                "DELETE"
            )

            assertEquals(HttpStatus.NOT_FOUND.value(), doesNotExist["status"])
        }

        @Test
        fun `Cannot get after delete`() {
            val rspDelete = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}",
                null,
                JwtToken(Access.ORG_WRITE).toString(),
                "DELETE"
            )
            assertEquals(HttpStatus.OK.value(), rspDelete["status"])

            val rspGet = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}",
                null,
                JwtToken(Access.ORG_WRITE).toString(),
                "GET"
            )

            assertEquals(HttpStatus.NOT_FOUND.value(), rspGet["status"])
        }
    }

}
