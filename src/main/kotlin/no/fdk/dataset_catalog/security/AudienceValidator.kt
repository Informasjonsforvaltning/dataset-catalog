package no.fdk.dataset_catalog.security

import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt

internal class AudienceValidator : OAuth2TokenValidator<Jwt> {
    private val error = OAuth2Error("no_audience", "Missing audience", null)
    override fun validate(jwt: Jwt): OAuth2TokenValidatorResult {
        return if (jwt.audience.contains("fdk-registration-api")) {
            OAuth2TokenValidatorResult.success()
        } else {
            OAuth2TokenValidatorResult.failure(error)
        }
    }
}