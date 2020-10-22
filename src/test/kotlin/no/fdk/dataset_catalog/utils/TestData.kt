package no.fdk.dataset_catalog.utils

import org.testcontainers.shaded.com.google.common.collect.ImmutableMap
import java.util.*

const val API_PORT = 8080
const val API_TEST_PORT = 5555
const val LOCAL_SERVER_PORT = 5000

const val API_TEST_URI = "http://localhost:$API_TEST_PORT"
const val WIREMOCK_TEST_URI = "http://localhost:$LOCAL_SERVER_PORT"

const val MONGO_USER = "testuser"
const val MONGO_PASSWORD = "testpassword"
const val MONGO_PORT = 27017
const val MONGO_COLLECTION = "dataset"

val MONGO_ENV_VALUES: Map<String, String> = ImmutableMap.of(
    "MONGO_INITDB_ROOT_USERNAME", MONGO_USER,
    "MONGO_INITDB_ROOT_PASSWORD", MONGO_PASSWORD
)
