package no.fdk.dataset_catalog.contract

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.fdk.dataset_catalog.model.Dataset
import no.fdk.dataset_catalog.model.DatasetDTO
import no.fdk.dataset_catalog.utils.*
import no.fdk.dataset_catalog.utils.jwk.Access
import no.fdk.dataset_catalog.utils.jwk.JwtToken
import org.junit.jupiter.api.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import kotlin.test.assertEquals

private val mapper = jacksonObjectMapper()

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@SpringBootTest(
    properties = ["spring.profiles.active=contract-test"],
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(initializers = [ApiTestContext.Initializer::class])
@Tag("contract")
class DatasetContractTest: ApiTestContext() {
    @Nested
    internal inner class CreateDataset{
        @Test
        fun `Illegal create`() {
            val notLoggedIn = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/", mapper.writeValueAsString(DATASET_1), null, "POST")
            val rootAccess = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/", mapper.writeValueAsString(DATASET_1), JwtToken(Access.ROOT).toString(), "POST")
            val readAccess = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/", mapper.writeValueAsString(DATASET_1), JwtToken(Access.ORG_READ).toString(), "POST")
            val wrongOrg = apiAuthorizedRequest("/catalogs/1/datasets/", mapper.writeValueAsString(DATASET_1), JwtToken(Access.ORG_WRITE).toString(), "POST")

            assertEquals(HttpStatus.UNAUTHORIZED.value(), notLoggedIn["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), rootAccess["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), readAccess["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), wrongOrg["status"])
        }


        @Test
        fun `Invalid create`() {
            val emptyBody = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/", "", JwtToken(Access.ORG_WRITE).toString(), "POST")
            val nonExistingCatalog = apiAuthorizedRequest("/catalogs/$CATALOG_ID_2/datasets/", mapper.writeValueAsString(DATASET_2), JwtToken(Access.ORG_WRITE).toString(), "POST")

            assertEquals(HttpStatus.BAD_REQUEST.value(), emptyBody["status"])
            assertEquals(HttpStatus.BAD_REQUEST.value(), nonExistingCatalog["status"])
        }

        @Test
        fun `Able to get after create`() {
            val rspCreate = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/", mapper.writeValueAsString(DATASET_1), JwtToken(Access.ORG_WRITE).toString(), "POST")
            Assumptions.assumeTrue(HttpStatus.CREATED.value() == rspCreate["status"])


            val rspGet = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/$DATASET_ID_1", null, JwtToken(Access.ORG_WRITE).toString(), "GET")
            Assumptions.assumeTrue(HttpStatus.OK.value() == rspGet["status"])

            val bodyGet: Dataset = mapper.readValue(rspGet["body"] as String)
            assertEquals(DATASET_1.copy(lastModified = bodyGet.lastModified, uri = bodyGet.uri, publisher = bodyGet.publisher), bodyGet)

            apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/$DATASET_ID_1", null, JwtToken(Access.ORG_WRITE).toString(), "DELETE")
        }

        @Test
        fun `All fields are persisted`() {
            val rspCreate = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/", mapper.writeValueAsString(DATASET_2), JwtToken(Access.ORG_WRITE).toString(), "POST")
            Assumptions.assumeTrue(HttpStatus.CREATED.value() == rspCreate["status"])


            val rspGet = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/$DATASET_ID_2", null, JwtToken(Access.ORG_WRITE).toString(), "GET")
            Assumptions.assumeTrue(HttpStatus.OK.value() == rspGet["status"])

            val bodyGet: Dataset = mapper.readValue(rspGet["body"] as String)
            assertEquals(
                DATASET_2.copy(
                    lastModified = bodyGet.lastModified,
                    concepts = bodyGet.concepts,
                    subjects = bodyGet.subjects,
                    uri = bodyGet.uri)
                , bodyGet)

            apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/$DATASET_ID_2", null, JwtToken(Access.ORG_WRITE).toString(), "DELETE")
        }

    }

    @Nested
    internal inner class GetDataset{
        @Test
        fun `Unable to get when not logged in as a user with org access`() {
            val notLoggedIn = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DATASET_ID_1}", null, null, "GET")
            val wrongOrg = apiAuthorizedRequest("/catalogs/1/datasets/${DATASET_ID_1}", null, JwtToken(Access.ORG_READ).toString(), "GET")

            assertEquals(HttpStatus.UNAUTHORIZED.value(), notLoggedIn["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), wrongOrg["status"])
        }

        @Test
        fun `Both read and write can read`() {
            val rspRead = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}", null, JwtToken(Access.ORG_READ).toString(), "GET")
            val rspWrite = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}", null, JwtToken(Access.ORG_WRITE).toString(), "GET")

            Assumptions.assumeTrue(HttpStatus.OK.value() == rspRead["status"])
            Assumptions.assumeTrue(HttpStatus.OK.value() == rspWrite["status"])

            val bodyRead: Dataset = mapper.readValue(rspRead["body"] as String)
            val bodyWrite: Dataset = mapper.readValue(rspWrite["body"] as String)

            assertEquals(DB_DATASET_1, bodyRead)
            assertEquals(DB_DATASET_1, bodyWrite)
        }

        @Test
        fun `Get All datasets returns all datasets in catalog`() {
            val rspRead = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets", null, JwtToken(Access.ORG_READ).toString(), "GET")
            val bodyRead: DatasetDTO = mapper.readValue(rspRead["body"] as String)

            assertEquals(listOf(DB_DATASET_ID_1, DB_DATASET_ID_2, DB_DATASET_ID_3), bodyRead._embedded?.get("datasets")?.map { it.id })
        }

    }

    @Nested
    internal inner class UpdateDataset{
        @Test
        fun `Illegal update`() {
            val notLoggedIn = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}", mapper.writeValueAsString(DATASET_1), null, "PATCH")
            val rootAccess = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}", mapper.writeValueAsString(DATASET_1), JwtToken(Access.ROOT).toString(), "PATCH")
            val readAccess = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}", mapper.writeValueAsString(DATASET_1), JwtToken(Access.ORG_READ).toString(), "PATCH")
            val wrongOrg = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_2/datasets/${DB_DATASET_ID_1}", mapper.writeValueAsString(DATASET_1), JwtToken(Access.ORG_WRITE).toString(), "PATCH")

            assertEquals(HttpStatus.UNAUTHORIZED.value(), notLoggedIn["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), rootAccess["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), readAccess["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), wrongOrg["status"])
        }

        @Test
        fun `Invalid update`() {
            val doesNotExist = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_4}", mapper.writeValueAsString(DATASET_1), JwtToken(Access.ORG_WRITE).toString(), "PATCH")

            assertEquals(HttpStatus.BAD_REQUEST.value(), doesNotExist["status"])
        }

        @Test
        fun `Able to get before and after update`() {
            val preUpdate = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}", null, JwtToken(Access.ORG_WRITE).toString(), "GET")
            Assumptions.assumeTrue(HttpStatus.OK.value() == preUpdate["status"])
            val bodyPreUpdate: Dataset = mapper.readValue(preUpdate["body"] as String)
            Assumptions.assumeTrue(DB_DATASET_1 == bodyPreUpdate)

            val toUpdate = DB_DATASET_1.copy(source = "brreg")
            Assumptions.assumeFalse(DB_DATASET_1 == toUpdate)

            val rspUpdate = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}", mapper.writeValueAsString(toUpdate), JwtToken(Access.ORG_WRITE).toString(), "PATCH")
            Assumptions.assumeTrue(HttpStatus.OK.value() == rspUpdate["status"])

            val postUpdate = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}", null, JwtToken(Access.ORG_WRITE).toString(), "GET")
            Assumptions.assumeTrue(HttpStatus.OK.value() == postUpdate["status"])
            val bodyPostUpdate: Dataset = mapper.readValue(postUpdate["body"] as String)
            assertEquals(toUpdate.copy(lastModified = bodyPostUpdate.lastModified), bodyPostUpdate)
        }

        @Test
        fun `Only specified fields are updated`() {
            val updated = DB_DATASET_2.copy(
                source = "brreg",
                catalog = DB_CATALOG_1
            )

            val rspUpdate = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_2}", mapper.writeValueAsString(updated), JwtToken(Access.ORG_WRITE).toString(), "PATCH")
            Assumptions.assumeTrue(HttpStatus.OK.value() == rspUpdate["status"])

            val postUpdate = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_2}", null, JwtToken(Access.ORG_WRITE).toString(), "GET")
            Assumptions.assumeTrue(HttpStatus.OK.value() == postUpdate["status"])

            val bodyPostUpdate: Dataset = mapper.readValue(postUpdate["body"] as String)

            assertEquals(
                updated.copy(
                    lastModified = bodyPostUpdate.lastModified,
                    concepts = bodyPostUpdate.concepts,
                    subjects = bodyPostUpdate.subjects,)
                , bodyPostUpdate)
        }
    }


    @Nested
    internal inner class DeleteDataset{
        @Test
        fun `Illegal delete`() {
            val notLoggedIn = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}", mapper.writeValueAsString(DATASET_1), null, "DELETE")
            val readAccess = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}", mapper.writeValueAsString(DATASET_1), JwtToken(Access.ORG_READ).toString(), "DELETE")
            val wrongOrg = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_2/datasets/${DB_DATASET_ID_1}", mapper.writeValueAsString(DATASET_1), JwtToken(Access.ORG_WRITE).toString(), "DELETE")

            assertEquals(HttpStatus.UNAUTHORIZED.value(), notLoggedIn["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), readAccess["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), wrongOrg["status"])
        }

        @Test
        fun `Invalid delete`() {
            val doesNotExist = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_4}", mapper.writeValueAsString(DATASET_1), JwtToken(Access.ORG_WRITE).toString(), "DELETE")

            assertEquals(HttpStatus.BAD_REQUEST.value(), doesNotExist["status"])
        }

        @Test
        fun `Cannot get after delete`() {
            val rspDelete = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}", null, JwtToken(Access.ORG_WRITE).toString(), "DELETE")
            Assumptions.assumeTrue(HttpStatus.OK.value() == rspDelete["status"])

            val rspGet = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}", null, JwtToken(Access.ORG_WRITE).toString(), "GET")

            assertEquals(HttpStatus.NOT_FOUND.value(), rspGet["status"])

            apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/", mapper.writeValueAsString(DB_DATASET_1), JwtToken(Access.ORG_WRITE).toString(), "POST")


        }
    }



}