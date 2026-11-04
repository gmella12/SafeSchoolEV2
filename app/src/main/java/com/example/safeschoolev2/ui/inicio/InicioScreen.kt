package com.example.safeschoolev2.ui.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safeschoolev2.data.auth.Usuario
import com.example.safeschoolev2.data.model.Sensor
import com.example.safeschoolev2.ui.componentes.BotonPrincipal
import com.example.safeschoolev2.ui.componentes.ChipEstado
import com.example.safeschoolev2.ui.componentes.IconoSensor
import com.example.safeschoolev2.ui.componentes.Iconos
import com.example.safeschoolev2.ui.componentes.SS
import com.example.safeschoolev2.ui.componentes.Tarjeta
import com.example.safeschoolev2.ui.componentes.iconoTipo
import com.example.safeschoolev2.ui.componentes.nombreTipo
import com.example.safeschoolev2.ui.componentes.valorTexto
import com.example.safeschoolev2.ui.login.LogoSafeSchool
import com.example.safeschoolev2.ui.panel.PanelViewModel

/** Panel principal: aviso de alertas activas y lista de sensores en tiempo real. */
@Composable
fun InicioScreen(
    usuario: Usuario,
    panel: PanelViewModel,
    onAbrirSensor: (String) -> Unit,
    onVerAlertas: () -> Unit
) {
    val sensores by panel.sensores.collectAsState()
    val alertas by panel.alertasActivas.collectAsState()
    val conectado by panel.conectado.collectAsState()

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth().background(Color.White).padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LogoSafeSchool(tamano = 40)
                Column(Modifier.weight(1f)) {
                    Text(
                        "Hola, ${usuario.nombre.ifBlank { "usuario" }}",
                        fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SS.Texto
                    )
                    Text("Rol: ${usuario.rol}", fontSize = 13.sp, color = SS.Secundario)
                }
                IndicadorConexion(conectado)
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(SS.Borde))
        }

        if (alertas.isNotEmpty()) {
            item {
                Column(
                    Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)).background(SS.RojoClaro)
                        .border(1.dp, Color(0xFFF4C7C3), RoundedCornerShape(14.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Iconos.Aviso, contentDescription = null, tint = SS.Rojo, modifier = Modifier.size(20.dp))
                        Text(
                            if (alertas.size == 1) "1 alerta activa" else "${alertas.size} alertas activas",
                            color = SS.Rojo, fontWeight = FontWeight.Bold, fontSize = 15.sp
                        )
                    }
                    Text(
                        alertas.take(2).joinToString("\n") { "${it.mensaje} · ${it.ubicacion}" },
                        fontSize = 14.sp, color = Color(0xFF5C1A15)
                    )
                    BotonPrincipal("Ver alertas", onClick = onVerAlertas, color = SS.Rojo)
                }
            }
        }

        item {
            Row(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Text("Sensores", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SS.Texto, modifier = Modifier.weight(1f))
                Text("${sensores.size} conectados", fontSize = 12.sp, color = SS.Secundario)
            }
        }

        if (sensores.isEmpty()) {
            item {
                Text(
                    if (conectado) "No hay sensores registrados" else "Cargando sensores…",
                    color = SS.Secundario, modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }

        items(sensores, key = { it.id }) { sensor ->
            Box(Modifier.padding(horizontal = 16.dp)) {
                TarjetaSensor(sensor) { onAbrirSensor(sensor.id) }
            }
        }
    }
}

@Composable
fun IndicadorConexion(conectado: Boolean) {
    val (fg, bg) = if (conectado) SS.Verde to SS.VerdeClaro else SS.Secundario to SS.Gris
    Row(
        Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(Iconos.Wifi, contentDescription = null, tint = fg, modifier = Modifier.size(14.dp))
        Text(if (conectado) "En línea" else "Sin conexión", color = fg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TarjetaSensor(sensor: Sensor, onClick: () -> Unit) {
    val l = sensor.ultimaLectura
    val estado = if (!sensor.activo) null else l?.estado
    val chip = when {
        !sensor.activo -> "DESACTIVADO"
        l == null -> "SIN DATOS"
        else -> l.estado
    }
    val detalle = buildList {
        add(sensor.ubicacion)
        if (sensor.tipo == "DHT22" && l?.valorSecundario != null) add("Humedad ${l.valorSecundario!!.toInt()} %")
        if (sensor.silenciado) add("Silenciado")
    }.joinToString(" · ")

    Tarjeta(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconoSensor(iconoTipo(sensor.tipo), estado)
            Column(Modifier.weight(1f)) {
                Text(nombreTipo(sensor.tipo), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = SS.Texto)
                Text(detalle, fontSize = 13.sp, color = SS.Secundario)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(valorTexto(sensor.tipo, l), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SS.Texto)
                ChipEstado(chip, estado)
            }
        }
    }
}
