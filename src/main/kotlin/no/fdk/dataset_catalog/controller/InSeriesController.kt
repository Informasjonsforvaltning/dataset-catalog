package no.fdk.dataset_catalog.controller

import no.fdk.dataset_catalog.model.Dataset
import no.fdk.dataset_catalog.security.EndpointPermissions
import no.fdk.dataset_catalog.service.DatasetService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin
@RequestMapping(value = ["/catalogs/{catalogId}/datasets/{id}/in-series"])
class InSeriesController(
    private val datasetService: DatasetService,
    private val endpointPermissions: EndpointPermissions
) {

    @PutMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun updateInSeries(@AuthenticationPrincipal jwt: Jwt,
                      @PathVariable("catalogId") catalogId: String,
                      @PathVariable id: String,
                      @RequestBody inSeries: String,
    ): ResponseEntity<Dataset> =
        if (endpointPermissions.hasOrgWritePermission(jwt, catalogId)) {
            datasetService.updateInSeries(catalogId, id, inSeries)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.BAD_REQUEST)
        } else ResponseEntity(HttpStatus.FORBIDDEN)

    @DeleteMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun removeInSeries(@AuthenticationPrincipal jwt: Jwt,
                      @PathVariable("catalogId") catalogId: String,
                      @PathVariable("id") id: String): ResponseEntity<Dataset> =
        if (endpointPermissions.hasOrgWritePermission(jwt, catalogId)) {
            ResponseEntity(datasetService.deleteInSeries(id), HttpStatus.OK)
        } else ResponseEntity(HttpStatus.FORBIDDEN)

}
