package no.fdk.dataset_catalog.contract

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.fdk.dataset_catalog.model.CatalogCount
import no.fdk.dataset_catalog.utils.ApiTestContext
import no.fdk.dataset_catalog.utils.DB_CATALOG_1
import no.fdk.dataset_catalog.utils.DB_CATALOG_ID_1
import no.fdk.dataset_catalog.utils.DB_CATALOG_ID_2
import no.fdk.dataset_catalog.utils.SERIES_CATALOG_ID
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    properties = ["spring.profiles.active=contract-test"],
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
@ContextConfiguration(initializers = [ApiTestContext.Initializer::class])
@Tag("contract")
class InternalCatalogContractTest : ApiTestContext() {

    @Nested
    internal inner class GetCatalog {
        @Test
        fun `Unable to get when not logged in as a user with org access`() {
            val notLoggedIn = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1",
                null,
                null,
                "GET"
            )
            val wrongOrg = apiAuthorizedRequest(
                "/internal/catalogs/1",
                null,
                JwtToken(Access.ORG_READ).toString(),
                "GET"
            )

            assertEquals(HttpStatus.UNAUTHORIZED.value(), notLoggedIn["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), wrongOrg["status"])
        }

        @Test
        fun `Both read and write can read`() {
            val responseRead = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1",
                null,
                JwtToken(Access.ORG_READ).toString(),
                "GET"
            )
            val responseWrite = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1",
                null,
                JwtToken(Access.ORG_WRITE).toString(),
                "GET"
            )

            assertTrue(HttpStatus.OK.value() == responseRead["status"])
            assertTrue(HttpStatus.OK.value() == responseWrite["status"])

            val resultRead: CatalogCount = mapper.readValue(responseRead["body"] as String)
            val resultWrite: CatalogCount = mapper.readValue(responseWrite["body"] as String)

            assertEquals(DB_CATALOG_1, resultRead)
            assertEquals(DB_CATALOG_1, resultWrite)
        }

        @Test
        fun `Get All catalogs returns all permitted catalogs`() {
            resetDB()
            val responseRead = apiAuthorizedRequest(
                "/internal/catalogs",
                null,
                JwtToken(Access.ORG_READ).toString(),
                "GET"
            )
            val resultRead: List<CatalogCount> = mapper.readValue(responseRead["body"] as String)

            val expectedRead: List<CatalogCount> = listOf(CatalogCount(DB_CATALOG_ID_1, 3))
            assertEquals(expectedRead, resultRead)

            val responseRoot = apiAuthorizedRequest("/internal/catalogs", null, JwtToken(Access.ROOT).toString(), "GET")
            val resultRoot: List<CatalogCount> = mapper.readValue(responseRoot["body"] as String)

            val expectedRoot: List<CatalogCount> = listOf(
                CatalogCount(SERIES_CATALOG_ID, 4),
                CatalogCount(DB_CATALOG_ID_1, 3),
                CatalogCount(DB_CATALOG_ID_2, 3),
            )
            assertEquals(expectedRoot, resultRoot.sortedBy { it.id })
        }

    }

}
