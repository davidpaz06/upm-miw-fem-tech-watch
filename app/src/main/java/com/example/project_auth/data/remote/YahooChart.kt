package com.example.project_auth.data.remote

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Response of Yahoo Finance's chart endpoint. It is an unofficial API, so every field is
 * optional and the mapping code must cope with anything being missing or null.
 */
data class YahooChartResponse(@SerializedName("chart") val chart: YahooChart?)

data class YahooChart(
    @SerializedName("result") val result: List<YahooChartResult>?,
)

data class YahooChartResult(
    @SerializedName("meta") val meta: YahooMeta?,
    /** Epoch seconds of each bar. */
    @SerializedName("timestamp") val timestamp: List<Long>?,
    @SerializedName("indicators") val indicators: YahooIndicators?,
)

data class YahooMeta(@SerializedName("currency") val currency: String?)

data class YahooIndicators(@SerializedName("quote") val quote: List<YahooSeries>?)

/** Parallel to the timestamps; an entry is null on days without a price. */
data class YahooSeries(
    @SerializedName("high") val high: List<Double?>?,
    @SerializedName("low") val low: List<Double?>?,
    @SerializedName("close") val close: List<Double?>?,
)

interface YahooChartService {
    @GET("v8/finance/chart/{symbol}")
    suspend fun chart(
        @Path("symbol") symbol: String,
        @Query("range") range: String,
        @Query("interval") interval: String,
    ): YahooChartResponse
}

object YahooChartClient {
    private const val BASE_URL = "https://query1.finance.yahoo.com/"

    // Yahoo rejects requests that do not look like a browser.
    private const val USER_AGENT = "Mozilla/5.0"

    val service: YahooChartService by lazy {
        val httpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder().header("User-Agent", USER_AGENT).build())
            }
            .build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(YahooChartService::class.java)
    }
}
