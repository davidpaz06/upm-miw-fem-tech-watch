package com.example.project_auth.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface FinnhubService {

    @GET("quote")
    suspend fun getQuote(@Query("symbol") symbol: String): Quote

    @GET("stock/profile2")
    suspend fun getProfile(@Query("symbol") symbol: String): CompanyProfile
}
