package no.fdk.dataset_catalog.controller

import no.fdk.dataset_catalog.model.Dataset
import no.fdk.dataset_catalog.security.EndpointPermissions
import no.fdk.dataset_catalog.service.DatasetService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin
@RequestMapping(value = ["/catalogs/{catalogId}/datasets"])
class DatasetController(
    private val datasetService: DatasetService,
    private val endpointPermissions: EndpointPermissions) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAll(@AuthenticationPrincipal jwt: Jwt,
               @PathVariable("catalogId") catalogId: String): ResponseEntity<Collection<Dataset>> =
        if (endpointPermissions.hasOrgReadPermission(jwt, catalogId)) {
            datasetService.getAll(catalogId)
                .let { ResponseEntity(it, HttpStatus.OK) }
        } else ResponseEntity(HttpStatus.FORBIDDEN)


    @GetMapping(value = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getById(@AuthenticationPrincipal jwt: Jwt,
                @PathVariable("catalogId") catalogId: String,
                @PathVariable id: String): ResponseEntity<Dataset> =
        if (endpointPermissions.hasOrgReadPermission(jwt, catalogId)) {
            datasetService.getByID(catalogId, id)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NOT_FOUND)
        } else ResponseEntity(HttpStatus.FORBIDDEN)


    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun create(@AuthenticationPrincipal jwt: Jwt,
               @PathVariable("catalogId") catalogId: String,
               @RequestBody dataset: Dataset): ResponseEntity<Unit> =
        if (endpointPermissions.hasOrgWritePermission(jwt, catalogId)) {
            try {
                datasetService.create(catalogId, dataset)
                ResponseEntity<Unit>(HttpStatus.CREATED)
            } catch (e : Exception) {
                ResponseEntity(HttpStatus.BAD_REQUEST)
            }
        } else ResponseEntity<Unit>(HttpStatus.FORBIDDEN)


    @PatchMapping(value = ["/{id}"], consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun updateDataset(@AuthenticationPrincipal jwt: Jwt,
                      @PathVariable("catalogId") catalogId: String,
                      @PathVariable id: String,
                      @RequestBody patch: Dataset): ResponseEntity<Unit> =
        if (endpointPermissions.hasOrgWritePermission(jwt, catalogId)) {
            try {
                datasetService.updateDataset(catalogId, id, patch)
                    ?.let { ResponseEntity(HttpStatus.OK) }
                    ?: ResponseEntity(HttpStatus.BAD_REQUEST)
            } catch (e : Exception) {
                ResponseEntity(HttpStatus.BAD_REQUEST)
            }
        } else ResponseEntity<Unit>(HttpStatus.FORBIDDEN)


    @DeleteMapping(value = ["/{id}"])
    fun remove(@AuthenticationPrincipal jwt: Jwt,
               @PathVariable("catalogId") catalogId: String,
               @PathVariable("id") id: String): ResponseEntity<Unit> =
        if (endpointPermissions.hasOrgWritePermission(jwt, catalogId)) {
            try {
                datasetService.delete(catalogId, id)
                ResponseEntity(HttpStatus.OK)
            } catch (e : Exception) {
                ResponseEntity(HttpStatus.BAD_REQUEST)
            }
        } else ResponseEntity(HttpStatus.FORBIDDEN)


}
