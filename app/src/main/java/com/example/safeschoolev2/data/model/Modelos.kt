package com.example.safeschoolev2.data.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ServerValue

/** Tipos de sensores del proyecto SafeSchool. */
enum class TipoSensor { PIR, DHT22, MQ2, MC38 }

/** Estado calculado para cada lectura. */
enum class EstadoSensor { NORMAL, ADVERTENCIA, ALERTA }

/**
 * Lectura de un sensor. Es el formato JSON común para los 4 sensores.
 *
 * - PIR y MC38: valor = 1.0 (movimiento / abierta) o 0.0 (sin movimiento / cerrada).
 * - DHT22: valor = temperatura (°C), valorSecundario = humedad (%).
 * - MQ2: valor = concentración de gas (ppm).
 *
 * Todas las propiedades tienen valor por defecto: Firebase necesita
 * un constructor sin argumentos para convertir el JSON en objeto.
 */
@IgnoreExtraProperties
data class LecturaSensor(
    var sensorId: String = "",
    var tipo: String = "",
    var ubicacion: String = "",
    var valor: Double = 0.0,
    var valorSecundario: Double? = null,
    var unidad: String = "",
    var estado: String = EstadoSensor.NORMAL.name,
    var nodoId: String = "",
    var timestamp: Long = 0L
) {
    /** Mapa para escribir en Firebase; la hora la pone el servidor. */
    fun toMap(): Map<String, Any?> = mapOf(
        "sensorId" to sensorId,
        "tipo" to tipo,
        "ubicacion" to ubicacion,
        "valor" to valor,
        "valorSecundario" to valorSecundario,
        "unidad" to unidad,
        "estado" to estado,
        "nodoId" to nodoId,
        "timestamp" to ServerValue.TIMESTAMP
    )
}

/** Configuración y último estado conocido de un sensor (nodo /sensores/{id}). */
@IgnoreExtraProperties
data class Sensor(
    @get:Exclude var id: String = "",
    var tipo: String = "",
    var ubicacion: String = "",
    var activo: Boolean = true,
    var silenciado: Boolean = false,
    var ultimaLectura: LecturaSensor? = null
)

/** Alerta generada cuando una lectura sale de rango (nodo /alertas/{id}). */
@IgnoreExtraProperties
data class Alerta(
    @get:Exclude var id: String = "",
    var sensorId: String = "",
    var tipo: String = "",
    var ubicacion: String = "",
    var mensaje: String = "",
    var nivel: String = EstadoSensor.ALERTA.name,
    var timestamp: Long = 0L,
    var reconocida: Boolean = false,
    var reconocidaPor: String = ""
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "sensorId" to sensorId,
        "tipo" to tipo,
        "ubicacion" to ubicacion,
        "mensaje" to mensaje,
        "nivel" to nivel,
        "timestamp" to ServerValue.TIMESTAMP,
        "reconocida" to reconocida,
        "reconocidaPor" to reconocidaPor
    )
}

/** Umbrales configurables por el administrador (nodo /umbrales). */
@IgnoreExtraProperties
data class Umbrales(
    var temperaturaMax: Double = 30.0,
    var gasMax: Double = 300.0,
    var horarioInicio: String = "07:30",
    var horarioFin: String = "19:00"
)
