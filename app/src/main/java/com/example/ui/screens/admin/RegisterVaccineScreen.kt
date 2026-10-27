package com.example.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VaccineCatalogEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.VacciTrackViewModel

/**
 * Tela de formulário Composable para cadastrar ou editar uma vacina,
 * integrando diretamente com o Room Database para persistir as informações inseridas.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RegisterVaccineScreen(
    viewModel: VacciTrackViewModel,
    vaccineToEdit: VaccineCatalogEntity? = null,
    onNavigateBack: () -> Unit = {},
    onVaccineSaved: (Long) -> Unit = {}
) {
    var vaccineName by remember { mutableStateOf(vaccineToEdit?.name ?: "") }
    var targetDisease by remember { mutableStateOf(vaccineToEdit?.targetDisease ?: "") }
    var dosesCount by remember {
        val initialDoses = vaccineToEdit?.dosesCount?.filter { it.isDigit() }?.toIntOrNull() ?: 1
        mutableIntStateOf(initialDoses.coerceIn(1, 10))
    }
    var scheduledAges by remember { mutableStateOf(vaccineToEdit?.scheduledAges ?: "") }
    var administrationRoute by remember { mutableStateOf(vaccineToEdit?.route ?: "Intramuscular") }
    var description by remember { mutableStateOf(vaccineToEdit?.description ?: "") }
    var selectedEmoji by remember { mutableStateOf(vaccineToEdit?.emoji ?: "💉") }

    // Validation & Error states
    var nameError by remember { mutableStateOf(false) }
    var diseaseError by remember { mutableStateOf(false) }
    var scheduleError by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val quickAgeSuggestions = listOf(
        "Ao nascer",
        "6 semanas",
        "10 semanas",
        "14 semanas",
        "9 meses",
        "12 meses",
        "18 meses",
        "9-14 anos"
    )

    val routeOptions = listOf(
        "Intramuscular",
        "Subcutânea",
        "Intradérmica",
        "Oral"
    )

    val emojiOptions = listOf("💉", "🛡️", "👶", "🩸", "💊", "🧪", "🧬", "🌡️")

    fun validateAndSubmit() {
        nameError = vaccineName.isBlank()
        diseaseError = targetDisease.isBlank()
        scheduleError = scheduledAges.isBlank()

        if (!nameError && !diseaseError && !scheduleError) {
            isSubmitting = true
            if (vaccineToEdit != null) {
                viewModel.updateVaccineCatalogItem(
                    item = vaccineToEdit.copy(
                        name = vaccineName.trim(),
                        targetDisease = targetDisease.trim(),
                        dosesCount = "$dosesCount ${if (dosesCount > 1) "doses" else "dose"}",
                        scheduledAges = scheduledAges.trim(),
                        description = if (description.isBlank()) "Vacina do esquema de imunização actualizada pelo administrador." else description.trim(),
                        route = administrationRoute,
                        emoji = selectedEmoji
                    ),
                    onSuccess = {
                        isSubmitting = false
                        onVaccineSaved(vaccineToEdit.id)
                        onNavigateBack()
                    },
                    onError = {
                        isSubmitting = false
                    }
                )
            } else {
                viewModel.registerNewVaccine(
                    name = vaccineName,
                    targetDisease = targetDisease,
                    dosesCount = dosesCount,
                    scheduledAges = scheduledAges,
                    description = if (description.isBlank()) "Vacina do esquema de imunização cadastrada pelo administrador." else description,
                    administrationRoute = administrationRoute,
                    emoji = selectedEmoji,
                    onSuccess = { newId ->
                        isSubmitting = false
                        onVaccineSaved(newId)
                        onNavigateBack()
                    }
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (vaccineToEdit != null) "Editar Vacina do PTV" else "Cadastrar Nova Vacina",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                        Text(
                            text = "Persistência local no Room Database",
                            fontSize = 12.sp,
                            color = SlateMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = SlateDark
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            vaccineName = ""
                            targetDisease = ""
                            dosesCount = 1
                            scheduledAges = ""
                            description = ""
                            administrationRoute = "Intramuscular"
                            selectedEmoji = "💉"
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Limpar formulário",
                            tint = SlateMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = SlateBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldSoftBg),
                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(EmeraldLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = EmeraldDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Catálogo Oficial PTV / Room Database",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = EmeraldDark
                        )
                        Text(
                            text = "A vacina adicionada ficará salva permanentemente no SQLite/Room e estará disponível na caderneta dos utentes.",
                            fontSize = 11.sp,
                            color = SlateMedium,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Form Main Container Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Dados Clínicos da Vacina",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SlateDark
                    )

                    // 1. Nome da Vacina
                    OutlinedTextField(
                        value = vaccineName,
                        onValueChange = {
                            vaccineName = it
                            if (nameError) nameError = false
                        },
                        label = { Text("Nome da Vacina *") },
                        placeholder = { Text("Ex: Varicela (Catapora), PCV13, Hepatite A") },
                        leadingIcon = {
                            Icon(Icons.Default.Vaccines, contentDescription = null, tint = EmeraldDark)
                        },
                        isError = nameError,
                        supportingText = {
                            if (nameError) {
                                Text("O nome da vacina é obrigatório", color = HealthRed, fontSize = 11.sp)
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_vaccine_name")
                    )

                    // 2. Doença-Alvo / Indicação
                    OutlinedTextField(
                        value = targetDisease,
                        onValueChange = {
                            targetDisease = it
                            if (diseaseError) diseaseError = false
                        },
                        label = { Text("Doença(s) Prevenida(s) *") },
                        placeholder = { Text("Ex: Varicela e complicações bacterianas graves") },
                        leadingIcon = {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = HealthBlueDark)
                        },
                        isError = diseaseError,
                        supportingText = {
                            if (diseaseError) {
                                Text("Informe as doenças prevenidas", color = HealthRed, fontSize = 11.sp)
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_target_disease")
                    )

                    // 3. Quantidade de Doses do Esquema
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Número de Doses no Esquema",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateDark
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedIconButton(
                                onClick = { if (dosesCount > 1) dosesCount-- },
                                enabled = dosesCount > 1,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Diminuir dose")
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(EmeraldSoftBg)
                                    .border(1.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$dosesCount ${if (dosesCount == 1) "Dose Única" else "Doses"}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = EmeraldDark
                                )
                            }

                            OutlinedIconButton(
                                onClick = { if (dosesCount < 6) dosesCount++ },
                                enabled = dosesCount < 6,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Aumentar dose")
                            }
                        }
                    }

                    // 4. Faixas Etárias / Esquema Recomendado
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = scheduledAges,
                            onValueChange = {
                                scheduledAges = it
                                if (scheduleError) scheduleError = false
                            },
                            label = { Text("Idade / Esquema Recomendado *") },
                            placeholder = { Text("Ex: 12 meses, reforço aos 18 meses") },
                            leadingIcon = {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = HealthPurpleDark)
                            },
                            isError = scheduleError,
                            supportingText = {
                                if (scheduleError) {
                                    Text("Informe o esquema/faixa etária", color = HealthRed, fontSize = 11.sp)
                                } else {
                                    Text("Toque nas sugestões rápidas abaixo:", fontSize = 11.sp, color = SlateMedium)
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_scheduled_ages")
                        )

                        // Quick suggestion chips
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            quickAgeSuggestions.forEach { age ->
                                val isSelected = scheduledAges.contains(age)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) HealthPurpleLight else SlateBg)
                                        .border(
                                            1.dp,
                                            if (isSelected) HealthPurpleDark else SlateBorder,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable {
                                            scheduledAges = if (scheduledAges.isBlank()) {
                                                age
                                            } else if (scheduledAges.contains(age)) {
                                                scheduledAges
                                            } else {
                                                "$scheduledAges, $age"
                                            }
                                            scheduleError = false
                                        }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "+ $age",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) HealthPurpleDark else SlateMedium
                                    )
                                }
                            }
                        }
                    }

                    // 5. Via de Administração
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Via de Administração",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateDark
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            routeOptions.forEach { route ->
                                val isSelected = administrationRoute == route
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) EmeraldDark else SlateBg)
                                        .border(
                                            1.dp,
                                            if (isSelected) EmeraldDark else SlateBorder,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { administrationRoute = route }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = route,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else SlateDark
                                    )
                                }
                            }
                        }
                    }

                    // 6. Emoji / Ícone Identificador
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Ícone Identificador",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateDark
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            emojiOptions.forEach { emoji ->
                                val isSelected = selectedEmoji == emoji
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) EmeraldSoftBg else SlateBg)
                                        .border(
                                            2.dp,
                                            if (isSelected) EmeraldDark else SlateBorder,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedEmoji = emoji },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 20.sp)
                                }
                            }
                        }
                    }

                    // 7. Descrição e Notas Clínicas
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descrição Clínica e Observações") },
                        placeholder = { Text("Indicações, esquema de reforço, contraindicações ou recomendações do MISAU...") },
                        leadingIcon = {
                            Icon(Icons.Default.Notes, contentDescription = null, tint = SlateMedium)
                        },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_vaccine_description")
                    )
                }
            }

            // Real-time Preview Card
            AnimatedVisibility(
                visible = vaccineName.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PRÉ-VISUALIZAÇÃO NO CATÁLOGO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark,
                                letterSpacing = 1.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(EmeraldSoftBg)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Activa · Room DB",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(EmeraldSoftBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = selectedEmoji, fontSize = 24.sp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = vaccineName.ifBlank { "Nome da Vacina" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = SlateDark
                                )
                                Text(
                                    text = "Prevenção: ${targetDisease.ifBlank { "Doenças prevenidas" }}",
                                    fontSize = 11.sp,
                                    color = SlateMedium
                                )
                                Text(
                                    text = "$dosesCount dose(s) · $administrationRoute · Idade: ${scheduledAges.ifBlank { "A definir" }}",
                                    fontSize = 10.sp,
                                    color = EmeraldDark,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { validateAndSubmit() },
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_vaccine_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("A persistir no Room...", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (vaccineToEdit != null) "Guardar Alterações da Vacina" else "Cadastrar Vacina no Banco de Dados",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                OutlinedButton(
                    onClick = onNavigateBack,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SlateDark),
                    border = BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("cancel_button")
                ) {
                    Text("Cancelar", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Diálogo Composable simplificado para cadastrar nova vacina,
 * ideal para acionamento direto a partir de qualquer tela ou lista.
 */
@Composable
fun RegisterVaccineDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, disease: String, doses: Int, ages: String, desc: String, route: String, emoji: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var disease by remember { mutableStateOf("") }
    var doses by remember { mutableIntStateOf(1) }
    var ages by remember { mutableStateOf("") }
    var route by remember { mutableStateOf("Intramuscular") }
    var desc by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("💉") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = emoji, fontSize = 22.sp)
                Column {
                    Text("Nova Vacina (Room DB)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Catálogo oficial de imunização", fontSize = 11.sp, color = SlateMedium)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = false },
                    label = { Text("Nome da Vacina *") },
                    placeholder = { Text("Ex: Varicela, HPV, PCV13") },
                    isError = error && name.isBlank(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = disease,
                    onValueChange = { disease = it; error = false },
                    label = { Text("Doença(s) Prevenida(s) *") },
                    placeholder = { Text("Ex: Catapora, Câncer de Colo do Útero") },
                    isError = error && disease.isBlank(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = ages,
                        onValueChange = { ages = it; error = false },
                        label = { Text("Idade/Esquema *") },
                        placeholder = { Text("Ex: 9 meses, 12 meses") },
                        isError = error && ages.isBlank(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = "$doses",
                        onValueChange = { doses = it.toIntOrNull() ?: 1 },
                        label = { Text("Doses") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.width(72.dp)
                    )
                }

                OutlinedTextField(
                    value = route,
                    onValueChange = { route = it },
                    label = { Text("Via de Administração") },
                    placeholder = { Text("Ex: Intramuscular, Oral") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Descrição / Observações") },
                    placeholder = { Text("Notas clínicas e recomendações...") },
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && disease.isNotBlank() && ages.isNotBlank()) {
                        onSave(name, disease, doses, ages, desc, route, emoji)
                    } else {
                        error = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Salvar no Room", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = SlateMedium)
            }
        },
        shape = RoundedCornerShape(18.dp),
        containerColor = Color.White
    )
}
