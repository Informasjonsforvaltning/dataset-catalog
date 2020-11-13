package no.fdk.dataset_catalog.controller

import no.fdk.dataset_catalog.model.Dataset
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
    fun getAllDatasets(@AuthenticationPrincipal jwt: Jwt,
                       @PathVariable("catalogId") catalogId: String): ResponseEntity<Collection<Dataset>> =
        if (endpointPermissions.hasOrgReadPermission(jwt, catalogId)) {
            logger.info("Fetching datasets for catalog with ID $catalogId")
            datasetService.getAll(catalogId)
                .let { ResponseEntity(it, HttpStatus.OK) }
        } else ResponseEntity(HttpStatus.FORBIDDEN)


    @GetMapping(value = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getDatasetById(@AuthenticationPrincipal jwt: Jwt,
                       @PathVariable("catalogId") catalogId: String,
                       @PathVariable id: String): ResponseEntity<Dataset> =
        if (endpointPermissions.hasOrgReadPermission(jwt, catalogId)) {
            logger.info("Fetching dataset with ID $id from catalog with ID $catalogId")
            datasetService.getByID(catalogId, id)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NOT_FOUND)
        } else ResponseEntity(HttpStatus.FORBIDDEN)


    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createDataset(@AuthenticationPrincipal jwt: Jwt,
                      @PathVariable("catalogId") catalogId: String,
                      @RequestBody dataset: Dataset): ResponseEntity<Unit> =
        if (endpointPermissions.hasOrgWritePermission(jwt, catalogId)) {
            try {
                logger.info("Creating dataset in catalog $catalogId")
                datasetService.create(catalogId, dataset)
                ResponseEntity<Unit>(HttpStatus.CREATED)
            } catch (e : Exception) {
                ResponseEntity(HttpStatus.BAD_REQUEST)
            }
        } else ResponseEntity<Unit>(HttpStatus.FORBIDDEN)


    @PostMapping(value = ["/{id}"], consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE], headers = ["X-HTTP-Method-Override=PATCH"])
    fun updateDatasetOverride(@AuthenticationPrincipal jwt: Jwt,
                              @PathVariable("catalogId") catalogId: String,
                              @PathVariable id: String,
                              @RequestBody patch: Dataset): ResponseEntity<Unit> = updateDataset(jwt, catalogId, id, patch)

    @PatchMapping(value = ["/{id}"], consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun updateDataset(@AuthenticationPrincipal jwt: Jwt,
                      @PathVariable("catalogId") catalogId: String,
                      @PathVariable id: String,
                      @RequestBody patch: Dataset): ResponseEntity<Unit> =
        if (endpointPermissions.hasOrgWritePermission(jwt, catalogId)) {
            try {
                logger.info("Updating dataset with ID $id for catalog with ID $catalogId")
                datasetService.updateDataset(catalogId, id, patch)
                    ?.let { ResponseEntity(HttpStatus.OK) }
                    ?: ResponseEntity(HttpStatus.BAD_REQUEST)
            } catch (e : Exception) {
                ResponseEntity(HttpStatus.BAD_REQUEST)
            }
        } else ResponseEntity<Unit>(HttpStatus.FORBIDDEN)


    @DeleteMapping(value = ["/{id}"])
    fun removeDataset(@AuthenticationPrincipal jwt: Jwt,
                      @PathVariable("catalogId") catalogId: String,
                      @PathVariable("id") id: String): ResponseEntity<Unit> =
        if (endpointPermissions.hasOrgWritePermission(jwt, catalogId)) {
            try {
                datasetService.delete(catalogId, id)
                logger.info("Successfully deleted dataset with ID $id from catalog with ID $catalogId")
                ResponseEntity(HttpStatus.OK)
            } catch (e : Exception) {
                logger.info("Failed to delete dataset with ID $id from catalog with ID $catalogId")
                ResponseEntity(HttpStatus.BAD_REQUEST)
            }
        } else ResponseEntity(HttpStatus.FORBIDDEN)


}
