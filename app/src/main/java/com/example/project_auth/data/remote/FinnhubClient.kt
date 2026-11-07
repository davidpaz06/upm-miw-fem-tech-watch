package com.example.project_auth.data.remote

import com.example.project_auth.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Sends the API key in a header so it never shows up in logged URLs. */
class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(chain.request().newBuilder().header("X-Finnhub-Token", apiKey).build())
}

object FinnhubClient {
    private const val BASE_URL = "https://finnhub.io/api/v1/"

    // The free plan allows about 60 requests per minute; keep some headroom below it.
    private const val MAX_REQUESTS_PER_MINUTE = 50
    private const val WINDOW_MILLIS = 60_000L

    val service: FinnhubService by lazy {
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(ThrottleInterceptor(SlidingWindowThrottle(MAX_REQUESTS_PER_MINUTE, WINDOW_MILLIS)))
            .addInterceptor(ApiKeyInterceptor(BuildConfig.FINNHUB_API_KEY))
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            .build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FinnhubService::class.java)
    }
}
