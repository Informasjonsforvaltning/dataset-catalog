package no.fdk.dataset_catalog.controller

import no.fdk.dataset_catalog.model.CatalogCount
import no.fdk.dataset_catalog.model.CatalogDTO
import no.fdk.dataset_catalog.security.EndpointPermissions
import no.fdk.dataset_catalog.service.CatalogService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

private val logger = LoggerFactory.getLogger(CatalogController::class.java)

@RestController
@RequestMapping(value = ["/catalogs"])
class CatalogController(
    private val catalogService: CatalogService,
    private val endpointPermissions: EndpointPermissions) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllPermitted(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<CatalogDTO> {
        val permittedOrgs = endpointPermissions.getOrgsByPermission(jwt, "read")
        logger.info(if (permittedOrgs.isEmpty()) "No permitted catalogs to fetch" else "Fetching catalogs for organizations in $permittedOrgs")
        return when {
            endpointPermissions.hasSysAdminPermission(jwt) ->
                ResponseEntity(catalogService.getAllAsDTO(), HttpStatus.OK)
            permittedOrgs.isNotEmpty() ->
                ResponseEntity(catalogService.getByIDsAsDTO(permittedOrgs.toList()), HttpStatus.OK)
            else -> ResponseEntity(CatalogDTO(null), HttpStatus.OK)
        }
    }

    @GetMapping(value = ["/{catalogId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getCatalogById(@AuthenticationPrincipal jwt: Jwt, @PathVariable catalogId: String): ResponseEntity<CatalogCount> =
        if (endpointPermissions.hasOrgReadPermission(jwt, catalogId)) {
            logger.info("Fetching catalog with ID $catalogId")
            catalogService.getByID(catalogId)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NOT_FOUND)
        } else ResponseEntity(HttpStatus.FORBIDDEN)

}
