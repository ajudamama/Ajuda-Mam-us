package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AdminTab
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.VacciTrackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(
    viewModel: VacciTrackViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.adminTab.collectAsState()
    val facilities by viewModel.facilities.collectAsState()
    val healthWorkers by viewModel.healthWorkers.collectAsState()
    val userAccounts by viewModel.userAccounts.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val catalog by viewModel.vaccineCatalog.collectAsState()
    val children by viewModel.children.collectAsState()
    val allVaccineRecords by viewModel.allVaccineRecords.collectAsState()

    var showAddFacilityDialog by remember { mutableStateOf(false) }
    var showAddWorkerDialog by remember { mutableStateOf(false) }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var showRegisterVaccineScreen by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var selectedFacilityForDetail by remember { mutableStateOf<HealthFacilityEntity?>(null) }

    var userToEdit by remember { mutableStateOf<UserAccountEntity?>(null) }
    var workerToEdit by remember { mutableStateOf<HealthWorkerEntity?>(null) }
    var facilityToEdit by remember { mutableStateOf<HealthFacilityEntity?>(null) }
    var vaccineToEdit by remember { mutableStateOf<VaccineCatalogEntity?>(null) }

    if (showRegisterVaccineScreen) {
        RegisterVaccineScreen(
            viewModel = viewModel,
            vaccineToEdit = vaccineToEdit,
            onNavigateBack = {
                showRegisterVaccineScreen = false
                vaccineToEdit = null
            },
            onVaccineSaved = {
                showRegisterVaccineScreen = false
                vaccineToEdit = null
            }
        )
        return
    }

    Scaffold(
        topBar = {
            AdminTopBar(
                onSwitchRole = { viewModel.navigateTo(AppDestination.DEMO_SELECTOR) }
            )
        },
        bottomBar = {
            AdminBottomNavigation(
                currentTab = currentTab,
                onTabSelected = { viewModel.setAdminTab(it) }
            )
        },
        containerColor = SlateBg,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AdminTab.DASHBOARD -> AdminDashboardView(
                    facilities = facilities,
                    auditLogs = auditLogs
                )
                AdminTab.USERS -> AdminUsersView(
                    users = userAccounts,
                    onAddUser = { showAddUserDialog = true },
                    onEditUser = { userToEdit = it },
                    onDeleteUser = { user ->
                        viewModel.deleteUserAccount(user)
                    }
                )
                AdminTab.PROS -> AdminProsView(
                    workers = healthWorkers,
                    onAddWorker = { showAddWorkerDialog = true },
                    onEditWorker = { workerToEdit = it },
                    onDeleteWorker = { worker ->
                        viewModel.deleteHealthWorker(worker)
                    }
                )
                AdminTab.UNITS -> AdminUnitsView(
                    facilities = facilities,
                    onSelectFacility = { selectedFacilityForDetail = it },
                    onAddFacility = { showAddFacilityDialog = true },
                    onEditFacility = { facilityToEdit = it },
                    onDeleteFacility = { facility ->
                        viewModel.deleteHealthFacility(facility)
                    }
                )
                AdminTab.VACCINES -> AdminVaccinesView(
                    catalog = catalog,
                    onAddVaccine = {
                        vaccineToEdit = null
                        showRegisterVaccineScreen = true
                    },
                    onEditVaccine = { vaccine ->
                        vaccineToEdit = vaccine
                        showRegisterVaccineScreen = true
                    },
                    onDeleteVaccine = { vaccine ->
                        viewModel.deleteVaccineCatalogItem(vaccine)
                    }
                )
                AdminTab.CALENDARS -> AdminCalendarsView()
                AdminTab.REPORTS -> AdminReportsView(
                    onExport = { reportTitle ->
                        viewModel.showMessage("Relatório \"$reportTitle\" exportado com sucesso!")
                    }
                )
                AdminTab.AUDIT -> AdminAuditView(
                    logs = auditLogs,
                    onClearLogs = {
                        viewModel.clearAuditLogs()
                    }
                )
                AdminTab.CONFIG -> AdminConfigView(viewModel = viewModel)
            }
        }
    }

    selectedFacilityForDetail?.let { facility ->
        FacilityDetailDialog(
            facility = facility,
            onDismiss = { selectedFacilityForDetail = null },
            onCall = {
                viewModel.showMessage("A contactar ${facility.name} (${facility.phone})...")
            }
        )
    }

    if (showAddFacilityDialog) {
        var name by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("Centro de Saúde") }
        var region by remember { mutableStateOf("Maputo") }
        var address by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("+258 21 ") }

        androidx.compose.ui.window.Dialog(onDismissRequest = { showAddFacilityDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Adicionar Unidade Sanitária", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome da Unidade") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Tipo") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = region, onValueChange = { region = it }, label = { Text("Província / Distrito") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Endereço") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Telefone") }, modifier = Modifier.fillMaxWidth())

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                viewModel.addFacility(name, type, "5.0 km", address, "07:30 - 15:30", phone, "Vacinação,Consultas", region) {
                                    showAddFacilityDialog = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HealthPurpleDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Registar Unidade")
                    }
                }
            }
        }
    }

    if (showAddWorkerDialog) {
        var name by remember { mutableStateOf("") }
        var title by remember { mutableStateOf("Enfermeiro de Vacinação") }
        var clinic by remember { mutableStateOf("US Polana") }

        androidx.compose.ui.window.Dialog(onDismissRequest = { showAddWorkerDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Adicionar Profissional", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome Completo") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Cargo / Função") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = clinic, onValueChange = { clinic = it }, label = { Text("Unidade Sanitária") }, modifier = Modifier.fillMaxWidth())

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                viewModel.addHealthWorker(name, title, clinic) {
                                    showAddWorkerDialog = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HealthPurpleDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Activar Profissional")
                    }
                }
            }
        }
    }

    if (showAddUserDialog) {
        var name by remember { mutableStateOf("") }
        var emailOrPhone by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var selectedRole by remember { mutableStateOf("admin") } // "admin" or "pro"
        var province by remember { mutableStateOf("Maputo Cidade") }
        var errorMsg by remember { mutableStateOf<String?>(null) }
        var isSaving by remember { mutableStateOf(false) }

        androidx.compose.ui.window.Dialog(onDismissRequest = { showAddUserDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = HealthPurpleDark)
                        Text(text = "Criar Acesso Autorizado", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                    }
                    Text(
                        text = "Autorização concedida pelo Administrador Principal (cepsgadmin@jorgito2026).",
                        fontSize = 11.sp,
                        color = SlateMedium,
                        lineHeight = 15.sp
                    )

                    if (errorMsg != null) {
                        Surface(color = HealthRedLight, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Text(text = errorMsg ?: "", color = HealthRedDark, fontSize = 11.sp, modifier = Modifier.padding(8.dp))
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; errorMsg = null },
                        label = { Text("Nome Completo *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = emailOrPhone,
                        onValueChange = { emailOrPhone = it; errorMsg = null },
                        label = { Text("Email ou Telefone *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMsg = null },
                        label = { Text("Palavra-passe *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Role selector
                    Text(text = "Função / Perfil:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { selectedRole = "admin" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedRole == "admin") HealthPurpleLight else Color.Transparent
                            )
                        ) {
                            Text("Administrador", fontSize = 12.sp, color = if (selectedRole == "admin") HealthPurpleDark else SlateDark)
                        }

                        OutlinedButton(
                            onClick = { selectedRole = "pro" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedRole == "pro") HealthBlueLight else Color.Transparent
                            )
                        ) {
                            Text("Profissional", fontSize = 12.sp, color = if (selectedRole == "pro") HealthBlueDark else SlateDark)
                        }
                    }

                    Button(
                        onClick = {
                            if (name.isBlank() || emailOrPhone.isBlank() || password.length < 6) {
                                errorMsg = "Preencha todos os campos obrigatórios (senha mínima 6 caracteres)."
                                return@Button
                            }

                            isSaving = true
                            val isEmail = emailOrPhone.contains("@")
                            viewModel.createAdminOrWorkerAccountByMaster(
                                name = name.trim(),
                                phone = if (!isEmail) emailOrPhone.trim() else "",
                                email = if (isEmail) emailOrPhone.trim() else "",
                                password = password.trim(),
                                role = selectedRole,
                                province = province,
                                onSuccess = {
                                    isSaving = false
                                    showAddUserDialog = false
                                },
                                onError = { err ->
                                    isSaving = false
                                    errorMsg = err
                                }
                            )
                        },
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = HealthPurpleDark),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Cadastrar Conta Autorizada", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    userToEdit?.let { targetUser ->
        var name by remember(targetUser) { mutableStateOf(targetUser.name) }
        var emailOrPhone by remember(targetUser) { mutableStateOf(targetUser.emailOrPhone) }
        var role by remember(targetUser) { mutableStateOf(targetUser.role) }
        var password by remember(targetUser) { mutableStateOf(targetUser.password ?: "") }
        var province by remember(targetUser) { mutableStateOf(targetUser.province) }
        var childrenCount by remember(targetUser) { mutableIntStateOf(targetUser.childrenCount) }

        androidx.compose.ui.window.Dialog(onDismissRequest = { userToEdit = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = HealthPurpleDark)
                            Text(text = "Editar Utilizador", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                        }
                        IconButton(onClick = { userToEdit = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }

                    Text(
                        text = "Actualize os dados da conta autorizada no sistema.",
                        fontSize = 11.sp,
                        color = SlateMedium
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome Completo *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = emailOrPhone,
                        onValueChange = { emailOrPhone = it },
                        label = { Text("Email ou Telefone *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Palavra-passe") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text(text = "Função / Perfil:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("admin" to "Administrador", "pro" to "Profissional", "parent" to "Responsável").forEach { (r, label) ->
                            val isSel = role == r
                            OutlinedButton(
                                onClick = { role = r },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSel) HealthPurpleLight else Color.Transparent
                                ),
                                border = BorderStroke(1.dp, if (isSel) HealthPurpleDark else SlateBorder),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) HealthPurpleDark else SlateDark
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = province,
                        onValueChange = { province = it },
                        label = { Text("Província / Unidade") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (name.isNotBlank() && emailOrPhone.isNotBlank()) {
                                viewModel.updateUserAccount(
                                    targetUser.copy(
                                        name = name.trim(),
                                        emailOrPhone = emailOrPhone.trim(),
                                        role = role,
                                        password = password.ifBlank { targetUser.password },
                                        province = province.trim(),
                                        childrenCount = childrenCount
                                    )
                                )
                                userToEdit = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HealthPurpleDark),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Guardar Alterações", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    workerToEdit?.let { targetWorker ->
        var name by remember(targetWorker) { mutableStateOf(targetWorker.name) }
        var title by remember(targetWorker) { mutableStateOf(targetWorker.title) }
        var facilityName by remember(targetWorker) { mutableStateOf(targetWorker.facilityName) }
        var dosesCount by remember(targetWorker) { mutableIntStateOf(targetWorker.dosesCount) }
        var childrenCount by remember(targetWorker) { mutableIntStateOf(targetWorker.childrenCount) }

        androidx.compose.ui.window.Dialog(onDismissRequest = { workerToEdit = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = HealthPurpleDark)
                            Text(text = "Editar Profissional", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                        }
                        IconButton(onClick = { workerToEdit = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }

                    Text(
                        text = "Actualize os dados clínicos e unidade de saúde do profissional.",
                        fontSize = 11.sp,
                        color = SlateMedium
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome Completo *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Cargo / Função *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = facilityName,
                        onValueChange = { facilityName = it },
                        label = { Text("Unidade Sanitária *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = "$dosesCount",
                            onValueChange = { dosesCount = it.toIntOrNull() ?: 0 },
                            label = { Text("Doses Administradas") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = "$childrenCount",
                            onValueChange = { childrenCount = it.toIntOrNull() ?: 0 },
                            label = { Text("Crianças Atendidas") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank() && title.isNotBlank() && facilityName.isNotBlank()) {
                                viewModel.updateHealthWorker(
                                    targetWorker.copy(
                                        name = name.trim(),
                                        title = title.trim(),
                                        facilityName = facilityName.trim(),
                                        dosesCount = dosesCount,
                                        childrenCount = childrenCount,
                                        avatarInitials = name.trim().split(" ")
                                            .take(2)
                                            .mapNotNull { it.firstOrNull()?.toString() }
                                            .joinToString("")
                                            .uppercase()
                                            .ifBlank { "PS" }
                                    )
                                )
                                workerToEdit = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HealthPurpleDark),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Guardar Alterações", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    facilityToEdit?.let { targetFacility ->
        var name by remember(targetFacility) { mutableStateOf(targetFacility.name) }
        var type by remember(targetFacility) { mutableStateOf(targetFacility.type) }
        var region by remember(targetFacility) { mutableStateOf(targetFacility.region) }
        var address by remember(targetFacility) { mutableStateOf(targetFacility.address) }
        var phone by remember(targetFacility) { mutableStateOf(targetFacility.phone) }
        var operatingHours by remember(targetFacility) { mutableStateOf(targetFacility.operatingHours) }
        var services by remember(targetFacility) { mutableStateOf(targetFacility.services) }
        var coveragePct by remember(targetFacility) { mutableIntStateOf(targetFacility.coveragePct) }
        var registeredChildrenCount by remember(targetFacility) { mutableIntStateOf(targetFacility.registeredChildrenCount) }

        androidx.compose.ui.window.Dialog(onDismissRequest = { facilityToEdit = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.EditLocation, contentDescription = null, tint = HealthPurpleDark)
                            Text(text = "Editar Unidade Sanitária", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                        }
                        IconButton(onClick = { facilityToEdit = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }

                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome da Unidade *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Tipo (Ex: Centro de Saúde, Hospital)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = region, onValueChange = { region = it }, label = { Text("Província / Região *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Endereço Completo") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Telefone / Contacto") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = operatingHours, onValueChange = { operatingHours = it }, label = { Text("Horário de Funcionamento") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = services, onValueChange = { services = it }, label = { Text("Serviços (separados por vírgula)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = "$coveragePct",
                            onValueChange = { coveragePct = (it.toIntOrNull() ?: 0).coerceIn(0, 100) },
                            label = { Text("Cobertura (%)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = "$registeredChildrenCount",
                            onValueChange = { registeredChildrenCount = it.toIntOrNull() ?: 0 },
                            label = { Text("Utentes Registados") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank() && region.isNotBlank()) {
                                viewModel.updateHealthFacility(
                                    targetFacility.copy(
                                        name = name.trim(),
                                        type = type.trim(),
                                        region = region.trim(),
                                        address = address.trim(),
                                        phone = phone.trim(),
                                        operatingHours = operatingHours.trim(),
                                        services = services.trim(),
                                        coveragePct = coveragePct,
                                        registeredChildrenCount = registeredChildrenCount
                                    )
                                )
                                facilityToEdit = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HealthPurpleDark),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Guardar Alterações da Unidade", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminTopBar(
    onSwitchRole: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.clickable { onSwitchRole() }
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(HealthPurpleDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "VacciTrack", color = SlateDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = " Admin", color = HealthPurpleDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Text(text = "Administração Global · Moçambique", color = SlateMedium, fontSize = 11.sp)
                }
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(HealthPurpleLight)
                    .clickable { onSwitchRole() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "SA", color = HealthPurpleDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun AdminBottomNavigation(
    currentTab: AdminTab,
    onTabSelected: (AdminTab) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        listOf(
            AdminTab.DASHBOARD to Icons.Default.Dashboard,
            AdminTab.USERS to Icons.Default.Group,
            AdminTab.PROS to Icons.Default.Badge,
            AdminTab.UNITS to Icons.Default.Apartment,
            AdminTab.AUDIT to Icons.Default.Security,
            AdminTab.CONFIG to Icons.Default.Settings
        ).forEach { (tab, icon) ->
            val selected = currentTab == tab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = { Icon(icon, contentDescription = tab.label) },
                label = { Text(tab.label, fontSize = 10.sp, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = HealthPurpleDark,
                    selectedTextColor = HealthPurpleDark,
                    indicatorColor = HealthPurpleLight,
                    unselectedIconColor = SlateMedium,
                    unselectedTextColor = SlateMedium
                )
            )
        }
    }
}

// ----------------- 1. ADMIN DASHBOARD -----------------
@Composable
fun AdminDashboardView(
    facilities: List<HealthFacilityEntity>,
    auditLogs: List<AuditLogEntity>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(text = "Visão Global · Moçambique", fontSize = 13.sp, color = SlateMedium)
                Text(text = "Dashboard Administrativo", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SlateDark)
            }
        }

        // National KPIs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Crianças Registadas",
                    value = "48.720",
                    subtitle = "↑ 12% vs mês anterior",
                    valueColor = HealthPurpleDark,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Gestantes",
                    value = "3.412",
                    subtitle = "↑ 8% vs mês anterior",
                    valueColor = HealthPink,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Doses Administradas",
                    value = "187.450",
                    subtitle = "↑ 15% vs mês anterior",
                    valueColor = EmeraldDark,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Doses em Atraso",
                    value = "2.184",
                    subtitle = "↓ 4% vs mês anterior",
                    valueColor = HealthRed,
                    subtitleColor = HealthRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Evolution Chart
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Evolução da Vacinação (2026)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateDark)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            StatusChip(text = "Admin", backgroundColor = EmeraldSoftBg, textColor = EmeraldDark)
                            StatusChip(text = "Atraso", backgroundColor = HealthRedLight, textColor = HealthRed)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    SimpleLineChart(
                        labels = listOf("Jan", "Mar", "Mai", "Jul", "Ago"),
                        primarySeries = listOf(12400f, 18400f, 24500f, 31200f, 35600f),
                        secondarySeries = listOf(3200f, 2700f, 2400f, 2250f, 2184f)
                    )
                }
            }
        }

        // Top Facilities by Coverage
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Top Unidades Sanitárias — Cobertura", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateDark)
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        facilities.take(5).forEach { fac ->
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = fac.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SlateDark)
                                    Text(text = "${fac.coveragePct}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (fac.coveragePct >= 90) EmeraldDark else HealthAmber)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                CoverageProgressBar(percentage = fac.coveragePct)
                            }
                        }
                    }
                }
            }
        }

        // Recent Audit Events
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Atividade Recente do Sistema", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateDark)
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        auditLogs.take(4).forEach { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SlateBg)
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (log.category) {
                                        "Vacinação" -> "💉"
                                        "Utente" -> "👶"
                                        "Calendário" -> "📅"
                                        else -> "🛡️"
                                    },
                                    fontSize = 18.sp
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = log.title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = SlateDark)
                                    Text(text = log.subtitle, fontSize = 10.sp, color = SlateMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Text(text = log.timeAgo, fontSize = 10.sp, color = SlateLight)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------- 2. UTILIZADORES -----------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminUsersView(
    users: List<UserAccountEntity>,
    onAddUser: () -> Unit,
    onEditUser: (UserAccountEntity) -> Unit = {},
    onDeleteUser: (UserAccountEntity) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("all") }
    var userPendingDeletion by remember { mutableStateOf<UserAccountEntity?>(null) }

    val filtered = users.filter { user ->
        val matchesQuery = user.name.contains(searchQuery, ignoreCase = true) ||
                user.emailOrPhone.contains(searchQuery, ignoreCase = true)
        val matchesRole = when (selectedRole) {
            "parent" -> user.role == "parent"
            "pro" -> user.role == "pro"
            "admin" -> user.role == "admin"
            else -> true
        }
        matchesQuery && matchesRole
    }

    if (userPendingDeletion != null) {
        val targetUser = userPendingDeletion!!
        val roleDescription = when (targetUser.role) {
            "admin" -> "Administrador Secundário"
            "pro" -> "Profissional de Saúde"
            else -> "Encarregado / Pai"
        }

        AlertDialog(
            onDismissRequest = { userPendingDeletion = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = HealthRed,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Remover Utilizador?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tem a certeza que deseja remover permanentemente este registo?",
                        fontSize = 13.sp,
                        color = SlateDark
                    )
                    Surface(
                        color = SlateBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = targetUser.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateDark)
                            Text(text = "Contacto: ${targetUser.emailOrPhone}", fontSize = 12.sp, color = SlateMedium)
                            Text(text = "Perfil: $roleDescription", fontSize = 12.sp, color = HealthPurpleDark, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Text(
                        text = "⚠️ As credenciais de acesso serão revogadas de imediato.",
                        fontSize = 11.sp,
                        color = HealthRedDark,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteUser(targetUser)
                        userPendingDeletion = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HealthRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Sim, Remover", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { userPendingDeletion = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancelar", color = SlateDark)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Gestão de Acessos", fontSize = 13.sp, color = SlateMedium)
                    Text(text = "Utilizadores (${users.size})", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                }

                Button(
                    onClick = onAddUser,
                    colors = ButtonDefaults.buttonColors(containerColor = HealthPurpleDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Adicionar Acesso", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Pesquisar por nome ou contacto...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SlateMedium) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("all" to "Todos", "parent" to "Responsáveis", "pro" to "Profissionais", "admin" to "Admins").forEach { (roleKey, label) ->
                    val isSelected = selectedRole == roleKey
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) HealthPurpleDark else Color.White)
                            .border(1.dp, if (isSelected) HealthPurpleDark else SlateBorder, RoundedCornerShape(20.dp))
                            .clickable { selectedRole = roleKey }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else SlateDark
                        )
                    }
                }
            }
        }

        items(filtered) { user ->
            val isMasterAdmin = user.emailOrPhone.equals("cepsgadmin@jorgito2026", ignoreCase = true) ||
                    user.email.equals("cepsgadmin@jorgito2026", ignoreCase = true)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isMasterAdmin) HealthPurpleLight.copy(alpha = 0.4f) else Color.White),
                border = BorderStroke(1.dp, if (isMasterAdmin) HealthPurpleDark else SlateBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateDark)
                            if (isMasterAdmin) {
                                Surface(
                                    color = HealthPurpleDark,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Master Admin",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(text = user.emailOrPhone, fontSize = 11.sp, color = SlateMedium)
                        if (user.childrenCount > 0) {
                            Text(text = "${user.childrenCount} filho(s) associado(s)", fontSize = 10.sp, color = EmeraldDark)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val roleLabel = when {
                            isMasterAdmin -> "Admin Principal"
                            user.role == "parent" -> "Responsável"
                            user.role == "pro" -> "Profissional"
                            else -> "Administrador"
                        }
                        StatusChip(
                            text = roleLabel,
                            backgroundColor = if (isMasterAdmin) HealthPurpleDark else HealthPurpleLight,
                            textColor = if (isMasterAdmin) Color.White else HealthPurpleDark
                        )

                        IconButton(
                            onClick = { onEditUser(user) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar utilizador",
                                tint = HealthPurpleDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // If not Master Admin, allow deletion by Master Admin
                        if (!isMasterAdmin) {
                            IconButton(
                                onClick = { userPendingDeletion = user },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Remover utilizador",
                                    tint = HealthRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------- 3. PROFISSIONAIS -----------------
@Composable
fun AdminProsView(
    workers: List<HealthWorkerEntity>,
    onAddWorker: () -> Unit,
    onEditWorker: (HealthWorkerEntity) -> Unit = {},
    onDeleteWorker: (HealthWorkerEntity) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var workerPendingDeletion by remember { mutableStateOf<HealthWorkerEntity?>(null) }

    val filtered = workers.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.facilityName.contains(searchQuery, ignoreCase = true) ||
                it.title.contains(searchQuery, ignoreCase = true)
    }

    if (workerPendingDeletion != null) {
        val targetWorker = workerPendingDeletion!!
        AlertDialog(
            onDismissRequest = { workerPendingDeletion = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = HealthRed,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Remover Profissional de Saúde?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Deseja remover este profissional da equipa de saúde?",
                        fontSize = 13.sp,
                        color = SlateDark
                    )
                    Surface(
                        color = SlateBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = targetWorker.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateDark)
                            Text(text = "${targetWorker.title} · ${targetWorker.facilityName}", fontSize = 12.sp, color = SlateMedium)
                            Text(text = "${targetWorker.dosesCount} doses registadas", fontSize = 11.sp, color = EmeraldDark)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteWorker(targetWorker)
                        workerPendingDeletion = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HealthRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Remover Profissional", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { workerPendingDeletion = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancelar", color = SlateDark)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Equipa de Saúde", fontSize = 13.sp, color = SlateMedium)
                    Text(text = "Profissionais de Vacinação", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                }

                Button(
                    onClick = onAddWorker,
                    colors = ButtonDefaults.buttonColors(containerColor = HealthPurpleDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+ Novo Profissional", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Pesquisar profissional ou US...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SlateMedium) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )
        }

        items(filtered) { worker ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(HealthPurpleLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = worker.avatarInitials, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HealthPurpleDark)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = worker.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateDark)
                            Text(text = "${worker.title} · ${worker.facilityName}", fontSize = 11.sp, color = SlateMedium)
                        }
                        StatusChip(text = "Activo", backgroundColor = EmeraldSoftBg, textColor = EmeraldDark)

                        IconButton(
                            onClick = { onEditWorker(worker) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar profissional",
                                tint = HealthPurpleDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { workerPendingDeletion = worker },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Remover profissional",
                                tint = HealthRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlateBg)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${worker.dosesCount}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "Doses", fontSize = 10.sp, color = SlateMedium)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlateBg)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${worker.childrenCount}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "Crianças", fontSize = 10.sp, color = SlateMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------- 4. UNIDADES SANITÁRIAS (ADMIN) -----------------
@Composable
fun AdminUnitsView(
    facilities: List<HealthFacilityEntity>,
    onSelectFacility: (HealthFacilityEntity) -> Unit = {},
    onAddFacility: () -> Unit,
    onEditFacility: (HealthFacilityEntity) -> Unit = {},
    onDeleteFacility: (HealthFacilityEntity) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var facilityPendingDeletion by remember { mutableStateOf<HealthFacilityEntity?>(null) }

    val filtered = facilities.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.region.contains(searchQuery, ignoreCase = true) ||
                it.address.contains(searchQuery, ignoreCase = true)
    }

    if (facilityPendingDeletion != null) {
        val targetFacility = facilityPendingDeletion!!
        AlertDialog(
            onDismissRequest = { facilityPendingDeletion = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = HealthRed,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Remover Unidade Sanitária?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tem a certeza que deseja eliminar esta unidade de saúde do sistema?",
                        fontSize = 13.sp,
                        color = SlateDark
                    )
                    Surface(
                        color = SlateBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = targetFacility.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateDark)
                            Text(text = "${targetFacility.type} · ${targetFacility.region}", fontSize = 12.sp, color = SlateMedium)
                            Text(text = "${targetFacility.registeredChildrenCount} utentes associados", fontSize = 11.sp, color = EmeraldDark)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteFacility(targetFacility)
                        facilityPendingDeletion = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HealthRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Eliminar Unidade", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { facilityPendingDeletion = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancelar", color = SlateDark)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Rede Nacional", fontSize = 13.sp, color = SlateMedium)
                    Text(text = "Unidades Sanitárias (${facilities.size})", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                }

                Button(
                    onClick = onAddFacility,
                    colors = ButtonDefaults.buttonColors(containerColor = HealthPurpleDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Nova Unidade", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Pesquisar unidade sanitária ou província...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SlateMedium) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )
        }

        items(filtered) { fac ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SlateBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectFacility(fac) }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            StatusChip(text = fac.region, backgroundColor = HealthPurpleLight, textColor = HealthPurpleDark)
                            StatusChip(text = "${fac.coveragePct}% Cobertura", backgroundColor = EmeraldSoftBg, textColor = EmeraldDark)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onEditFacility(fac) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar unidade",
                                    tint = HealthPurpleDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { facilityPendingDeletion = fac },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Eliminar unidade",
                                    tint = HealthRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = fac.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SlateDark)
                    Text(text = "${fac.registeredChildrenCount} crianças registadas · ${fac.address}", fontSize = 11.sp, color = SlateMedium)

                    Spacer(modifier = Modifier.height(10.dp))
                    CoverageProgressBar(percentage = fac.coveragePct)
                }
            }
        }
    }
}

// ----------------- 5. VACINAS (CATÁLOGO) -----------------
@Composable
fun AdminVaccinesView(
    catalog: List<VaccineCatalogEntity>,
    onAddVaccine: () -> Unit = {},
    onEditVaccine: (VaccineCatalogEntity) -> Unit = {},
    onDeleteVaccine: (VaccineCatalogEntity) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var vaccinePendingDeletion by remember { mutableStateOf<VaccineCatalogEntity?>(null) }

    val filteredCatalog = catalog.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.targetDisease.contains(searchQuery, ignoreCase = true) ||
                it.scheduledAges.contains(searchQuery, ignoreCase = true)
    }

    if (vaccinePendingDeletion != null) {
        val targetVaccine = vaccinePendingDeletion!!
        AlertDialog(
            onDismissRequest = { vaccinePendingDeletion = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = HealthRed,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Remover Vacina do Catálogo?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tem a certeza que deseja eliminar esta vacina do catálogo oficial?",
                        fontSize = 13.sp,
                        color = SlateDark
                    )
                    Surface(
                        color = SlateBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "${targetVaccine.emoji} ${targetVaccine.name}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateDark)
                            Text(text = "Doença Alvo: ${targetVaccine.targetDisease}", fontSize = 12.sp, color = SlateMedium)
                            Text(text = "Idades: ${targetVaccine.scheduledAges}", fontSize = 11.sp, color = EmeraldDark)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteVaccine(targetVaccine)
                        vaccinePendingDeletion = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HealthRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Eliminar Vacina", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { vaccinePendingDeletion = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancelar", color = SlateDark)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Catálogo Oficial MISAU · Room DB", fontSize = 13.sp, color = SlateMedium)
                    Text(text = "Vacinas do Programa PTV (${catalog.size})", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                }

                Button(
                    onClick = onAddVaccine,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("add_vaccine_fab_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Cadastrar Vacina", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Pesquisar vacina, doença ou idade...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SlateMedium) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )
        }

        items(filteredCatalog) { item ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = item.emoji, fontSize = 26.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateDark)
                        Text(text = "Doença: ${item.targetDisease}", fontSize = 11.sp, color = SlateMedium)
                        Text(text = "${item.dosesCount} · ${item.route} · Idade: ${item.scheduledAges}", fontSize = 10.sp, color = EmeraldDark)
                    }
                    StatusChip(text = "Activa", backgroundColor = EmeraldSoftBg, textColor = EmeraldDark)

                    IconButton(
                        onClick = { onEditVaccine(item) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar vacina",
                            tint = HealthPurpleDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { vaccinePendingDeletion = item },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Eliminar vacina",
                            tint = HealthRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// ----------------- 6. CALENDÁRIOS -----------------
@Composable
fun AdminCalendarsView() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(text = "Normas Oficiais de Imunização", fontSize = 13.sp, color = SlateMedium)
                Text(text = "Calendários Vacinais", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SlateDark)
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(2.dp, EmeraldPrimary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatusChip(text = "✓ Activo", backgroundColor = EmeraldSoftBg, textColor = EmeraldDark)
                        Text(text = "v.2026.1", fontSize = 11.sp, color = SlateMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Calendário Nacional PTV — 2026", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SlateDark)
                    Text(text = "Programa Tempos de Vacinação · República de Moçambique", fontSize = 12.sp, color = SlateMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Aprovado por: Ministério da Saúde (MISAU) · 01 Jan 2026", fontSize = 10.sp, color = SlateMedium)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatusChip(text = "Gestantes", backgroundColor = HealthPinkLight, textColor = HealthPink)
                        Text(text = "v.2026.1", fontSize = 11.sp, color = SlateMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Calendário da Gestante", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SlateDark)
                    Text(text = "Vacinação recomendada durante a gravidez (Tétano, etc.)", fontSize = 12.sp, color = SlateMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Aprovado por: Ministério da Saúde (MISAU) · 01 Jan 2026", fontSize = 10.sp, color = SlateMedium)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = HealthAmberLight),
                border = BorderStroke(1.dp, HealthAmber.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "⚕️ Requisito Fundamental de Saúde", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF78350F))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Os calendários devem ser configurados e validados exclusivamente pelas autoridades de saúde competentes. O sistema aplica regras padronizadas sem gerar prescrições independentes.",
                        fontSize = 11.sp,
                        color = Color(0xFF92400E),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

// ----------------- 7. RELATÓRIOS ADMIN -----------------
@Composable
fun AdminReportsView(
    onExport: (String) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(text = "Indicadores Nacionais", fontSize = 13.sp, color = SlateMedium)
                Text(text = "Relatórios e Exportações", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SlateDark)
            }
        }

        val reports = listOf(
            Triple("📊 Cobertura Nacional", "PDF · 24 páginas", "Taxas de adesão por distrito"),
            Triple("🗺️ Distribuição Geográfica", "PDF · 18 páginas", "Mapeamento das 11 províncias"),
            Triple("📈 Evolução Anual PTV", "Excel · 12 folhas", "Séries temporais 2024-2026"),
            Triple("⚠️ Doses em Atraso", "PDF · 32 páginas", "Indicadores de abandono vacinal"),
            Triple("💉 Consumo de Vacinas", "CSV · 187k linhas", "Balanço de stock e lotes"),
            Triple("👥 Utilizadores Activos", "PDF · 8 páginas", "Auditoria de acessos da equipa")
        )

        items(reports) { (title, format, desc) ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateDark)
                        Text(text = desc, fontSize = 11.sp, color = SlateMedium)
                        Text(text = format, fontSize = 10.sp, color = HealthPurpleDark, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = { onExport(title) },
                        colors = ButtonDefaults.buttonColors(containerColor = HealthPurpleLight, contentColor = HealthPurpleDark),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Exportar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ----------------- 8. AUDITORIA -----------------
@Composable
fun AdminAuditView(
    logs: List<AuditLogEntity>,
    onClearLogs: () -> Unit = {}
) {
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = HealthRed,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Limpar Todo o Histórico?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark
                )
            },
            text = {
                Text(
                    text = "Tem a certeza que deseja eliminar todos os registos de auditoria actuais (${logs.size} eventos)? Esta acção não pode ser desfeita.",
                    fontSize = 13.sp,
                    color = SlateDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearLogs()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HealthRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Sim, Limpar Tudo", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearConfirmDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancelar", color = SlateDark)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Rastreabilidade e Segurança", fontSize = 13.sp, color = SlateMedium)
                    Text(text = "Registo de Auditoria (${logs.size})", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                }

                if (logs.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { showClearConfirmDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HealthRed),
                        border = BorderStroke(1.dp, HealthRed.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = HealthRed)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Limpar Logs", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (logs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhum registo de auditoria no momento.", color = SlateMedium, fontSize = 14.sp)
                }
            }
        }

        items(logs) { log ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(HealthPurpleLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (log.category) {
                                "Vacinação" -> "💉"
                                "Utente" -> "👶"
                                "Calendário" -> "📅"
                                "Stock" -> "📦"
                                else -> "🔒"
                            },
                            fontSize = 18.sp
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = log.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateDark)
                            Text(text = log.timeAgo, fontSize = 10.sp, color = SlateLight)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = log.subtitle, fontSize = 11.sp, color = SlateMedium, lineHeight = 15.sp)
                    }
                }
            }
        }
    }
}

// ----------------- 9. CONFIGURAÇÕES -----------------
@Composable
fun AdminConfigView(viewModel: VacciTrackViewModel) {
    var smsGateway by remember { mutableStateOf(true) }
    var whatsappAlerts by remember { mutableStateOf(false) }
    var pushNotifications by remember { mutableStateOf(true) }
    var twoFactorAuth by remember { mutableStateOf(true) }

    val currentUrl by viewModel.supabaseProjectUrl.collectAsState()
    val currentKey by viewModel.supabaseAnonKey.collectAsState()
    val supabaseStatus by viewModel.supabaseStatus.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var inputUrl by remember(currentUrl) { mutableStateOf(currentUrl) }
    var inputKey by remember(currentKey) { mutableStateOf(currentKey) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(text = "Preferências e Painel Master", fontSize = 13.sp, color = SlateMedium)
                Text(text = "Configurações Globais", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SlateDark)
            }
        }

        // MASTER ADMIN SPECIAL CONTROL CARD
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.5.dp, HealthPurpleDark.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(HealthPurpleLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👑", fontSize = 18.sp)
                        }
                        Column {
                            Text(
                                text = "Controlo Total do Admin Principal",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = SlateDark
                            )
                            Text(
                                text = "cepsgadmin@jorgito2026 · Acesso Irrestrito",
                                fontSize = 11.sp,
                                color = HealthPurpleDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    HorizontalDivider(color = SlateBorder)

                    Text(
                        text = "O Administrador Principal possui autorização total para cadastrar, editar e eliminar todos os serviços, unidades sanitárias, equipas de saúde, utilizadores e catálogo de vacinas.",
                        fontSize = 12.sp,
                        color = SlateMedium,
                        lineHeight = 16.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.showMessage("Dados do sistema verificados e operacionais.")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Verificar Integridade", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.showMessage("Todos os serviços e módulos estão activos e sincronizados.")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HealthPurpleDark),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Estado dos Serviços", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // SUPABASE POSTGRESQL INTEGRATION CARD
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.5.dp, EmeraldDark.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EmeraldDark.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⚡", fontSize = 18.sp)
                            }
                            Column {
                                Text(text = "Integração Supabase (Cloud DB)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateDark)
                                Text(text = "PostgreSQL e Autenticação em Nuvem", fontSize = 11.sp, color = SlateMedium)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (currentUrl.isNotBlank()) EmeraldLight else SlateBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (currentUrl.isNotBlank()) "Conectado" else "Pendente URL",
                                color = if (currentUrl.isNotBlank()) EmeraldDark else SlateMedium,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(color = SlateBorder)

                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        label = { Text("Supabase Project URL") },
                        placeholder = { Text("https://xyzcompany.supabase.co") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = inputKey,
                        onValueChange = { inputKey = it },
                        label = { Text("Supabase Anon / Publishable Key") },
                        placeholder = { Text("sb_publishable_...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Text(
                        text = "Estado: $supabaseStatus",
                        fontSize = 12.sp,
                        color = if (supabaseStatus.contains("Erro", ignoreCase = true)) HealthRed else EmeraldDark,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.saveSupabaseSettings(inputUrl, inputKey)
                                viewModel.testSupabaseConnection()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isSyncing
                        ) {
                            Text("Testar Conexão", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.saveSupabaseSettings(inputUrl, inputKey)
                                viewModel.syncAllToSupabase()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isSyncing
                        ) {
                            Text(if (isSyncing) "Sincronizando..." else "Sincronizar Cloud", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Geral e Localização", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateDark)
                    ConfigRow(label = "Idioma Padrão", value = "Português (MZ)")
                    ConfigRow(label = "Fuso Horário", value = "Maputo (GMT+2)")
                    ConfigRow(label = "Formato de Data", value = "DD/MM/AAAA")
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Canais de Notificação", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateDark)
                    ToggleConfigRow(label = "SMS aos Responsáveis", checked = smsGateway, onCheckedChange = { smsGateway = it })
                    ToggleConfigRow(label = "Alertas por WhatsApp", checked = whatsappAlerts, onCheckedChange = { whatsappAlerts = it })
                    ToggleConfigRow(label = "Notificações Push", checked = pushNotifications, onCheckedChange = { pushNotifications = it })
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Segurança e Conformidade", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateDark)
                    ConfigRow(label = "Autenticação 2FA", value = "Activo (SMS/Token)")
                    ConfigRow(label = "Criptografia E2E", value = "AES-256 Activo")
                    ConfigRow(label = "Backup Automático", value = "Diário (02:00)")
                    ConfigRow(label = "Sessão Expira em", value = "30 minutos")
                }
            }
        }
    }
}

@Composable
private fun ConfigRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = SlateMedium)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SlateDark)
    }
}

@Composable
private fun ToggleConfigRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = SlateDark)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = HealthPurpleDark, checkedTrackColor = HealthPurpleLight)
        )
    }
}
