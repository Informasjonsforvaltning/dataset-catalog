package no.fdk.dataset_catalog

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import no.fdk.dataset_catalog.configuration.ApplicationProperties
import no.fdk.dataset_catalog.configuration.SecurityProperties
import org.springframework.boot.SpringApplication

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties::class, SecurityProperties::class)
open class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
