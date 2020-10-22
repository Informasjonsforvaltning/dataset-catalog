package no.fdk.dataset_catalog.utils

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import no.fdk.dataset_catalog.utils.ApiTestContext.Companion.mongoContainer
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.net.URL
import org.springframework.http.HttpStatus
import java.net.HttpURLConnection

private val logger = LoggerFactory.getLogger(ApiTestContext::class.java)

fun apiGet(endpoint: String, acceptHeader: String?): Map<String,Any> {

    return try {
        val connection = URL("$API_TEST_URI$endpoint").openConnection() as HttpURLConnection
        if(acceptHeader != null) connection.setRequestProperty("Accept", acceptHeader)
        connection.connect()

        if(isOK(connection.responseCode)) {
            val responseBody = connection.inputStream.bufferedReader().use(BufferedReader::readText)
            mapOf(
                "body"   to responseBody,
                "header" to connection.headerFields.toString(),
                "status" to connection.responseCode)
        } else {
            mapOf(
                "status" to connection.responseCode,
                "header" to " ",
                "body"   to " "
            )
        }
    } catch (e: Exception) {
        mapOf(
            "status" to e.toString(),
            "header" to " ",
            "body"   to " "
        )
    }
}

private fun isOK(response: Int?): Boolean =
    if(response == null) false
    else HttpStatus.resolve(response)?.is2xxSuccessful == true

fun populateDB() {
    val connectionString = ConnectionString("mongodb://${MONGO_USER}:${MONGO_PASSWORD}@localhost:${mongoContainer.getMappedPort(MONGO_PORT)}/$MONGO_COLLECTION?authSource=admin&authMechanism=SCRAM-SHA-1")
    val pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()))

    val client: MongoClient = MongoClients.create(connectionString)
    val mongoDatabase = client.getDatabase(MONGO_COLLECTION).withCodecRegistry(pojoCodecRegistry)

    /*
    val miscCollection = mongoDatabase.getCollection("misc")
    miscCollection.insertMany(miscDBPopulation())

    val catalogCollection = mongoDatabase.getCollection("catalog")
    catalogCollection.insertMany(catalogDBPopulation())

    val serviceCollection = mongoDatabase.getCollection("informationmodel")
    serviceCollection.insertMany(serviceDBPopulation())*/

    client.close()
}