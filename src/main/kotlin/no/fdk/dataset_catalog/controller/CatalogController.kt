package no.fdk.dataset_catalog.controller

import no.fdk.dataset_catalog.model.Catalog
import no.fdk.dataset_catalog.security.EndpointPermissions
import no.fdk.dataset_catalog.service.CatalogService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin
@RequestMapping(value = ["/catalogs"])
class CatalogController (
    private val catalogService: CatalogService,
    private val endpointPermissions: EndpointPermissions) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllPermitted(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<List<Catalog>> {
        val permittedOrgs = endpointPermissions.getOrgsByReadPermission(jwt)
        return when {
            endpointPermissions.hasSysAdminPermission(jwt) ->
                ResponseEntity(catalogService.getAll(), HttpStatus.OK)
            permittedOrgs.isNotEmpty() ->
                ResponseEntity(catalogService.getByIDs(permittedOrgs), HttpStatus.OK)
            else -> ResponseEntity(HttpStatus.FORBIDDEN)
        }
    }

    @GetMapping(value = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getById(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: String): ResponseEntity<Catalog> =
        if (endpointPermissions.hasOrgReadPermission(jwt, id)) {
            catalogService.getByID(id)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NOT_FOUND)
        } else ResponseEntity(HttpStatus.FORBIDDEN)

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun create(@AuthenticationPrincipal jwt: Jwt, @RequestBody catalog: Catalog): ResponseEntity<Unit> =
        if (catalog.id != null && endpointPermissions.hasOrgWritePermission(jwt, catalog.id)) {
            try {
                catalogService.create(catalog)
                ResponseEntity(HttpStatus.CREATED)
            } catch (e : Exception) {
                ResponseEntity(HttpStatus.BAD_REQUEST)
            }
        } else ResponseEntity(HttpStatus.FORBIDDEN)


    @DeleteMapping(value = ["/{id}"])
    fun remove(@AuthenticationPrincipal jwt: Jwt, @PathVariable("id") id: String): ResponseEntity<Unit> =
        if (endpointPermissions.hasOrgWritePermission(jwt, id)) {
            try {
                catalogService.delete(id)
                ResponseEntity(HttpStatus.OK)
            } catch (e : Exception) {
                ResponseEntity(HttpStatus.BAD_REQUEST)
            }
        } else ResponseEntity(HttpStatus.FORBIDDEN)


    @PutMapping(value = ["/{id}"])
    fun update(@AuthenticationPrincipal jwt: Jwt,
               @PathVariable("id") id: String,
               @RequestBody catalog: Catalog): ResponseEntity<Catalog> =
        if (catalog.id == id && endpointPermissions.hasOrgWritePermission(jwt, catalog.id)) {
            catalogService.update(id, catalog)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.BAD_REQUEST)
        } else ResponseEntity(HttpStatus.FORBIDDEN)

}