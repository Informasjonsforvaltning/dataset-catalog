package no.fdk.dataset_catalog.controller

import no.fdk.dataset_catalog.rdf.*
import no.fdk.dataset_catalog.service.RDFService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@CrossOrigin
@RequestMapping(
    value = ["/catalogs"],
    produces = ["text/turtle",
        "application/rdf+json",
        "application/ld+json",
        "application/rdf+xml",
        "application/n-triples"])
class RDFController (val rdfService: RDFService) {

    @GetMapping()
    fun getAll(httpServletRequest: HttpServletRequest): ResponseEntity<Any> =
    when (acceptHeaderToJenaType(httpServletRequest.getHeader("Accept"))) {
            JenaType.NOT_ACCEPTABLE -> ResponseEntity(HttpStatus.NOT_ACCEPTABLE)
            else -> ResponseEntity(rdfService.getAll()?.createRDFResponse(), HttpStatus.OK)
        }

    @GetMapping(value = ["/{id}"])
    fun getById(httpServletRequest: HttpServletRequest, @PathVariable id: String): ResponseEntity<Any> =
        when (acceptHeaderToJenaType(httpServletRequest.getHeader("Accept"))) {
            JenaType.NOT_ACCEPTABLE -> ResponseEntity(HttpStatus.NOT_ACCEPTABLE)
            else -> rdfService.getById(id)?.createRDFResponse()
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NOT_FOUND)
        }



}