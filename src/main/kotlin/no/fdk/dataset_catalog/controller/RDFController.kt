package no.fdk.dataset_catalog.controller

import no.fdk.dataset_catalog.rdf.*
import no.fdk.dataset_catalog.service.RDFService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(
    value = ["/catalogs"],
    produces = ["text/turtle", "text/n3", "application/rdf+json", "application/ld+json", "application/rdf+xml",
        "application/n-triples", "application/n-quads", "application/trig", "application/trix"]
)
class RDFController(private val rdfService: RDFService) {

    @GetMapping
    fun getAll(@RequestHeader(HttpHeaders.ACCEPT) accept: String?): ResponseEntity<String> =
        ResponseEntity(rdfService.getAll().createRDFResponse(jenaLangFromAcceptHeader(accept)), HttpStatus.OK)

    @GetMapping(value = ["/{catalogId}"])
    fun getCatalogById(
        @RequestHeader(HttpHeaders.ACCEPT) accept: String?,
        @PathVariable catalogId: String
    ): ResponseEntity<String> =
        rdfService.getCatalogById(catalogId)
            ?.createRDFResponse(jenaLangFromAcceptHeader(accept))
            ?.let { ResponseEntity(it, HttpStatus.OK) }
            ?: ResponseEntity(HttpStatus.NOT_FOUND)

    @GetMapping(value = ["/{catalogId}/datasets/{id}"])
    fun getDatasetById(
        @RequestHeader(HttpHeaders.ACCEPT) accept: String?,
        @PathVariable catalogId: String,
        @PathVariable id: String
    ): ResponseEntity<String> =
        rdfService.getDatasetById(catalogId, id)
            ?.createRDFResponse(jenaLangFromAcceptHeader(accept))
            ?.let { ResponseEntity(it, HttpStatus.OK) }
            ?: ResponseEntity(HttpStatus.NOT_FOUND)

}
