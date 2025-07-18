package no.fdk.dataset_catalog.controller

import no.fdk.dataset_catalog.model.DatasetDBO
import no.fdk.dataset_catalog.model.DatasetToCreate
import no.fdk.dataset_catalog.security.EndpointPermissions
import no.fdk.dataset_catalog.service.DatasetService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/internal/catalogs/{catalogId}/datasets")
open class InternalDatasetController(
    private val datasetService: DatasetService,
    private val endpointPermissions: EndpointPermissions
) {

    @GetMapping(value = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getDatasetById(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable("catalogId") catalogId: String,
        @PathVariable id: String
    ): ResponseEntity<DatasetDBO> =
        if (endpointPermissions.hasOrgReadPermission(jwt, catalogId)) {
            datasetService.getDatasetByID(catalogId, id)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NOT_FOUND)
        } else ResponseEntity(HttpStatus.FORBIDDEN)

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getDatasetsByCatalogId(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable("catalogId") catalogId: String
    ): ResponseEntity<List<DatasetDBO>> =
        if (endpointPermissions.hasOrgReadPermission(jwt, catalogId)) {
            ResponseEntity(datasetService.getAllDatasets(catalogId), HttpStatus.OK)
        } else ResponseEntity(HttpStatus.FORBIDDEN)

    @PostMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun createDataset(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable("catalogId") catalogId: String,
        @RequestBody dataset: DatasetToCreate
    ): ResponseEntity<Void> {
        if (endpointPermissions.hasOrgWritePermission(jwt, catalogId)) {
            val datasetId = datasetService.createDataset(catalogId, dataset)
            return ResponseEntity
                .created(URI("/internal/catalogs/${catalogId}/datasets/${datasetId}"))
                .build()
        } else return ResponseEntity(HttpStatus.FORBIDDEN)
    }

}
