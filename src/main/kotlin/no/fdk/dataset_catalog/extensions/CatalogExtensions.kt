package no.fdk.dataset_catalog.extensions

import no.fdk.dataset_catalog.model.Catalog
import org.springframework.beans.factory.annotation.Value

@Value("\${application.catalogURI}")
private val catalogURI: String? = null

fun Catalog.verifyId(other: Catalog): Catalog? = if (this.id.equals(other.id)) this else null

fun Catalog.updateUriIfNeeded(): Catalog =
    if (id != null && uri == null) {
        copy(uri = getCatalogURI(id))
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
        themeTaxonomy = newValues.themeTaxonomy ?: themeTaxonomy,
        dataset = newValues.dataset ?: dataset,
    )

private fun getCatalogURI(id: String) = catalogURI + id