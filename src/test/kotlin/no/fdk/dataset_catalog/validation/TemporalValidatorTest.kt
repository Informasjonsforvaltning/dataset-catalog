package no.fdk.dataset_catalog.validation

import no.fdk.dataset_catalog.model.DatasetDBO
import no.fdk.dataset_catalog.model.PeriodOfTimeDBO
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import kotlin.test.assertEquals

@Tag("unit")
class TemporalValidatorTest {

    private fun dataset(vararg periods: PeriodOfTimeDBO) = DatasetDBO(
        id = "id",
        catalogId = "cat",
        lastModified = LocalDateTime.now(),
        uri = "uri",
        temporal = periods.toList()
    )

    private fun assertRejected(period: PeriodOfTimeDBO) {
        val ex = assertThrows(ResponseStatusException::class.java) {
            TemporalValidator.validate(dataset(period))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun `null temporal accepted`() {
        TemporalValidator.validate(
            DatasetDBO(id = "id", catalogId = "cat", lastModified = LocalDateTime.now(), uri = "uri", temporal = null)
        )
    }

    @Test
    fun `empty temporal list accepted`() {
        TemporalValidator.validate(dataset())
    }

    @Test
    fun `period with both bounds null accepted`() {
        TemporalValidator.validate(dataset(PeriodOfTimeDBO()))
    }

    @Test
    fun `valid year-only accepted`() {
        TemporalValidator.validate(dataset(PeriodOfTimeDBO(startDate = "2024", endDate = "2024")))
    }

    @Test
    fun `valid year-month accepted`() {
        TemporalValidator.validate(dataset(PeriodOfTimeDBO(startDate = "2024-06", endDate = "2024-12")))
    }

    @Test
    fun `valid full date accepted`() {
        TemporalValidator.validate(dataset(PeriodOfTimeDBO(startDate = "2024-06-15", endDate = "2024-12-31")))
    }

    @Test
    fun `mixed precision accepted`() {
        TemporalValidator.validate(dataset(PeriodOfTimeDBO(startDate = "2024", endDate = "2024-06-15")))
        TemporalValidator.validate(dataset(PeriodOfTimeDBO(startDate = "2024-06", endDate = "2024-06-15")))
    }

    @Test
    fun `only startDate accepted`() {
        TemporalValidator.validate(dataset(PeriodOfTimeDBO(startDate = "2024")))
    }

    @Test
    fun `only endDate accepted`() {
        TemporalValidator.validate(dataset(PeriodOfTimeDBO(endDate = "2024")))
    }

    @Test
    fun `empty string rejected`() {
        assertRejected(PeriodOfTimeDBO(startDate = ""))
        assertRejected(PeriodOfTimeDBO(endDate = ""))
    }

    @Test
    fun `whitespace rejected`() {
        assertRejected(PeriodOfTimeDBO(startDate = " 2024 "))
        assertRejected(PeriodOfTimeDBO(startDate = "2024 "))
    }

    @Test
    fun `malformed strings rejected`() {
        listOf("24-01", "2024/01", "2024-1-1", "abcd", "2024-", "2024-06-", "20240615").forEach {
            assertRejected(PeriodOfTimeDBO(startDate = it))
        }
    }

    @Test
    fun `invalid calendar date rejected`() {
        listOf("2023-02-29", "2024-02-30", "2024-13-01", "2024-00-15", "2024-04-31").forEach {
            assertRejected(PeriodOfTimeDBO(startDate = it))
        }
    }

    @Test
    fun `leap year February 29 accepted`() {
        TemporalValidator.validate(dataset(PeriodOfTimeDBO(startDate = "2024-02-29")))
    }

    @Test
    fun `end before start on full date rejected`() {
        assertRejected(PeriodOfTimeDBO(startDate = "2024-06-15", endDate = "2024-06-14"))
    }

    @Test
    fun `end before start on year-month rejected`() {
        assertRejected(PeriodOfTimeDBO(startDate = "2024-06", endDate = "2024-05"))
    }

    @Test
    fun `same date accepted`() {
        TemporalValidator.validate(dataset(PeriodOfTimeDBO(startDate = "2024-06-15", endDate = "2024-06-15")))
    }

    @Test
    fun `year-month start within year end accepted`() {
        TemporalValidator.validate(dataset(PeriodOfTimeDBO(startDate = "2024-06", endDate = "2024")))
    }

    @Test
    fun `year start with year-month end accepted`() {
        TemporalValidator.validate(dataset(PeriodOfTimeDBO(startDate = "2024", endDate = "2024-06")))
    }

    @Test
    fun `date within month end accepted`() {
        TemporalValidator.validate(dataset(PeriodOfTimeDBO(startDate = "2024-06-30", endDate = "2024-06")))
    }

    @Test
    fun `date after month end rejected`() {
        assertRejected(PeriodOfTimeDBO(startDate = "2024-07-01", endDate = "2024-06"))
    }

    @Test
    fun `error message identifies period index`() {
        val ex = assertThrows(ResponseStatusException::class.java) {
            TemporalValidator.validate(
                dataset(
                    PeriodOfTimeDBO(startDate = "2024"),
                    PeriodOfTimeDBO(startDate = "bogus")
                )
            )
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        assert(ex.reason!!.contains("temporal[1].startDate"))
    }
}
