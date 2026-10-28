package com.example.safeschoolev2.ui.panel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safeschoolev2.data.SensorRepository
import com.example.safeschoolev2.data.model.Alerta
import com.example.safeschoolev2.data.model.LecturaSensor
import com.example.safeschoolev2.data.model.Sensor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Datos y acciones del panel de monitoreo (teléfono 2). */
class PanelViewModel(
    private val repo: SensorRepository = SensorRepository()
) : ViewModel() {

    val sensores: StateFlow<List<Sensor>> = repo.observarSensores()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val alertasActivas: StateFlow<List<Alerta>> = repo.observarAlertas()
        .map { lista -> lista.filter { !it.reconocida } }
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val historialAlertas: StateFlow<List<Alerta>> = repo.observarAlertas()
        .map { lista -> lista.filter { it.reconocida } }
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Para el gráfico de la pantalla "Detalle de sensor". */
    fun historial(sensorId: String): Flow<List<LecturaSensor>> =
        repo.observarHistorial(sensorId).catch { emit(emptyList()) }

    fun silenciar(sensorId: String, silenciado: Boolean) = viewModelScope.launch {
        runCatching { repo.silenciarSensor(sensorId, silenciado) }
    }

    fun activar(sensorId: String, activo: Boolean) = viewModelScope.launch {
        runCatching { repo.activarSensor(sensorId, activo) }
    }

    /** [usuario]: correo o uid del usuario con sesión iniciada (lo entrega el login). */
    fun reconocer(alertaId: String, usuario: String) = viewModelScope.launch {
        runCatching { repo.reconocerAlerta(alertaId, usuario) }
    }
}
