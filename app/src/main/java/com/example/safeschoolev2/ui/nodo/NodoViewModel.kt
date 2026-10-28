package com.example.safeschoolev2.ui.nodo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safeschoolev2.data.SensorRepository
import com.example.safeschoolev2.data.model.EstadoSensor
import com.example.safeschoolev2.data.model.LecturaSensor
import com.example.safeschoolev2.data.model.TipoSensor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.roundToInt

data class NodoUiState(
    val movimiento: Boolean = false,
    val puertaAbierta: Boolean = false,
    val temperatura: Float = 24f,
    val humedad: Float = 48f,
    val gas: Float = 120f,
    val enviando: Boolean = false,
    val mensaje: String? = null
)

/** Modo nodo sensor (teléfono 1): simula los 4 sensores y publica sus lecturas. */
class NodoViewModel(
    private val repo: SensorRepository = SensorRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(NodoUiState())
    val ui: StateFlow<NodoUiState> = _ui.asStateFlow()

    fun setMovimiento(v: Boolean) = _ui.update { it.copy(movimiento = v) }
    fun setPuertaAbierta(v: Boolean) = _ui.update { it.copy(puertaAbierta = v) }
    fun setTemperatura(v: Float) = _ui.update { it.copy(temperatura = v) }
    fun setHumedad(v: Float) = _ui.update { it.copy(humedad = v) }
    fun setGas(v: Float) = _ui.update { it.copy(gas = v) }

    fun enviar() {
        val s = _ui.value
        _ui.update { it.copy(enviando = true, mensaje = null) }

        val lecturas = listOf(
            LecturaSensor(
                sensorId = "pir_bodega", tipo = TipoSensor.PIR.name, ubicacion = "Bodega",
                valor = if (s.movimiento) 1.0 else 0.0, unidad = "bool", nodoId = NODO_ID
            ),
            LecturaSensor(
                sensorId = "mc38_sala101", tipo = TipoSensor.MC38.name, ubicacion = "Ventana Sala 101",
                valor = if (s.puertaAbierta) 1.0 else 0.0, unidad = "bool", nodoId = NODO_ID
            ),
            LecturaSensor(
                sensorId = "dht22_sala204", tipo = TipoSensor.DHT22.name, ubicacion = "Sala 204",
                valor = (s.temperatura * 10).roundToInt() / 10.0,
                valorSecundario = s.humedad.roundToInt().toDouble(),
                unidad = "°C", nodoId = NODO_ID
            ),
            LecturaSensor(
                sensorId = "mq2_lab01", tipo = TipoSensor.MQ2.name, ubicacion = "Laboratorio 1",
                valor = s.gas.roundToInt().toDouble(), unidad = "ppm", nodoId = NODO_ID
            )
        )

        viewModelScope.launch {
            val resultado = runCatching {
                // Si no hay conexión, Firebase guarda la lectura en el teléfono
                // y la envía después; aquí solo esperamos 5 s la confirmación.
                withTimeoutOrNull(5_000) { lecturas.map { repo.publicarLectura(it) } }
            }
            val mensaje = when {
                resultado.isFailure ->
                    "Error al enviar: ${resultado.exceptionOrNull()?.message}"
                resultado.getOrNull() == null ->
                    "Sin conexión: la lectura se enviará al recuperar la conexión"
                else -> {
                    val estados = resultado.getOrNull()!!
                    val enAlerta = estados.count { it != null && it != EstadoSensor.NORMAL }
                    val inactivos = estados.count { it == null }
                    buildString {
                        append("Lecturas enviadas")
                        if (enAlerta > 0) append(" · $enAlerta fuera de rango")
                        if (inactivos > 0) append(" · $inactivos sensor(es) desactivado(s)")
                    }
                }
            }
            _ui.update { it.copy(enviando = false, mensaje = mensaje) }
        }
    }

    companion object {
        const val NODO_ID = "nodo-01"
    }
}
