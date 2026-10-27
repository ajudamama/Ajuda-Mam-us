package com.example.ui.screens.parent

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.ParentTab
import com.example.ui.viewmodel.VacciTrackViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ParentMainScreen(
    viewModel: VacciTrackViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.parentTab.collectAsState()
    val children by viewModel.children.collectAsState()
    val selectedChildId by viewModel.selectedChildId.collectAsState()
    val records by viewModel.currentChildRecords.collectAsState()
    val catalog by viewModel.vaccineCatalog.collectAsState()
    val facilities by viewModel.facilities.collectAsState()

    var showAddChildDialog by remember { mutableStateOf(false) }
    var selectedRecordForDetail by remember { mutableStateOf<VaccineRecordEntity?>(null) }
    var showCertificateDialog by remember { mutableStateOf(false) }
    var selectedFacilityForDetail by remember { mutableStateOf<HealthFacilityEntity?>(null) }

    val activeChild = children.firstOrNull { it.id == selectedChildId }
        ?: children.firstOrNull()
        ?: ChildEntity(1, "VT-MZ-000001", "Amílcar Macuácua", "07/01/2026", "Masculino", "8 meses", parentName = "Ana Macuácua", parentContact = "+258 84 123 4567")

    Scaffold(
        topBar = {
            ParentTopBar(
                activeChildName = activeChild.fullName,
                onOpenAlerts = { viewModel.setParentTab(ParentTab.ALERTAS) },
                onSwitchRole = { viewModel.navigateTo(AppDestination.DEMO_SELECTOR) }
            )
        },
        bottomBar = {
            ParentBottomNavigation(
                currentTab = currentTab,
                onTabSelected = { viewModel.setParentTab(it) }
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
                ParentTab.HOME -> ParentHomeScreen(
                    activeChild = activeChild,
                    children = children,
                    records = records,
                    onSelectChild = { viewModel.selectChild(it) },
                    onAddChild = { showAddChildDialog = true },
                    onNavigateTab = { viewModel.setParentTab(it) }
                )
                ParentTab.CADERNETA -> ParentCadernetaScreen(
                    child = activeChild,
                    records = records,
                    onOpenVaccineDetail = { selectedRecordForDetail = it },
                    onOpenCertificate = { showCertificateDialog = true }
                )
                ParentTab.CALENDARIO -> ParentCalendarScreen(
                    child = activeChild,
                    records = records,
                    onOpenVaccineDetail = { selectedRecordForDetail = it }
                )
                ParentTab.HISTORICO -> ParentHistoryScreen(
                    child = activeChild,
                    records = records,
                    onOpenVaccineDetail = { selectedRecordForDetail = it },
                    onOpenCertificate = { showCertificateDialog = true }
                )
                ParentTab.ALERTAS -> ParentAlertsScreen(
                    child = activeChild,
                    records = records,
                    onSchedule = { viewModel.showMessage("Ligando para US Polana (+258 21 461 234)...") }
                )
                ParentTab.VACINAS -> ParentVaccineGuideScreen(
                    catalog = catalog
                )
                ParentTab.UNIDADES -> ParentFacilitiesScreen(
                    facilities = facilities,
                    onSelectFacility = { selectedFacilityForDetail = it },
                    onCall = { facility -> viewModel.showMessage("A contactar ${facility.name} (${facility.phone})") }
                )
            }
        }
    }

    if (showAddChildDialog) {
        AddChildDialog(
            onDismiss = { showAddChildDialog = false },
            onConfirm = { name, birthDate, gender, birthPlace, birthWeight, parentName, parentContact, parentType, facilityName ->
                viewModel.registerNewChild(name, birthDate, gender, birthPlace, birthWeight, parentName, parentContact, parentType, facilityName) { newId ->
                    viewModel.selectChild(newId)
                }
            }
        )
    }

    selectedRecordForDetail?.let { record ->
        VaccineDetailDialog(
            record = record,
            child = activeChild,
            onDismiss = { selectedRecordForDetail = null },
            onOpenCertificate = {
                selectedRecordForDetail = null
                showCertificateDialog = true
            }
        )
    }

    if (showCertificateDialog) {
        DigitalCertificateDialog(
            child = activeChild,
            records = records,
            onDismiss = { showCertificateDialog = false },
            onShare = {
                viewModel.showMessage("Certificado Digital de ${activeChild.fullName} copiado e pronto a partilhar!")
            }
        )
    }

    selectedFacilityForDetail?.let { facility ->
        FacilityDetailDialog(
            facility = facility,
            onDismiss = { selectedFacilityForDetail = null },
            onCall = {
                viewModel.showMessage("A ligar para ${facility.name} (${facility.phone})...")
            }
        )
    }
}

@Composable
private fun ParentTopBar(
    activeChildName: String,
    onOpenAlerts: () -> Unit,
    onSwitchRole: () -> Unit
) {
    Surface(
        color = EmeraldDark,
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clickable { onSwitchRole() }
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "VacciTrack", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = " · Pais", color = EmeraldLight, fontSize = 12.sp)
                    }
                    Text(text = "Moçambique", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onOpenAlerts) {
                    BadgedBox(badge = {
                        Badge(containerColor = HealthRed) {
                            Text("3", fontSize = 10.sp, color = Color.White)
                        }
                    }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Alertas", tint = Color.White)
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(EmeraldLight)
                        .clickable { onSwitchRole() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "AM", color = EmeraldDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ParentBottomNavigation(
    currentTab: ParentTab,
    onTabSelected: (ParentTab) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        listOf(
            ParentTab.HOME to Icons.Default.Home,
            ParentTab.CADERNETA to Icons.Default.Description,
            ParentTab.CALENDARIO to Icons.Default.CalendarMonth,
            ParentTab.ALERTAS to Icons.Default.Notifications,
            ParentTab.VACINAS to Icons.Default.MedicalServices,
            ParentTab.UNIDADES to Icons.Default.LocationOn
        ).forEach { (tab, icon) ->
            val selected = currentTab == tab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = { Icon(icon, contentDescription = tab.label) },
                label = { Text(tab.label, fontSize = 10.sp, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = EmeraldDark,
                    selectedTextColor = EmeraldDark,
                    indicatorColor = EmeraldSoftBg,
                    unselectedIconColor = SlateMedium,
                    unselectedTextColor = SlateMedium
                )
            )
        }
    }
}

// ----------------- 1. PARENT HOME SCREEN -----------------
@Composable
fun ParentHomeScreen(
    activeChild: ChildEntity,
    children: List<ChildEntity>,
    records: List<VaccineRecordEntity>,
    onSelectChild: (Long) -> Unit,
    onAddChild: () -> Unit,
    onNavigateTab: (ParentTab) -> Unit
) {
    val adminCount = records.count { it.status == "admin" }
    val proxCount = records.count { it.status == "prox" }
    val atrasoCount = records.count { it.status == "atraso" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(text = "Olá, Ana 👋", fontSize = 13.sp, color = SlateMedium)
                Text(text = "A sua caderneta digital", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SlateDark)
            }
        }

        // Children Selector Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(children) { child ->
                    val isSelected = child.id == activeChild.id
                    Card(
                        modifier = Modifier
                            .width(110.dp)
                            .clickable { onSelectChild(child.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = if (isSelected) BorderStroke(2.dp, EmeraldPrimary) else BorderStroke(1.dp, SlateBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (child.gender == "Feminino") HealthPinkLight else EmeraldSoftBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = child.avatarEmoji, fontSize = 22.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = child.fullName.split(" ").firstOrNull() ?: child.fullName, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(text = child.ageDescription, fontSize = 11.sp, color = SlateMedium)
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .width(110.dp)
                            .clickable { onAddChild() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = EmeraldSoftBg),
                        border = BorderStroke(1.5.dp, EmeraldPrimary.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Adicionar", tint = EmeraldDark)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Adicionar", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
                            Text(text = "Novo filho", fontSize = 11.sp, color = EmeraldDeep)
                        }
                    }
                }
            }
        }

        // Hero Upcoming Vaccine Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(EmeraldPrimary, EmeraldDark, EmeraldDeep)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PulseDot(color = Color(0xFFFDE047))
                            Text(text = "Próxima vacinação", color = EmeraldLight, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(text = "Sarampo — 1ª dose", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Para ${activeChild.fullName} · 09 de Setembro 2026", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column {
                                    Text(text = "Administradas", fontSize = 10.sp, color = EmeraldLight)
                                    Text(text = "$adminCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Column {
                                    Text(text = "Próximas", fontSize = 10.sp, color = EmeraldLight)
                                    Text(text = "$proxCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Column {
                                    Text(text = "Em atraso", fontSize = 10.sp, color = EmeraldLight)
                                    Text(text = "$atrasoCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFDE047))
                                }
                            }

                            Button(
                                onClick = { onNavigateTab(ParentTab.CADERNETA) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = EmeraldDark),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(text = "Ver caderneta", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Quick Actions Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionItem(
                    title = "Caderneta",
                    subtitle = "Esquema vacinal",
                    icon = Icons.Default.Description,
                    bgColor = EmeraldSoftBg,
                    tint = EmeraldDark,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(ParentTab.CADERNETA) }
                )
                QuickActionItem(
                    title = "Calendário",
                    subtitle = "Datas PTV",
                    icon = Icons.Default.CalendarMonth,
                    bgColor = HealthBlueLight,
                    tint = HealthBlueDark,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(ParentTab.CALENDARIO) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionItem(
                    title = "Alertas",
                    subtitle = "3 avisos",
                    icon = Icons.Default.Notifications,
                    bgColor = HealthAmberLight,
                    tint = HealthAmber,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(ParentTab.ALERTAS) }
                )
                QuickActionItem(
                    title = "Unidades",
                    subtitle = "Postos de saúde",
                    icon = Icons.Default.LocationOn,
                    bgColor = HealthPurpleLight,
                    tint = HealthPurpleDark,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(ParentTab.UNIDADES) }
                )
            }
        }

        // Recent Activity Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Atividade Recente", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateDark)
                        Text(
                            text = "Ver tudo →",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark,
                            modifier = Modifier.clickable { onNavigateTab(ParentTab.HISTORICO) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        RecentActivityRow(
                            emoji = "💉",
                            title = "Pneumo 3 — administrada",
                            subtitle = "US Polana · 15 Ago 2026",
                            status = "admin"
                        )
                        RecentActivityRow(
                            emoji = "⏰",
                            title = "Sarampo 1 — prevista",
                            subtitle = "09 Set 2026 · US Polana",
                            status = "prox"
                        )
                        RecentActivityRow(
                            emoji = "⚠️",
                            title = "Febre Amarela — em atraso",
                            subtitle = "Prevista: 09 Ago 2026",
                            status = "atraso"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bgColor: Color,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SlateBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateDark)
                Text(text = subtitle, fontSize = 10.sp, color = SlateMedium)
            }
        }
    }
}

@Composable
private fun RecentActivityRow(
    emoji: String,
    title: String,
    subtitle: String,
    status: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SlateBg)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = emoji, fontSize = 20.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = SlateDark)
            Text(text = subtitle, fontSize = 10.sp, color = SlateMedium)
        }
        VaccineStatusBadge(status = status)
    }
}

// ----------------- 2. CADERNETA DIGITAL -----------------
@Composable
fun ParentCadernetaScreen(
    child: ChildEntity,
    records: List<VaccineRecordEntity>,
    onOpenVaccineDetail: (VaccineRecordEntity) -> Unit = {},
    onOpenCertificate: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("all") }

    val adminCount = records.count { it.status == "admin" }
    val proxCount = records.count { it.status == "prox" }
    val atrasoCount = records.count { it.status == "atraso" }
    val pendCount = records.count { it.status == "pend" }

    val filteredRecords = when (selectedFilter) {
        "admin" -> records.filter { it.status == "admin" }
        "prox" -> records.filter { it.status == "prox" }
        "atraso" -> records.filter { it.status == "atraso" }
        "pend" -> records.filter { it.status == "pend" }
        else -> records
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Child Info Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(EmeraldSoftBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = child.avatarEmoji, fontSize = 28.sp)
                            }
                            Column {
                                Text(text = child.fullName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                                Text(text = "Nascimento: ${child.birthDate} (${child.ageDescription})", fontSize = 12.sp, color = SlateMedium)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldSoftBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = child.code, color = EmeraldDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = SlateBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "US: ${child.facilityName}", fontSize = 11.sp, color = SlateMedium)
                        Text(text = "Sexo: ${child.gender}", fontSize = 11.sp, color = SlateMedium)
                        Text(text = "Peso: ${child.birthWeight}", fontSize = 11.sp, color = SlateMedium)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onOpenCertificate,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Emitir Certificado Digital (QR Code)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Summary Counts
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryPill(count = adminCount, label = "Admin", color = EmeraldPrimary, isSelected = selectedFilter == "admin", onClick = { selectedFilter = if (selectedFilter == "admin") "all" else "admin" }, modifier = Modifier.weight(1f))
                SummaryPill(count = proxCount, label = "Próximas", color = HealthAmber, isSelected = selectedFilter == "prox", onClick = { selectedFilter = if (selectedFilter == "prox") "all" else "prox" }, modifier = Modifier.weight(1f))
                SummaryPill(count = atrasoCount, label = "Atraso", color = HealthRed, isSelected = selectedFilter == "atraso", onClick = { selectedFilter = if (selectedFilter == "atraso") "all" else "atraso" }, modifier = Modifier.weight(1f))
                SummaryPill(count = pendCount, label = "Pendentes", color = SlateMedium, isSelected = selectedFilter == "pend", onClick = { selectedFilter = if (selectedFilter == "pend") "all" else "pend" }, modifier = Modifier.weight(1f))
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Esquema Vacinal (${filteredRecords.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SlateDark)
                Text(text = "Toque para detalhes", fontSize = 11.sp, color = SlateMedium)
            }
        }

        items(filteredRecords) { record ->
            VaccineCardItem(
                record = record,
                onClick = { onOpenVaccineDetail(record) }
            )
        }
    }
}

@Composable
private fun SummaryPill(
    count: Int,
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) color.copy(alpha = 0.15f) else Color.White),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) color else SlateBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "$count", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, fontSize = 10.sp, color = SlateMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun VaccineCardItem(
    record: VaccineRecordEntity,
    onClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SlateBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val statusColor = when (record.status) {
                "admin" -> EmeraldPrimary
                "prox" -> HealthAmber
                "atraso" -> HealthRed
                else -> Color(0xFF94A3B8)
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(statusColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = record.vaccineName.take(3).uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = statusColor
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = record.vaccineName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateDark)
                Text(text = "${record.doseName} · ${record.scheduledAge}", fontSize = 11.sp, color = SlateMedium)
                val dateLabel = if (record.administeredDate != null) "Administrada: ${record.administeredDate}" else "Prevista: ${record.scheduledDate}"
                Text(text = dateLabel, fontSize = 10.sp, color = if (record.status == "atraso") HealthRed else SlateMedium)
            }

            VaccineStatusBadge(status = record.status)
        }
    }
}

// ----------------- 3. CALENDÁRIO -----------------
@Composable
fun ParentCalendarScreen(
    child: ChildEntity,
    records: List<VaccineRecordEntity>,
    onOpenVaccineDetail: (VaccineRecordEntity) -> Unit = {}
) {
    var currentMonth by remember { mutableStateOf("Agosto 2026") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(text = "Calendário Vacinal", fontSize = 13.sp, color = SlateMedium)
                Text(text = "$currentMonth", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SlateDark)
            }
        }

        // Calendar Month View Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { currentMonth = "Julho 2026" }) {
                            Text("←", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(text = currentMonth, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        IconButton(onClick = { currentMonth = "Setembro 2026" }) {
                            Text("→", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Days of week header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb").forEach { day ->
                            Text(text = day, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateMedium, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Simple calendar grid representation for August 2026
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Week 1
                        CalendarWeekRow(days = listOf(null, null, null, null, null, null, 1))
                        // Week 2
                        CalendarWeekRow(days = listOf(2, 3, 4, 5, 6, 7, 8))
                        // Week 3 (09 is atraso, 15 is admin)
                        CalendarWeekRow(
                            days = listOf(9, 10, 11, 12, 13, 14, 15),
                            specialDays = mapOf(9 to HealthRed, 15 to EmeraldPrimary)
                        )
                        // Week 4 (20 is today)
                        CalendarWeekRow(
                            days = listOf(16, 17, 18, 19, 20, 21, 22),
                            today = 20
                        )
                        // Week 5
                        CalendarWeekRow(days = listOf(23, 24, 25, 26, 27, 28, 29))
                        // Week 6
                        CalendarWeekRow(days = listOf(30, 31, null, null, null, null, null))
                    }
                }
            }
        }

        item {
            Text(text = "Agenda de Vacinação", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SlateDark)
        }

        // Agenda Items
        item {
            AgendaCard(
                month = "AGO",
                day = "09",
                title = "Febre Amarela",
                subtitle = "⚠️ Em atraso — Dirija-se à US Polana",
                color = HealthRed,
                bgColor = HealthRedLight,
                onClick = {
                    val r = records.firstOrNull { it.vaccineName.contains("Febre", ignoreCase = true) }
                    if (r != null) onOpenVaccineDetail(r)
                }
            )
        }

        item {
            AgendaCard(
                month = "SET",
                day = "09",
                title = "Sarampo — 1ª dose",
                subtitle = "Prevista daqui a 20 dias · US Polana",
                color = HealthAmber,
                bgColor = HealthAmberLight,
                onClick = {
                    val r = records.firstOrNull { it.vaccineName.contains("Sarampo", ignoreCase = true) }
                    if (r != null) onOpenVaccineDetail(r)
                }
            )
        }

        item {
            AgendaCard(
                month = "SET",
                day = "09",
                title = "VIP — 2ª dose (reforço)",
                subtitle = "Prevista daqui a 20 dias · US Polana",
                color = HealthAmber,
                bgColor = HealthAmberLight,
                onClick = {
                    val r = records.firstOrNull { it.vaccineName.contains("VIP", ignoreCase = true) }
                    if (r != null) onOpenVaccineDetail(r)
                }
            )
        }
    }
}

@Composable
private fun CalendarWeekRow(
    days: List<Int?>,
    specialDays: Map<Int, Color> = emptyMap(),
    today: Int? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        days.forEach { day ->
            if (day == null) {
                Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
            } else {
                val isToday = day == today
                val specialColor = specialDays[day]
                val bg = when {
                    isToday -> EmeraldDark
                    specialColor != null -> specialColor.copy(alpha = 0.18f)
                    else -> Color.Transparent
                }
                val textColor = when {
                    isToday -> Color.White
                    specialColor != null -> specialColor
                    else -> SlateDark
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bg)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$day",
                        fontSize = 12.sp,
                        fontWeight = if (isToday || specialColor != null) FontWeight.Bold else FontWeight.Normal,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun AgendaCard(
    month: String,
    day: String,
    title: String,
    subtitle: String,
    color: Color,
    bgColor: Color,
    onClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = month, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = day, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, lineHeight = 16.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateDark)
                Text(text = subtitle, fontSize = 11.sp, color = color)
            }
        }
    }
}

// ----------------- 4. HISTÓRICO -----------------
@Composable
fun ParentHistoryScreen(
    child: ChildEntity,
    records: List<VaccineRecordEntity>,
    onOpenVaccineDetail: (VaccineRecordEntity) -> Unit = {},
    onOpenCertificate: () -> Unit = {}
) {
    val adminRecords = records.filter { it.status == "admin" }.reversed()

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
                    Text(text = "Histórico Completo", fontSize = 13.sp, color = SlateMedium)
                    Text(text = "Doses Administradas (${adminRecords.size})", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                }
                Button(
                    onClick = onOpenCertificate,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Certificado", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(adminRecords) { record ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SlateBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenVaccineDetail(record) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(EmeraldSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "💉", fontSize = 18.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "${record.vaccineName} · ${record.doseName}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateDark)
                            VaccineStatusBadge(status = "admin")
                        }
                        Text(text = "${record.administeredDate ?: record.scheduledDate} · ${record.facilityName}", fontSize = 12.sp, color = EmeraldDark, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Profissional: ${record.professionalName ?: "Enf. de Turno"} · Lote: ${record.lotNumber ?: "PT-2026"}",
                            fontSize = 11.sp,
                            color = SlateMedium
                        )
                    }
                }
            }
        }
    }
}

// ----------------- 5. ALERTAS -----------------
@Composable
fun ParentAlertsScreen(
    child: ChildEntity,
    records: List<VaccineRecordEntity>,
    onSchedule: () -> Unit
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
                Text(text = "Central de Notificações", fontSize = 13.sp, color = SlateMedium)
                Text(text = "Alertas e Lembretes", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SlateDark)
            }
        }

        item {
            AlertCardItem(
                emoji = "⚠️",
                title = "Dose em atraso",
                time = "há 11 dias",
                message = "Febre Amarela de ${child.fullName} estava prevista para 09/08/2026. Agende ou dirija-se à US mais próxima.",
                borderColor = HealthRed,
                actionLabel = "Agendar agora →",
                onAction = onSchedule
            )
        }

        item {
            AlertCardItem(
                emoji = "🔔",
                title = "Vacinação prevista em 20 dias",
                time = "hoje",
                message = "Sarampo (1ª dose) de ${child.fullName} está agendada para 09/09/2026 na US Polana.",
                borderColor = HealthAmber
            )
        }

        item {
            AlertCardItem(
                emoji = "✅",
                title = "Vacinação registada no sistema",
                time = "há 5 dias",
                message = "Pneumo 3 foi confirmada pelo profissional de saúde. A caderneta digital está actualizada.",
                borderColor = EmeraldPrimary
            )
        }

        item {
            AlertCardItem(
                emoji = "📢",
                title = "Actualização do Calendário Nacional",
                time = "há 2 semanas",
                message = "O Ministério da Saúde publicou novas directrizes do Programa Tempos de Vacinação (PTV 2026).",
                borderColor = HealthBlue
            )
        }
    }
}

@Composable
private fun AlertCardItem(
    emoji: String,
    title: String,
    time: String,
    message: String,
    borderColor: Color,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SlateBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(borderColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 20.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateDark)
                    Text(text = time, fontSize = 10.sp, color = SlateLight)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(text = message, fontSize = 12.sp, color = SlateMedium, lineHeight = 16.sp)

                if (actionLabel != null && onAction != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = actionLabel,
                        color = EmeraldDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onAction() }
                    )
                }
            }
        }
    }
}

// ----------------- 6. GUIA DE VACINAS -----------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ParentVaccineGuideScreen(
    catalog: List<VaccineCatalogEntity>
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todas") }

    val categories = listOf("Todas", "Ao nascer", "6-14 semanas", "9-18 meses", "Adolescentes")

    val filtered = catalog.filter { item ->
        val matchesQuery = item.name.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true) ||
                item.targetDisease.contains(searchQuery, ignoreCase = true)
        val matchesCategory = when (selectedCategory) {
            "Ao nascer" -> item.scheduledAges.contains("nascer", ignoreCase = true)
            "6-14 semanas" -> item.scheduledAges.contains("semana", ignoreCase = true) || item.scheduledAges.contains("meses", ignoreCase = true)
            "9-18 meses" -> item.scheduledAges.contains("9", ignoreCase = true) || item.scheduledAges.contains("18", ignoreCase = true)
            "Adolescentes" -> item.scheduledAges.contains("9-14", ignoreCase = true) || item.name.contains("HPV", ignoreCase = true)
            else -> true
        }
        matchesQuery && matchesCategory
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
                Text(text = "Informação Educativa", fontSize = 13.sp, color = SlateMedium)
                Text(text = "Vacinas do Calendário PTV", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SlateDark)
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Pesquisar vacina ou doença...") },
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
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) EmeraldDark else Color.White)
                            .border(1.dp, if (isSelected) EmeraldDark else SlateBorder, RoundedCornerShape(20.dp))
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else SlateDark
                        )
                    }
                }
            }
        }

        items(filtered) { item ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(EmeraldSoftBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = item.emoji, fontSize = 22.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SlateDark)
                            Text(text = "${item.dosesCount} dose(s) · ${item.scheduledAges}", fontSize = 11.sp, color = EmeraldDark, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = item.description, fontSize = 12.sp, color = SlateMedium, lineHeight = 17.sp)

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "🛡️ Protege contra: ${item.targetDisease}", fontSize = 10.sp, color = SlateDark, fontWeight = FontWeight.Medium)
                    }
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
                    Text(text = "⚕️ Nota Importante", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF78350F))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Esta informação é educativa e não substitui a consulta com o profissional de saúde. Contraindicações e decisões clínicas devem ser sempre avaliadas presencialmente na unidade sanitária.",
                        fontSize = 11.sp,
                        color = Color(0xFF92400E),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

// ----------------- 7. UNIDADES SANITÁRIAS -----------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ParentFacilitiesScreen(
    facilities: List<HealthFacilityEntity>,
    onSelectFacility: (HealthFacilityEntity) -> Unit = {},
    onCall: (HealthFacilityEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filtered = facilities.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.address.contains(searchQuery, ignoreCase = true) ||
                it.region.contains(searchQuery, ignoreCase = true)
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
                Text(text = "Rede de Saúde", fontSize = 13.sp, color = SlateMedium)
                Text(text = "Unidades Sanitárias", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SlateDark)
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Pesquisar unidade ou localização...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SlateMedium) },
                trailingIcon = {
                    TextButton(onClick = { searchQuery = "Maputo" }) {
                        Text("📍 Perto", fontSize = 12.sp, color = EmeraldDark, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )
        }

        items(filtered) { facility ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SlateBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectFacility(facility) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(HealthPurpleLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = HealthPurpleDark)
                            }
                            Column {
                                Text(text = facility.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateDark)
                                Text(text = "${facility.type} · ${facility.distance}", fontSize = 11.sp, color = SlateMedium)
                            }
                        }

                        StatusChip(text = "Aberto", backgroundColor = EmeraldSoftBg, textColor = EmeraldDark)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = facility.address, fontSize = 12.sp, color = SlateMedium)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlateBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "🕐 ${facility.operatingHours}", fontSize = 11.sp, color = SlateDark)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlateBg)
                                .clickable { onCall(facility) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "📞 ${facility.phone}", fontSize = 11.sp, color = EmeraldDark, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        facility.services.split(",").forEach { s ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(EmeraldSoftBg)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = s.trim(), fontSize = 10.sp, color = EmeraldDark)
                            }
                        }
                    }
                }
            }
        }
    }
}
