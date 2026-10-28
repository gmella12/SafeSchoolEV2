package com.example.safeschoolev2.data.auth

import com.example.safeschoolev2.data.esperar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.Exclude
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.IgnoreExtraProperties

/** Roles válidos del sistema. */
object Roles {
    const val ADMIN = "admin"
    const val GUARDIA = "guardia"
    const val DOCENTE = "docente"
    val TODOS = setOf(ADMIN, GUARDIA, DOCENTE)
}

/** Datos del usuario guardados en /usuarios/{uid}. La contraseña NUNCA se guarda aquí. */
@IgnoreExtraProperties
data class Usuario(
    @get:Exclude var uid: String = "",
    var nombre: String = "",
    var correo: String = "",
    var rol: String = ""
) {
    @get:Exclude val esAdmin: Boolean get() = rol == Roles.ADMIN
}

/** La cuenta existe en Firebase Authentication, pero el administrador no le asignó un rol. */
class SinRolException : Exception("Tu cuenta no tiene un rol asignado. Contacta al administrador.")

/**
 * Autenticación con Firebase Authentication (correo y contraseña).
 *
 * - Firebase guarda y valida las credenciales: la contraseña se almacena
 *   cifrada (hash) en sus servidores y nunca pasa por nuestra base de datos.
 * - El rol de cada usuario se lee desde Realtime Database (/usuarios/{uid}).
 * - La sesión queda guardada en el teléfono con un token, así el usuario
 *   no tiene que ingresar sus datos cada vez que abre la app.
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    private val usuariosRef = db.getReference("usuarios")

    /** Valida correo y contraseña; si son correctos, devuelve el usuario con su rol. */
    suspend fun iniciarSesion(correo: String, clave: String): Usuario {
        val resultado = auth.signInWithEmailAndPassword(correo.trim(), clave).esperar()
        val uid = resultado.user?.uid ?: throw IllegalStateException("No se pudo iniciar sesión")
        return obtenerUsuario(uid) ?: run {
            auth.signOut() // sin rol no se permite el ingreso
            throw SinRolException()
        }
    }

    /** Usuario con sesión guardada en el teléfono, o null si debe iniciar sesión. */
    suspend fun usuarioConSesion(): Usuario? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            obtenerUsuario(uid)
        } catch (e: Exception) {
            null
        }
    }

    /** Envía el correo para restablecer la contraseña (lo gestiona Firebase). */
    suspend fun recuperarClave(correo: String) {
        auth.sendPasswordResetEmail(correo.trim()).esperar()
    }

    fun cerrarSesion() = auth.signOut()

    private suspend fun obtenerUsuario(uid: String): Usuario? {
        val ref = usuariosRef.child(uid)
        ref.keepSynced(true)
        val usuario = ref.get().esperar().getValue(Usuario::class.java) ?: return null
        if (usuario.rol !in Roles.TODOS) return null
        return usuario.apply {
            this.uid = uid
            if (correo.isBlank()) correo = auth.currentUser?.email.orEmpty()
        }
    }
}
