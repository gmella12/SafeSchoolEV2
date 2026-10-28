package com.example.safeschoolev2.data

import com.example.safeschoolev2.data.model.EstadoSensor
import com.example.safeschoolev2.data.model.LecturaSensor
import com.example.safeschoolev2.data.model.TipoSensor
import com.example.safeschoolev2.data.model.Umbrales
import java.time.LocalTime

/** Decide si una lectura es normal, advertencia o alerta según los umbrales. */
object EvaluadorEstado {

    fun evaluar(
        lectura: LecturaSensor,
        umbrales: Umbrales,
        ahora: LocalTime = LocalTime.now()
    ): EstadoSensor = when (lectura.tipo) {
        TipoSensor.MQ2.name -> when {
            lectura.valor > umbrales.gasMax -> EstadoSensor.ALERTA
            lectura.valor > umbrales.gasMax * 0.8 -> EstadoSensor.ADVERTENCIA
            else -> EstadoSensor.NORMAL
        }
        TipoSensor.DHT22.name -> when {
            lectura.valor > umbrales.temperaturaMax -> EstadoSensor.ALERTA
            lectura.valor > umbrales.temperaturaMax - 3 -> EstadoSensor.ADVERTENCIA
            else -> EstadoSensor.NORMAL
        }
        // Los PIR están en zonas restringidas: cualquier movimiento es alerta.
        TipoSensor.PIR.name ->
            if (lectura.valor >= 1.0) EstadoSensor.ALERTA else EstadoSensor.NORMAL
        // Una apertura solo es anormal fuera del horario permitido.
        TipoSensor.MC38.name ->
            if (lectura.valor >= 1.0 && !dentroDeHorario(umbrales, ahora)) EstadoSensor.ADVERTENCIA
            else EstadoSensor.NORMAL
        else -> EstadoSensor.NORMAL
    }

    fun mensaje(lectura: LecturaSensor): String = when (lectura.tipo) {
        TipoSensor.MQ2.name -> "Humo o gas detectado (${lectura.valor.toInt()} ppm)"
        TipoSensor.DHT22.name -> "Temperatura alta (${"%.1f".format(lectura.valor)} °C)"
        TipoSensor.PIR.name -> "Movimiento en zona restringida"
        TipoSensor.MC38.name -> "Apertura fuera de horario"
        else -> "Lectura fuera de rango"
    }

    private fun dentroDeHorario(u: Umbrales, ahora: LocalTime): Boolean = try {
        val inicio = LocalTime.parse(u.horarioInicio)
        val fin = LocalTime.parse(u.horarioFin)
        !ahora.isBefore(inicio) && !ahora.isAfter(fin)
    } catch (e: Exception) {
        true
    }
}
