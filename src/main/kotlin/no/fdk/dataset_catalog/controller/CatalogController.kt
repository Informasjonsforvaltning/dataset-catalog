package no.fdk.dataset_catalog.controller

import no.fdk.dataset_catalog.model.Catalog
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
        val adminableOrgs = endpointPermissions.getOrgsByPermission(jwt, "admin")
        catalogService.createCatalogsIfNeeded(adminableOrgs)
        logger.info(if (permittedOrgs.isEmpty()) "No permitted catalogs to fetch" else "Fetching catalogs for organizations in $permittedOrgs")
        return when {
            endpointPermissions.hasSysAdminPermission(jwt) ->
                ResponseEntity(catalogService.getAllAsDTO(), HttpStatus.OK)
            permittedOrgs.isNotEmpty() ->
                ResponseEntity(catalogService.getByIDsAsDTO(permittedOrgs), HttpStatus.OK)
            else -> ResponseEntity(CatalogDTO(null), HttpStatus.OK)
        }
    }

    @GetMapping(value = ["/{catalogId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getCatalogById(@AuthenticationPrincipal jwt: Jwt, @PathVariable catalogId: String): ResponseEntity<Catalog> =
        if (endpointPermissions.hasOrgReadPermission(jwt, catalogId)) {
            logger.info("Fetching catalog with ID $catalogId")
            catalogService.getByID(catalogId)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NOT_FOUND)
        } else ResponseEntity(HttpStatus.FORBIDDEN)

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createCatalog(@AuthenticationPrincipal jwt: Jwt, @RequestBody catalog: Catalog): ResponseEntity<Unit> =
        if (catalog.id != null && endpointPermissions.hasOrgWritePermission(jwt, catalog.id)) {
            try {
                catalogService.create(catalog)
                logger.info("Created catalog with ID ${catalog.id}")
                ResponseEntity(HttpStatus.CREATED)
            } catch (e: Exception) {
                logger.info("Failed to create catalog with ID ${catalog.id}. Reason:", e)
                ResponseEntity(HttpStatus.BAD_REQUEST)
            }
        } else ResponseEntity(HttpStatus.FORBIDDEN)


    @DeleteMapping(value = ["/{catalogId}"])
    fun removeCatalog(@AuthenticationPrincipal jwt: Jwt, @PathVariable("catalogId") catalogId: String): ResponseEntity<Unit> =
        if (endpointPermissions.hasOrgWritePermission(jwt, catalogId)) {
            try {
                catalogService.delete(catalogId)
                logger.info("Deleted catalog with ID $catalogId")
                ResponseEntity(HttpStatus.OK)
            } catch (e: Exception) {
                logger.info("Failed to delete catalog with ID $catalogId.", e)
                ResponseEntity(HttpStatus.BAD_REQUEST)
            }
        } else ResponseEntity(HttpStatus.FORBIDDEN)


    @PutMapping(value = ["/{catalogId}"])
    fun updateCatalog(@AuthenticationPrincipal jwt: Jwt,
                      @PathVariable("catalogId") catalogId: String,
                      @RequestBody catalog: Catalog): ResponseEntity<Catalog> =
        if (catalog.id == catalogId && endpointPermissions.hasOrgWritePermission(jwt, catalog.id)) {
            logger.info("Updating catalog with ID $catalogId")
            catalogService.update(catalogId, catalog)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.BAD_REQUEST)
        } else ResponseEntity(HttpStatus.FORBIDDEN)

}