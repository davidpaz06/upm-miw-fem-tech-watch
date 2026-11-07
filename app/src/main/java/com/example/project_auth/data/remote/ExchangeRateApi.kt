package com.example.project_auth.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

/** Response of open.er-api.com: [rates] holds how many units of each currency equal 1 US dollar. */
data class ExchangeRatesResponse(
    @SerializedName("result") val result: String?,
    @SerializedName("rates") val rates: Map<String, Double>?,
)

interface ExchangeRateService {
    @GET("v6/latest/USD")
    suspend fun latestUsdRates(): ExchangeRatesResponse
}

object ExchangeRateClient {
    private const val BASE_URL = "https://open.er-api.com/"

    val service: ExchangeRateService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExchangeRateService::class.java)
    }
}
