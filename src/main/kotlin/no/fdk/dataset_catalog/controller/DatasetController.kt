package no.fdk.dataset_catalog.controller

import no.fdk.dataset_catalog.extensions.toDTO
import no.fdk.dataset_catalog.model.Dataset
import no.fdk.dataset_catalog.model.DatasetDTO
import no.fdk.dataset_catalog.security.EndpointPermissions
import no.fdk.dataset_catalog.service.DatasetService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

private val logger = LoggerFactory.getLogger(DatasetController::class.java)

@RestController
@CrossOrigin
@RequestMapping(value = ["/catalogs/{catalogId}/datasets"])
class DatasetController(
    private val datasetService: DatasetService,
    private val endpointPermissions: EndpointPermissions) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllDatasets(
                       @PathVariable("catalogId") catalogId: String): ResponseEntity<DatasetDTO> =
        if (true) {
            logger.info("Fetching datasets for catalog with ID $catalogId")
            ResponseEntity(datasetService.getAll(catalogId).toDTO(), HttpStatus.OK)
        } else ResponseEntity(HttpStatus.FORBIDDEN)


    @GetMapping(value = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getDatasetById(
                       @PathVariable("catalogId") catalogId: String,
                       @PathVariable id: String): ResponseEntity<Dataset> =
        if (true) {
            logger.info("Fetching dataset with ID $id from catalog with ID $catalogId")
            datasetService.getByID(catalogId, id)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NOT_FOUND)
        } else ResponseEntity(HttpStatus.FORBIDDEN)


    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createDataset(
                      @PathVariable("catalogId") catalogId: String,
                      @RequestBody dataset: Dataset): ResponseEntity<Dataset> =
        if (true) {
            try {
                logger.info("Creating dataset in catalog $catalogId")
                ResponseEntity(datasetService.create(catalogId, dataset), HttpStatus.CREATED)
            } catch (e : Exception) {
                logger.error("Failed to create dataset. Reason:", e)
                ResponseEntity(HttpStatus.BAD_REQUEST)
            }
        } else ResponseEntity(HttpStatus.FORBIDDEN)


    @PatchMapping(value = ["/{id}"], consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun updateDataset(
                      @PathVariable("catalogId") catalogId: String,
                      @PathVariable id: String,
                      @RequestBody patch: Dataset): ResponseEntity<Dataset> =
        if (true) {
            try {
                logger.info("Updating dataset with ID $id for catalog with ID $catalogId")
                datasetService.updateDataset(catalogId, id, patch)
                    ?.let {ResponseEntity(it, HttpStatus.OK) }
                    ?: ResponseEntity(HttpStatus.BAD_REQUEST)
            } catch (e : Exception) {
                logger.error("Failed to update dataset. Reason:", e)
                ResponseEntity(HttpStatus.BAD_REQUEST)
            }
        } else ResponseEntity(HttpStatus.FORBIDDEN)


    @DeleteMapping(value = ["/{id}"])
    fun removeDataset(
                      @PathVariable("catalogId") catalogId: String,
                      @PathVariable("id") id: String): ResponseEntity<Unit> =
        if (true) {
            try {
                datasetService.delete(catalogId, id)
                logger.info("Successfully deleted dataset with ID $id from catalog with ID $catalogId")
                ResponseEntity(HttpStatus.OK)
            } catch (e : Exception) {
                logger.info("Failed to delete dataset with ID $id from catalog with ID $catalogId. ",e)
                ResponseEntity(HttpStatus.BAD_REQUEST)
            }
        } else ResponseEntity(HttpStatus.FORBIDDEN)


}
