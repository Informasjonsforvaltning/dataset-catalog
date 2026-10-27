package no.fdk.datasetcatalog.contract

import no.fdk.datasetcatalog.utils.API_TEST_PORT
import no.fdk.datasetcatalog.utils.ApiTestContext
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    properties = ["spring.profiles.active=contract-test"],
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
)
@ContextConfiguration(initializers = [ApiTestContext.Initializer::class])
@Tag("contract")
class CompressionTest : ApiTestContext() {
    // java.net.http never decompresses on its own, so the raw bytes and headers are observable
    private val client = HttpClient.newHttpClient()

    private fun get(acceptEncoding: String?): HttpResponse<ByteArray> {
        val builder = HttpRequest.newBuilder(URI("http://localhost:$API_TEST_PORT/v3/api-docs")).GET()
        if (acceptEncoding != null) builder.header("Accept-Encoding", acceptEncoding)
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray())
    }

    @Test
    fun `gzips the response when the client accepts it`() {
        val response = get("gzip")
        assertEquals(200, response.statusCode())
        assertEquals("gzip", response.headers().firstValue("Content-Encoding").orElse(null))
        assertTrue(response.headers().allValues("Vary").joinToString(",").lowercase().contains("accept-encoding"))
        val body = response.body()
        assertTrue(body.size > 2 && body[0] == 0x1f.toByte() && body[1] == 0x8b.toByte(), "body is not gzip")
    }

    @Test
    fun `leaves the response uncompressed when the client does not ask`() {
        val response = get(null)
        assertEquals(200, response.statusCode())
        assertTrue(response.headers().firstValue("Content-Encoding").isEmpty)
        assertTrue(response.body().size > 1024, "test endpoint must exceed the 1 KB threshold")
    }
}
