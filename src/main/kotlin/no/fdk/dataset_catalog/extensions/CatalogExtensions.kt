package no.fdk.dataset_catalog.extensions

import no.fdk.dataset_catalog.model.Catalog
import no.fdk.dataset_catalog.model.CatalogDTO

fun List<Catalog>.toDTO() : CatalogDTO = CatalogDTO(mapOf(Pair("catalogs", this)))

fun Catalog.verifyId(other: Catalog): Catalog? = if (this.id.equals(other.id)) this else null

fun Catalog.updateUriIfNeeded(catalogUriHost: String): Catalog =
    if (id != null && uri == null) {
        copy(uri = getCatalogURI(catalogUriHost, id))
    } else this

fun Catalog.update(newValues: Catalog): Catalog =
    copy(
        id = newValues.id ?: id,
        uri  = newValues.uri ?: uri,
        title = newValues.title ?: title,
        description = newValues.description ?: description,
        publisher = newValues.publisher ?: publisher,
        issued = newValues.issued ?: issued,
        modified = newValues.modified ?: modified,
        language = newValues.language ?: language,
        dataset = newValues.dataset ?: dataset,
    )

private fun getCatalogURI(catalogUriHost: String, id: String): String = "$catalogUriHost/catalogs/$id"