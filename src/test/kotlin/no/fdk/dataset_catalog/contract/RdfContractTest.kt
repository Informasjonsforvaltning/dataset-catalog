package no.fdk.dataset_catalog.contract

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.fdk.dataset_catalog.utils.*
import no.fdk.dataset_catalog.utils.jwk.Access
import no.fdk.dataset_catalog.utils.jwk.JwtToken
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import kotlin.test.assertEquals

private val mapper = jacksonObjectMapper()

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@SpringBootTest(
    properties = ["spring.profiles.active=contract-test"],
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(initializers = [ApiTestContext.Initializer::class])
@Tag("contract")
class RdfContractTest: ApiTestContext() {
    @Nested
    internal inner class GetRDF {
        @Test
        fun `Gets Catalog`() {
            val getAll = apiAuthorizedRequest("/catalogs/", method="GET", accept=MediaType("text","turtle"))
            val getOne = apiAuthorizedRequest("/catalogs/$DB_CATALOG_ID_1", method="GET", accept=MediaType("text","turtle"))

            assertEquals(HttpStatus.OK.value(), getAll["status"])
            assertEquals(HttpStatus.OK.value(), getOne["status"])
        }
    }
}