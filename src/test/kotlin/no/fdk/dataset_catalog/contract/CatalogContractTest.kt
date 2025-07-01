package no.fdk.dataset_catalog.contract

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.fdk.dataset_catalog.model.CatalogCount
import no.fdk.dataset_catalog.model.CatalogDTO
import no.fdk.dataset_catalog.utils.*
import no.fdk.dataset_catalog.utils.jwk.Access
import no.fdk.dataset_catalog.utils.jwk.JwtToken
import org.junit.jupiter.api.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import kotlin.test.assertEquals

private val mapper = jacksonObjectMapper()

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    properties = ["spring.profiles.active=contract-test"],
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(initializers = [ApiTestContext.Initializer::class])
@Tag("contract")
class CatalogContractTest: ApiTestContext() {

    @Nested
    internal inner class GetCatalog {
        @Test
        fun `Unable to get when not logged in as a user with org access`() {
            val notLoggedIn = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1", null, null, "GET")
            val wrongOrg = apiAuthorizedRequest("/catalogs/1", null, JwtToken(Access.ORG_READ).toString(), "GET")

            assertEquals(HttpStatus.UNAUTHORIZED.value(), notLoggedIn["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), wrongOrg["status"])
        }

        @Test
        fun `Both read and write can read`() {
            val rspRead =
                apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1", null, JwtToken(Access.ORG_READ).toString(), "GET")
            val rspWrite =
                apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1", null, JwtToken(Access.ORG_WRITE).toString(), "GET")

            Assumptions.assumeTrue(HttpStatus.OK.value() == rspRead["status"])
            Assumptions.assumeTrue(HttpStatus.OK.value() == rspWrite["status"])

            val bodyRead: CatalogCount = mapper.readValue(rspRead["body"] as String)
            val bodyWrite: CatalogCount = mapper.readValue(rspWrite["body"] as String)

            assertEquals(DB_CATALOG_1, bodyRead)
            assertEquals(DB_CATALOG_1, bodyWrite)
        }

        @Test
        fun `Get All catalogs returns all permitted catalogs`() {
            resetDB()
            val rspRead = apiAuthorizedRequest("/catalogs", null, JwtToken(Access.ORG_READ).toString(), "GET")
            val bodyRead: CatalogDTO = mapper.readValue(rspRead["body"] as String)

            val expectedRead: Map<String, Long> = mapOf(
                Pair(DB_CATALOG_ID_1, 3)
            )
            assertEquals(expectedRead, bodyRead._embedded!!["catalogs"]!!.associate { Pair(it.id, it.datasetCount) })

            val rspRoot = apiAuthorizedRequest("/catalogs", null, JwtToken(Access.ROOT).toString(), "GET")
            val bodyRoot: CatalogDTO = mapper.readValue(rspRoot["body"] as String)

            val expectedRoot: Map<String, Long> = mapOf(
                Pair(SERIES_CATALOG_ID, 4),
                Pair(DB_CATALOG_ID_1, 3),
                Pair(DB_CATALOG_ID_2, 3),
            )
            assertEquals(expectedRoot, bodyRoot._embedded!!["catalogs"]!!.associate { Pair(it.id, it.datasetCount) })
        }

    }

}