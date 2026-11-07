package com.example.project_auth.data.remote

import com.google.gson.annotations.SerializedName

/** Response of `GET /quote`. Finnhub returns all zeros for an unknown symbol. */
data class Quote(
    @SerializedName("c") val current: Double,
    @SerializedName("d") val change: Double?,
    @SerializedName("dp") val changePercent: Double?,
    @SerializedName("h") val high: Double,
    @SerializedName("l") val low: Double,
    @SerializedName("o") val open: Double,
    @SerializedName("pc") val previousClose: Double,
    @SerializedName("t") val timestamp: Long,
)

/** Response of `GET /stock/profile2`. The body is empty (`{}`) for an unknown symbol. */
data class CompanyProfile(
    @SerializedName("name") val name: String?,
    @SerializedName("ticker") val ticker: String?,
    @SerializedName("logo") val logo: String?,
    @SerializedName("exchange") val exchange: String?,
    @SerializedName("finnhubIndustry") val industry: String?,
    /** In millions. The currency is not always the one in [currency]; see tradingCurrency(). */
    @SerializedName("marketCapitalization") val marketCap: Double?,
    /** Reporting currency of the company, which is not necessarily the one it trades in. */
    @SerializedName("currency") val currency: String?,
)
