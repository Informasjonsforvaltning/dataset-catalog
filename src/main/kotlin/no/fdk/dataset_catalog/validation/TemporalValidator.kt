package no.fdk.dataset_catalog.validation

import no.fdk.dataset_catalog.model.DatasetDBO
import no.fdk.dataset_catalog.model.PeriodOfTimeDBO
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

object TemporalValidator {

    private val YEAR_FMT: DateTimeFormatter =
        DateTimeFormatter.ofPattern("uuuu").withResolverStyle(ResolverStyle.STRICT)
    private val YEAR_MONTH_FMT: DateTimeFormatter =
        DateTimeFormatter.ofPattern("uuuu-MM").withResolverStyle(ResolverStyle.STRICT)
    private val DATE_FMT: DateTimeFormatter =
        DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT)

    fun validate(dataset: DatasetDBO) {
        dataset.temporal?.forEachIndexed { index, period -> validatePeriod(period, index) }
    }

    private fun validatePeriod(period: PeriodOfTimeDBO, index: Int) {
        period.startDate?.let { validateValue(it, "temporal[$index].startDate") }
        period.endDate?.let { validateValue(it, "temporal[$index].endDate") }

        val start = period.startDate
        val end = period.endDate
        if (start != null && end != null) {
            if (startBound(start).isAfter(endBound(end))) {
                throw badRequest(
                    "temporal[$index]: startDate ($start) must not be after endDate ($end)"
                )
            }
        }
    }

    private fun validateValue(value: String, path: String) {
        try {
            when (value.length) {
                4 -> Year.parse(value, YEAR_FMT)
                7 -> YearMonth.parse(value, YEAR_MONTH_FMT)
                10 -> LocalDate.parse(value, DATE_FMT)
                else -> throw badRequest(
                    "$path: '$value' must be yyyy, yyyy-MM, or yyyy-MM-dd"
                )
            }
        } catch (ex: DateTimeParseException) {
            throw badRequest("$path: '$value' is not a valid date")
        }
    }

    private fun startBound(value: String): LocalDate = when (value.length) {
        4 -> LocalDate.of(value.toInt(), 1, 1)
        7 -> YearMonth.parse(value, YEAR_MONTH_FMT).atDay(1)
        else -> LocalDate.parse(value, DATE_FMT)
    }

    private fun endBound(value: String): LocalDate = when (value.length) {
        4 -> LocalDate.of(value.toInt(), 12, 31)
        7 -> YearMonth.parse(value, YEAR_MONTH_FMT).atEndOfMonth()
        else -> LocalDate.parse(value, DATE_FMT)
    }

    private fun badRequest(message: String): ResponseStatusException =
        ResponseStatusException(HttpStatus.BAD_REQUEST, message)
}
