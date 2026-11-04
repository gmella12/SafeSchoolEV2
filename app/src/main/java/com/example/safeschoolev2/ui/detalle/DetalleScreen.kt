package com.example.safeschoolev2.ui.detalle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safeschoolev2.data.model.LecturaSensor
import com.example.safeschoolev2.data.model.Sensor
import com.example.safeschoolev2.data.model.Umbrales
import com.example.safeschoolev2.ui.componentes.BotonPrincipal
import com.example.safeschoolev2.ui.componentes.BotonSecundario
import com.example.safeschoolev2.ui.componentes.ChipEstado
import com.example.safeschoolev2.ui.componentes.Encabezado
import com.example.safeschoolev2.ui.componentes.FilaDato
import com.example.safeschoolev2.ui.componentes.SS
import com.example.safeschoolev2.ui.componentes.Tarjeta
import com.example.safeschoolev2.ui.componentes.horaDe
import com.example.safeschoolev2.ui.componentes.nombreTipo
import com.example.safeschoolev2.ui.componentes.valorTexto
import com.example.safeschoolev2.ui.panel.PanelViewModel

/** Detalle de un sensor: valor actual, gráfico de lecturas y acciones sobre el dispositivo. */
@Composable
fun DetalleScreen(
    sensorId: String,
    panel: PanelViewModel,
    puedeActuar: Boolean,
    onVolver: () -> Unit
) {
    val sensores by panel.sensores.collectAsState()
    val umbrales by panel.umbrales.collectAsState()
    val historial by remember(sensorId) { panel.historial(sensorId) }.collectAsState(initial = emptyList())
    val sensor = sensores.firstOrNull { it.id == sensorId }

    Column(Modifier.fillMaxSize()) {
        Encabezado(
            titulo = sensor?.let { nombreTipo(it.tipo) } ?: "Sensor",
            subtitulo = sensor?.ubicacion,
            onVolver = onVolver
        )
        if (sensor == null) {
            Text("Cargando…", color = SS.Secundario, modifier = Modifier.padding(20.dp))
        } else {
            ContenidoDetalle(sensor, umbrales, historial, puedeActuar, panel)
        }
    }
}

@Composable
private fun ContenidoDetalle(
    sensor: Sensor,
    umbrales: Umbrales,
    historial: List<LecturaSensor>,
    puedeActuar: Boolean,
    panel: PanelViewModel
) {
    val l = sensor.ultimaLectura
    val umbral: Double? = when (sensor.tipo) {
        "MQ2" -> umbrales.gasMax
        "DHT22" -> umbrales.temperaturaMax
        else -> null
    }
    val textoUmbral = when (sensor.tipo) {
        "MQ2" -> "Umbral de alerta: ${umbrales.gasMax.toInt()} ppm"
        "DHT22" -> "Umbral de alerta: ${umbrales.temperaturaMax} °C"
        "PIR" -> "Zona restringida: cualquier movimiento genera alerta"
        else -> "Horario permitido: ${umbrales.horarioInicio} – ${umbrales.horarioFin}"
    }
    val estado = if (sensor.activo) l?.estado else null
    val chip = when {
        !sensor.activo -> "DESACTIVADO"
        l == null -> "SIN DATOS"
        sensor.silenciado -> "${l.estado} · SILENCIADO"
        else -> l.estado
    }

    Column(
        Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Tarjeta {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipEstado(chip, estado)
                Text(valorTexto(sensor.tipo, l), fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = SS.Texto)
                if (sensor.tipo == "DHT22" && l?.valorSecundario != null) {
                    Text("Humedad ${l.valorSecundario!!.toInt()} %", fontSize = 14.sp, color = SS.Secundario)
                }
                Text(textoUmbral, fontSize = 13.sp, color = SS.Secundario)
            }
        }

        Tarjeta {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Text("Últimas lecturas", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SS.Texto, modifier = Modifier.weight(1f))
                Text("${historial.size} registros", fontSize = 12.sp, color = SS.Secundario)
            }
            if (historial.isEmpty()) {
                Text("Aún no hay lecturas", color = SS.Secundario, modifier = Modifier.padding(top = 12.dp))
            } else {
                GraficoLecturas(historial, umbral, Modifier.padding(top = 12.dp))
                Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                    Text(horaDe(historial.first().timestamp), fontSize = 11.sp, color = SS.Secundario, modifier = Modifier.weight(1f))
                    Text(horaDe(historial.last().timestamp), fontSize = 11.sp, color = SS.Secundario)
                }
            }
        }

        Tarjeta {
            FilaDato("Última lectura", horaDe(l?.timestamp ?: 0))
            FilaDato("Dispositivo", l?.nodoId?.ifBlank { "—" } ?: "—")
            FilaDato(
                "Sensor",
                if (sensor.activo) "Activo" else "Desactivado",
                if (sensor.activo) SS.Verde else SS.Secundario
            )
            FilaDato("Alarma", if (sensor.silenciado) "Silenciada" else "Activa")
        }

        if (puedeActuar) {
            BotonPrincipal(
                if (sensor.silenciado) "Reactivar alarma" else "Silenciar alarma",
                onClick = { panel.silenciar(sensor.id, !sensor.silenciado) }
            )
            BotonSecundario(
                if (sensor.activo) "Desactivar sensor" else "Activar sensor",
                onClick = { panel.activar(sensor.id, !sensor.activo) }
            )
        } else {
            Text(
                "Tu rol solo permite consultar. Las acciones sobre los sensores son del guardia y del administrador.",
                fontSize = 13.sp, color = SS.Secundario
            )
        }
    }
}

/** Gráfico de barras de las últimas lecturas, con la línea del umbral. */
@Composable
private fun GraficoLecturas(lecturas: List<LecturaSensor>, umbral: Double?, modifier: Modifier = Modifier) {
    val maxValor = maxOf(lecturas.maxOf { it.valor }, umbral ?: 0.0, 1.0) * 1.15
    Canvas(modifier.fillMaxWidth().height(140.dp)) {
        val n = lecturas.size
        val paso = size.width / n
        val ancho = paso * 0.7f
        lecturas.forEachIndexed { i, lectura ->
            val alto = maxOf((lectura.valor / maxValor).toFloat() * size.height, 3.dp.toPx())
            val color = when (lectura.estado) {
                "ALERTA" -> SS.Rojo
                "ADVERTENCIA" -> Color(0xFFE0A100)
                else -> Color(0xFF8AB4F8)
            }
            drawRect(color, topLeft = Offset(i * paso + (paso - ancho) / 2, size.height - alto), size = Size(ancho, alto))
        }
        if (umbral != null) {
            val y = size.height - (umbral / maxValor).toFloat() * size.height
            drawLine(
                SS.Rojo, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
            )
        }
        drawLine(SS.Borde, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
    }
}
