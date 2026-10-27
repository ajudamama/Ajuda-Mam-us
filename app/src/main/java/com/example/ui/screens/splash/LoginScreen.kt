package com.example.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.util.ValidationUtils
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.VacciTrackViewModel

@Composable
fun LoginScreen(
    viewModel: VacciTrackViewModel,
    modifier: Modifier = Modifier
) {
    var selectedRole by remember { mutableStateOf(UserRole.PARENT) }
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) }

    // Error and loading state
    var identifierError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var loginErrorMessage by remember { mutableStateOf<String?>(null) }
    var isAuthenticating by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(EmeraldDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text(
                                text = "Entrar no Sistema",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateDark
                            )
                            Text(
                                text = "Autenticação Segura via Firebase Auth",
                                fontSize = 12.sp,
                                color = EmeraldDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Role selector tabs
                    Text(
                        text = "Perfil de Acesso *",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            UserRole.PARENT to "Pai / Mãe",
                            UserRole.PROFESSIONAL to "Profissional",
                            UserRole.ADMIN to "Admin"
                        ).forEach { (role, label) ->
                            val isSelected = selectedRole == role
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color.White else Color.Transparent)
                                    .clickable {
                                        selectedRole = role
                                        loginErrorMessage = null
                                    }
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) EmeraldDark else SlateMedium
                                )
                            }
                        }
                    }

                    if (loginErrorMessage != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            color = HealthRedLight,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = loginErrorMessage ?: "",
                                    color = HealthRedDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "→ Criar Conta com Dados Reais agora",
                                    color = EmeraldDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { viewModel.navigateTo(AppDestination.REGISTER) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = identifier,
                        onValueChange = {
                            identifier = it
                            identifierError = null
                            loginErrorMessage = null
                        },
                        label = { Text("Telefone (+258...) ou Email") },
                        placeholder = { Text("Ex: 84 123 4567 ou email@exemplo.com") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SlateMedium) },
                        isError = identifierError != null,
                        supportingText = identifierError?.let { { Text(it, color = HealthRedDark) } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_identifier_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                            loginErrorMessage = null
                        },
                        label = { Text("Palavra-passe") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SlateMedium) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Alternar visibilidade",
                                    tint = SlateMedium
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = passwordError != null,
                        supportingText = passwordError?.let { { Text(it, color = HealthRedDark) } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(checkedColor = EmeraldDark)
                            )
                            Text(text = "Lembrar sessão", fontSize = 12.sp, color = SlateMedium)
                        }
                        TextButton(onClick = { viewModel.showMessage("Para repor o acesso, contacte o seu centro de saúde ou registe uma nova conta.") }) {
                            Text(text = "Esqueci a senha", fontSize = 12.sp, color = EmeraldDark, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val cleanId = identifier.trim()
                            val cleanPass = password.trim()

                            var hasErr = false
                            if (cleanId.isBlank()) {
                                identifierError = "Insira o seu contacto registado (telefone ou email)."
                                hasErr = true
                            }
                            if (cleanPass.isBlank()) {
                                passwordError = "Insira a palavra-passe da sua conta."
                                hasErr = true
                            }

                            if (hasErr) return@Button

                            isAuthenticating = true
                            loginErrorMessage = null

                            viewModel.loginWithCredentials(
                                identifier = cleanId,
                                password = cleanPass,
                                role = selectedRole,
                                onSuccess = {
                                    isAuthenticating = false
                                },
                                onError = { errorMsg ->
                                    isAuthenticating = false
                                    loginErrorMessage = errorMsg
                                }
                            )
                        },
                        enabled = !isAuthenticating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_submit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isAuthenticating) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        } else {
                            Text(text = "Entrar no Sistema", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Ainda não tem conta? ", fontSize = 13.sp, color = SlateMedium)
                        Text(
                            text = "Criar conta real",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark,
                            modifier = Modifier
                                .clickable { viewModel.navigateTo(AppDestination.REGISTER) }
                                .testTag("navigate_to_register_button")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { viewModel.navigateTo(AppDestination.SPLASH) }) {
                Text(text = "← Voltar ao início", color = SlateMedium, fontSize = 13.sp)
            }
        }
    }
}
