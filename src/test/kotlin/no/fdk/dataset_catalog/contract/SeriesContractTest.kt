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
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val mapper = jacksonObjectMapper()

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@SpringBootTest(
    properties = ["spring.profiles.active=contract-test"],
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(initializers = [ApiTestContext.Initializer::class])
@Tag("contract")
class SeriesContractTest: ApiTestContext() {

    @BeforeEach
    fun beforeEach() { resetDB() }

    @Nested
    internal inner class Create{
        @Test
        fun `New series with order also updates relevant datasets`() {
            val rspCreate = apiAuthorizedRequest("/catalogs/$SERIES_CATALOG_ID/datasets", mapper.writeValueAsString(SERIES_DATASET_3), JwtToken(Access.SERIES_WRITE).toString(), "POST")
            assertEquals(HttpStatus.CREATED.value(), rspCreate["status"])

            val rspGetNewSeries = apiAuthorizedRequest("/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_3", null, JwtToken(Access.SERIES_WRITE).toString(), "GET")
            assertEquals(HttpStatus.OK.value(), rspGetNewSeries["status"])
            val bodyGetNewSeries: Dataset = mapper.readValue(rspGetNewSeries["body"] as String)
            assertEquals(SERIES_DATASET_3.copy(lastModified = bodyGetNewSeries.lastModified), bodyGetNewSeries)

            val rspGetDataset1 = apiAuthorizedRequest("/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_1", null, JwtToken(Access.SERIES_WRITE).toString(), "GET")
            assertEquals(HttpStatus.OK.value(), rspGetDataset1["status"])
            val bodyGetDataset1: Dataset = mapper.readValue(rspGetDataset1["body"] as String)
            assertTrue(bodyGetDataset1.inSeries?.contains(SERIES_DATASET_ID_3) ?: false)

            val rspGetDataset2 = apiAuthorizedRequest("/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_2", null, JwtToken(Access.SERIES_WRITE).toString(), "GET")
            assertEquals(HttpStatus.OK.value(), rspGetDataset2["status"])
            val bodyGetDataset2: Dataset = mapper.readValue(rspGetDataset2["body"] as String)
            assertTrue(bodyGetDataset2.inSeries?.contains(SERIES_DATASET_ID_3) ?: false)
        }

        @Test
        fun `New dataset with inSeries also updates relevant series`() {
            val rspCreate = apiAuthorizedRequest("/catalogs/$SERIES_CATALOG_ID/datasets", mapper.writeValueAsString(SERIES_DATASET_4), JwtToken(Access.SERIES_WRITE).toString(), "POST")
            assertEquals(HttpStatus.CREATED.value(), rspCreate["status"])

            val rspGetNewDataset = apiAuthorizedRequest("/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_4", null, JwtToken(Access.SERIES_WRITE).toString(), "GET")
            assertEquals(HttpStatus.OK.value(), rspGetNewDataset["status"])
            val bodyGetNewDataset: Dataset = mapper.readValue(rspGetNewDataset["body"] as String)
            assertEquals(SERIES_DATASET_4.copy(lastModified = bodyGetNewDataset.lastModified), bodyGetNewDataset)

            val rspGetSeries = apiAuthorizedRequest("/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_0", null, JwtToken(Access.SERIES_WRITE).toString(), "GET")
            assertEquals(HttpStatus.OK.value(), rspGetSeries["status"])
            val bodyGetSeries: Dataset = mapper.readValue(rspGetSeries["body"] as String)
            assertTrue(bodyGetSeries.seriesDatasetOrder?.keys?.contains(SERIES_DATASET_ID_4) ?: false)
        }
    }

    @Nested
    internal inner class Update{
        @Test
        fun `Update of inSeries also updates series order`() {
            val update1 = listOf(JsonPatchOperation(OpEnum.REMOVE, "/inSeries"))
            val update2 = listOf(JsonPatchOperation(OpEnum.ADD, "/inSeries", SERIES_DATASET_ID_0))
            val rspUpdate1 = apiAuthorizedRequest("/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_1", mapper.writeValueAsString(update1), JwtToken(Access.SERIES_WRITE).toString(), "PATCH")
            val rspUpdate2 = apiAuthorizedRequest("/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_2", mapper.writeValueAsString(update2), JwtToken(Access.SERIES_WRITE).toString(), "PATCH")
            assertEquals(HttpStatus.OK.value(), rspUpdate1["status"])
            assertEquals(HttpStatus.OK.value(), rspUpdate2["status"])

            val rspGetSeries = apiAuthorizedRequest("/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_0", null, JwtToken(Access.SERIES_WRITE).toString(), "GET")
            assertEquals(HttpStatus.OK.value(), rspGetSeries["status"])
            val bodyGetSeries: Dataset = mapper.readValue(rspGetSeries["body"] as String)
            assertTrue(SERIES_DATASET_ID_1 !in (bodyGetSeries.seriesDatasetOrder?.keys ?: emptyList()))
            assertTrue(SERIES_DATASET_ID_2 in (bodyGetSeries.seriesDatasetOrder?.keys ?: emptyList()))
        }

        @Test
        fun `Update of series order also updates relevant datasets`() {
            val update = listOf(JsonPatchOperation(OpEnum.REPLACE, "/seriesDatasetOrder", mapOf(Pair(SERIES_DATASET_ID_2, 0))))
            val rspUpdate = apiAuthorizedRequest("/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_0", mapper.writeValueAsString(update), JwtToken(Access.SERIES_WRITE).toString(), "PATCH")
            assertEquals(HttpStatus.OK.value(), rspUpdate["status"])

            val rspGet1 = apiAuthorizedRequest("/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_1", null, JwtToken(Access.SERIES_WRITE).toString(), "GET")
            assertEquals(HttpStatus.OK.value(), rspGet1["status"])
            val bodyGet1: Dataset = mapper.readValue(rspGet1["body"] as String)
            assertNull(bodyGet1.inSeries)

            val rspGet2 = apiAuthorizedRequest("/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_2", null, JwtToken(Access.SERIES_WRITE).toString(), "GET")
            assertEquals(HttpStatus.OK.value(), rspGet2["status"])
            val bodyGet2: Dataset = mapper.readValue(rspGet2["body"] as String)
            assertEquals(SERIES_DATASET_ID_0, bodyGet2.inSeries)
        }
    }

    @Nested
    internal inner class Delete{
        @Test
        fun `Deleted series is removed from inSeries of datasets`() {
            val rspDelete = apiAuthorizedRequest("/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_0", null, JwtToken(Access.SERIES_WRITE).toString(), "DELETE")
            assertEquals(HttpStatus.OK.value(), rspDelete["status"])
            val rspGet = apiAuthorizedRequest("/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_0", null, JwtToken(Access.SERIES_WRITE).toString(), "GET")
            assertEquals(HttpStatus.NOT_FOUND.value(), rspGet["status"])

            val rspGetDataset1 = apiAuthorizedRequest("/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_1", null, JwtToken(Access.SERIES_WRITE).toString(), "GET")
            assertEquals(HttpStatus.OK.value(), rspGetDataset1["status"])
            val bodyGetDataset1: Dataset = mapper.readValue(rspGetDataset1["body"] as String)
            assertNull(bodyGetDataset1.inSeries)

            val rspGetDataset2 = apiAuthorizedRequest("/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_2", null, JwtToken(Access.SERIES_WRITE).toString(), "GET")
            assertEquals(HttpStatus.OK.value(), rspGetDataset2["status"])
            val bodyGetDataset2: Dataset = mapper.readValue(rspGetDataset2["body"] as String)
            assertNull(bodyGetDataset2.inSeries)
        }

        @Test
        fun `Deleted dataset is removed from series order`() {
            val rspDelete = apiAuthorizedRequest("/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_1", null, JwtToken(Access.SERIES_WRITE).toString(), "DELETE")
            assertEquals(HttpStatus.OK.value(), rspDelete["status"])
            val rspGet = apiAuthorizedRequest("/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_1", null, JwtToken(Access.SERIES_WRITE).toString(), "GET")
            assertEquals(HttpStatus.NOT_FOUND.value(), rspGet["status"])

            val rspGetSeries = apiAuthorizedRequest("/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_0", null, JwtToken(Access.SERIES_WRITE).toString(), "GET")
            assertEquals(HttpStatus.OK.value(), rspGetSeries["status"])
            val bodyGetSeries: Dataset = mapper.readValue(rspGetSeries["body"] as String)
            assertTrue(SERIES_DATASET_ID_1 !in (bodyGetSeries.seriesDatasetOrder?.keys ?: emptyList()))
        }
    }

    @Nested
    internal inner class GetDatasetsWithSeriesParam{
        @Test
        fun `Get all datasets with specialized-type param value series returns correct list`() {
            val rspLower = apiAuthorizedRequest("/catalogs/$SERIES_CATALOG_ID/datasets?specializedType=series", null, JwtToken(Access.SERIES_WRITE).toString(), "GET")
            val rspUpper = apiAuthorizedRequest("/catalogs/$SERIES_CATALOG_ID/datasets?specializedType=SERIES", null, JwtToken(Access.SERIES_WRITE).toString(), "GET")
            val rspMixed = apiAuthorizedRequest("/catalogs/$SERIES_CATALOG_ID/datasets?specializedType=seRiEs", null, JwtToken(Access.SERIES_WRITE).toString(), "GET")

            val bodyLower: DatasetEmbeddedWrapperDTO = mapper.readValue(rspLower["body"] as String)
            val bodyUpper: DatasetEmbeddedWrapperDTO = mapper.readValue(rspUpper["body"] as String)
            val bodyMixed: DatasetEmbeddedWrapperDTO = mapper.readValue(rspMixed["body"] as String)

            assertEquals(listOf(SERIES_DATASET_ID_0), bodyLower._embedded?.get("datasets")?.map { it.id })
            assertEquals(bodyLower, bodyUpper)
            assertEquals(bodyLower, bodyMixed)
        }

    }
}
