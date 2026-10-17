package com.opencockpit.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencockpit.launcher.telemetry.VehicleTelemetry

@Composable
fun TelemetryFloatingCards(
    telemetry: VehicleTelemetry,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Card CVT Fluid Temp (HCF-2)
        TelemetryCard(
            title = "CVT HCF-2",
            value = String.format("%.1f °C", telemetry.cvtFluidTempC),
            subtext = if (telemetry.isCvtOverheating) "ALERTA: SOBREAQUECIMENTO (>95°C)" else "Status Normal",
            isAlert = telemetry.isCvtOverheating,
            modifier = Modifier.weight(1f)
        )

        // Card Coolant
        TelemetryCard(
            title = "Líquido de Arrefecimento",
            value = String.format("%.0f °C", telemetry.coolantTempC),
            subtext = "Motor i-VTEC 1.8L",
            isAlert = telemetry.coolantTempC > 105f,
            modifier = Modifier.weight(1f)
        )

        // Card Combustível
        TelemetryCard(
            title = "Nível do Tanque",
            value = String.format("%.0f%%", telemetry.fuelLevelPct),
            subtext = if (telemetry.isFuelLowAlert) "Gatilho Preço da Hora Ativo" else "Autonomia Estável",
            isAlert = telemetry.isFuelLowAlert,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TelemetryCard(
    title: String,
    value: String,
    subtext: String,
    isAlert: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isAlert) Color(0xFFEF4444) else Color(0x3300E5FF)
    val bgColor = if (isAlert) Color(0x33EF4444) else Color(0xD90F172A)
    val accentColor = if (isAlert) Color(0xFFEF4444) else Color(0xFF00E5FF)

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title.uppercase(),
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = value,
                color = accentColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = subtext,
                color = if (isAlert) Color(0xFFFCA5A5) else Color(0xFF64748B),
                fontSize = 10.sp
            )
        }
    }
}
