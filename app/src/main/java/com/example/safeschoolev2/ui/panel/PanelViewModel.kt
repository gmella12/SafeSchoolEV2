package com.example.safeschoolev2.ui.panel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safeschoolev2.data.SensorRepository
import com.example.safeschoolev2.data.model.Alerta
import com.example.safeschoolev2.data.model.LecturaSensor
import com.example.safeschoolev2.data.model.Sensor
import com.example.safeschoolev2.data.model.Umbrales
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Datos y acciones del panel de monitoreo: sensores, alertas y umbrales en tiempo real. */
class PanelViewModel(
    private val repo: SensorRepository = SensorRepository()
) : ViewModel() {

    val sensores: StateFlow<List<Sensor>> = repo.observarSensores()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val alertas: StateFlow<List<Alerta>> = repo.observarAlertas()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val alertasActivas: StateFlow<List<Alerta>> = alertas
        .map { lista -> lista.filter { !it.reconocida } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val historialAlertas: StateFlow<List<Alerta>> = alertas
        .map { lista -> lista.filter { it.reconocida } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val umbrales: StateFlow<Umbrales> = repo.observarUmbrales()
        .catch { emit(Umbrales()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Umbrales())

    val conectado: StateFlow<Boolean> = repo.observarConexion()
        .catch { emit(false) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** Flujo directo de alertas: la primera lista es la base y las siguientes traen las nuevas. */
    fun flujoAlertas(): Flow<List<Alerta>> = repo.observarAlertas()

    /** Últimas lecturas de un sensor, para el gráfico del detalle. */
    fun historial(sensorId: String): Flow<List<LecturaSensor>> =
        repo.observarHistorial(sensorId).catch { emit(emptyList()) }

    fun silenciar(sensorId: String, silenciado: Boolean) = viewModelScope.launch {
        runCatching { repo.silenciarSensor(sensorId, silenciado) }
    }

    fun activar(sensorId: String, activo: Boolean) = viewModelScope.launch {
        runCatching { repo.activarSensor(sensorId, activo) }
    }

    /** [usuario]: correo del usuario con sesión iniciada (queda registrado en la alerta). */
    fun reconocer(alertaId: String, usuario: String) = viewModelScope.launch {
        runCatching { repo.reconocerAlerta(alertaId, usuario) }
    }

    /** Solo el administrador puede guardar (lo exigen también las reglas de Firebase). */
    fun guardarUmbrales(nuevos: Umbrales, onResultado: (String) -> Unit) = viewModelScope.launch {
        val r = runCatching { repo.guardarUmbrales(nuevos) }
        onResultado(if (r.isSuccess) "Umbrales guardados" else "No se pudieron guardar los umbrales")
    }
}
