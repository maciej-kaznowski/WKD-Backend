package com.innercirclesoftware.wkd_core.health

import io.micronaut.context.annotation.Property
import jakarta.inject.Inject
import jakarta.inject.Singleton
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

private val baseUrl = "https://hc-ping.com".toHttpUrl()

/**
 * A class which reports to [healthchecks.io](https://healthchecks.io)
 *
 */
@Singleton
class HealthchecksIoService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    @Property(name = "healthchecksio.pingkey") private val pingKey: String,
) {

    private val basePingUrl = baseUrl.newBuilder()
        .addPathSegment(pingKey)
        .build()

    fun ping(slug: String, success: Boolean = true) {
        val url = basePingUrl.newBuilder()
            .addPathSegment(slug)
            .apply {
                if (success.not()) {
                    addPathSegment("fail")
                }
            }
            .build()

        val request = Request.Builder()
            .url(url)
            .build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("Could not ping healthcheck.io. response=$response")
            }
        }
    }
}