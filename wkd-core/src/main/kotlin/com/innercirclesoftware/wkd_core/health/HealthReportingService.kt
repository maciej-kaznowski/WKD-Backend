package com.innercirclesoftware.wkd_core.health

import arrow.core.Either
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class HealthReportingService @Inject constructor(
    private val healthchecksIoService: HealthchecksIoService,
) {

    private val logger = LoggerFactory.getLogger(HealthReportingService::class.java)

    fun reportInternalCheck(success: Boolean = true) {
        Either
            .catch {
                healthchecksIoService.ping(
                    slug = Slugs.HEALTHCHECK_INTERNAL_SEARCH,
                    success = success,
                )
            }
            .tapLeft { error ->
                logger.error(
                    "Error reporting internal check to healthchecks.io for success=$success",
                    error,
                )
            }
    }

    fun reportSearchEndpointHit(success: Boolean = true) {
        Either
            .catch {
                healthchecksIoService.ping(
                    slug = Slugs.PUBLIC_ENDPOINT_HIT,
                    success = success,
                )
            }
            .tapLeft { error ->
                logger.error(
                    "Error reporting search endpoint hit event to healthchecks.io for success=$success",
                    error,
                )
            }
    }
}

private object Slugs {

    const val HEALTHCHECK_INTERNAL_SEARCH = "healthcheck-internal-search"
    const val PUBLIC_ENDPOINT_HIT = "public-endpoint-hit"

}