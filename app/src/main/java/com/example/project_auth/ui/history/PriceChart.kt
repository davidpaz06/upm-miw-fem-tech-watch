package com.example.project_auth.ui.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/** Line chart of [prices], oldest to newest. Needs at least two points to draw a line. */
@Composable
fun PriceChart(prices: List<Double>, modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Canvas(modifier.fillMaxWidth().height(160.dp)) {
        if (prices.size < 2) return@Canvas

        val min = prices.min()
        val max = prices.max()
        // A flat series would divide by zero, so give it a nominal height.
        val range = (max - min).takeIf { it > 0 } ?: 1.0
        val stepX = size.width / (prices.size - 1)

        fun point(index: Int) = Offset(
            x = index * stepX,
            y = (size.height * (1 - (prices[index] - min) / range)).toFloat(),
        )

        listOf(0f, size.height / 2, size.height).forEach { y ->
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }

        val path = Path().apply {
            moveTo(point(0).x, point(0).y)
            for (i in 1 until prices.size) lineTo(point(i).x, point(i).y)
        }
        drawPath(path, lineColor, style = Stroke(width = 2.dp.toPx()))
    }
}
