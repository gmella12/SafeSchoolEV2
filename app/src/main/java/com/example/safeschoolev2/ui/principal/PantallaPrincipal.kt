package com.example.safeschoolev2.ui.principal

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.safeschoolev2.data.auth.Roles
import com.example.safeschoolev2.data.auth.Usuario
import com.example.safeschoolev2.notificaciones.NotificacionesAlertas
import com.example.safeschoolev2.ui.SesionViewModel
import com.example.safeschoolev2.ui.alertas.AlertasScreen
import com.example.safeschoolev2.ui.componentes.Iconos
import com.example.safeschoolev2.ui.componentes.SS
import com.example.safeschoolev2.ui.detalle.DetalleScreen
import com.example.safeschoolev2.ui.inicio.InicioScreen
import com.example.safeschoolev2.ui.nodo.NodoScreen
import com.example.safeschoolev2.ui.nodo.NodoViewModel
import com.example.safeschoolev2.ui.panel.PanelViewModel
import com.example.safeschoolev2.ui.perfil.PerfilScreen
import kotlinx.coroutines.flow.catch

enum class Pestana(val titulo: String) {
    INICIO("Inicio"), ALERTAS("Alertas"), NODO("Nodo"), PERFIL("Perfil");

    val icono: ImageVector
        get() = when (this) {
            INICIO -> Iconos.Inicio
            ALERTAS -> Iconos.Alertas
            NODO -> Iconos.Nodo
            PERFIL -> Iconos.Perfil
        }
}

/**
 * Pantalla principal después del login: barra inferior con Inicio, Alertas,
 * Nodo y Perfil; el detalle de sensor se abre encima. También pide el permiso
 * de notificaciones y avisa con una notificación cada alerta nueva.
 */
@Composable
fun PantallaPrincipal(usuario: Usuario, sesion: SesionViewModel) {
    // Un ViewModel por usuario: al cambiar de cuenta se crean listeners nuevos.
    val panel: PanelViewModel = viewModel(key = "panel-${usuario.uid}")
    val nodo: NodoViewModel = viewModel(key = "nodo-${usuario.uid}")
    val context = LocalContext.current

    // Control de acceso en la interfaz: el docente solo consulta.
    val puedeActuar = usuario.rol != Roles.DOCENTE
    val pestanas = Pestana.entries.filter { it != Pestana.NODO || puedeActuar }

    var actual by rememberSaveable { mutableStateOf(Pestana.INICIO) }
    var sensorAbierto by rememberSaveable { mutableStateOf<String?>(null) }
    val conectado by panel.conectado.collectAsState()
    val alertasActivas by panel.alertasActivas.collectAsState()
    val mensajeCuenta by sesion.mensaje.collectAsState()

    BackHandler(enabled = sensorAbierto != null) { sensorAbierto = null }
    BackHandler(enabled = sensorAbierto == null && actual != Pestana.INICIO) { actual = Pestana.INICIO }

    // Permiso de notificaciones (Android 13 o superior lo pide en tiempo de ejecución).
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !NotificacionesAlertas.tienePermiso(context)) {
            pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Notifica cada alerta nueva (la primera lista recibida solo sirve de base).
    LaunchedEffect(usuario.uid) {
        var vistas: Set<String>? = null
        panel.flujoAlertas()
            .catch { /* sin permiso o sesión cerrada: no se notifica */ }
            .collect { lista ->
                val previas = vistas
                vistas = (previas ?: emptySet()) + lista.map { it.id }
                if (previas != null) {
                    val silenciados = panel.sensores.value.filter { it.silenciado }.map { it.id }.toSet()
                    lista.filter { it.id !in previas && !it.reconocida && it.sensorId !in silenciados }
                        .forEach { NotificacionesAlertas.mostrar(context, it) }
                }
            }
    }

    Scaffold(
        containerColor = SS.Fondo,
        bottomBar = {
            if (sensorAbierto == null) {
                NavigationBar(containerColor = Color.White) {
                    pestanas.forEach { p ->
                        NavigationBarItem(
                            selected = actual == p,
                            onClick = { actual = p },
                            icon = { Icon(p.icono, contentDescription = null) },
                            label = {
                                Text(if (p == Pestana.ALERTAS && alertasActivas.isNotEmpty()) "Alertas (${alertasActivas.size})" else p.titulo)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SS.Azul,
                                selectedTextColor = SS.Azul,
                                indicatorColor = SS.AzulClaro,
                                unselectedIconColor = SS.Secundario,
                                unselectedTextColor = SS.Secundario
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            val id = sensorAbierto
            if (id != null) {
                DetalleScreen(id, panel, puedeActuar, onVolver = { sensorAbierto = null })
            } else {
                when (actual) {
                    Pestana.INICIO -> InicioScreen(
                        usuario, panel,
                        onAbrirSensor = { sensorAbierto = it },
                        onVerAlertas = { actual = Pestana.ALERTAS }
                    )
                    Pestana.ALERTAS -> AlertasScreen(usuario, panel, puedeActuar, onAbrirSensor = { sensorAbierto = it })
                    Pestana.NODO -> NodoScreen(nodo, conectado)
                    Pestana.PERFIL -> PerfilScreen(
                        usuario, panel, mensajeCuenta,
                        onCambiarClave = { sesion.cambiarClave(usuario.correo) },
                        onCerrarSesion = sesion::cerrarSesion
                    )
                }
            }
        }
    }
}
