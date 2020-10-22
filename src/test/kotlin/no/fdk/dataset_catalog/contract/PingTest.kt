package no.fdk.dataset_catalog.contract

import no.fdk.dataset_catalog.utils.ApiTestContext
import no.fdk.dataset_catalog.utils.apiGet
import org.junit.jupiter.api.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    properties = ["spring.profiles.active=contract-test"],
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(initializers = [ApiTestContext.Initializer::class])
@Tag("contract")
class PingTest: ApiTestContext() {
    @Test
    fun findSpecific() {
        val response = apiGet("/ping", "text/plain")

        assertTrue { HttpStatus.OK.value() == response["status"] }
    }


}