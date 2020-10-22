package no.fdk.dataset_catalog.controller

import no.fdk.dataset_catalog.model.Dataset
import no.fdk.dataset_catalog.service.DatasetService
import no.fdk.dataset_catalog.webutils.exceptions.FDKException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin
@RequestMapping(value = ["/catalogs/{catalogId}/datasets"])
class DatasetController(private val datasetService: DatasetService) {

    @GetMapping(value = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getById(@PathVariable("catalogId") catalogId: String, @PathVariable id: String): ResponseEntity<Dataset> =
        datasetService.getByID(catalogId, id)
            ?.let { ResponseEntity(it, HttpStatus.OK) }
            ?: ResponseEntity(HttpStatus.NOT_FOUND)

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @Throws(FDKException::class)
    fun create(@PathVariable("catalogId") catalogId: String, @RequestBody dataset: Dataset): ResponseEntity<Unit> =
        try {
            datasetService.create(catalogId, dataset)
            ResponseEntity(HttpStatus.CREATED)
        } catch (e : Exception) {
            ResponseEntity(HttpStatus.BAD_REQUEST)
        }
}