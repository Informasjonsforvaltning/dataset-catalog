package no.fdk.dataset_catalog.controller

import no.fdk.dataset_catalog.rdf.*
import no.fdk.dataset_catalog.service.RDFService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin
@RequestMapping(value = ["/catalogs"], produces = ["text/turtle"])
class RDFController (private val rdfService: RDFService) {

    @GetMapping
    fun getAll(): ResponseEntity<String> =
        ResponseEntity(rdfService.getAll().createRDFResponse(), HttpStatus.OK)

    @GetMapping(value = ["/{catalogId}"])
    fun getCatalogById(@PathVariable catalogId: String): ResponseEntity<String> =
        rdfService.getCatalogById(catalogId)?.createRDFResponse()
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NOT_FOUND)

    @GetMapping(value = ["/{catalogId}/datasets/{id}"])
    fun getDatasetById(@PathVariable catalogId: String, @PathVariable id: String): ResponseEntity<String> =
        rdfService.getDatasetById(catalogId, id)?.createRDFResponse()
            ?.let { ResponseEntity(it, HttpStatus.OK) }
            ?: ResponseEntity(HttpStatus.NOT_FOUND)

}
