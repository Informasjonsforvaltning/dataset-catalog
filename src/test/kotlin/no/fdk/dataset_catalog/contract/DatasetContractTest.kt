package no.fdk.dataset_catalog.contract

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.fdk.dataset_catalog.model.Dataset
import no.fdk.dataset_catalog.model.DatasetEmbeddedWrapperDTO
import no.fdk.dataset_catalog.model.JsonPatchOperation
import no.fdk.dataset_catalog.model.OpEnum
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
            val notLoggedIn = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets", mapper.writeValueAsString(DATASET_1), null, "POST")
            val readAccess = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets", mapper.writeValueAsString(DATASET_1), JwtToken(Access.ORG_READ).toString(), "POST")
            val wrongOrg = apiAuthorizedRequest("/catalogs/1/datasets", mapper.writeValueAsString(DATASET_1), JwtToken(Access.ORG_WRITE).toString(), "POST")

            assertEquals(HttpStatus.UNAUTHORIZED.value(), notLoggedIn["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), readAccess["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), wrongOrg["status"])
        }


        @Test
        fun `Invalid create`() {
            val emptyBody = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets", "", JwtToken(Access.ORG_WRITE).toString(), "POST")

            assertEquals(HttpStatus.BAD_REQUEST.value(), emptyBody["status"])
        }

        @Test
        fun `Able to get after create`() {
            val rspCreate = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets", mapper.writeValueAsString(DATASET_1), JwtToken(Access.ORG_WRITE).toString(), "POST")
            Assumptions.assumeTrue(HttpStatus.CREATED.value() == rspCreate["status"])

            val rspGet = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/$DATASET_ID_1", null, JwtToken(Access.ORG_WRITE).toString(), "GET")
            Assumptions.assumeTrue(HttpStatus.OK.value() == rspGet["status"])

            val bodyGet: Dataset = mapper.readValue(rspGet["body"] as String)

            assertEquals(DATASET_1.copy(lastModified = bodyGet.lastModified, uri = bodyGet.uri, publisher = bodyGet.publisher), bodyGet)

        }

        @Test
        fun `All fields are persisted`() {
            val rspCreate = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets", mapper.writeValueAsString(DATASET_2), JwtToken(Access.ORG_WRITE).toString(), "POST")
            Assumptions.assumeTrue(HttpStatus.CREATED.value() == rspCreate["status"])

            val rspGet = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/$DATASET_ID_2", null, JwtToken(Access.ORG_WRITE).toString(), "GET")
            Assumptions.assumeTrue(HttpStatus.OK.value() == rspGet["status"])

            val bodyGet: Dataset = mapper.readValue(rspGet["body"] as String)

            assertEquals(
                DATASET_2.copy(
                    lastModified = bodyGet.lastModified,
                    concepts = bodyGet.concepts,
                    uri = bodyGet.uri,
                    losTheme = setOf("https://psi.norge.no/los/tema/arbeid"), // Temporary - remove when refactoring themes
                    euDataTheme = setOf("http://publications.europa.eu/resource/authority/data-theme/AGRI") // Temporary - remove when refactoring themes
                ), bodyGet)
        }
    }

    @Nested
    internal inner class GetDataset{
        @Test
        fun `Unable to get when not logged in as a user with org access`() {
            resetDB()
            val notLoggedIn = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DATASET_ID_1}", null, null, "GET")
            val wrongOrg = apiAuthorizedRequest("/catalogs/1/datasets/${DATASET_ID_1}", null, JwtToken(Access.ORG_READ).toString(), "GET")

            assertEquals(HttpStatus.UNAUTHORIZED.value(), notLoggedIn["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), wrongOrg["status"])
        }

        @Test
        fun `Both read and write can read`() {
            resetDB()
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
            resetDB()
            val rspRead = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_2/datasets", null, JwtToken(Access.ROOT).toString(), "GET")
            val bodyRead: DatasetEmbeddedWrapperDTO = mapper.readValue(rspRead["body"] as String)

            assertEquals(listOf(DB_DATASET_ID_4, DB_DATASET_ID_5, DB_DATASET_ID_6), bodyRead._embedded?.get("datasets")?.map { it.id })
        }
    }

    @Nested
    internal inner class UpdateDataset{
        @Test
        fun `Illegal update`() {
            resetDB()
            val body = mapper.writeValueAsString(listOf(JsonPatchOperation(op = OpEnum.ADD, path = "/title/en", "en title")))
            val notLoggedIn = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}", body, null, "PATCH")
            val readAccess = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}", body, JwtToken(Access.ORG_READ).toString(), "PATCH")
            val wrongOrg = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_2/datasets/${DB_DATASET_ID_1}", body, JwtToken(Access.ORG_WRITE).toString(), "PATCH")

            assertEquals(HttpStatus.UNAUTHORIZED.value(), notLoggedIn["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), readAccess["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), wrongOrg["status"])
        }

        @Test
        fun `Invalid update`() {
            resetDB()
            val doesNotExist = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_4}", mapper.writeValueAsString(listOf(JsonPatchOperation(op = OpEnum.ADD, path = "/source", "brreg"))), JwtToken(Access.ORG_WRITE).toString(), "PATCH")
            val invalidValue = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}", mapper.writeValueAsString(listOf(JsonPatchOperation(op = OpEnum.ADD, path = "/keyword", "invalid value"))), JwtToken(Access.ORG_WRITE).toString(), "PATCH")
            val wrongOperation = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}", mapper.writeValueAsString(listOf(JsonPatchOperation(op = OpEnum.REPLACE, path = "/source", "wrong operation"))), JwtToken(Access.ORG_WRITE).toString(), "PATCH")
            val invalidOperation = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}", mapper.writeValueAsString(listOf(JsonPatchOperation(op = OpEnum.MOVE, path = "/source", "wrong operation"))), JwtToken(Access.ORG_WRITE).toString(), "PATCH")

            assertEquals(HttpStatus.BAD_REQUEST.value(), doesNotExist["status"])
            assertEquals(HttpStatus.BAD_REQUEST.value(), invalidValue["status"])
            assertEquals(HttpStatus.BAD_REQUEST.value(), wrongOperation["status"])
            assertEquals(HttpStatus.BAD_REQUEST.value(), invalidOperation["status"])
        }

        @Test
        fun `Invalid fields are ignored on update`() {
            resetDB()
            val preUpdate = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}", null, JwtToken(Access.ORG_WRITE).toString(), "GET")
            assertEquals(HttpStatus.OK.value(), preUpdate["status"])
            val bodyPreUpdate: Dataset = mapper.readValue(preUpdate["body"] as String)

            val invalidField = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}", mapper.writeValueAsString(listOf(JsonPatchOperation(op = OpEnum.ADD, path = "/invalidField", "invalid field"))), JwtToken(Access.ORG_WRITE).toString(), "PATCH")
            assertEquals(HttpStatus.OK.value(), invalidField["status"])
            val bodyInvalidField: Dataset = mapper.readValue(invalidField["body"] as String)

            assertEquals(bodyPreUpdate.copy(lastModified = bodyInvalidField.lastModified), bodyInvalidField)
        }

        @Test
        fun `Able to get before and after update`() {
            resetDB()
            val preUpdate = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}", null, JwtToken(Access.ORG_WRITE).toString(), "GET")
            assertEquals(HttpStatus.OK.value(), preUpdate["status"])
            val bodyPreUpdate: Dataset = mapper.readValue(preUpdate["body"] as String)
            assertEquals(DB_DATASET_1, bodyPreUpdate)

            val patchBody = mapper.writeValueAsString(listOf(JsonPatchOperation(op = OpEnum.ADD, path = "/source", "brreg")))

            val rspUpdate = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}", patchBody, JwtToken(Access.ORG_WRITE).toString(), "PATCH")
            assertEquals(HttpStatus.OK.value(), rspUpdate["status"])

            val postUpdate = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}", null, JwtToken(Access.ORG_WRITE).toString(), "GET")
            assertEquals(HttpStatus.OK.value(), postUpdate["status"])
            val bodyPostUpdate: Dataset = mapper.readValue(postUpdate["body"] as String)
            assertEquals(DB_DATASET_1.copy(lastModified = bodyPostUpdate.lastModified, source = "brreg"), bodyPostUpdate)
        }

        @Test
        fun `Only specified fields are updated`() {
            resetDB()
            val update = listOf(JsonPatchOperation(OpEnum.ADD, "/source", "brreg"))

            val rspUpdate = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}", mapper.writeValueAsString(update), JwtToken(Access.ORG_WRITE).toString(), "PATCH")
            assertEquals(HttpStatus.OK.value(), rspUpdate["status"])

            val postUpdate = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}", null, JwtToken(Access.ORG_WRITE).toString(), "GET")
            assertEquals(HttpStatus.OK.value(), postUpdate["status"])

            val bodyPostUpdate: Dataset = mapper.readValue(postUpdate["body"] as String)

            assertEquals(
                DB_DATASET_1.copy(
                    lastModified = bodyPostUpdate.lastModified,
                    source = "brreg",
                )
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
            assertEquals(HttpStatus.OK.value(), rspDelete["status"])

            val rspGet = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}", null, JwtToken(Access.ORG_WRITE).toString(), "GET")

            assertEquals(HttpStatus.NOT_FOUND.value(), rspGet["status"])
        }
    }

    // Temporary - remove when refactoring themes
    @Nested
    internal inner class Themes{
        @Test
        fun `losTheme and euDataTheme is saved correctly`() {
            val rspCreate = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets", mapper.writeValueAsString(DATASET_3), JwtToken(Access.ORG_WRITE).toString(), "POST")
            Assumptions.assumeTrue(HttpStatus.CREATED.value() == rspCreate["status"])

            val rspGet = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/$DATASET_ID_3", null, JwtToken(Access.ORG_WRITE).toString(), "GET")
            assertEquals(HttpStatus.OK.value(), rspGet["status"])

            val bodyGet: Dataset = mapper.readValue(rspGet["body"] as String)

            val expectedEuDataTheme = setOf("http://publications.europa.eu/resource/authority/data-theme/AGRI")
            val expectedLosTheme = setOf("https://psi.norge.no/los/tema/arbeid")

            assertEquals(DATASET_3.copy(lastModified = bodyGet.lastModified, uri = bodyGet.uri, publisher = bodyGet.publisher, euDataTheme = expectedEuDataTheme, losTheme = expectedLosTheme), bodyGet)
        }

        @Test
        fun `losTheme and euDataTheme is separated correctly`() {
            val rspCreate = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets", mapper.writeValueAsString(DATASET_2), JwtToken(Access.ORG_WRITE).toString(), "POST")
            Assumptions.assumeTrue(HttpStatus.CREATED.value() == rspCreate["status"])

            val rspGet = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1/datasets/$DATASET_ID_2", null, JwtToken(Access.ORG_WRITE).toString(), "GET")
            assertEquals(HttpStatus.OK.value(), rspGet["status"])

            val bodyGet: Dataset = mapper.readValue(rspGet["body"] as String)

            val expectedEuDataTheme = setOf("http://publications.europa.eu/resource/authority/data-theme/AGRI")
            val expectedLosTheme = setOf("https://psi.norge.no/los/tema/arbeid")

            assertEquals(DATASET_2.copy(lastModified = bodyGet.lastModified, uri = bodyGet.uri, publisher = bodyGet.publisher, euDataTheme = expectedEuDataTheme, losTheme = expectedLosTheme), bodyGet)
        }
    }
}
