package com.example.project_auth.domain

import androidx.annotation.StringRes
import com.example.project_auth.R

enum class Sector(@StringRes val label: Int) {
    CLOUD(R.string.sector_cloud),
    AI(R.string.sector_ai),
    CONSULTING(R.string.sector_consulting),
    HARDWARE(R.string.sector_hardware),
}

data class Company(val symbol: String, val name: String, val sector: Sector)

object Companies {
    val all: List<Company> = listOf(
        Company("MSFT", "Microsoft", Sector.CLOUD),
        Company("AMZN", "Amazon", Sector.CLOUD),
        Company("GOOGL", "Alphabet", Sector.CLOUD),
        Company("ORCL", "Oracle", Sector.CLOUD),
        Company("SNOW", "Snowflake", Sector.CLOUD),
        Company("CRM", "Salesforce", Sector.CLOUD),
        Company("NOW", "ServiceNow", Sector.CLOUD),
        Company("ADBE", "Adobe", Sector.CLOUD),
        Company("WDAY", "Workday", Sector.CLOUD),
        Company("DDOG", "Datadog", Sector.CLOUD),
        Company("NET", "Cloudflare", Sector.CLOUD),
        Company("MDB", "MongoDB", Sector.CLOUD),
        Company("ZS", "Zscaler", Sector.CLOUD),
        Company("CRWD", "CrowdStrike", Sector.CLOUD),
        Company("TEAM", "Atlassian", Sector.CLOUD),
        Company("TWLO", "Twilio", Sector.CLOUD),
        Company("PANW", "Palo Alto Networks", Sector.CLOUD),
        Company("OKTA", "Okta", Sector.CLOUD),
        Company("HUBS", "HubSpot", Sector.CLOUD),
        Company("SHOP", "Shopify", Sector.CLOUD),

        Company("NVDA", "NVIDIA", Sector.AI),
        Company("AMD", "AMD", Sector.AI),
        Company("PLTR", "Palantir", Sector.AI),
        Company("META", "Meta", Sector.AI),
        Company("AVGO", "Broadcom", Sector.AI),
        Company("TSM", "TSMC", Sector.AI),
        Company("SMCI", "Super Micro Computer", Sector.AI),
        Company("ARM", "Arm Holdings", Sector.AI),
        Company("MU", "Micron Technology", Sector.AI),
        Company("INTC", "Intel", Sector.AI),
        Company("QCOM", "Qualcomm", Sector.AI),
        Company("MRVL", "Marvell Technology", Sector.AI),
        Company("AI", "C3.ai", Sector.AI),
        Company("SOUN", "SoundHound AI", Sector.AI),
        Company("PATH", "UiPath", Sector.AI),
        Company("SNPS", "Synopsys", Sector.AI),
        Company("CDNS", "Cadence Design Systems", Sector.AI),
        Company("VRT", "Vertiv", Sector.AI),
        Company("CRWV", "CoreWeave", Sector.AI),
        Company("APP", "AppLovin", Sector.AI),

        Company("ACN", "Accenture", Sector.CONSULTING),
        Company("IBM", "IBM", Sector.CONSULTING),
        Company("CTSH", "Cognizant", Sector.CONSULTING),
        Company("INFY", "Infosys", Sector.CONSULTING),
        Company("BAH", "Booz Allen Hamilton", Sector.CONSULTING),
        Company("FCN", "FTI Consulting", Sector.CONSULTING),
        Company("GLOB", "Globant", Sector.CONSULTING),
        Company("EPAM", "EPAM Systems", Sector.CONSULTING),
        Company("WIT", "Wipro", Sector.CONSULTING),
        Company("G", "Genpact", Sector.CONSULTING),
        Company("DXC", "DXC Technology", Sector.CONSULTING),
        Company("KD", "Kyndryl", Sector.CONSULTING),
        Company("IT", "Gartner", Sector.CONSULTING),
        Company("CRAI", "CRA International", Sector.CONSULTING),
        Company("HURN", "Huron Consulting", Sector.CONSULTING),
        Company("ICFI", "ICF International", Sector.CONSULTING),
        Company("EXLS", "ExlService", Sector.CONSULTING),
        Company("EXPO", "Exponent", Sector.CONSULTING),
        Company("SAIC", "Science Applications (SAIC)", Sector.CONSULTING),
        Company("CACI", "CACI International", Sector.CONSULTING),

        // Samsung is not offered by Finnhub's free plan; Google and Microsoft are already in Cloud.
        Company("AAPL", "Apple", Sector.HARDWARE),
        Company("DELL", "Dell Technologies", Sector.HARDWARE),
        Company("HPQ", "HP Inc.", Sector.HARDWARE),
        Company("SONY", "Sony", Sector.HARDWARE),
        Company("LNVGY", "Lenovo", Sector.HARDWARE),
        Company("XIACF", "Xiaomi", Sector.HARDWARE),
        Company("HPE", "Hewlett Packard Enterprise", Sector.HARDWARE),
        Company("LOGI", "Logitech", Sector.HARDWARE),
        Company("GRMN", "Garmin", Sector.HARDWARE),
        Company("NTAP", "NetApp", Sector.HARDWARE),
        Company("WDC", "Western Digital", Sector.HARDWARE),
        Company("STX", "Seagate", Sector.HARDWARE),
        Company("CSCO", "Cisco", Sector.HARDWARE),
        Company("ZBRA", "Zebra Technologies", Sector.HARDWARE),
        Company("SONO", "Sonos", Sector.HARDWARE),
        Company("CRSR", "Corsair Gaming", Sector.HARDWARE),
        Company("MSI", "Motorola Solutions", Sector.HARDWARE),
        Company("ANET", "Arista Networks", Sector.HARDWARE),
        Company("GLW", "Corning", Sector.HARDWARE),
        Company("KEYS", "Keysight Technologies", Sector.HARDWARE),
    )

    fun bySector(sector: Sector): List<Company> = all.filter { it.sector == sector }

    fun find(symbol: String): Company? = all.firstOrNull { it.symbol == symbol }
}
