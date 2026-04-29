package no.fdk.dataset_catalog.utils

import no.fdk.dataset_catalog.model.*

const val API_TEST_PORT = 5555
const val LOCAL_SERVER_PORT = 5050

val DB_USER =
    System.getenv("POSTGRES_USER")
        ?: System.getenv("POSTGRESQL_USER")
        ?: System.getenv("DB_USER")
        ?: "root"

val DB_PASSWORD =
    System.getenv("POSTGRES_PASSWORD")
        ?: System.getenv("POSTGRESQL_PASSWORD")
        ?: System.getenv("DB_PASSWORD")
        ?: "secret"

val DB_NAME =
    System.getenv("POSTGRES_DB")
        ?: System.getenv("POSTGRESQL_DB")
        ?: System.getenv("DB_NAME")
        ?: "dataset_catalog"

val DB_CATALOG_ID_1 = "123456789"
val DB_CATALOG_ID_2 = "987456321"

val DATASET_ID_1 = "ds1"
val DATASET_ID_2 = "ds2"


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
val SERIES_DATASET_ID_5 = "series5"


val DATASET_1 = DatasetDBO(
    DATASET_ID_1,
    DB_CATALOG_ID_1,
    uri = "http://$DATASET_ID_1",
    published = true,
    approved = true,
    lastModified = null,
)

val DATASET_2 = TEST_DATASET_0.copy(
    id = DATASET_ID_2,
    catalogId = DB_CATALOG_ID_1,
    uri = "http://$DATASET_ID_2",
    published = true,
    approved = true,
    losTheme = setOf("https://psi.norge.no/los/tema/arbeid"),
    euDataTheme = setOf("http://publications.europa.eu/resource/authority/data-theme/AGRI")
)

val DB_DATASET_1 = DatasetDBO(
    DB_DATASET_ID_1,
    DB_CATALOG_ID_1,
    lastModified = null,
    uri = "http://$DB_DATASET_ID_1",
    title = LocalizedStrings(nb = "test tittel"),
    published = true,
    approved = true,
)

val DB_DATASET_2 = DatasetDBO(
    DB_DATASET_ID_2,
    DB_CATALOG_ID_1,
    lastModified = null,
    uri = "http://$DB_DATASET_ID_2",
    type = "http://publications.europa.eu/resource/authority/dataset-type/TEST_DATA",
    title = LocalizedStrings(nb = "enda en"),
    description = LocalizedStrings(en = "test words"),
    published = true,
    approved = true,
    references = listOf(REFERENCE_EX),
)

val DB_DATASET_3 = DatasetDBO(
    DB_DATASET_ID_3,
    DB_CATALOG_ID_1,
    lastModified = null,
    uri = "http://$DB_DATASET_ID_3",
    description = LocalizedStrings(en = "the description"),
    published = true,
    approved = true,
)

val DB_DATASET_4 = DatasetDBO(
    DB_DATASET_ID_4,
    DB_CATALOG_ID_2,
    lastModified = null,
    uri = "http://$DB_DATASET_ID_4",
    published = true,
    approved = true,
)

val DB_DATASET_5 = DatasetDBO(
    DB_DATASET_ID_5,
    DB_CATALOG_ID_2,
    lastModified = null,
    uri = "http://$DB_DATASET_ID_5",
    published = true,
    approved = true,
)

val DB_DATASET_6 = DatasetDBO(
    DB_DATASET_ID_6,
    DB_CATALOG_ID_2,
    lastModified = null,
    uri = "http://$DB_DATASET_ID_6",
    published = false,
    approved = false,
)

val DB_CATALOG_1 = CatalogCount(
    DB_CATALOG_ID_1,
    datasetCount = 3,
)

val SERIES_DATASET_0 = DatasetDBO(
    SERIES_DATASET_ID_0,
    SERIES_CATALOG_ID,
    lastModified = null,
    specializedType = SpecializedType.SERIES,
    seriesDatasetOrder = mapOf(Pair(SERIES_DATASET_ID_1, 0), Pair(SERIES_DATASET_ID_5, 1)),
    published = true,
    approved = true,
    uri = "http://localhost:5050/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_0",
)

val SERIES_DATASET_1 = DatasetDBO(
    SERIES_DATASET_ID_1,
    SERIES_CATALOG_ID,
    lastModified = null,
    inSeries = SERIES_DATASET_ID_0,
    published = true,
    approved = true,
    uri = "http://localhost:5050/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_1",
)

val SERIES_DATASET_2 = DatasetDBO(
    SERIES_DATASET_ID_2,
    SERIES_CATALOG_ID,
    lastModified = null,
    published = false,
    approved = false,
    uri = "http://localhost:5050/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_2",
)

val SERIES_DATASET_5 = DatasetDBO(
    SERIES_DATASET_ID_5,
    SERIES_CATALOG_ID,
    lastModified = null,
    inSeries = SERIES_DATASET_ID_0,
    published = true,
    approved = true,
    uri = "http://localhost:5050/catalogs/$SERIES_CATALOG_ID/datasets/$SERIES_DATASET_ID_5",
)

fun datasetDbPopulation() = listOf(
    DB_DATASET_1, DB_DATASET_2, DB_DATASET_3, DB_DATASET_4, DB_DATASET_5, DB_DATASET_6,
    SERIES_DATASET_0, SERIES_DATASET_1, SERIES_DATASET_2, SERIES_DATASET_5
)
