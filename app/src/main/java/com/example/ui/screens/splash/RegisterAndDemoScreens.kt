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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: VacciTrackViewModel,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.PARENT) }
    var parentType by remember { mutableStateOf("Pai/Mãe") }
    var selectedProvince by remember { mutableStateOf("Maputo Cidade") }
    var provinceExpanded by remember { mutableStateOf(false) }
    var isTermsAccepted by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) }

    // Validation error states
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.navigateTo(AppDestination.LOGIN) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar ao Login", tint = SlateDark)
                        }

                        Column {
                            Text(
                                text = "Criar Conta Real",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateDark
                            )
                            Text(
                                text = "Registo oficial com dados autênticos",
                                fontSize = 12.sp,
                                color = SlateMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Firebase Auth Security Notice Badge
                    Surface(
                        color = EmeraldSoftBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = EmeraldDark,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "Autenticação Segura com Firebase",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark
                                )
                                Text(
                                    text = "Conta protegida no Firebase Auth com dados reais para acesso à caderneta de saúde.",
                                    fontSize = 11.sp,
                                    color = EmeraldDark.copy(alpha = 0.85f),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    if (generalError != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = HealthRedLight,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = generalError ?: "",
                                color = HealthRedDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Role selector tabs (Admins can only be added by Master Admin)
                    Text(
                        text = "Tipo de Utilizador *",
                        fontSize = 13.sp,
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
                            UserRole.PARENT to "Pai / Gestante",
                            UserRole.PROFESSIONAL to "Profissional de Saúde"
                        ).forEach { (role, label) ->
                            val isSelected = selectedRole == role
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color.White else Color.Transparent)
                                    .clickable {
                                        selectedRole = role
                                        if (role == UserRole.PROFESSIONAL) parentType = "Profissional"
                                        else parentType = "Pai/Mãe"
                                    }
                                    .padding(vertical = 8.dp),
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

                    // Informational notice about Admins
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "🔒 Contas de Administrador não podem ser criadas aqui. Devem ser adicionadas exclusivamente pelo Admin Principal (cepsgadmin@jorgito2026).",
                        fontSize = 10.sp,
                        color = SlateMedium,
                        lineHeight = 14.sp
                    )

                    if (selectedRole == UserRole.PARENT) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { parentType = "Pai/Mãe" },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (parentType == "Pai/Mãe") EmeraldSoftBg else Color.Transparent
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(if (parentType == "Pai/Mãe") EmeraldPrimary else SlateBorder)
                                )
                            ) {
                                Text("Pai / Mãe / Tutor", fontSize = 12.sp, color = if (parentType == "Pai/Mãe") EmeraldDark else SlateDark, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = { parentType = "Gestante" },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (parentType == "Gestante") HealthPinkLight else Color.Transparent
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(if (parentType == "Gestante") HealthPink else SlateBorder)
                                )
                            ) {
                                Text("Mulher Grávida", fontSize = 12.sp, color = if (parentType == "Gestante") HealthPink else SlateDark, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Full Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = null
                            generalError = null
                        },
                        label = { Text("Nome Completo *") },
                        placeholder = { Text("Ex: Manuel Joaquim Macuácua") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SlateMedium) },
                        isError = nameError != null,
                        supportingText = nameError?.let { { Text(it, color = HealthRedDark) } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_fullname_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mozambique Phone
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            phoneError = null
                            generalError = null
                        },
                        label = { Text("Telefone Moçambicano (Contacto Real) *") },
                        placeholder = { Text("Ex: +258 84 123 4567 ou 841234567") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = SlateMedium) },
                        isError = phoneError != null,
                        supportingText = phoneError?.let { { Text(it, color = HealthRedDark) } } ?: {
                            Text("Prefixo 82, 83, 84, 85, 86 ou 87 (9 dígitos)", fontSize = 11.sp, color = SlateMedium)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_phone_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                            generalError = null
                        },
                        label = { Text("Email Pessoal (Opcional)") },
                        placeholder = { Text("Ex: meuemail@exemplo.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SlateMedium) },
                        isError = emailError != null,
                        supportingText = emailError?.let { { Text(it, color = HealthRedDark) } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Province Selection
                    ExposedDropdownMenuBox(
                        expanded = provinceExpanded,
                        onExpandedChange = { provinceExpanded = !provinceExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedProvince,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Província / Região *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = provinceExpanded) },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = SlateMedium) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = provinceExpanded,
                            onDismissRequest = { provinceExpanded = false }
                        ) {
                            ValidationUtils.MOZAMBIQUE_PROVINCES.forEach { province ->
                                DropdownMenuItem(
                                    text = { Text(province) },
                                    onClick = {
                                        selectedProvince = province
                                        provinceExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                            generalError = null
                        },
                        label = { Text("Palavra-passe (Mínimo 6 caracteres) *") },
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
                            .testTag("register_password_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Confirm Password
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            confirmPasswordError = null
                            generalError = null
                        },
                        label = { Text("Confirmar Palavra-passe *") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SlateMedium) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = confirmPasswordError != null,
                        supportingText = confirmPasswordError?.let { { Text(it, color = HealthRedDark) } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Terms declaration checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = isTermsAccepted,
                            onCheckedChange = { isTermsAccepted = it },
                            colors = CheckboxDefaults.colors(checkedColor = EmeraldDark)
                        )
                        Text(
                            text = "Declaro que todos os dados fornecidos são autênticos e autorizo o registo de saúde.",
                            fontSize = 11.sp,
                            color = SlateDark,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            // Perform strict real data validation
                            val nameValidation = ValidationUtils.validateFullName(name)
                            val phoneValidation = ValidationUtils.validateMozambiquePhone(phone)
                            val emailValidation = ValidationUtils.validateEmail(email, isOptional = true)
                            val passwordValidation = ValidationUtils.validatePassword(password)

                            var hasError = false
                            if (!nameValidation.isValid) {
                                nameError = nameValidation.errorMessage
                                hasError = true
                            }
                            if (!phoneValidation.isValid) {
                                phoneError = phoneValidation.errorMessage
                                hasError = true
                            }
                            if (!emailValidation.isValid) {
                                emailError = emailValidation.errorMessage
                                hasError = true
                            }
                            if (!passwordValidation.isValid) {
                                passwordError = passwordValidation.errorMessage
                                hasError = true
                            }
                            if (password != confirmPassword) {
                                confirmPasswordError = "As palavras-passe não coincidem."
                                hasError = true
                            }
                            if (!isTermsAccepted) {
                                generalError = "Por favor confirme a declaração de dados autênticos para continuar."
                                hasError = true
                            }

                            if (hasError) return@Button

                            isSubmitting = true
                            val formattedPhone = ValidationUtils.formatMozambiquePhone(phone)

                            viewModel.registerNewAccount(
                                name = name.trim(),
                                phone = formattedPhone,
                                email = email.trim(),
                                password = password.trim(),
                                role = selectedRole,
                                parentType = parentType,
                                province = selectedProvince,
                                onSuccess = {
                                    isSubmitting = false
                                },
                                onError = { err ->
                                    isSubmitting = false
                                    generalError = err
                                }
                            )
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("create_real_account_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Registar Conta e Aceder", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Já tem conta criada? ", fontSize = 13.sp, color = SlateMedium)
                        Text(
                            text = "Fazer Login",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark,
                            modifier = Modifier.clickable { viewModel.navigateTo(AppDestination.LOGIN) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun DemoSelectorScreen(
    viewModel: VacciTrackViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Perfis do Sistema",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SlateDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Para aceder a qualquer área, utilize uma conta com dados reais",
                fontSize = 13.sp,
                color = SlateMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Profile 1: Parent
            DemoProfileCard(
                title = "Área do Responsável / Pais",
                subtitle = "Caderneta digital, calendário oficial e notificações",
                iconEmoji = "👶",
                badgeText = "Acesso Requer Conta",
                themeColor = EmeraldPrimary,
                badgeBgColor = EmeraldSoftBg,
                badgeTextColor = EmeraldDark,
                onClick = { viewModel.navigateTo(AppDestination.LOGIN) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Profile 2: Professional
            DemoProfileCard(
                title = "Profissional de Saúde",
                subtitle = "Registo de doses, consulta de histórico e sincronização offline",
                iconEmoji = "🩺",
                badgeText = "Acesso Clínico Autorizado",
                themeColor = HealthBlue,
                badgeBgColor = HealthBlueLight,
                badgeTextColor = HealthBlueDark,
                onClick = { viewModel.navigateTo(AppDestination.LOGIN) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Profile 3: Admin
            DemoProfileCard(
                title = "Administrador Sanitário",
                subtitle = "Gestão de unidades, cobertura vacinal e auditoria",
                iconEmoji = "🏛️",
                badgeText = "Gestão PTV",
                themeColor = HealthPurple,
                badgeBgColor = HealthPurpleLight,
                badgeTextColor = HealthPurpleDark,
                onClick = { viewModel.navigateTo(AppDestination.LOGIN) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.navigateTo(AppDestination.REGISTER) },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Criar Nova Conta com Dados Reais", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { viewModel.navigateTo(AppDestination.SPLASH) }) {
                Text(text = "← Voltar ao início", color = SlateMedium, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun DemoProfileCard(
    title: String,
    subtitle: String,
    iconEmoji: String,
    badgeText: String,
    themeColor: Color,
    badgeBgColor: Color,
    badgeTextColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(badgeBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(text = iconEmoji, fontSize = 28.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = badgeBgColor,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark
                )

                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = SlateMedium,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
