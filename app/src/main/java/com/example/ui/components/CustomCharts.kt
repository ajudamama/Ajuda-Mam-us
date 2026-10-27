package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class ChartBarData(
    val label: String,
    val value: Float,
    val color: Color = EmeraldPrimary
)

data class DonutSliceData(
    val label: String,
    val value: Float,
    val color: Color
)

@Composable
fun SimpleBarChart(
    data: List<ChartBarData>,
    modifier: Modifier = Modifier,
    barMaxHeight: Float = 140f
) {
    val maxValue = (data.maxOfOrNull { it.value } ?: 1f).coerceAtLeast(1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { item ->
                val fraction = (item.value / maxValue).coerceIn(0.05f, 1f)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.value.toInt().toString(),
                        fontSize = 10.sp,
                        color = SlateMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .fillMaxHeight(fraction)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(item.color)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Labels Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEach { item ->
                Text(
                    text = item.label,
                    fontSize = 11.sp,
                    color = SlateMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SimpleDonutChart(
    slices: List<DonutSliceData>,
    centerText: String = "",
    centerSubtext: String = "",
    modifier: Modifier = Modifier
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(150.dp)) {
                var currentAngle = -90f
                val strokeWidth = 28.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val centerOffset = Offset(size.width / 2, size.height / 2)

                slices.forEach { slice ->
                    val sweepAngle = (slice.value / total) * 360f
                    drawArc(
                        color = slice.color,
                        startAngle = currentAngle,
                        sweepAngle = sweepAngle - 2f, // subtle gap
                        useCenter = false,
                        topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    currentAngle += sweepAngle
                }
            }

            if (centerText.isNotEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = centerText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateDark
                    )
                    if (centerSubtext.isNotEmpty()) {
                        Text(
                            text = centerSubtext,
                            fontSize = 10.sp,
                            color = SlateMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Legend
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            maxItemsInEachRow = 3
        ) {
            slices.forEach { slice ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(slice.color)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${slice.label} (${slice.value.toInt()}%)",
                        fontSize = 10.sp,
                        color = SlateDark,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleLineChart(
    labels: List<String>,
    primarySeries: List<Float>,
    secondarySeries: List<Float>? = null,
    primaryColor: Color = EmeraldPrimary,
    secondaryColor: Color = HealthRed,
    modifier: Modifier = Modifier
) {
    val maxPrimary = primarySeries.maxOrNull() ?: 1f
    val maxSecondary = secondarySeries?.maxOrNull() ?: 1f
    val globalMax = maxOf(maxPrimary, maxSecondary).coerceAtLeast(1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            val width = size.width
            val height = size.height
            val stepX = width / (labels.size - 1).coerceAtLeast(1)

            // Primary line
            if (primarySeries.size > 1) {
                val path = Path()
                val fillPath = Path()

                primarySeries.forEachIndexed { i, value ->
                    val x = i * stepX
                    val y = height - (value / globalMax * (height - 20f)) - 10f
                    if (i == 0) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, height)
                        fillPath.lineTo(x, y)
                    } else {
                        path.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                }
                fillPath.lineTo((primarySeries.size - 1) * stepX, height)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.25f), Color.Transparent)
                    )
                )
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Secondary line
            if (secondarySeries != null && secondarySeries.size > 1) {
                val pathSec = Path()
                secondarySeries.forEachIndexed { i, value ->
                    val x = i * stepX
                    val y = height - (value / globalMax * (height - 20f)) - 10f
                    if (i == 0) pathSec.moveTo(x, y) else pathSec.lineTo(x, y)
                }
                drawPath(
                    path = pathSec,
                    color = secondaryColor,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = SlateMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun CoverageProgressBar(
    percentage: Int,
    modifier: Modifier = Modifier
) {
    val barColor = when {
        percentage >= 90 -> EmeraldPrimary
        percentage >= 75 -> HealthAmber
        else -> HealthRed
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Taxa de Cobertura", fontSize = 11.sp, color = SlateMedium)
            Text(text = "$percentage%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = barColor)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(SlateBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((percentage / 100f).coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(999.dp))
                    .background(barColor)
            )
        }
    }
}
