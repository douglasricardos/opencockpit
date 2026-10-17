package com.opencockpit.launcher.telemetry

data class VehicleTelemetry(
    val speedKmh: Float = 0f,
    val rpm: Float = 0f,
    val coolantTempC: Float = 0f,
    val cvtFluidTempC: Float = 0f,
    val fuelLevelPct: Float = 100f,
    val isCvtOverheating: Boolean = false,
    val isFuelLowAlert: Boolean = false,
    val timestampMs: Long = System.currentTimeMillis()
)

object HondaPids {
    const val SPEED_PID = 0x0D
    const val RPM_PID = 0x0C
    const val COOLANT_PID = 0x05
    const val FUEL_LEVEL_PID = 0x2F
    // Custom PID Honda HCF-2 CVT Fluid Temp
    const val HONDA_CVT_TEMP_PID = 0x221E01
}
