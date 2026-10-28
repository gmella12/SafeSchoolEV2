package com.example.safeschoolev2.ui.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safeschoolev2.data.auth.AuthRepository
import com.example.safeschoolev2.data.auth.SinRolException
import com.example.safeschoolev2.data.auth.Usuario
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val mostrarClave: Boolean = false,
    val cargando: Boolean = false,
    val error: String? = null,
    val info: String? = null,
    val segundosBloqueo: Int = 0
) {
    val bloqueado: Boolean get() = segundosBloqueo > 0
}

class LoginViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui.asStateFlow()

    private var intentosFallidos = 0

    fun setCorreo(v: String) = _ui.update { it.copy(correo = v, error = null, info = null) }
    fun setClave(v: String) = _ui.update { it.copy(clave = v, error = null, info = null) }
    fun alternarMostrarClave() = _ui.update { it.copy(mostrarClave = !it.mostrarClave) }

    fun iniciarSesion(onExito: (Usuario) -> Unit) {
        val s = _ui.value
        if (s.cargando || s.bloqueado) return

        validar(s.correo, s.clave)?.let { error ->
            _ui.update { it.copy(error = error) }
            return
        }

        _ui.update { it.copy(cargando = true, error = null, info = null) }
        viewModelScope.launch {
            try {
                val usuario = repo.iniciarSesion(s.correo, s.clave)
                intentosFallidos = 0
                _ui.update { LoginUiState() } // limpia el formulario (incluida la contraseña)
                onExito(usuario)
            } catch (e: Exception) {
                intentosFallidos++
                _ui.update { it.copy(cargando = false, clave = "", error = mensajeError(e)) }
                if (intentosFallidos >= MAX_INTENTOS) bloquearTemporalmente()
            }
        }
    }

    fun recuperarClave() {
        val correo = _ui.value.correo.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            _ui.update { it.copy(error = "Escribe tu correo para recuperar la contraseña") }
            return
        }
        viewModelScope.launch {
            runCatching { repo.recuperarClave(correo) }
            // Mismo mensaje exista o no la cuenta: no revela qué correos están registrados.
            _ui.update {
                it.copy(error = null, info = "Si el correo está registrado, recibirás un enlace para restablecer tu contraseña.")
            }
        }
    }

    private fun validar(correo: String, clave: String): String? = when {
        correo.isBlank() -> "Ingresa tu correo electrónico"
        !Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches() -> "El correo no tiene un formato válido"
        clave.isBlank() -> "Ingresa tu contraseña"
        clave.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
        else -> null
    }

    private fun mensajeError(e: Exception): String = when (e) {
        is SinRolException -> e.message.orEmpty()
        // Mensaje genérico: no indica si falló el correo o la contraseña.
        is FirebaseAuthInvalidCredentialsException,
        is FirebaseAuthInvalidUserException -> "Correo o contraseña incorrectos"
        is FirebaseTooManyRequestsException -> "Demasiados intentos. Intenta más tarde."
        is FirebaseNetworkException -> "Sin conexión a internet"
        else -> "No se pudo iniciar sesión. Intenta nuevamente."
    }

    /** Tras varios intentos fallidos, bloquea el botón durante unos segundos. */
    private fun bloquearTemporalmente() {
        viewModelScope.launch {
            for (seg in SEGUNDOS_BLOQUEO downTo 1) {
                _ui.update { it.copy(segundosBloqueo = seg) }
                delay(1_000)
            }
            intentosFallidos = 0
            _ui.update { it.copy(segundosBloqueo = 0) }
        }
    }

    companion object {
        private const val MAX_INTENTOS = 5
        private const val SEGUNDOS_BLOQUEO = 30
    }
}
