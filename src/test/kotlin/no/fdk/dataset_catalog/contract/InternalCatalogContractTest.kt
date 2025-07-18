package no.fdk.dataset_catalog.contract

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.fdk.dataset_catalog.extensions.datasetToDBO
import no.fdk.dataset_catalog.model.CatalogCount
import no.fdk.dataset_catalog.model.DatasetDBO
import no.fdk.dataset_catalog.model.JsonPatchOperation
import no.fdk.dataset_catalog.model.OpEnum
import no.fdk.dataset_catalog.utils.ApiTestContext
import no.fdk.dataset_catalog.utils.DB_CATALOG_1
import no.fdk.dataset_catalog.utils.DB_CATALOG_ID_1
import no.fdk.dataset_catalog.utils.DB_CATALOG_ID_2
import no.fdk.dataset_catalog.utils.DB_DATASET_1
import no.fdk.dataset_catalog.utils.DB_DATASET_ID_1
import no.fdk.dataset_catalog.utils.DB_DATASET_ID_4
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

    @Nested
    internal inner class UpdateDataset {
        @Test
        fun `Illegal update`() {
            resetDB()
            val body =
                mapper.writeValueAsString(listOf(JsonPatchOperation(op = OpEnum.ADD, path = "/title/en", "en title")))
            val notLoggedIn =
                apiAuthorizedRequest("/internal/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}", body, null, "PATCH")
            val readAccess = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}",
                body,
                JwtToken(Access.ORG_READ).toString(),
                "PATCH"
            )
            val wrongOrg = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_2/datasets/${DB_DATASET_ID_1}",
                body,
                JwtToken(Access.ORG_WRITE).toString(),
                "PATCH"
            )

            assertEquals(HttpStatus.UNAUTHORIZED.value(), notLoggedIn["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), readAccess["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), wrongOrg["status"])
        }

        @Test
        fun `Invalid update`() {
            resetDB()
            val doesNotExist = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_4}",
                mapper.writeValueAsString(listOf(JsonPatchOperation(op = OpEnum.ADD, path = "/source", "brreg"))),
                JwtToken(Access.ORG_WRITE).toString(),
                "PATCH"
            )
            val invalidValue = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}",
                mapper.writeValueAsString(
                    listOf(
                        JsonPatchOperation(
                            op = OpEnum.ADD,
                            path = "/keyword",
                            "invalid value"
                        )
                    )
                ),
                JwtToken(Access.ORG_WRITE).toString(),
                "PATCH"
            )
            val wrongOperation = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}",
                mapper.writeValueAsString(
                    listOf(
                        JsonPatchOperation(
                            op = OpEnum.REPLACE,
                            path = "/source",
                            "wrong operation"
                        )
                    )
                ),
                JwtToken(Access.ORG_WRITE).toString(),
                "PATCH"
            )
            val invalidOperation = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}",
                mapper.writeValueAsString(
                    listOf(
                        JsonPatchOperation(
                            op = OpEnum.MOVE,
                            path = "/source",
                            "wrong operation"
                        )
                    )
                ),
                JwtToken(Access.ORG_WRITE).toString(),
                "PATCH"
            )

            assertEquals(HttpStatus.NOT_FOUND.value(), doesNotExist["status"])
            assertEquals(HttpStatus.BAD_REQUEST.value(), invalidValue["status"])
            assertEquals(HttpStatus.BAD_REQUEST.value(), wrongOperation["status"])
            assertEquals(HttpStatus.BAD_REQUEST.value(), invalidOperation["status"])
        }

        @Test
        fun `Invalid fields are ignored on update`() {
            resetDB()
            val preUpdate = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}",
                null,
                JwtToken(Access.ORG_WRITE).toString(),
                "GET"
            )
            assertEquals(HttpStatus.OK.value(), preUpdate["status"])
            val bodyPreUpdate: DatasetDBO = mapper.readValue(preUpdate["body"] as String)

            val invalidField = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}",
                mapper.writeValueAsString(
                    listOf(
                        JsonPatchOperation(
                            op = OpEnum.ADD,
                            path = "/invalidField",
                            "invalid field"
                        )
                    )
                ),
                JwtToken(Access.ORG_WRITE).toString(),
                "PATCH"
            )
            assertEquals(HttpStatus.OK.value(), invalidField["status"])
            val bodyInvalidField: DatasetDBO = mapper.readValue(invalidField["body"] as String)

            assertEquals(bodyPreUpdate.copy(lastModified = bodyInvalidField.lastModified), bodyInvalidField)
        }

        @Test
        fun `Able to get before and after update`() {
            resetDB()
            val preUpdate = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}",
                null,
                JwtToken(Access.ORG_WRITE).toString(),
                "GET"
            )
            assertEquals(HttpStatus.OK.value(), preUpdate["status"])
            val bodyPreUpdate: DatasetDBO = mapper.readValue(preUpdate["body"] as String)
            assertEquals(DB_DATASET_1.datasetToDBO(), bodyPreUpdate)

            val patchBody =
                mapper.writeValueAsString(listOf(JsonPatchOperation(op = OpEnum.ADD, path = "/type", "test")))

            val rspUpdate = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}",
                patchBody,
                JwtToken(Access.ORG_WRITE).toString(),
                "PATCH"
            )
            assertEquals(HttpStatus.OK.value(), rspUpdate["status"])

            val postUpdate = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}",
                null,
                JwtToken(Access.ORG_WRITE).toString(),
                "GET"
            )
            assertEquals(HttpStatus.OK.value(), postUpdate["status"])
            val bodyPostUpdate: DatasetDBO = mapper.readValue(postUpdate["body"] as String)
            assertEquals(DB_DATASET_1.datasetToDBO().copy(lastModified = bodyPostUpdate.lastModified, type = "test"), bodyPostUpdate)
        }

        @Test
        fun `Only specified fields are updated`() {
            resetDB()
            val update = listOf(JsonPatchOperation(OpEnum.ADD, "/type", "test"))

            val rspUpdate = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}",
                mapper.writeValueAsString(update),
                JwtToken(Access.ORG_WRITE).toString(),
                "PATCH"
            )
            assertEquals(HttpStatus.OK.value(), rspUpdate["status"])

            val postUpdate = apiAuthorizedRequest(
                "/internal/catalogs/$DB_CATALOG_ID_1/datasets/${DB_DATASET_ID_1}",
                null,
                JwtToken(Access.ORG_WRITE).toString(),
                "GET"
            )
            assertEquals(HttpStatus.OK.value(), postUpdate["status"])

            val bodyPostUpdate: DatasetDBO = mapper.readValue(postUpdate["body"] as String)

            assertEquals(
                DB_DATASET_1.datasetToDBO().copy(
                    lastModified = bodyPostUpdate.lastModified,
                    type = "test",
                ), bodyPostUpdate
            )
        }
    }
}
