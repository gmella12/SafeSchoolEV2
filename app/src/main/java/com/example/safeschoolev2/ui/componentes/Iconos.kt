package com.example.safeschoolev2.ui.componentes

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Íconos de línea propios (los mismos trazos de los mockups).
 * Se dibujan con rutas SVG para no depender de librerías de íconos extra.
 */
object Iconos {
    val Inicio by lazy { icono("inicio", "M3 11l9-7 9 7", "M5 10v10h14V10") }
    val Alertas by lazy { icono("alertas", "M6 16v-5a6 6 0 0 1 12 0v5l2 2H4l2-2z", "M10 20a2 2 0 0 0 4 0") }
    val Nodo by lazy {
        icono(
            "nodo",
            "M10 12a2 2 0 1 0 4 0a2 2 0 1 0 -4 0",
            "M8.5 8.5a5 5 0 0 0 0 7M15.5 8.5a5 5 0 0 1 0 7M5.6 5.6a9 9 0 0 0 0 12.8M18.4 5.6a9 9 0 0 1 0 12.8"
        )
    }
    val Perfil by lazy { icono("perfil", "M8 8a4 4 0 1 0 8 0a4 4 0 1 0 -8 0", "M4 21c0-4 4-6 8-6s8 2 8 6") }
    val Volver by lazy { icono("volver", "M15 6l-6 6 6 6") }
    val Fuego by lazy { icono("fuego", "M12 3c1 4 5 5.5 5 10a5 5 0 0 1-10 0c0-2.5 1.5-4 2.5-6 .8 1.2 1.6 2 2.5 2.5 0-2.2 0-4.3 0-6.5z") }
    val Termometro by lazy { icono("termometro", "M14 14.8V5a2 2 0 0 0-4 0v9.8a4 4 0 1 0 4 0z") }
    val Pulso by lazy { icono("pulso", "M3 12h4l3-8 4 16 3-8h4") }
    val Puerta by lazy { icono("puerta", "M6 21V4a1 1 0 0 1 1-1h10a1 1 0 0 1 1 1v17", "M3 21h18") }
    val Wifi by lazy { icono("wifi", "M5 12.5a10 10 0 0 1 14 0", "M8.5 16a5 5 0 0 1 7 0", "M12 19h0.01") }
    val Aviso by lazy { icono("aviso", "M12 3l9.5 17h-19L12 3z", "M12 10v4", "M12 17h0.01") }

    private fun icono(nombre: String, vararg rutas: String): ImageVector =
        ImageVector.Builder(
            name = nombre,
            defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f
        ).apply {
            rutas.forEach { ruta ->
                addPath(
                    pathData = addPathNodes(ruta),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                )
            }
        }.build()
}
