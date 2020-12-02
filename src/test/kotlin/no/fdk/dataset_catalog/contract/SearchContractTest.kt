package no.fdk.dataset_catalog.contract

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.fdk.dataset_catalog.model.SEARCH_TYPE
import no.fdk.dataset_catalog.model.SearchRequest
import no.fdk.dataset_catalog.model.SearchResult
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
class SearchContractTest {

    @Nested
    internal inner class SearchDataset{
        @Test
        fun `Unable to search when not logged in as a user with org access`() {
            val request = mapper.writeValueAsString(SearchRequest(SEARCH_TYPE.DATASET_BY_QUERY, listOf(DB_CATALOG_ID_2)))
            val notLoggedIn = apiAuthorizedRequest("/v2/search", request, null, "POST")
            val wrongOrg = apiAuthorizedRequest("/v2/search", request, JwtToken(Access.ORG_WRITE).toString(), "POST")

            assertEquals(HttpStatus.UNAUTHORIZED.value(), notLoggedIn["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), wrongOrg["status"])
        }

        @Test
        fun `Both read and write can search`() {
            val request = mapper.writeValueAsString(SearchRequest(SEARCH_TYPE.DATASET_BY_QUERY, listOf(DB_CATALOG_ID_1)))

            val rspRead = apiAuthorizedRequest("/v2/search", request, JwtToken(Access.ORG_READ).toString(), "POST")
            val rspWrite = apiAuthorizedRequest("/v2/search", request, JwtToken(Access.ORG_WRITE).toString(), "POST")

            assertEquals(HttpStatus.OK.value(), rspRead["status"])
            assertEquals(HttpStatus.OK.value(), rspWrite["status"])
        }

        @Test
        fun `Search returns relevant results`() {
            resetDB()

            val q1 = mapper.writeValueAsString(SearchRequest(SEARCH_TYPE.DATASET_BY_QUERY, listOf(DB_CATALOG_ID_1), "test"))
            val q2 = mapper.writeValueAsString(SearchRequest(SEARCH_TYPE.DATASET_BY_QUERY, listOf(DB_CATALOG_ID_1), "description"))

            val rspQ1 = apiAuthorizedRequest("/v2/search", q1, JwtToken(Access.ORG_READ).toString(), "POST")
            val rspQ2 = apiAuthorizedRequest("/v2/search", q2, JwtToken(Access.ORG_READ).toString(), "POST")

            val bodyQ1: SearchResult = mapper.readValue(rspQ1["body"] as String)
            val bodyQ2: SearchResult = mapper.readValue(rspQ2["body"] as String)

            assertEquals(listOf(DB_DATASET_ID_1, DB_DATASET_ID_2), bodyQ1.datasets.map { it.id })
            assertEquals(listOf(DB_DATASET_ID_3), bodyQ2.datasets.map { it.id })
        }
    }
}