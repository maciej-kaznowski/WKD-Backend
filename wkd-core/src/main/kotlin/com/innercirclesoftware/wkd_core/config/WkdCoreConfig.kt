package com.innercirclesoftware.wkd_core.config

import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton
import okhttp3.OkHttpClient

@Factory
class WkdCoreConfig {

    @Singleton
    fun okHttpClient(): OkHttpClient = OkHttpClient.Builder().build()

}