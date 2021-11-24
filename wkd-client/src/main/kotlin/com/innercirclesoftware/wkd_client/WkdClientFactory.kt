package com.innercirclesoftware.wkd_client

import com.innercirclesoftware.wkd_api.services.WkdJourneyService
import com.innercirclesoftware.wkd_client.internal.retrofit.JourneyRetrofitService
import com.innercirclesoftware.wkd_client.internal.services.WkdJourneyRestService
import com.squareup.moshi.*
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import java.time.Instant
import java.time.format.DateTimeFormatter

class WkdClientFactory(private val retrofit: Retrofit) {

    constructor(baseUrl: String) : this(Defaults.retrofit(baseUrl))

    fun createWkdJourneyService(): WkdJourneyService {
        val retrofitService = retrofit.create<JourneyRetrofitService>()
        return WkdJourneyRestService(retrofitService)
    }

    object Defaults {

        fun retrofit(
            baseUrl: String,
            moshi: Moshi,
        ): Retrofit = retrofit(
            baseUrl = baseUrl,
            converterFactory = MoshiConverterFactory.create(moshi)
        )

        fun retrofit(
            baseUrl: String,
            converterFactory: Converter.Factory = MoshiConverterFactory.create(moshi()),
        ): Retrofit {
            return Retrofit.Builder()
                .addConverterFactory(converterFactory)
                .baseUrl(baseUrl)
                .build()
        }

        fun moshi(
            instantAdapter: JsonAdapter<Instant> = WkdMoshiInstantAdapter
        ): Moshi {
            return Moshi.Builder()
                .add(Instant::class.java, instantAdapter)
                .build()
        }
    }
}

object WkdMoshiInstantAdapter : JsonAdapter<Instant>() {

    override fun fromJson(reader: JsonReader): Instant? {
        return when (val token: JsonReader.Token = reader.peek()) {
            JsonReader.Token.NULL -> null
            JsonReader.Token.STRING -> {
                val str = reader.nextString()
                val temporalAccessor = runCatching { DateTimeFormatter.ISO_INSTANT.parse(str) }
                    .getOrElse { cause ->
                        throw JsonDataException(
                            "Could not deserialize Instant from string='$str' as it is not an ISO-8601 timestamp",
                            cause
                        )
                    }
                return Instant.from(temporalAccessor)
            }
            else -> {
                throw JsonDataException("Can not convert token='$token' to instant.")
            }
        }
    }

    override fun toJson(writer: JsonWriter, value: Instant?) {
        writer.value(value?.let { DateTimeFormatter.ISO_INSTANT.format(it) })
    }
}