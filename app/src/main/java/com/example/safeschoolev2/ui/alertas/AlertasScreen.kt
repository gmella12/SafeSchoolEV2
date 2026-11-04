package com.example.safeschoolev2.ui.alertas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import com.example.safeschoolev2.data.auth.Usuario
import com.example.safeschoolev2.data.model.Alerta
import com.example.safeschoolev2.ui.componentes.BotonPrincipal
import com.example.safeschoolev2.ui.componentes.BotonSecundario
import com.example.safeschoolev2.ui.componentes.ChipEstado
import com.example.safeschoolev2.ui.componentes.Encabezado
import com.example.safeschoolev2.ui.componentes.SS
import com.example.safeschoolev2.ui.componentes.Selector
import com.example.safeschoolev2.ui.componentes.Tarjeta
import com.example.safeschoolev2.ui.componentes.fechaDe
import com.example.safeschoolev2.ui.panel.PanelViewModel

/** Alertas activas (para reconocer) e historial de alertas atendidas. */
@Composable
fun AlertasScreen(
    usuario: Usuario,
    panel: PanelViewModel,
    puedeActuar: Boolean,
    onAbrirSensor: (String) -> Unit
) {
    val activas by panel.alertasActivas.collectAsState()
    val historial by panel.historialAlertas.collectAsState()
    var pestana by rememberSaveable { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        Encabezado("Alertas")
        Box(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp)) {
            Selector(listOf("Activas (${activas.size})", "Historial"), pestana) { pestana = it }
        }
        val lista = if (pestana == 0) activas else historial

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (lista.isEmpty()) {
                item {
                    Text(
                        if (pestana == 0) "No hay alertas activas" else "Aún no hay alertas atendidas",
                        color = SS.Secundario, modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            items(lista, key = { it.id }) { alerta ->
                TarjetaAlerta(
                    alerta = alerta,
                    mostrarAcciones = pestana == 0,
                    puedeActuar = puedeActuar,
                    onReconocer = { panel.reconocer(alerta.id, usuario.correo) },
                    onVerSensor = { onAbrirSensor(alerta.sensorId) }
                )
            }
        }
    }
}

@Composable
private fun TarjetaAlerta(
    alerta: Alerta,
    mostrarAcciones: Boolean,
    puedeActuar: Boolean,
    onReconocer: () -> Unit,
    onVerSensor: () -> Unit
) {
    Tarjeta {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) { ChipEstado(alerta.nivel, alerta.nivel) }
            Text(fechaDe(alerta.timestamp), fontSize = 12.sp, color = SS.Secundario)
        }
        Text(
            alerta.mensaje, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SS.Texto,
            modifier = Modifier.padding(top = 10.dp)
        )
        Text(alerta.ubicacion, fontSize = 13.sp, color = SS.Secundario)

        if (!mostrarAcciones) {
            Text(
                "Atendida por ${alerta.reconocidaPor.ifBlank { "—" }}",
                fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SS.Verde,
                modifier = Modifier.padding(top = 6.dp)
            )
        } else {
            Row(
                Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (puedeActuar) {
                    BotonPrincipal("Reconocer", onClick = onReconocer, modifier = Modifier.weight(1f))
                }
                BotonSecundario("Ver sensor", onClick = onVerSensor, modifier = Modifier.weight(1f), color = Color(0xFF1F2328))
            }
        }
    }
}
