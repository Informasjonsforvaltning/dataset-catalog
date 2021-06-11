package no.fdk.dataset_catalog.utils

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import no.fdk.dataset_catalog.rdf.createRDFResponse
import no.fdk.dataset_catalog.utils.ApiTestContext.Companion.mongoContainer
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.Lang
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.slf4j.Logger
import org.springframework.http.*
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.io.StringReader


private fun isOK(response: Int?): Boolean =
    if(response == null) false
    else HttpStatus.resolve(response)?.is2xxSuccessful == true

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
            "body" to response.body,
            "header" to response.headers.toString(),
            "status" to response.statusCode.value()
        )

    } catch (e: HttpClientErrorException) {
        mapOf(
            "status" to e.rawStatusCode,
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
    val connectionString = ConnectionString("mongodb://${MONGO_USER}:${MONGO_PASSWORD}@localhost:${mongoContainer.getMappedPort(MONGO_PORT)}/$MONGO_DB_NAME?authSource=admin&authMechanism=SCRAM-SHA-1")
    val pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()))

    val client: MongoClient = MongoClients.create(connectionString)
    val mongoDatabase = client.getDatabase(MONGO_DB_NAME).withCodecRegistry(pojoCodecRegistry)

    val catalogCollection = mongoDatabase.getCollection("catalogs")
    catalogCollection.deleteMany(org.bson.Document())
    catalogCollection.insertMany(catalogDbPopulation())

    val datasetCollection = mongoDatabase.getCollection("datasets")
    datasetCollection.deleteMany(org.bson.Document())
    datasetCollection.insertMany(datasetDbPopulation())


    client.close()
}

fun checkIfIsomorphicAndPrintDiff(actual: Model, expected: Model, name: String, logger: Logger): Boolean {
    // Its necessary to parse the created models from strings to have the same base, and ensure blank node validity
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