package no.fdk.dataset_catalog.controller

import no.fdk.dataset_catalog.exception.NotPermittedException
import no.fdk.dataset_catalog.model.*
import no.fdk.dataset_catalog.service.SearchService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

private val logger = LoggerFactory.getLogger(SearchController::class.java)

@RestController
@CrossOrigin
@RequestMapping(value = ["/v2/search"])
class SearchController (
    private val searchService: SearchService) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun search(@AuthenticationPrincipal jwt: Jwt,
              @RequestBody searchRequest: SearchRequest): ResponseEntity<SearchResult> =
        try {
            when (searchRequest.searchType) {
                SEARCH_TYPE.DATASET_BY_QUERY -> ResponseEntity(searchService.datasetByQuery(jwt, searchRequest), HttpStatus.OK)
            }

        } catch (e: NotPermittedException) {
            logger.error("Failed to execute search. Reason:", e)
            ResponseEntity(HttpStatus.FORBIDDEN)

        } catch (e : Exception) {
                logger.error("Failed to execute search. Reason:", e)
                ResponseEntity(HttpStatus.BAD_REQUEST)
        }
}

