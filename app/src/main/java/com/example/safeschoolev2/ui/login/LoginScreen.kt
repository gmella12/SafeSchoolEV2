package com.example.safeschoolev2.ui.login

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.safeschoolev2.data.auth.Usuario

private val Azul = Color(0xFF1A73E8)
private val TextoPrincipal = Color(0xFF1F2328)
private val TextoSecundario = Color(0xFF5F6368)
private val Borde = Color(0xFFD0D5DD)
private val FondoCampo = Color(0xFFF8F9FB)
private val Rojo = Color(0xFFB3261E)
private val Verde = Color(0xFF146C2E)

/** Pantalla de inicio de sesión (diseño según el mockup "1 · Login"). */
@Composable
fun LoginScreen(
    onLoginExitoso: (Usuario) -> Unit,
    vm: LoginViewModel = viewModel()
) {
    val ui by vm.ui.collectAsState()
    val foco = LocalFocusManager.current
    val habilitado = !ui.cargando && !ui.bloqueado

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        LogoSafeSchool()
        Spacer(Modifier.height(14.dp))
        Text("SafeSchool", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal)
        Text(
            "Monitoreo de seguridad en tiempo real para tu establecimiento",
            fontSize = 14.sp, color = TextoSecundario, textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp)
        )

        Spacer(Modifier.height(36.dp))

        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Etiqueta("Correo electrónico")
            OutlinedTextField(
                value = ui.correo,
                onValueChange = vm::setCorreo,
                placeholder = { Text("nombre@colegio.cl") },
                singleLine = true,
                enabled = habilitado,
                shape = RoundedCornerShape(10.dp),
                colors = coloresCampo(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))
            Etiqueta("Contraseña")
            OutlinedTextField(
                value = ui.clave,
                onValueChange = vm::setClave,
                placeholder = { Text("Ingresa tu contraseña") },
                singleLine = true,
                enabled = habilitado,
                shape = RoundedCornerShape(10.dp),
                colors = coloresCampo(),
                visualTransformation = if (ui.mostrarClave) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = vm::alternarMostrarClave) {
                        Text(if (ui.mostrarClave) "Ocultar" else "Mostrar", color = Azul, fontSize = 13.sp)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    foco.clearFocus()
                    vm.iniciarSesion(onLoginExitoso)
                }),
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(
                onClick = vm::recuperarClave,
                enabled = !ui.cargando,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("¿Olvidaste tu contraseña?", color = Azul, fontSize = 13.sp)
            }
        }

        ui.error?.let {
            Text(it, color = Rojo, fontSize = 14.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp))
        }
        ui.info?.let {
            Text(it, color = Verde, fontSize = 14.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp))
        }

        Button(
            onClick = {
                foco.clearFocus()
                vm.iniciarSesion(onLoginExitoso)
            },
            enabled = habilitado,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Azul),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            when {
                ui.cargando -> CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                ui.bloqueado -> Text("Espera ${ui.segundosBloqueo} s", fontSize = 16.sp)
                else -> Text("Iniciar sesión", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Text(
            "¿Problemas para ingresar?\nContacta al administrador",
            fontSize = 13.sp, color = TextoSecundario, textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 56.dp)
        )
    }
}

@Composable
private fun Etiqueta(texto: String) {
    Text(texto, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextoPrincipal)
}

@Composable
private fun coloresCampo() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = Borde,
    focusedBorderColor = Azul,
    unfocusedContainerColor = FondoCampo,
    focusedContainerColor = Color.White,
    disabledContainerColor = FondoCampo
)

/** Logo: escudo blanco con check sobre un cuadrado azul redondeado. */
@Composable
fun LogoSafeSchool(tamano: Int = 64) {
    Box(
        modifier = Modifier
            .size(tamano.dp)
            .clip(RoundedCornerShape((tamano / 4).dp))
            .background(Azul),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size((tamano * 0.55f).dp)) {
            val w = size.width
            val h = size.height
            val trazo = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            val escudo = Path().apply {
                moveTo(w * 0.5f, h * 0.06f)
                lineTo(w * 0.88f, h * 0.2f)
                lineTo(w * 0.88f, h * 0.45f)
                cubicTo(w * 0.88f, h * 0.7f, w * 0.72f, h * 0.86f, w * 0.5f, h * 0.95f)
                cubicTo(w * 0.28f, h * 0.86f, w * 0.12f, h * 0.7f, w * 0.12f, h * 0.45f)
                lineTo(w * 0.12f, h * 0.2f)
                close()
            }
            val check = Path().apply {
                moveTo(w * 0.34f, h * 0.5f)
                lineTo(w * 0.46f, h * 0.62f)
                lineTo(w * 0.67f, h * 0.4f)
            }
            drawPath(escudo, Color.White, style = trazo)
            drawPath(check, Color.White, style = trazo)
        }
    }
}
