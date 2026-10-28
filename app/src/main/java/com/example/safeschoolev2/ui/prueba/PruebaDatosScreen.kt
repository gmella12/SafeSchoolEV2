package com.example.safeschoolev2.ui.prueba

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.safeschoolev2.data.auth.Usuario
import com.example.safeschoolev2.data.model.Sensor
import com.example.safeschoolev2.data.model.TipoSensor
import com.example.safeschoolev2.ui.nodo.NodoViewModel
import com.example.safeschoolev2.ui.panel.PanelViewModel

/**
 * Pantalla SOLO para probar la capa de datos con dos teléfonos:
 * en uno se usa la pestaña "Nodo" y en el otro la pestaña "Panel".
 * Las pantallas definitivas (según los mockups) usarán los mismos ViewModels.
 */
@Composable
fun PruebaDatosScreen(usuario: Usuario, onCerrarSesion: () -> Unit) {
    var pestana by rememberSaveable { mutableIntStateOf(0) }
    Scaffold(
        topBar = {
            Column(Modifier.padding(top = 32.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(usuario.nombre.ifBlank { usuario.correo }, fontWeight = FontWeight.Bold)
                        Text("Rol: ${usuario.rol}", style = MaterialTheme.typography.bodySmall)
                    }
                    TextButton(onClick = onCerrarSesion) { Text("Cerrar sesión") }
                }
                TabRow(selectedTabIndex = pestana) {
                    Tab(selected = pestana == 0, onClick = { pestana = 0 }, text = { Text("Nodo sensor") })
                    Tab(selected = pestana == 1, onClick = { pestana = 1 }, text = { Text("Panel") })
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (pestana == 0) NodoPrueba() else PanelPrueba(usuario.correo)
        }
    }
}

@Composable
private fun NodoPrueba(vm: NodoViewModel = viewModel()) {
    val ui by vm.ui.collectAsState()
    Column(
        Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FilaSwitch("Movimiento (PIR)", ui.movimiento, vm::setMovimiento)
        FilaSwitch("Puerta / ventana abierta (MC-38)", ui.puertaAbierta, vm::setPuertaAbierta)

        Text("Temperatura (DHT22): ${"%.1f".format(ui.temperatura)} °C")
        Slider(value = ui.temperatura, onValueChange = vm::setTemperatura, valueRange = 10f..45f)

        Text("Humedad (DHT22): ${ui.humedad.toInt()} %")
        Slider(value = ui.humedad, onValueChange = vm::setHumedad, valueRange = 0f..100f)

        Text("Humo y gases (MQ-2): ${ui.gas.toInt()} ppm")
        Slider(value = ui.gas, onValueChange = vm::setGas, valueRange = 0f..800f)

        Button(onClick = vm::enviar, enabled = !ui.enviando, modifier = Modifier.fillMaxWidth()) {
            Text(if (ui.enviando) "Enviando..." else "Enviar lectura")
        }
        ui.mensaje?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun PanelPrueba(correoUsuario: String, vm: PanelViewModel = viewModel()) {
    val sensores by vm.sensores.collectAsState()
    val alertas by vm.alertasActivas.collectAsState()

    LazyColumn(
        Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Alertas activas: ${alertas.size}", fontWeight = FontWeight.Bold)
        }
        items(alertas, key = { it.id }) { alerta ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("${alerta.nivel} · ${alerta.mensaje}", fontWeight = FontWeight.SemiBold)
                    Text(alerta.ubicacion, style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(onClick = { vm.reconocer(alerta.id, correoUsuario) }) { Text("Reconocer") }
                }
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            Text("Sensores (${sensores.size})", fontWeight = FontWeight.Bold)
        }
        items(sensores, key = { it.id }) { sensor ->
            TarjetaSensor(sensor, onSilenciar = { vm.silenciar(sensor.id, it) })
        }
    }
}

@Composable
private fun TarjetaSensor(sensor: Sensor, onSilenciar: (Boolean) -> Unit) {
    val l = sensor.ultimaLectura
    val valor = when (sensor.tipo) {
        TipoSensor.PIR.name -> if ((l?.valor ?: 0.0) >= 1) "Movimiento" else "Sin movimiento"
        TipoSensor.MC38.name -> if ((l?.valor ?: 0.0) >= 1) "Abierta" else "Cerrada"
        TipoSensor.DHT22.name -> "${l?.valor} °C · ${l?.valorSecundario?.toInt()} %"
        else -> "${l?.valor?.toInt()} ${l?.unidad}"
    }
    val color = when (l?.estado) {
        "ALERTA" -> Color(0xFFB3261E)
        "ADVERTENCIA" -> Color(0xFF8A5300)
        else -> Color(0xFF146C2E)
    }
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("${sensor.tipo} · ${sensor.ubicacion}", fontWeight = FontWeight.SemiBold)
                Text(valor)
                Text(l?.estado ?: "SIN DATOS", color = color, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Silenciar", style = MaterialTheme.typography.labelSmall)
                Switch(checked = sensor.silenciado, onCheckedChange = onSilenciar)
            }
        }
    }
}

@Composable
private fun FilaSwitch(texto: String, valor: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(texto, Modifier.weight(1f))
        Switch(checked = valor, onCheckedChange = onChange)
    }
}
