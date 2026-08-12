package no.fdk.dataset_catalog.utils
import java.net.URI

fun String?.isValidURI(): Boolean {
    if (this.isNullOrBlank()) return false
    return try {
        URI(this)
        true
    } catch (e: java.lang.Exception) {
        false
    }
}
