package com.example.safeschoolev2

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

/**
 * Se ejecuta una vez al abrir la app.
 * Activa la persistencia local de Firebase: los datos quedan guardados
 * en el teléfono, se pueden consultar sin conexión y lo que se escribe
 * offline se envía solo al recuperar la conexión.
 */
class SafeSchoolApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}
