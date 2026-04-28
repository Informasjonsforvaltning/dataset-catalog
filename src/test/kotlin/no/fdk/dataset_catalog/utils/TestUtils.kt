package no.fdk.dataset_catalog.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.fdk.dataset_catalog.model.DatasetDBO
import no.fdk.dataset_catalog.model.toEntity
import no.fdk.dataset_catalog.rdf.createRDFResponse
import no.fdk.dataset_catalog.utils.ApiTestContext.Companion.postgresContainer
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.Lang
import org.flywaydb.core.Flyway
import org.postgresql.util.PGobject
import org.slf4j.Logger
import org.springframework.http.*
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.io.StringReader
import java.sql.DriverManager
import java.sql.Timestamp

private val testMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

fun apiAuthorizedRequest(path: String, body: String? = null, token: String? = null, method: String, accept: MediaType = MediaType.APPLICATION_JSON): Map<String, Any> {
    val request = RestTemplate()
    request.requestFactory = HttpComponentsClientHttpRequestFactory()
    val url = "http://localhost:$API_TEST_PORT$path"
    val headers = HttpHeaders()
    headers.accept = listOf(accept)
    token?.let { headers.setBearerAuth(it) }
    headers.contentType = MediaType.APPLICATION_JSON
    val entity: HttpEntity<String> = HttpEntity(body, headers)
    val httpMethod = when (method) {
        "GET" -> HttpMethod.GET
        "POST" -> HttpMethod.POST
        "PUT" -> HttpMethod.PUT
        "PATCH" -> HttpMethod.PATCH
        "DELETE" -> HttpMethod.DELETE
        else -> throw Exception()
    }

    return try {
        val response = request.exchange(url, httpMethod, entity, String::class.java)
        mapOf(
            "body" to (response.body ?: ""),
            "header" to response.headers,
            "status" to response.statusCode.value()
        )

    } catch (e: HttpClientErrorException) {
        mapOf(
            "status" to e.statusCode.value(),
            "header" to " ",
            "body" to e.toString()
        )
    } catch (e: Exception) {
        mapOf(
            "status" to e.toString(),
            "header" to " ",
            "body" to " "
        )
    }

}

fun resetDB() {
    Flyway.configure()
        .dataSource(postgresContainer.getJdbcUrl(), DB_USER, DB_PASSWORD)
        .load()
        .migrate()

    val conn = DriverManager.getConnection(postgresContainer.getJdbcUrl(), DB_USER, DB_PASSWORD)

    conn.createStatement().execute("DELETE FROM datasets")

    val sql = """
        INSERT INTO datasets (id, catalog_id, published, approved, last_modified, uri, specialized_type, application_profile, data)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """
    val ps = conn.prepareStatement(sql)

    datasetDbPopulation().forEach { dataset ->
        val entity = dataset.toEntity(testMapper)
        ps.setString(1, entity.id)
        ps.setString(2, entity.catalogId)
        ps.setBoolean(3, entity.published)
        ps.setBoolean(4, entity.approved)
        if (entity.lastModified != null) {
            ps.setTimestamp(5, Timestamp.valueOf(entity.lastModified))
        } else {
            ps.setNull(5, java.sql.Types.TIMESTAMP)
        }
        ps.setString(6, entity.uri)
        ps.setString(7, entity.specializedType?.name)
        ps.setString(8, entity.applicationProfile.name)

        val jsonData = PGobject()
        jsonData.type = "jsonb"
        jsonData.value = if (entity.data != null) testMapper.writeValueAsString(entity.data) else null
        ps.setObject(9, jsonData)

        ps.addBatch()
    }

    ps.executeBatch()
    ps.close()
    conn.close()
}

fun checkIfIsomorphicAndPrintDiff(actual: Model, expected: Model, name: String, logger: Logger): Boolean {
    val parsedActual = ModelFactory.createDefaultModel().read(StringReader(actual.createRDFResponse(Lang.TURTLE)), null, "TURTLE")
    val parsedExpected = ModelFactory.createDefaultModel().read(StringReader(expected.createRDFResponse(Lang.TURTLE)), null, "TURTLE")

    val isIsomorphic = parsedActual.isIsomorphicWith(parsedExpected)

    if (!isIsomorphic) {
        val actualDiff = parsedActual.difference(parsedExpected).createRDFResponse(Lang.TURTLE)
        val expectedDiff = parsedExpected.difference(parsedActual).createRDFResponse(Lang.TURTLE)

        if (actualDiff.isNotEmpty()) {
            logger.error("non expected nodes in $name:")
            logger.error(actualDiff)
        }
        if (expectedDiff.isNotEmpty()) {
            logger.error("missing nodes in $name:")
            logger.error(expectedDiff)
        }
    }
    return isIsomorphic
}
