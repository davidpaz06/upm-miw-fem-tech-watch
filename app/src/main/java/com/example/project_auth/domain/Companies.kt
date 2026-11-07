package com.example.project_auth.domain

import androidx.annotation.StringRes
import com.example.project_auth.R

enum class Sector(@StringRes val label: Int) {
    CLOUD(R.string.sector_cloud),
    AI(R.string.sector_ai),
    CONSULTING(R.string.sector_consulting),
}

data class Company(val symbol: String, val name: String, val sector: Sector)

object Companies {
    val all: List<Company> = listOf(
        Company("MSFT", "Microsoft", Sector.CLOUD),
        Company("AMZN", "Amazon", Sector.CLOUD),
        Company("GOOGL", "Alphabet", Sector.CLOUD),
        Company("ORCL", "Oracle", Sector.CLOUD),
        Company("SNOW", "Snowflake", Sector.CLOUD),

        Company("NVDA", "NVIDIA", Sector.AI),
        Company("AMD", "AMD", Sector.AI),
        Company("PLTR", "Palantir", Sector.AI),
        Company("META", "Meta", Sector.AI),

        Company("ACN", "Accenture", Sector.CONSULTING),
        Company("IBM", "IBM", Sector.CONSULTING),
        Company("CTSH", "Cognizant", Sector.CONSULTING),
        Company("INFY", "Infosys", Sector.CONSULTING),
        Company("BAH", "Booz Allen Hamilton", Sector.CONSULTING),
        Company("FCN", "FTI Consulting", Sector.CONSULTING),
        Company("GLOB", "Globant", Sector.CONSULTING),
        Company("EPAM", "EPAM Systems", Sector.CONSULTING),
    )

    fun bySector(sector: Sector): List<Company> = all.filter { it.sector == sector }

    fun find(symbol: String): Company? = all.firstOrNull { it.symbol == symbol }
}
