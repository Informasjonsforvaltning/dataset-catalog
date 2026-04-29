package no.fdk.dataset_catalog.utils

import org.slf4j.LoggerFactory
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.PostgreSQLContainer
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

abstract class ApiTestContext {

    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            TestPropertyValues.of(
                "spring.datasource.url=$postgresJdbcUrl",
                "spring.datasource.username=$DB_USER",
                "spring.datasource.password=$DB_PASSWORD",
            ).applyTo(configurableApplicationContext.environment)
        }
    }

    companion object {

        private val logger = LoggerFactory.getLogger(ApiTestContext::class.java)
        private var postgresContainer: KPostgreSQLContainer? = null
        var postgresJdbcUrl: String = ""

        init {

            startMockServer()

            val externalHost = System.getenv("POSTGRES_HOST")
                ?: System.getenv("POSTGRESQL_HOST")
                ?: System.getenv("DB_HOST")

            if (externalHost != null) {
                val port = System.getenv("POSTGRES_PORT")
                    ?: System.getenv("POSTGRESQL_PORT")
                    ?: System.getenv("DB_PORT")
                    ?: "5432"
                val dbName = System.getenv("POSTGRES_DB")
                    ?: System.getenv("POSTGRESQL_DB")
                    ?: System.getenv("DB_NAME")
                    ?: DB_NAME
                postgresJdbcUrl = "jdbc:postgresql://$externalHost:$port/$dbName"
                logger.info("Using external Postgres at {}", postgresJdbcUrl)
            } else {
                postgresContainer = KPostgreSQLContainer("postgres:16")
                    .withDatabaseName(DB_NAME)
                    .withUsername(DB_USER)
                    .withPassword(DB_PASSWORD)
                postgresContainer!!.start()
                postgresJdbcUrl = postgresContainer!!.jdbcUrl
            }

            resetDB()

            try {
                val con = URL("http://localhost:5050/ping").openConnection() as HttpURLConnection
                con.connect()
                if (con.responseCode != 200) {
                    logger.debug("Ping to mock server failed")
                    stopMockServer()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

        }
    }

}

class KPostgreSQLContainer(imageName: String) : PostgreSQLContainer<KPostgreSQLContainer>(imageName)
