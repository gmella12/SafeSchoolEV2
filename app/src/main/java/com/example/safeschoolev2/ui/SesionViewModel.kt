package com.example.safeschoolev2.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safeschoolev2.data.auth.AuthRepository
import com.example.safeschoolev2.data.auth.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface EstadoSesion {
    data object Cargando : EstadoSesion
    data object SinSesion : EstadoSesion
    data class Activa(val usuario: Usuario) : EstadoSesion
}

/** Decide qué mostrar al abrir la app: login o la app con la sesión guardada. */
class SesionViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoSesion>(EstadoSesion.Cargando)
    val estado: StateFlow<EstadoSesion> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            val usuario = repo.usuarioConSesion()
            _estado.value = if (usuario != null) {
                EstadoSesion.Activa(usuario)
            } else {
                repo.cerrarSesion()
                EstadoSesion.SinSesion
            }
        }
    }

    fun sesionIniciada(usuario: Usuario) {
        _estado.value = EstadoSesion.Activa(usuario)
    }

    fun cerrarSesion() {
        repo.cerrarSesion()
        _estado.value = EstadoSesion.SinSesion
    }
}
