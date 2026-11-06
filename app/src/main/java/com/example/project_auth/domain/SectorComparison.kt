package com.example.project_auth.domain

import com.example.project_auth.data.QuoteSnapshot

data class SectorSummary(
    val sector: Sector,
    /** Mean of each company's total % change; null if no company has two or more snapshots. */
    val averageChangePercent: Double?,
    val companiesWithData: Int,
    val totalCompanies: Int,
)

/** [snapshots] may mix symbols and sectors; they are grouped here. */
fun compareSectors(snapshots: List<QuoteSnapshot>): List<SectorSummary> {
    val changeBySymbol = snapshots
        .groupBy { it.symbol }
        .mapValues { (_, list) -> list.sortedBy { it.timestamp }.toPriceStats()?.totalChangePercent }

    return Sector.entries.map { sector ->
        val companies = Companies.bySector(sector)
        val changes = companies.mapNotNull { changeBySymbol[it.symbol] }
        SectorSummary(
            sector = sector,
            averageChangePercent = changes.takeIf { it.isNotEmpty() }?.average(),
            companiesWithData = changes.size,
            totalCompanies = companies.size,
        )
    }
}
