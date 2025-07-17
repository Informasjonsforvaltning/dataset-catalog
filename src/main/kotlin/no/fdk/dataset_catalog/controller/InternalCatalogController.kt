package no.fdk.dataset_catalog.controller

import no.fdk.dataset_catalog.model.CatalogCount
import no.fdk.dataset_catalog.security.EndpointPermissions
import no.fdk.dataset_catalog.service.CatalogService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/internal/catalogs"])
class InternalCatalogController(
    private val catalogService: CatalogService,
    private val endpointPermissions: EndpointPermissions
) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllPermitted(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<List<CatalogCount>> {
        val permittedOrgs = endpointPermissions.getOrgsByPermission(jwt, "read")
        return when {
            endpointPermissions.hasSysAdminPermission(jwt) ->
                ResponseEntity(catalogService.getAll(), HttpStatus.OK)
            permittedOrgs.isNotEmpty() ->
                ResponseEntity(catalogService.getByIDs(permittedOrgs.toList()), HttpStatus.OK)
            else -> ResponseEntity(emptyList(), HttpStatus.OK)
        }
    }

    @GetMapping(value = ["/{catalogId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getCatalogById(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String
    ): ResponseEntity<CatalogCount> =
        if (endpointPermissions.hasOrgReadPermission(jwt, catalogId)) {
            catalogService.getByID(catalogId)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NOT_FOUND)
        } else ResponseEntity(HttpStatus.FORBIDDEN)

}
