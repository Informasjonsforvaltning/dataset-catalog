package no.fdk.catalog_catalog.controller

import no.fdk.dataset_catalog.model.Catalog
import no.fdk.dataset_catalog.service.CatalogService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin
@RequestMapping(value = ["/catalogs"])
class CatalogController (val catalogService: CatalogService) {
    //    TODO: Add permission checks

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAll() = catalogService.getAll()

    @GetMapping(value = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getById(@PathVariable id: String): ResponseEntity<Catalog> =
        catalogService.getByID(id)
            ?.let { ResponseEntity(it, HttpStatus.OK) }
            ?: ResponseEntity(HttpStatus.NOT_FOUND)

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun create(@RequestBody catalog: Catalog): ResponseEntity<Unit> =
        try {
            catalogService.create(catalog)
            ResponseEntity(HttpStatus.CREATED)
        } catch (e : Exception) {
            ResponseEntity(HttpStatus.BAD_REQUEST)
        }


    @DeleteMapping(value = ["/{id}"])
    fun remove(@PathVariable("id") id: String): ResponseEntity<Unit> =
        try {
            catalogService.delete(id)
            ResponseEntity(HttpStatus.OK)
        } catch (e : Exception) {
            ResponseEntity(HttpStatus.BAD_REQUEST)
        }

}