package com.example.safeschoolev2.data

import com.example.safeschoolev2.data.model.Alerta
import com.example.safeschoolev2.data.model.EstadoSensor
import com.example.safeschoolev2.data.model.LecturaSensor
import com.example.safeschoolev2.data.model.Sensor
import com.example.safeschoolev2.data.model.Umbrales
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Única puerta de entrada a los datos de los sensores.
 *
 * Estructura en Firebase Realtime Database:
 *   /sensores/{sensorId}            configuración + última lectura
 *   /lecturas/{sensorId}/{pushId}   historial de lecturas
 *   /alertas/{pushId}               alertas generadas
 *   /umbrales                       límites configurados por el administrador
 *
 * Las pantallas nunca hablan con Firebase directamente: usan esta clase.
 */
class SensorRepository(
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    private val sensoresRef = db.getReference("sensores")
    private val lecturasRef = db.getReference("lecturas")
    private val alertasRef = db.getReference("alertas")
    private val umbralesRef = db.getReference("umbrales")

    init {
        // Mantiene estos nodos siempre copiados en el teléfono (caché local).
        sensoresRef.keepSynced(true)
        alertasRef.keepSynced(true)
        umbralesRef.keepSynced(true)
    }

    // ---------- Consultas en tiempo real (panel de monitoreo) ----------

    /** Lista de sensores; se vuelve a emitir cada vez que cambia algo en Firebase. */
    fun observarSensores(): Flow<List<Sensor>> = observar(sensoresRef) { snap ->
        snap.children.mapNotNull { hijo ->
            hijo.getValue(Sensor::class.java)?.apply { id = hijo.key ?: "" }
        }
    }

    /** Últimas [cantidad] lecturas de un sensor, de la más antigua a la más reciente. */
    fun observarHistorial(sensorId: String, cantidad: Int = 20): Flow<List<LecturaSensor>> =
        observar(lecturasRef.child(sensorId).orderByChild("timestamp").limitToLast(cantidad)) { snap ->
            snap.children.mapNotNull { it.getValue(LecturaSensor::class.java) }
        }

    /** Últimas 50 alertas, de la más reciente a la más antigua. */
    fun observarAlertas(): Flow<List<Alerta>> =
        observar(alertasRef.orderByChild("timestamp").limitToLast(50)) { snap ->
            snap.children.mapNotNull { hijo ->
                hijo.getValue(Alerta::class.java)?.apply { id = hijo.key ?: "" }
            }.reversed()
        }

    // ---------- Escritura de lecturas (nodo sensor) ----------

    /**
     * Guarda una lectura: la agrega al historial, actualiza la última lectura
     * del sensor y, si sale de rango, crea una alerta. Todo en una sola
     * operación atómica. Devuelve el estado calculado, o null si el sensor
     * está desactivado.
     */
    suspend fun publicarLectura(lectura: LecturaSensor): EstadoSensor? {
        if (!estaActivo(lectura.sensorId)) return null

        val estado = EvaluadorEstado.evaluar(lectura, obtenerUmbrales())
        val datos = lectura.copy(estado = estado.name).toMap()
        val idLectura = lecturasRef.child(lectura.sensorId).push().key ?: return estado

        val cambios = hashMapOf<String, Any?>(
            "lecturas/${lectura.sensorId}/$idLectura" to datos,
            "sensores/${lectura.sensorId}/ultimaLectura" to datos,
            "sensores/${lectura.sensorId}/tipo" to lectura.tipo,
            "sensores/${lectura.sensorId}/ubicacion" to lectura.ubicacion
        )

        if (estado != EstadoSensor.NORMAL) {
            val idAlerta = alertasRef.push().key
            if (idAlerta != null) {
                cambios["alertas/$idAlerta"] = Alerta(
                    sensorId = lectura.sensorId,
                    tipo = lectura.tipo,
                    ubicacion = lectura.ubicacion,
                    mensaje = EvaluadorEstado.mensaje(lectura),
                    nivel = estado.name
                ).toMap()
            }
        }

        db.reference.updateChildren(cambios).esperar()
        return estado
    }

    // ---------- Acciones sobre los dispositivos (panel de monitoreo) ----------

    suspend fun silenciarSensor(sensorId: String, silenciado: Boolean) {
        sensoresRef.child(sensorId).child("silenciado").setValue(silenciado).esperar()
    }

    suspend fun activarSensor(sensorId: String, activo: Boolean) {
        sensoresRef.child(sensorId).child("activo").setValue(activo).esperar()
    }

    suspend fun reconocerAlerta(alertaId: String, usuario: String) {
        alertasRef.child(alertaId).updateChildren(
            mapOf("reconocida" to true, "reconocidaPor" to usuario)
        ).esperar()
    }

    // ---------- Umbrales (administrador) ----------

    suspend fun obtenerUmbrales(): Umbrales = try {
        umbralesRef.get().esperar().getValue(Umbrales::class.java) ?: Umbrales()
    } catch (e: Exception) {
        Umbrales() // sin conexión ni caché: valores por defecto
    }

    suspend fun guardarUmbrales(umbrales: Umbrales) {
        umbralesRef.setValue(umbrales).esperar()
    }

    // ---------- Utilidades internas ----------

    private suspend fun estaActivo(sensorId: String): Boolean = try {
        sensoresRef.child(sensorId).child("activo").get().esperar()
            .getValue(Boolean::class.java) ?: true
    } catch (e: Exception) {
        true
    }

    /** Convierte un listener de Firebase en un Flow de Kotlin. */
    private fun <T> observar(query: Query, convertir: (DataSnapshot) -> T): Flow<T> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(convertir(snapshot))
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }
}

/** Espera a que termine una tarea de Firebase sin bloquear la interfaz. */
suspend fun <T> Task<T>.esperar(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
}
