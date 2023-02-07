package no.fdk.dataset_catalog.controller

import no.fdk.dataset_catalog.service.DatasetService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin
class ApplicationStatusController(private val datasetService: DatasetService) {

    @GetMapping("/ping")
    fun ping(): ResponseEntity<Unit> =
        ResponseEntity.ok().build()

    @GetMapping("/ready")
    fun ready(): ResponseEntity<Unit> =
        try {
            datasetService.count()
            ResponseEntity(HttpStatus.OK)
        } catch (e: Exception) {
            ResponseEntity(HttpStatus.SERVICE_UNAVAILABLE)
        }

}