package no.fdk.dataset_catalog.utils

import no.fdk.dataset_catalog.model.*
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap

const val API_TEST_PORT = 5555
const val LOCAL_SERVER_PORT = 5050

const val API_TEST_URI = "http://localhost:$API_TEST_PORT"
const val WIREMOCK_TEST_URI = "http://localhost:$LOCAL_SERVER_PORT"

const val MONGO_USER = "testuser"
const val MONGO_PASSWORD = "testpassword"
const val MONGO_PORT = 27017
const val MONGO_DB_NAME = "datasetCatalog"

val MONGO_ENV_VALUES: Map<String, String> = ImmutableMap.of(
    "MONGO_INITDB_ROOT_USERNAME", MONGO_USER,
    "MONGO_INITDB_ROOT_PASSWORD", MONGO_PASSWORD
)

val DB_CATALOG_ID_1 = "123456789"
val DB_CATALOG_ID_2 = "987456321"
val DB_CATALOG_ID_3 = "192837465"
val DB_CATALOG_ID_4 = "554433221"
val DB_CATALOG_ID_5 = "111111111"


val CATALOG_ID_1 = "246813579"
val CATALOG_ID_2 = "111222333"

val DATASET_ID_1 = "ds1"
val DATASET_ID_2 = "ds2"
val DATASET_ID_3 = "ds3"


val DB_DATASET_ID_1 = "db1"
val DB_DATASET_ID_2 = "db2"
val DB_DATASET_ID_3 = "db3"
val DB_DATASET_ID_4 = "db4"
val DB_DATASET_ID_5 = "db5"
val DB_DATASET_ID_6 = "db6"

val SERIES_CATALOG_ID = "123123123"
val SERIES_DATASET_ID_0 = "series0"
val SERIES_DATASET_ID_1 = "series1"
val SERIES_DATASET_ID_2 = "series2"
val SERIES_DATASET_ID_3 = "series3"
val SERIES_DATASET_ID_4 = "series4"
val SERIES_DATASET_ID_5 = "series5"


val DATASET_1 = Dataset(
    DATASET_ID_1,
    DB_CATALOG_ID_1,
    uri = "http://$DATASET_ID_1",
    registrationStatus = REGISTRATION_STATUS.PUBLISH,
)

val DATASET_2 = TEST_DATASET_0.copy(
    id = DATASET_ID_2,
    catalogId = DB_CATALOG_ID_1,
    uri = "http://$DATASET_ID_2",
    registrationStatus = REGISTRATION_STATUS.PUBLISH,
)

val DATASET_3 = Dataset(
    DATASET_ID_3,
    DB_CATALOG_ID_1,
    uri = "http://$DATASET_ID_3",
    registrationStatus = REGISTRATION_STATUS.PUBLISH,
    euDataTheme = setOf("http://publications.europa.eu/resource/authority/data-theme/AGRI"),
    losTheme = setOf("https://psi.norge.no/los/tema/arbeid"),
)

val DB_DATASET_1 = Dataset(
    DB_DATASET_ID_1,
    DB_CATALOG_ID_1,
    uri = "http://$DB_DATASET_ID_1",
    title = mapOf(Pair("nb", "test tittel")),
    registrationStatus = REGISTRATION_STATUS.PUBLISH,
)

val DB_DATASET_2 = TEST_DATASET_0.copy(
    DB_DATASET_ID_2,
    DB_CATALOG_ID_1,
    uri = "http://$DB_DATASET_ID_2",
    type = "http://publications.europa.eu/resource/authority/dataset-type/TEST_DATA",
    title = mapOf(Pair("nb", "enda en")),
    description = mapOf(Pair("en", "test words")),
    registrationStatus = REGISTRATION_STATUS.PUBLISH,
)

val DB_DATASET_3 = Dataset(
    DB_DATASET_ID_3,
    DB_CATALOG_ID_1,
    uri = "http://$DB_DATASET_ID_3",
    description = mapOf(Pair("en", "the description")),
    registrationStatus = REGISTRATION_STATUS.PUBLISH,
)

val DB_DATASET_4 = Dataset(
    DB_DATASET_ID_4,
    DB_CATALOG_ID_2,
    uri = "http://$DB_DATASET_ID_4",
    registrationStatus = REGISTRATION_STATUS.PUBLISH,
)

val DB_DATASET_5 = Dataset(
    DB_DATASET_ID_5,
    DB_CATALOG_ID_2,
    uri = "http://$DB_DATASET_ID_5",
    registrationStatus = REGISTRATION_STATUS.PUBLISH,
)

val DB_DATASET_6 = Dataset(
    DB_DATASET_ID_6,
    DB_CATALOG_ID_2,
    uri = "http://$DB_DATASET_ID_6",
    registrationStatus = REGISTRATION_STATUS.DRAFT,
)

val CATALOG_1 = Catalog(
    CATALOG_ID_1,
    uri = "http://$CATALOG_ID_1",
)

val CATALOG_2 = Catalog(
    CATALOG_ID_2,
    uri = "http://$CATALOG_ID_2",
)

val DB_CATALOG_1 = Catalog(
    DB_CATALOG_ID_1,
    uri = "http://$DB_CATALOG_ID_1",
)

val DB_CATALOG_2 = Catalog(
    DB_CATALOG_ID_2,
    uri = "http://$DB_CATALOG_ID_2",
)

val DB_CATALOG_3 = Catalog(
    DB_CATALOG_ID_3,
    uri = "http://$DB_CATALOG_ID_3",
)

val DB_CATALOG_4 = Catalog(
    DB_CATALOG_ID_4,
    uri = "http://$DB_CATALOG_ID_4",
)

val DB_CATALOG_5 = Catalog(
    DB_CATALOG_ID_5,
    uri = "http://$DB_CATALOG_ID_5",
)

val SERIES_CATALOG = Catalog(
    SERIES_CATALOG_ID,
    uri = "http://localhost:5050/catalogs/$SERIES_CATALOG_ID",
)

val SERIES_DATASET_0 = Dataset(
    SERIES_DATASET_ID_0,
    SERIES_CATALOG_ID,
    specializedType = SpecializedType.SERIES,
    seriesDatasetOrder = mapOf(Pair(SERIES_DATASET_ID_1, 0), Pair(SERIES_DATASET_ID_5, 1)),
    registrationStatus = REGISTRATION_STATUS.PUBLISH,
    uri = "http://localhost:5050/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_0",
)

val SERIES_DATASET_1 = Dataset(
    SERIES_DATASET_ID_1,
    SERIES_CATALOG_ID,
    inSeries = SERIES_DATASET_ID_0,
    registrationStatus = REGISTRATION_STATUS.PUBLISH,
    uri = "http://localhost:5050/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_1",
)

val SERIES_DATASET_2 = Dataset(
    SERIES_DATASET_ID_2,
    SERIES_CATALOG_ID,
    registrationStatus = REGISTRATION_STATUS.DRAFT,
    uri = "http://localhost:5050/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_2",
)

val SERIES_DATASET_3 = Dataset(
    SERIES_DATASET_ID_3,
    SERIES_CATALOG_ID,
    specializedType = SpecializedType.SERIES,
    seriesDatasetOrder = mapOf(Pair(SERIES_DATASET_ID_1, 0), Pair(SERIES_DATASET_ID_2, 1)),
    registrationStatus = REGISTRATION_STATUS.DRAFT,
    uri = "http://localhost:5050/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_3",
)

val SERIES_DATASET_4 = Dataset(
    SERIES_DATASET_ID_4,
    SERIES_CATALOG_ID,
    inSeries = SERIES_DATASET_ID_0,
    registrationStatus = REGISTRATION_STATUS.DRAFT,
    uri = "http://localhost:5050/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_4",
)

val SERIES_DATASET_5 = Dataset(
    SERIES_DATASET_ID_5,
    SERIES_CATALOG_ID,
    inSeries = SERIES_DATASET_ID_0,
    registrationStatus = REGISTRATION_STATUS.PUBLISH,
    uri = "http://localhost:5050/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_5",
)

fun datasetDbPopulation() = listOf(
    DB_DATASET_1, DB_DATASET_2, DB_DATASET_3, DB_DATASET_4, DB_DATASET_5, DB_DATASET_6,
    SERIES_DATASET_0, SERIES_DATASET_1, SERIES_DATASET_2, SERIES_DATASET_5
)
    .map { it.mapDBO() }

fun catalogDbPopulation() = listOf(DB_CATALOG_1, DB_CATALOG_2, DB_CATALOG_3, DB_CATALOG_4, DB_CATALOG_5, SERIES_CATALOG)
    .map { it.mapDBO() }


private fun Dataset.mapDBO(): org.bson.Document =
    org.bson.Document()
        .append("_id", id)
        .append("uri", uri)
        .append("type", type)
        .append("catalogId", catalogId)
        .append("title", title)
        .append("description", description)
        .append("references", references?.map { it.mapDBO() })
        .append("registrationStatus", registrationStatus.toString())
        .append("specializedType", specializedType)
        .append("inSeries", inSeries)
        .append("seriesDatasetOrder", seriesDatasetOrder)

private fun Catalog.mapDBO(): org.bson.Document =
    org.bson.Document()
        .append("_id", id)
        .append("uri", uri)

private fun Reference.mapDBO(): org.bson.Document =
    org.bson.Document()
        .append("referenceType", referenceType?.mapDBO())
        .append("source", source?.mapDBO())

private fun SkosCode.mapDBO(): org.bson.Document =
    org.bson.Document()
        .append("uri", uri)
        .append("code", code)
        .append("prefLabel", prefLabel)

private fun SkosConcept.mapDBO(): org.bson.Document =
    org.bson.Document()
        .append("uri", uri)
        .append("prefLabel", prefLabel)
        .append("extraType", extraType)
