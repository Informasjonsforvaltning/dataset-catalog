package no.fdk.dataset_catalog.contract

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.fdk.dataset_catalog.model.Catalog
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
    internal inner class CreateCatalog {
        @Test
        fun `Illegal create`() {
            val notLoggedIn = apiAuthorizedRequest("/catalogs/", mapper.writeValueAsString(CATALOG_1), null, "POST")
            val readAccess = apiAuthorizedRequest("/catalogs/", mapper.writeValueAsString(CATALOG_1), JwtToken(Access.ORG_READ).toString(), "POST")
            val wrongOrg = apiAuthorizedRequest("/catalogs/", mapper.writeValueAsString(CATALOG_1.copy(id="1")), JwtToken(Access.ORG_WRITE).toString(), "POST")
            val noID = apiAuthorizedRequest("/catalogs/", mapper.writeValueAsString(Catalog()), JwtToken(Access.ORG_WRITE).toString(), "POST")

            assertEquals(HttpStatus.UNAUTHORIZED.value(), notLoggedIn["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), readAccess["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), wrongOrg["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), noID["status"])
        }

        @Test
        fun `Invalid create`() {
            val emptyBody = apiAuthorizedRequest("/catalogs/", "", JwtToken(Access.ORG_WRITE).toString(), "POST")

            assertEquals(HttpStatus.BAD_REQUEST.value(), emptyBody["status"])
        }

        @Test
        fun `Able to get after create`() {
            val rspCreate = apiAuthorizedRequest("/catalogs/", mapper.writeValueAsString(CATALOG_1), JwtToken(Access.ORG_WRITE).toString(), "POST")
            Assumptions.assumeTrue(HttpStatus.CREATED.value() == rspCreate["status"])


            val rspGet = apiAuthorizedRequest("/catalogs/$CATALOG_ID_1", null, JwtToken(Access.ORG_WRITE).toString(), "GET")
            Assumptions.assumeTrue(HttpStatus.OK.value() == rspGet["status"])

            val bodyGet: Catalog = mapper.readValue(rspGet["body"] as String)
            assertEquals(CATALOG_1.copy(publisher = bodyGet.publisher, uri = bodyGet.uri), bodyGet)
        }
    }

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
            val rspRead = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_5", null, JwtToken(Access.ORG_READ).toString(), "GET")
            val rspWrite = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_5", null, JwtToken(Access.ORG_WRITE).toString(), "GET")

            Assumptions.assumeTrue(HttpStatus.OK.value() == rspRead["status"])
            Assumptions.assumeTrue(HttpStatus.OK.value() == rspWrite["status"])

            val bodyRead: Catalog = mapper.readValue(rspRead["body"] as String)
            val bodyWrite: Catalog = mapper.readValue(rspWrite["body"] as String)

            assertEquals(DB_CATALOG_5.copy(publisher = bodyRead.publisher), bodyRead)
            assertEquals(DB_CATALOG_5.copy(publisher = bodyRead.publisher), bodyWrite)
        }

        @Test
        fun `Get All catalogs returns all permitted catalogs`() {
            resetDB()
            val rspRead = apiAuthorizedRequest("/catalogs/", null, JwtToken(Access.ORG_READ).toString(), "GET")
            val bodyRead: CatalogDTO = mapper.readValue(rspRead["body"] as String)

            assertEquals(setOf(DB_CATALOG_ID_1, DB_CATALOG_ID_5) , bodyRead._embedded?.get("catalogs")?.map { it.id }?.toSet())
        }

    }

    @Nested
    internal inner class UpdateCatalog {
        @Test
        fun `Illegal update`() {
            val notLoggedIn = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1", mapper.writeValueAsString(DB_CATALOG_1), null, "PUT")
            val readAccess = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1", mapper.writeValueAsString(DB_CATALOG_1), JwtToken(Access.ORG_READ).toString(), "PUT")
            val wrongOrg = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_2", mapper.writeValueAsString(DB_CATALOG_1), JwtToken(Access.ORG_WRITE).toString(), "PUT")

            assertEquals(HttpStatus.UNAUTHORIZED.value(), notLoggedIn["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), readAccess["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), wrongOrg["status"])
        }

        @Test
        fun `Invalid update`() {
            val doesNotExist = apiAuthorizedRequest("/catalogs/$CATALOG_ID_2", mapper.writeValueAsString(CATALOG_2), JwtToken(Access.ORG_WRITE).toString(), "PUT")

            assertEquals(HttpStatus.BAD_REQUEST.value(), doesNotExist["status"])
        }

        @Test
        fun `Able to get before and after update`() {
            val preUpdate = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_5", null, JwtToken(Access.ORG_WRITE).toString(), "GET")
            Assumptions.assumeTrue(HttpStatus.OK.value() == preUpdate["status"])
            val bodyPreUpdate: Catalog = mapper.readValue(preUpdate["body"] as String)
            Assumptions.assumeTrue(DB_CATALOG_5 == bodyPreUpdate)

            val toUpdate = DB_CATALOG_5.copy(language = "English")
            Assumptions.assumeFalse(DB_CATALOG_5 == toUpdate)

            val rspUpdate = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_5", mapper.writeValueAsString(toUpdate), JwtToken(Access.ORG_WRITE).toString(), "PUT")
            Assumptions.assumeTrue(HttpStatus.OK.value() == rspUpdate["status"])

            val postUpdate = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_5", null, JwtToken(Access.ORG_WRITE).toString(), "GET")
            Assumptions.assumeTrue(HttpStatus.OK.value() == postUpdate["status"])
            val bodyPostUpdate: Catalog = mapper.readValue(postUpdate["body"] as String)
            assertEquals(toUpdate.copy(publisher = bodyPostUpdate.publisher, uri = bodyPostUpdate.uri), bodyPostUpdate)
        }

    }

    @Nested
    internal inner class DeleteCatalog {
        @Test
        fun `Illegal delete`() {
            val notLoggedIn = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1", mapper.writeValueAsString(CATALOG_1), null, "DELETE")
            val readAccess = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1", mapper.writeValueAsString(CATALOG_1), JwtToken(Access.ORG_READ).toString(), "DELETE")
            val wrongOrg = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_3", mapper.writeValueAsString(CATALOG_1), JwtToken(Access.ORG_WRITE).toString(), "DELETE")

            assertEquals(HttpStatus.UNAUTHORIZED.value(), notLoggedIn["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), readAccess["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), wrongOrg["status"])
        }

        @Test
        fun `Invalid delete`() {
            val doesNotExist = apiAuthorizedRequest("/catalogs/$CATALOG_ID_2", mapper.writeValueAsString(CATALOG_1), JwtToken(Access.ORG_WRITE).toString(), "DELETE")

            assertEquals(HttpStatus.BAD_REQUEST.value(), doesNotExist["status"])
        }

        @Test
        fun `Cannot get after delete`() {
            val rspDelete = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_4", null, JwtToken(Access.ORG_WRITE).toString(), "DELETE")
            Assumptions.assumeTrue(HttpStatus.OK.value() == rspDelete["status"])

            val rspGet = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_4", null, JwtToken(Access.ORG_WRITE).toString(), "GET")

            assertEquals(HttpStatus.NOT_FOUND.value(), rspGet["status"])
        }

    }
}