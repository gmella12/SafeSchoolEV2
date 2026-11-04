package com.example.safeschoolev2.ui.nodo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safeschoolev2.ui.componentes.BotonPrincipal
import com.example.safeschoolev2.ui.componentes.Encabezado
import com.example.safeschoolev2.ui.componentes.IconoSensor
import com.example.safeschoolev2.ui.componentes.Iconos
import com.example.safeschoolev2.ui.componentes.SS
import com.example.safeschoolev2.ui.componentes.Tarjeta

/**
 * Modo nodo sensor: este teléfono simula los cuatro sensores y publica sus
 * lecturas en Firebase; el otro teléfono (panel) las recibe en tiempo real.
 */
@Composable
fun NodoScreen(nodo: NodoViewModel, conectado: Boolean) {
    val ui by nodo.ui.collectAsState()

    Column(Modifier.fillMaxSize()) {
        Encabezado("Modo nodo sensor", "Este dispositivo envía las lecturas al panel de monitoreo")

        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Tarjeta {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconoSensor(Iconos.Wifi, if (conectado) "NORMAL" else null)
                    Column {
                        Text(
                            if (conectado) "Conectado a Firebase" else "Sin conexión (se enviará al reconectar)",
                            fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = SS.Texto
                        )
                        Text("${NodoViewModel.NODO_ID} · Wi-Fi", fontSize = 13.sp, color = SS.Secundario)
                    }
                }
            }

            Tarjeta {
                FilaInterruptor(
                    "Movimiento (PIR)", if (ui.movimiento) "Movimiento detectado" else "Sin movimiento",
                    ui.movimiento, nodo::setMovimiento
                )
                FilaInterruptor(
                    "Puerta / ventana (MC-38)", if (ui.puertaAbierta) "Abierta" else "Cerrada",
                    ui.puertaAbierta, nodo::setPuertaAbierta
                )
            }

            Tarjeta {
                Control("Temperatura (DHT22)", "%.1f °C".format(ui.temperatura), ui.temperatura, 10f..45f, nodo::setTemperatura)
                Control("Humedad (DHT22)", "${ui.humedad.toInt()} %", ui.humedad, 0f..100f, nodo::setHumedad)
                Control(
                    "Humo y gases (MQ-2)", "${ui.gas.toInt()} ppm", ui.gas, 0f..800f, nodo::setGas,
                    colorValor = if (ui.gas > 300) SS.Rojo else SS.Texto
                )
            }

            BotonPrincipal(if (ui.enviando) "Enviando…" else "Enviar lectura", onClick = nodo::enviar, enabled = !ui.enviando)
            ui.mensaje?.let {
                Text(it, fontSize = 13.sp, color = SS.Secundario, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun FilaInterruptor(titulo: String, estado: String, valor: Boolean, onCambio: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(titulo, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = SS.Texto)
            Text(estado, fontSize = 13.sp, color = SS.Secundario)
        }
        Switch(
            checked = valor, onCheckedChange = onCambio,
            colors = SwitchDefaults.colors(checkedTrackColor = SS.Azul, checkedThumbColor = Color.White)
        )
    }
}

@Composable
private fun Control(
    titulo: String,
    texto: String,
    valor: Float,
    rango: ClosedFloatingPointRange<Float>,
    onCambio: (Float) -> Unit,
    colorValor: Color = SS.Texto
) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SS.Texto, modifier = Modifier.weight(1f))
            Text(texto, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorValor)
        }
        Slider(
            value = valor, onValueChange = onCambio, valueRange = rango,
            colors = SliderDefaults.colors(thumbColor = SS.Azul, activeTrackColor = SS.Azul)
        )
    }
}
