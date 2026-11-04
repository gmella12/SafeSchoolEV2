package com.example.safeschoolev2.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safeschoolev2.data.model.LecturaSensor
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Colores de la app (mismos que los mockups). */
object SS {
    val Azul = Color(0xFF1A73E8)
    val AzulClaro = Color(0xFFE8F0FE)
    val Texto = Color(0xFF1F2328)
    val Secundario = Color(0xFF5F6368)
    val Fondo = Color(0xFFF5F7FA)
    val Borde = Color(0xFFE5E7EB)
    val BordeCampo = Color(0xFFD0D5DD)
    val Rojo = Color(0xFFB3261E)
    val RojoClaro = Color(0xFFFCE8E6)
    val Ambar = Color(0xFF8A5300)
    val AmbarClaro = Color(0xFFFEF3C7)
    val Verde = Color(0xFF146C2E)
    val VerdeClaro = Color(0xFFE6F4EA)
    val Gris = Color(0xFFF0F2F5)
}

/** Color de texto y de fondo según el estado de un sensor o alerta. */
fun coloresEstado(estado: String?): Pair<Color, Color> = when (estado) {
    "ALERTA" -> SS.Rojo to SS.RojoClaro
    "ADVERTENCIA" -> SS.Ambar to SS.AmbarClaro
    "NORMAL" -> SS.Verde to SS.VerdeClaro
    else -> SS.Secundario to SS.Gris
}

@Composable
fun ChipEstado(texto: String, estado: String?) {
    val (fg, bg) = coloresEstado(estado)
    Text(
        texto, color = fg, fontSize = 11.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.clip(RoundedCornerShape(50)).background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

/** Tarjeta blanca con borde gris, opcionalmente presionable. */
@Composable
fun Tarjeta(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contenido: @Composable ColumnScope.() -> Unit
) {
    val forma = RoundedCornerShape(14.dp)
    var m = modifier.fillMaxWidth().clip(forma).background(Color.White).border(1.dp, SS.Borde, forma)
    if (onClick != null) m = m.clickable(onClick = onClick)
    Column(m.padding(16.dp), content = contenido)
}

@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = SS.Azul
) {
    Button(
        onClick = onClick, enabled = enabled, shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = modifier.fillMaxWidth().height(48.dp)
    ) { Text(texto, fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
}

@Composable
fun BotonSecundario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = SS.Texto,
    colorBorde: Color = SS.BordeCampo
) {
    OutlinedButton(
        onClick = onClick, shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, colorBorde),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color),
        modifier = modifier.fillMaxWidth().height(48.dp)
    ) { Text(texto, fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
}

/** Encabezado blanco de cada pantalla. */
@Composable
fun Encabezado(
    titulo: String,
    subtitulo: String? = null,
    onVolver: (() -> Unit)? = null,
    acciones: @Composable RowScope.() -> Unit = {}
) {
    Row(
        Modifier.fillMaxWidth().background(Color.White)
            .padding(horizontal = if (onVolver != null) 8.dp else 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (onVolver != null) {
            IconButton(onClick = onVolver) { Icon(Iconos.Volver, contentDescription = "Volver", tint = SS.Texto) }
        }
        Column(Modifier.weight(1f)) {
            Text(titulo, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SS.Texto)
            if (subtitulo != null) Text(subtitulo, fontSize = 13.sp, color = SS.Secundario)
        }
        acciones()
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(SS.Borde))
}

/** Selector de dos o más opciones (pestañas tipo "Activas / Historial"). */
@Composable
fun Selector(opciones: List<String>, seleccion: Int, onSeleccion: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(SS.Gris).padding(4.dp)
    ) {
        opciones.forEachIndexed { i, texto ->
            val activa = i == seleccion
            Box(
                Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(8.dp))
                    .background(if (activa) Color.White else Color.Transparent)
                    .clickable { onSeleccion(i) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    texto, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = if (activa) SS.Texto else SS.Secundario
                )
            }
        }
    }
}

/** Fila "etiqueta ........ valor" de las secciones de información. */
@Composable
fun FilaDato(etiqueta: String, valor: String, colorValor: Color = SS.Texto) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(etiqueta, fontSize = 15.sp, color = SS.Secundario, modifier = Modifier.weight(1f))
        Text(valor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colorValor)
    }
}

@Composable
fun TituloSeccion(texto: String) {
    Text(
        texto.uppercase(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SS.Secundario,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 2.dp)
    )
}

/** Ícono de sensor dentro de un cuadrado de color. */
@Composable
fun IconoSensor(icono: ImageVector, estado: String?) {
    val (fg, bg) = when (estado) {
        "ALERTA" -> SS.Rojo to SS.RojoClaro
        "ADVERTENCIA" -> SS.Ambar to SS.AmbarClaro
        else -> SS.Azul to SS.AzulClaro
    }
    Box(
        Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(bg),
        contentAlignment = Alignment.Center
    ) { Icon(icono, contentDescription = null, tint = fg, modifier = Modifier.size(22.dp)) }
}

// ---------- Textos de sensores ----------

fun nombreTipo(tipo: String): String = when (tipo) {
    "MQ2" -> "Humo y gases · MQ-2"
    "DHT22" -> "Temperatura · DHT22"
    "PIR" -> "Movimiento · PIR"
    "MC38" -> "Apertura · MC-38"
    else -> tipo
}

fun iconoTipo(tipo: String): ImageVector = when (tipo) {
    "MQ2" -> Iconos.Fuego
    "DHT22" -> Iconos.Termometro
    "PIR" -> Iconos.Pulso
    else -> Iconos.Puerta
}

fun valorTexto(tipo: String, l: LecturaSensor?): String {
    if (l == null) return "—"
    return when (tipo) {
        "PIR" -> if (l.valor >= 1) "Movimiento" else "Sin mov."
        "MC38" -> if (l.valor >= 1) "Abierta" else "Cerrada"
        "DHT22" -> "%.1f °C".format(l.valor)
        "MQ2" -> "${l.valor.toInt()} ppm"
        else -> l.valor.toString()
    }
}

private val formatoHora = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())
private val formatoFecha = DateTimeFormatter.ofPattern("dd-MM HH:mm").withZone(ZoneId.systemDefault())

fun horaDe(ts: Long): String = if (ts <= 0) "—" else formatoHora.format(Instant.ofEpochMilli(ts))
fun fechaDe(ts: Long): String = if (ts <= 0) "—" else formatoFecha.format(Instant.ofEpochMilli(ts))
