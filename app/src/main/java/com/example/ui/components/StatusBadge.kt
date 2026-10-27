package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VaccineStatus
import com.example.ui.theme.*

@Composable
fun VaccineStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val vaccineStatus = VaccineStatus.fromCode(status)
    val (bgColor, textColor, label) = when (vaccineStatus) {
        VaccineStatus.ADMINISTERED -> Triple(EmeraldPrimary, Color.White, "✓ Administrada")
        VaccineStatus.UPCOMING -> Triple(HealthAmber, Color.White, "⏰ Próxima")
        VaccineStatus.DELAYED -> Triple(HealthRed, Color.White, "⚠️ Em atraso")
        VaccineStatus.PENDING -> Triple(Color(0xFFE2E8F0), SlateMedium, "Pendente")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun StatusChip(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PulseDot(
    color: Color = Color(0xFFFDE047),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
}
