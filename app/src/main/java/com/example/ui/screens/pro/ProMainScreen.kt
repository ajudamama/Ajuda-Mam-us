package com.example.ui.screens.pro

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.ProTab
import com.example.ui.viewmodel.VacciTrackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProMainScreen(
    viewModel: VacciTrackViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.proTab.collectAsState()
    val children by viewModel.children.collectAsState()
    val allRecords by viewModel.allVaccineRecords.collectAsState()
    val delayedRecords by viewModel.delayedRecords.collectAsState()
    val offlinePending by viewModel.offlinePendingRecords.collectAsState()
    val stockItems by viewModel.stockItems.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var showAddChildDialog by remember { mutableStateOf(false) }
    var showFastAdministerDialog by remember { mutableStateOf(false) }
    var selectedChildForAdminister by remember { mutableStateOf<ChildEntity?>(null) }
    var showSmsDialog by remember { mutableStateOf(false) }
    var smsTargetChild by remember { mutableStateOf<Pair<String, String>?>(null) }

    Scaffold(
        topBar = {
            ProTopBar(
                onSwitchRole = { viewModel.navigateTo(AppDestination.DEMO_SELECTOR) },
                onSync = { viewModel.syncOfflineRecords() },
                isSyncing = isSyncing,
                pendingCount = offlinePending.size
            )
        },
        bottomBar = {
            ProBottomNavigation(
                currentTab = currentTab,
                onTabSelected = { viewModel.setProTab(it) }
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
                ProTab.DASHBOARD -> ProDashboardScreen(
                    children = children,
                    delayedRecords = delayedRecords,
                    onNavigateTab = { viewModel.setProTab(it) },
                    onAdministerForChild = { child ->
                        selectedChildForAdminister = child
                        showFastAdministerDialog = true
                    }
                )
                ProTab.REGISTAR -> ProRegisterPatientScreen(
                    onRegister = { name, birthDate, gender, birthPlace, birthWeight, parentName, parentContact, parentType, facilityName ->
                        viewModel.registerNewChild(name, birthDate, gender, birthPlace, birthWeight, parentName, parentContact, parentType, facilityName) {
                            viewModel.setProTab(ProTab.DASHBOARD)
                        }
                    }
                )
                ProTab.VACINAR -> ProAdministerScreen(
                    children = children,
                    stockItems = stockItems,
                    onAdminister = { childId, vaccine, dose, date, lot, notes, offline ->
                        viewModel.administerVaccine(childId, vaccine, dose, date, "US Polana", "Dr. Carlos Tembe", lot, null, notes, offline) {
                            viewModel.setProTab(ProTab.DASHBOARD)
                        }
                    }
                )
                ProTab.CONSULTAR -> ProConsultHistoryScreen(
                    children = children,
                    records = allRecords
                )
                ProTab.ATRASO -> ProDelayedDosesScreen(
                    children = children,
                    delayedRecords = delayedRecords,
                    onAdminister = { child ->
                        selectedChildForAdminister = child
                        showFastAdministerDialog = true
                    },
                    onContact = { childName, contact ->
                        smsTargetChild = Pair(childName, contact)
                        showSmsDialog = true
                    }
                )
                ProTab.DOSES -> ProStockScreen(
                    stockItems = stockItems,
                    onAddStock = { vaccine, lot, qty, exp ->
                        viewModel.addStockItem(vaccine, lot, qty, exp) {}
                    }
                )
                ProTab.SYNC -> ProSyncScreen(
                    offlinePending = offlinePending,
                    isSyncing = isSyncing,
                    supabaseStatus = viewModel.supabaseStatus.collectAsState().value,
                    onTriggerSync = { viewModel.syncOfflineRecords() },
                    onSupabaseSync = { viewModel.syncAllToSupabase() }
                )
                ProTab.RELATORIOS -> ProReportsScreen()
            }
        }
    }

    if (showAddChildDialog) {
        AddChildDialog(
            onDismiss = { showAddChildDialog = false },
            onConfirm = { name, birthDate, gender, birthPlace, birthWeight, parentName, parentContact, parentType, facilityName ->
                viewModel.registerNewChild(name, birthDate, gender, birthPlace, birthWeight, parentName, parentContact, parentType, facilityName) {}
            }
        )
    }

    selectedChildForAdminister?.let { child ->
        if (showFastAdministerDialog) {
            FastAdministerDialog(
                child = child,
                onDismiss = {
                    showFastAdministerDialog = false
                    selectedChildForAdminister = null
                },
                onConfirm = { vaccine, dose, date, lot, notes, offline ->
                    viewModel.administerVaccine(child.id, vaccine, dose, date, "US Polana", "Dr. Carlos Tembe", lot, null, notes, offline) {
                        showFastAdministerDialog = false
                        selectedChildForAdminister = null
                    }
                }
            )
        }
    }

    smsTargetChild?.let { (name, contact) ->
        if (showSmsDialog) {
            SmsGuardianDialog(
                childName = name,
                parentContact = contact,
                vaccineName = "Febre Amarela / Sarampo",
                daysDelayed = 11,
                onDismiss = {
                    showSmsDialog = false
                    smsTargetChild = null
                },
                onSent = {
                    viewModel.showMessage("Lembrete SMS enviado com sucesso para $contact")
                }
            )
        }
    }
}

@Composable
private fun ProTopBar(
    onSwitchRole: () -> Unit,
    onSync: () -> Unit,
    isSyncing: Boolean,
    pendingCount: Int
) {
    Surface(
        color = SlateNavy,
        shadowElevation = 4.dp
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
                        .background(HealthBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "VacciTrack", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = " Pro", color = HealthBlueLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Text(text = "Dr. Carlos Tembe · US Polana", color = SlateLight, fontSize = 11.sp)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (pendingCount > 0) {
                    Button(
                        onClick = onSync,
                        colors = ButtonDefaults.buttonColors(containerColor = HealthAmber),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        enabled = !isSyncing
                    ) {
                        Text(text = if (isSyncing) "A sincronizar..." else "📥 Sync ($pendingCount)", fontSize = 11.sp, color = SlateDark, fontWeight = FontWeight.Bold)
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(HealthBlue)
                        .clickable { onSwitchRole() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "CT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ProBottomNavigation(
    currentTab: ProTab,
    onTabSelected: (ProTab) -> Unit
) {
    NavigationBar(
        containerColor = SlateNavy,
        tonalElevation = 8.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        listOf(
            ProTab.DASHBOARD to Icons.Default.Dashboard,
            ProTab.VACINAR to Icons.Default.Vaccines,
            ProTab.REGISTAR to Icons.Default.PersonAdd,
            ProTab.ATRASO to Icons.Default.Warning,
            ProTab.DOSES to Icons.Default.Inventory2,
            ProTab.SYNC to Icons.Default.Sync
        ).forEach { (tab, icon) ->
            val selected = currentTab == tab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = { Icon(icon, contentDescription = tab.label) },
                label = { Text(tab.label, fontSize = 10.sp, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    indicatorColor = HealthBlue,
                    unselectedIconColor = SlateLight,
                    unselectedTextColor = SlateLight
                )
            )
        }
    }
}

// ----------------- 1. PRO DASHBOARD -----------------
@Composable
fun ProDashboardScreen(
    children: List<ChildEntity>,
    delayedRecords: List<VaccineRecordEntity>,
    onNavigateTab: (ProTab) -> Unit,
    onAdministerForChild: (ChildEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredChildren = children.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.code.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Bom dia, Dr. Tembe", fontSize = 13.sp, color = SlateMedium)
                    Text(text = "Dashboard · US Polana", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                }

                Button(
                    onClick = { onNavigateTab(ProTab.VACINAR) },
                    colors = ButtonDefaults.buttonColors(containerColor = HealthBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "+ Registar dose", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // KPIs Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Crianças",
                    value = "1.250",
                    subtitle = "↑ +23 esta semana",
                    iconEmoji = "👶",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Vacinas",
                    value = "3.420",
                    subtitle = "↑ +142 este mês",
                    iconEmoji = "💉",
                    valueColor = EmeraldDark,
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
                    title = "Previstas",
                    value = "450",
                    subtitle = "próximos 30 dias",
                    iconEmoji = "📅",
                    valueColor = HealthAmber,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Em atraso",
                    value = "${delayedRecords.size + 80}",
                    subtitle = "↑ requer atenção",
                    iconEmoji = "⚠️",
                    valueColor = HealthRed,
                    subtitleColor = HealthRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Charts Section
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Administrações por Mês (US Polana)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateDark)
                    Spacer(modifier = Modifier.height(12.dp))
                    SimpleBarChart(
                        data = listOf(
                            ChartBarData("Mar", 420f, EmeraldPrimary),
                            ChartBarData("Abr", 510f, EmeraldPrimary),
                            ChartBarData("Mai", 480f, EmeraldPrimary),
                            ChartBarData("Jun", 620f, EmeraldPrimary),
                            ChartBarData("Jul", 580f, EmeraldPrimary),
                            ChartBarData("Ago", 450f, EmeraldDark)
                        )
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Cobertura por Vacina (%)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    SimpleDonutChart(
                        slices = listOf(
                            DonutSliceData("BCG", 94f, EmeraldPrimary),
                            DonutSliceData("Penta", 88f, HealthBlue),
                            DonutSliceData("VOP", 85f, HealthPurple),
                            DonutSliceData("Pneumo", 82f, HealthAmber),
                            DonutSliceData("Sarampo", 76f, HealthRed)
                        ),
                        centerText = "85%",
                        centerSubtext = "Média US"
                    )
                }
            }
        }

        // Search Child Quick Registry
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Pesquisar e Atender Criança", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateDark)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Nome, código VT-MZ-... ou QR") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { searchQuery = "VT-MZ" }) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = "QR Code Scanner")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        filteredChildren.take(4).forEach { child ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SlateBg)
                                    .clickable { onAdministerForChild(child) }
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(text = child.avatarEmoji, fontSize = 22.sp)
                                    Column {
                                        Text(text = child.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateDark)
                                        Text(text = "${child.code} · ${child.ageDescription}", fontSize = 11.sp, color = SlateMedium)
                                    }
                                }

                                Button(
                                    onClick = { onAdministerForChild(child) },
                                    colors = ButtonDefaults.buttonColors(containerColor = HealthBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Vacinar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------- 2. REGISTAR UTENTE -----------------
@Composable
fun ProRegisterPatientScreen(
    onRegister: (name: String, birthDate: String, gender: String, birthPlace: String, birthWeight: String, parentName: String, parentContact: String, parentType: String, facilityName: String) -> Unit
) {
    var patientType by remember { mutableStateOf("Criança") }
    var name by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("2026-08-20") }
    var gender by remember { mutableStateOf("Masculino") }
    var birthPlace by remember { mutableStateOf("Hospital Central de Maputo") }
    var parentName by remember { mutableStateOf("") }
    var parentContact by remember { mutableStateOf("+258 84 ") }
    var birthWeight by remember { mutableStateOf("3.2 kg") }
    var facilityName by remember { mutableStateOf("US Polana") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(text = "Novo Utente", fontSize = 13.sp, color = SlateMedium)
                Text(text = "Registar Criança ou Gestante", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SlateDark)
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Criança", "Gestante", "Recém-nascido").forEach { type ->
                    val isSelected = patientType == type
                    Button(
                        onClick = { patientType = type },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) HealthBlue else Color.White, contentColor = if (isSelected) Color.White else SlateDark),
                        border = BorderStroke(1.dp, if (isSelected) HealthBlue else SlateBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text(text = type, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome Completo *") },
                        placeholder = { Text("Ex: Inácio Mondlane") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    MaterialDatePickerField(
                        value = birthDate,
                        onValueChange = { birthDate = it },
                        label = "Data de Nascimento *",
                        testTag = "patient_birth_date_picker"
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { gender = "Masculino" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = if (gender == "Masculino") HealthBlueLight else Color.Transparent),
                            border = BorderStroke(1.dp, if (gender == "Masculino") HealthBlue else SlateBorder)
                        ) {
                            Text("Masculino", color = if (gender == "Masculino") HealthBlueDark else SlateDark)
                        }

                        OutlinedButton(
                            onClick = { gender = "Feminino" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = if (gender == "Feminino") HealthPinkLight else Color.Transparent),
                            border = BorderStroke(1.dp, if (gender == "Feminino") HealthPink else SlateBorder)
                        ) {
                            Text("Feminino", color = if (gender == "Feminino") HealthPink else SlateDark)
                        }
                    }

                    OutlinedTextField(
                        value = birthPlace,
                        onValueChange = { birthPlace = it },
                        label = { Text("Local de Nascimento") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = parentName,
                        onValueChange = { parentName = it },
                        label = { Text("Nome do Responsável *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = parentContact,
                        onValueChange = { parentContact = it },
                        label = { Text("Contacto (+258...) *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = birthWeight,
                        onValueChange = { birthWeight = it },
                        label = { Text("Peso ao Nascer") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            if (name.isNotBlank() && parentName.isNotBlank()) {
                                onRegister(name, birthDate, gender, birthPlace, birthWeight, parentName, parentContact, patientType, facilityName)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HealthBlue),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = "Registar e Gerar Código PTV", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ----------------- 3. REGISTAR VACINAÇÃO -----------------
@Composable
fun ProAdministerScreen(
    children: List<ChildEntity>,
    stockItems: List<VaccineStockEntity>,
    onAdminister: (childId: Long, vaccine: String, dose: String, date: String, lot: String, notes: String?, offline: Boolean) -> Unit
) {
    var selectedChildId by remember { mutableStateOf(children.firstOrNull()?.id ?: 1L) }
    var selectedVaccine by remember { mutableStateOf("Febre Amarela") }
    var selectedDose by remember { mutableStateOf("Dose única") }
    var administeredDate by remember { mutableStateOf("2026-08-20") }
    var lotNumber by remember { mutableStateOf("FA-2026-06") }
    var notes by remember { mutableStateOf("") }
    var isOffline by remember { mutableStateOf(false) }

    val activeChild = children.firstOrNull { it.id == selectedChildId } ?: children.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(text = "Administração de Vacina", fontSize = 13.sp, color = SlateMedium)
                Text(text = "Registar Vacinação", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SlateDark)
            }
        }

        // Child selector chip row
        item {
            Text(text = "Seleccionar Utente:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateMedium)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(children) { child ->
                    val isSelected = child.id == selectedChildId
                    Card(
                        modifier = Modifier.clickable { selectedChildId = child.id },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) HealthBlueLight else Color.White),
                        border = BorderStroke(1.dp, if (isSelected) HealthBlue else SlateBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = child.avatarEmoji, fontSize = 16.sp)
                            Column {
                                Text(text = child.fullName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                                Text(text = child.code, fontSize = 10.sp, color = SlateMedium)
                            }
                        }
                    }
                }
            }
        }

        // Form Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = selectedVaccine,
                        onValueChange = { selectedVaccine = it },
                        label = { Text("Vacina *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = selectedDose,
                        onValueChange = { selectedDose = it },
                        label = { Text("Dose *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    MaterialDatePickerField(
                        value = administeredDate,
                        onValueChange = { administeredDate = it },
                        label = "Data de Administração *",
                        testTag = "administer_date_picker"
                    )

                    OutlinedTextField(
                        value = lotNumber,
                        onValueChange = { lotNumber = it },
                        label = { Text("Número do Lote *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Observações Clínicas (Reacções, etc.)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = isOffline,
                            onCheckedChange = { isOffline = it },
                            colors = CheckboxDefaults.colors(checkedColor = HealthBlue)
                        )
                        Text(text = "Guardar na fila offline para sincronizar depois", fontSize = 12.sp, color = SlateDark)
                    }

                    // Anti-duplication safety indicator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(HealthBlueLight)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(text = "🛡️ Validação Automática PTV", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = HealthBlueDark)
                            Text(
                                text = "O sistema verificou que $selectedVaccine ($selectedDose) está pendente para ${activeChild?.fullName}. Não há risco de sobredosagem.",
                                fontSize = 11.sp,
                                color = HealthBlueDark.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            if (selectedVaccine.isNotBlank() && lotNumber.isNotBlank()) {
                                onAdminister(selectedChildId, selectedVaccine, selectedDose, administeredDate, lotNumber, notes.ifBlank { null }, isOffline)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HealthBlue),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = "✓ Validar e Registar Dose", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ----------------- 4. CONSULTAR HISTÓRICO -----------------
@Composable
fun ProConsultHistoryScreen(
    children: List<ChildEntity>,
    records: List<VaccineRecordEntity>
) {
    var selectedChildId by remember { mutableStateOf(children.firstOrNull()?.id ?: 1L) }
    val activeChild = children.firstOrNull { it.id == selectedChildId } ?: children.firstOrNull()
    val childRecords = records.filter { it.childId == selectedChildId }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(text = "Histórico Clínico", fontSize = 13.sp, color = SlateMedium)
                Text(text = "Consultar Linha do Tempo", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SlateDark)
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(children) { child ->
                    val isSelected = child.id == selectedChildId
                    Card(
                        modifier = Modifier.clickable { selectedChildId = child.id },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) HealthBlueLight else Color.White),
                        border = BorderStroke(1.dp, if (isSelected) HealthBlue else SlateBorder)
                    ) {
                        Text(
                            text = "${child.avatarEmoji} ${child.fullName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = if (isSelected) HealthBlueDark else SlateDark
                        )
                    }
                }
            }
        }

        activeChild?.let { child ->
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SlateBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(EmeraldSoftBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = child.avatarEmoji, fontSize = 26.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = child.fullName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                            Text(text = "${child.code} · ${child.ageDescription} · Resp: ${child.parentName}", fontSize = 11.sp, color = SlateMedium)
                        }
                    }
                }
            }
        }

        items(childRecords) { record ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "${record.vaccineName} (${record.doseName})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateDark)
                        Text(text = "Data: ${record.administeredDate ?: record.scheduledDate} · US: ${record.facilityName}", fontSize = 11.sp, color = SlateMedium)
                        if (record.lotNumber != null) {
                            Text(text = "Lote: ${record.lotNumber} · Prof: ${record.professionalName ?: "Dr. Carlos Tembe"}", fontSize = 10.sp, color = EmeraldDark)
                        }
                    }
                    VaccineStatusBadge(status = record.status)
                }
            }
        }
    }
}

// ----------------- 5. DOSES EM ATRASO -----------------
@Composable
fun ProDelayedDosesScreen(
    children: List<ChildEntity>,
    delayedRecords: List<VaccineRecordEntity>,
    onAdminister: (ChildEntity) -> Unit,
    onContact: (childName: String, contact: String) -> Unit
) {
    var filterBucket by remember { mutableStateOf("all") }

    val delayedItems = listOf(
        DelayedChildDisplay("Amílcar Macuácua", "VT-MZ-000001", "Febre Amarela", "09/08/2026", 11, "+258 84 123 4567"),
        DelayedChildDisplay("João Sitoe", "VT-MZ-000003", "Sarampo 2", "27/07/2026", 24, "+258 84 999 1122"),
        DelayedChildDisplay("Fátima Langa", "VT-MZ-000007", "Pentavalente 3", "15/08/2026", 5, "+258 84 456 7890"),
        DelayedChildDisplay("Elsa Nhaca", "VT-MZ-000012", "BCG", "01/07/2026", 50, "+258 84 334 5566"),
        DelayedChildDisplay("Ricardo Cossa", "VT-MZ-000018", "VOP 2", "12/08/2026", 8, "+258 84 765 4321")
    )

    val filtered = when (filterBucket) {
        "1-7" -> delayedItems.filter { it.daysDelayed in 1..7 }
        "8-30" -> delayedItems.filter { it.daysDelayed in 8..30 }
        "30+" -> delayedItems.filter { it.daysDelayed > 30 }
        else -> delayedItems
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(text = "Acompanhamento Activo", fontSize = 13.sp, color = SlateMedium)
                Text(text = "Doses em Atraso (${delayedItems.size} utentes)", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SlateDark)
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "all" to "Todas (${delayedItems.size})",
                    "1-7" to "1-7 dias",
                    "8-30" to "8-30 dias",
                    "30+" to "+30 dias"
                ).forEach { (bucket, label) ->
                    val isSelected = filterBucket == bucket
                    Button(
                        onClick = { filterBucket = bucket },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) HealthRed else Color.White, contentColor = if (isSelected) Color.White else SlateDark),
                        border = BorderStroke(1.dp, if (isSelected) HealthRed else SlateBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                    ) {
                        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        items(filtered) { item ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateDark)
                            Text(text = "${item.code} · ${item.vaccine}", fontSize = 12.sp, color = SlateMedium)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(HealthRedLight)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "${item.daysDelayed} dias de atraso", color = HealthRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Prevista para: ${item.scheduledDate}", fontSize = 11.sp, color = SlateMedium)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onContact(item.name, item.contact) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("✉️ Contactar SMS", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                val match = children.firstOrNull { it.code == item.code } ?: children.first()
                                onAdminister(match)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HealthBlue),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Registar Dose", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private data class DelayedChildDisplay(
    val name: String,
    val code: String,
    val vaccine: String,
    val scheduledDate: String,
    val daysDelayed: Int,
    val contact: String
)

// ----------------- 6. GESTÃO DE DOSES / STOCK -----------------
@Composable
fun ProStockScreen(
    stockItems: List<VaccineStockEntity>,
    onAddStock: (vaccine: String, lot: String, qty: Int, exp: String) -> Unit
) {
    var showNewLotDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Inventário da Farmácia", fontSize = 13.sp, color = SlateMedium)
                    Text(text = "Gestão de Doses e Lotes", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                }

                Button(
                    onClick = { showNewLotDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = HealthBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+ Novo Lote", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Em Stock",
                    value = "2.840",
                    subtitle = "8 vacinas activas",
                    valueColor = EmeraldDark,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "A Vencer (30d)",
                    value = "124",
                    subtitle = "Lote Pentavalente",
                    valueColor = HealthAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(text = "Inventário por Vacina", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SlateDark)
        }

        items(stockItems) { stock ->
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
                    Column {
                        Text(text = stock.vaccineName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateDark)
                        Text(text = "Lote: ${stock.lotNumber} · Validade: ${stock.expirationDate}", fontSize = 11.sp, color = SlateMedium)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "${stock.quantity} doses", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateDark)
                        val badgeColor = if (stock.status == "OK") EmeraldSoftBg else HealthAmberLight
                        val textCol = if (stock.status == "OK") EmeraldDark else HealthAmber
                        StatusChip(text = stock.status, backgroundColor = badgeColor, textColor = textCol)
                    }
                }
            }
        }
    }

    if (showNewLotDialog) {
        var vacName by remember { mutableStateOf("BCG") }
        var lotNum by remember { mutableStateOf("BCG-2026-09") }
        var qtyStr by remember { mutableStateOf("250") }
        var expStr by remember { mutableStateOf("Dez 2027") }

        androidx.compose.ui.window.Dialog(onDismissRequest = { showNewLotDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Adicionar Novo Lote", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = vacName, onValueChange = { vacName = it }, label = { Text("Vacina") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = lotNum, onValueChange = { lotNum = it }, label = { Text("Número do Lote") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = qtyStr, onValueChange = { qtyStr = it }, label = { Text("Quantidade de Doses") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = expStr, onValueChange = { expStr = it }, label = { Text("Data de Validade") }, modifier = Modifier.fillMaxWidth())

                    Button(
                        onClick = {
                            onAddStock(vacName, lotNum, qtyStr.toIntOrNull() ?: 100, expStr)
                            showNewLotDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = HealthBlue)
                    ) {
                        Text("Registar Lote")
                    }
                }
            }
        }
    }
}

// ----------------- 7. SINCRONIZAÇÃO OFFLINE -----------------
@Composable
fun ProSyncScreen(
    offlinePending: List<VaccineRecordEntity>,
    isSyncing: Boolean,
    supabaseStatus: String = "Pronto",
    onTriggerSync: () -> Unit,
    onSupabaseSync: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(text = "Modo Offline e Sincronização", fontSize = 13.sp, color = SlateMedium)
                Text(text = "Sincronização Central & Supabase", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SlateDark)
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldDark)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color.White)
                        }
                        Column {
                            Text(text = "Estado da Ligação", fontSize = 12.sp, color = EmeraldLight)
                            Text(text = "Online · Supabase Cloud", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Supabase: $supabaseStatus", fontSize = 12.sp, color = EmeraldLight)

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onTriggerSync,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = EmeraldDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            enabled = !isSyncing
                        ) {
                            Text(text = if (isSyncing) "A sincronizar..." else "Sync Local", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = onSupabaseSync,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.9f), contentColor = EmeraldDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            enabled = !isSyncing
                        ) {
                            Text(text = "Sync Supabase ⚡", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Fila Offline Pendente", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateDark)
                        Text(text = "${offlinePending.size} registos", fontSize = 12.sp, color = HealthAmber, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (offlinePending.isEmpty()) {
                        Text(text = "Todos os dados locais foram sincronizados com o servidor central.", fontSize = 12.sp, color = SlateMedium)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            offlinePending.forEach { rec ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SlateBg)
                                        .padding(8.dp)
                                ) {
                                    Text(text = "💉 ${rec.vaccineName} (${rec.doseName}) - Lote ${rec.lotNumber ?: "N/A"}", fontSize = 12.sp, color = SlateDark)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------- 8. RELATÓRIOS PRO -----------------
@Composable
fun ProReportsScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(text = "Análise e Métricas", fontSize = 13.sp, color = SlateMedium)
                Text(text = "Relatórios da Unidade", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SlateDark)
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ReportExportCard(title = "Cobertura Vacinal", format = "PDF · 12 págs", modifier = Modifier.weight(1f))
                ReportExportCard(title = "Doses em Atraso", format = "PDF · 8 págs", modifier = Modifier.weight(1f))
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ReportExportCard(title = "Consumo Vacinas", format = "Excel · 3 folhas", modifier = Modifier.weight(1f))
                ReportExportCard(title = "Crianças Registadas", format = "CSV · 1.250 linhas", modifier = Modifier.weight(1f))
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Distribuição por Faixa Etária (Crianças)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateDark)
                    Spacer(modifier = Modifier.height(10.dp))
                    SimpleBarChart(
                        data = listOf(
                            ChartBarData("0-3m", 180f, HealthBlue),
                            ChartBarData("3-6m", 240f, HealthBlue),
                            ChartBarData("6-12m", 320f, HealthBlueDark),
                            ChartBarData("1-2a", 280f, HealthBlue),
                            ChartBarData("2-5a", 230f, HealthBlue)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportExportCard(
    title: String,
    format: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SlateBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateDark)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = format, fontSize = 11.sp, color = SlateMedium)
        }
    }
}
