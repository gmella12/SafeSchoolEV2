package com.example.safeschoolev2.ui.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safeschoolev2.data.auth.Usuario
import com.example.safeschoolev2.data.model.Umbrales
import com.example.safeschoolev2.ui.componentes.BotonSecundario
import com.example.safeschoolev2.ui.componentes.FilaDato
import com.example.safeschoolev2.ui.componentes.SS
import com.example.safeschoolev2.ui.componentes.Tarjeta
import com.example.safeschoolev2.ui.componentes.TituloSeccion
import com.example.safeschoolev2.ui.panel.PanelViewModel

/**
 * Perfil del usuario. Guardia y docente ven su información y su cuenta;
 * el administrador ve además los umbrales de alerta y puede editarlos.
 */
@Composable
fun PerfilScreen(
    usuario: Usuario,
    panel: PanelViewModel,
    mensajeCuenta: String?,
    onCambiarClave: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val umbrales by panel.umbrales.collectAsState()
    var editando by remember { mutableStateOf(false) }
    var mensajeUmbrales by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize()) {
        // Encabezado con iniciales
        Row(
            Modifier.fillMaxWidth().background(Color.White).padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                Modifier.size(56.dp).clip(CircleShape).background(SS.AzulClaro),
                contentAlignment = Alignment.Center
            ) { Text(iniciales(usuario), color = SS.Azul, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            Column(Modifier.weight(1f)) {
                Text(usuario.nombre.ifBlank { "Usuario" }, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SS.Texto)
                Text(usuario.correo, fontSize = 13.sp, color = SS.Secundario)
            }
            Text(
                usuario.rol.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = if (usuario.esAdmin) SS.Azul else Color(0xFF3C4043),
                modifier = Modifier.clip(RoundedCornerShape(50))
                    .background(if (usuario.esAdmin) SS.AzulClaro else SS.Gris)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(SS.Borde))

        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TituloSeccion("Mi información")
            Tarjeta {
                FilaDato("Rol", usuario.rol.replaceFirstChar { it.uppercase() })
                FilaDato("Permisos", if (usuario.rol == "docente") "Solo consulta" else "Consulta y control")
            }

            TituloSeccion("Cuenta")
            Tarjeta {
                Text(
                    "Cambiar contraseña", fontSize = 15.sp, color = SS.Azul, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onCambiarClave).padding(vertical = 10.dp)
                )
                if (mensajeCuenta != null) Text(mensajeCuenta, fontSize = 13.sp, color = SS.Verde)
            }

            if (usuario.esAdmin) {
                TituloSeccion("Umbrales de alerta")
                Tarjeta {
                    FilaDato("Temperatura máxima", "${umbrales.temperaturaMax} °C")
                    FilaDato("Humo y gases (MQ-2)", "${umbrales.gasMax.toInt()} ppm")
                    FilaDato("Horario permitido", "${umbrales.horarioInicio} – ${umbrales.horarioFin}")
                    Text(
                        "Editar umbrales", fontSize = 15.sp, color = SS.Azul, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth().clickable { editando = true }.padding(vertical = 10.dp)
                    )
                    if (mensajeUmbrales != null) Text(mensajeUmbrales!!, fontSize = 13.sp, color = SS.Verde)
                }
            }

            BotonSecundario(
                "Cerrar sesión", onClick = onCerrarSesion,
                color = SS.Rojo, colorBorde = Color(0xFFF4C7C3),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    if (editando) {
        DialogoUmbrales(
            actuales = umbrales,
            onCancelar = { editando = false },
            onGuardar = { nuevos ->
                editando = false
                panel.guardarUmbrales(nuevos) { mensajeUmbrales = it }
            }
        )
    }
}

private fun iniciales(usuario: Usuario): String =
    usuario.nombre.split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { usuario.correo.take(2).uppercase() }

@Composable
private fun DialogoUmbrales(actuales: Umbrales, onCancelar: () -> Unit, onGuardar: (Umbrales) -> Unit) {
    var temp by remember { mutableStateOf(actuales.temperaturaMax.toString()) }
    var gas by remember { mutableStateOf(actuales.gasMax.toInt().toString()) }
    var inicio by remember { mutableStateOf(actuales.horarioInicio) }
    var fin by remember { mutableStateOf(actuales.horarioFin) }
    var error by remember { mutableStateOf<String?>(null) }
    val hora = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Umbrales de alerta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(temp, { temp = it }, label = { Text("Temperatura máxima (°C)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(gas, { gas = it }, label = { Text("Humo y gases máximo (ppm)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(inicio, { inicio = it }, label = { Text("Inicio horario (HH:mm)") }, singleLine = true)
                OutlinedTextField(fin, { fin = it }, label = { Text("Fin horario (HH:mm)") }, singleLine = true)
                if (error != null) Text(error!!, color = SS.Rojo, fontSize = 13.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val t = temp.replace(',', '.').toDoubleOrNull()
                val g = gas.toDoubleOrNull()
                error = when {
                    t == null || t !in 0.0..80.0 -> "Temperatura entre 0 y 80 °C"
                    g == null || g !in 50.0..2000.0 -> "Gas entre 50 y 2000 ppm"
                    !hora.matches(inicio.trim()) || !hora.matches(fin.trim()) -> "Usa el formato HH:mm"
                    else -> null
                }
                if (error == null) onGuardar(Umbrales(t!!, g!!, inicio.trim(), fin.trim()))
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}
