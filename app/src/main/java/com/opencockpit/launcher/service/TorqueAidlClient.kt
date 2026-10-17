package com.opencockpit.launcher.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.opencockpit.launcher.telemetry.HondaPids
import com.opencockpit.launcher.telemetry.VehicleTelemetry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.prowl.torque.remote.ITorqueService

class TorqueAidlClient(private val context: Context) {
    private val TAG = "TorqueAidlClient"
    private var torqueService: ITorqueService? = null
    private var isBound = false

    private val _telemetryState = MutableStateFlow(VehicleTelemetry())
    val telemetryState: StateFlow<VehicleTelemetry> = _telemetryState.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            torqueService = ITorqueService.Stub.asInterface(service)
            isBound = true
            Log.i(TAG, "Torque AIDL Service Conectado com Sucesso")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            torqueService = null
            isBound = false
            Log.w(TAG, "Torque AIDL Service Desconectado")
        }
    }

    fun bindTorqueService() {
        val intent = Intent()
        intent.setClassName("org.prowl.torque", "org.prowl.torque.remote.TorqueService")
        try {
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao fazer bind com o Torque Pro: ${e.message}")
        }
    }

    fun unbindTorqueService() {
        if (isBound) {
            try {
                context.unbindService(serviceConnection)
                isBound = false
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao desvincular servico: ${e.message}")
            }
        }
    }

    fun pollTelemetry() {
        val service = torqueService ?: return
        try {
            val speed = service.getValueForPid(HondaPids.SPEED_PID.toLong(), true)
            val rpm = service.getValueForPid(HondaPids.RPM_PID.toLong(), true)
            val coolant = service.getValueForPid(HondaPids.COOLANT_PID.toLong(), true)
            val fuel = service.getValueForPid(HondaPids.FUEL_LEVEL_PID.toLong(), true)
            val cvtTemp = service.getValueForPid(HondaPids.HONDA_CVT_TEMP_PID.toLong(), true)

            _telemetryState.value = VehicleTelemetry(
                speedKmh = if (speed > 0) speed else 0f,
                rpm = if (rpm > 0) rpm else 0f,
                coolantTempC = coolant,
                cvtFluidTempC = cvtTemp,
                fuelLevelPct = fuel,
                isCvtOverheating = cvtTemp > 95f,
                isFuelLowAlert = fuel <= 50f
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erro lendo telemetria do AIDL: ${e.message}")
        }
    }
}
