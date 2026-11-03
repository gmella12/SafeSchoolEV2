package com.example.safeschoolev2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.safeschoolev2.ui.EstadoSesion
import com.example.safeschoolev2.ui.SesionViewModel
import com.example.safeschoolev2.ui.login.LoginScreen
import com.example.safeschoolev2.ui.principal.PantallaPrincipal
import com.example.safeschoolev2.ui.theme.SafeSchoolEV2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SafeSchoolEV2Theme {
                AppRaiz()
            }
        }
    }
}

@Composable
fun AppRaiz(sesion: SesionViewModel = viewModel()) {
    val estado by sesion.estado.collectAsState()
    when (val e = estado) {
        EstadoSesion.Cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        EstadoSesion.SinSesion -> LoginScreen(onLoginExitoso = sesion::sesionIniciada)
        is EstadoSesion.Activa -> PantallaPrincipal(usuario = e.usuario, sesion = sesion)
    }
}
