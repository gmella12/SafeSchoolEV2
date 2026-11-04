package com.example.safeschoolev2.notificaciones

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.safeschoolev2.data.model.Alerta

/**
 * Notificaciones locales del sistema para las alertas nuevas.
 * Se generan en el teléfono cuando Firebase entrega una alerta nueva
 * mientras la app está en ejecución (no requieren servidor propio).
 */
object NotificacionesAlertas {

    private const val CANAL = "alertas_safeschool"

    /** Crea el canal de notificaciones (se llama una vez al abrir la app). */
    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL, "Alertas de seguridad", NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Humo, movimiento, temperatura o aperturas fuera de rango" }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }
    }

    /** En Android 13 o superior el usuario debe autorizar las notificaciones. */
    fun tienePermiso(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission") // el permiso se verifica en tienePermiso()
    fun mostrar(context: Context, alerta: Alerta) {
        if (!tienePermiso(context)) return

        val abrirApp = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendiente = abrirApp?.let {
            PendingIntent.getActivity(
                context, 0, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        val titulo = if (alerta.nivel == "ALERTA") "Alerta: ${alerta.ubicacion}" else "Advertencia: ${alerta.ubicacion}"

        val notificacion = NotificationCompat.Builder(context, CANAL)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(titulo)
            .setContentText(alerta.mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .apply { if (pendiente != null) setContentIntent(pendiente) }
            .build()

        NotificationManagerCompat.from(context).notify(alerta.id.hashCode(), notificacion)
    }
}
