package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ChildEntity
import com.example.data.model.HealthFacilityEntity
import com.example.data.model.VaccineRecordEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChildDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        birthDate: String,
        gender: String,
        birthPlace: String,
        birthWeight: String,
        parentName: String,
        parentContact: String,
        parentType: String,
        facilityName: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("2026-08-20") }
    var gender by remember { mutableStateOf("Masculino") }
    var birthPlace by remember { mutableStateOf("Hospital Central de Maputo") }
    var birthWeight by remember { mutableStateOf("3.2 kg") }
    var parentName by remember { mutableStateOf("Ana Macuácua") }
    var parentContact by remember { mutableStateOf("+258 84 123 4567") }
    var parentType by remember { mutableStateOf("Pai/Mãe") }
    var facilityName by remember { mutableStateOf("US Polana") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Novo Utente", fontSize = 12.sp, color = SlateMedium)
                        Text(text = "Registar Criança / Gestante", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = SlateMedium)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome Completo *") },
                    placeholder = { Text("Ex: Amílcar Macuácua") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                MaterialDatePickerField(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    label = "Data de Nascimento *",
                    testTag = "dialog_birth_date_picker"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { gender = "Masculino" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (gender == "Masculino") EmeraldSoftBg else Color.Transparent
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(if (gender == "Masculino") EmeraldPrimary else SlateBorder)
                        )
                    ) {
                        Text("👦 Masculino", color = if (gender == "Masculino") EmeraldDark else SlateDark)
                    }

                    OutlinedButton(
                        onClick = { gender = "Feminino" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (gender == "Feminino") HealthPinkLight else Color.Transparent
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(if (gender == "Feminino") HealthPink else SlateBorder)
                        )
                    ) {
                        Text("👧 Feminino", color = if (gender == "Feminino") HealthPink else SlateDark)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = parentName,
                    onValueChange = { parentName = it },
                    label = { Text("Nome do Responsável *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = parentContact,
                    onValueChange = { parentContact = it },
                    label = { Text("Telefone / Contacto *") },
                    placeholder = { Text("+258 84 ...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = birthPlace,
                    onValueChange = { birthPlace = it },
                    label = { Text("Local de Nascimento") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = facilityName,
                    onValueChange = { facilityName = it },
                    label = { Text("Unidade Sanitária de Referência") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank() && parentName.isNotBlank()) {
                            onConfirm(name, birthDate, gender, birthPlace, birthWeight, parentName, parentContact, parentType, facilityName)
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Registar e Gerar Código PTV", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FastAdministerDialog(
    child: ChildEntity,
    vaccinePreselected: String = "Febre Amarela",
    dosePreselected: String = "Dose única",
    onDismiss: () -> Unit,
    onConfirm: (
        vaccineName: String,
        doseName: String,
        administeredDate: String,
        lotNumber: String,
        notes: String?,
        isOffline: Boolean
    ) -> Unit
) {
    var vaccineName by remember { mutableStateOf(vaccinePreselected) }
    var doseName by remember { mutableStateOf(dosePreselected) }
    var administeredDate by remember { mutableStateOf("2026-08-20") }
    var lotNumber by remember { mutableStateOf("FA-2026-06") }
    var notes by remember { mutableStateOf("") }
    var isOffline by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Administração Rápida", fontSize = 12.sp, color = SlateMedium)
                        Text(text = child.fullName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = SlateMedium)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Código: ${child.code} · ${child.ageDescription}", fontSize = 12.sp, color = EmeraldDark, fontWeight = FontWeight.SemiBold)

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = vaccineName,
                    onValueChange = { vaccineName = it },
                    label = { Text("Vacina *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = doseName,
                    onValueChange = { doseName = it },
                    label = { Text("Dose *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                MaterialDatePickerField(
                    value = administeredDate,
                    onValueChange = { administeredDate = it },
                    label = "Data de Administração *",
                    testTag = "fast_administer_date_picker"
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = lotNumber,
                    onValueChange = { lotNumber = it },
                    label = { Text("Número de Lote *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações Clínicas (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isOffline,
                        onCheckedChange = { isOffline = it },
                        colors = CheckboxDefaults.colors(checkedColor = EmeraldDark)
                    )
                    Text(text = "Guardar na fila offline (sem internet)", fontSize = 12.sp, color = SlateDark)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (vaccineName.isNotBlank() && lotNumber.isNotBlank()) {
                            onConfirm(vaccineName, doseName, administeredDate, lotNumber, notes.ifBlank { null }, isOffline)
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HealthBlueDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("✓ Validar e Registar Dose", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SmsGuardianDialog(
    childName: String,
    parentContact: String,
    vaccineName: String,
    daysDelayed: Int,
    onDismiss: () -> Unit,
    onSent: () -> Unit
) {
    var messageText by remember {
        mutableStateOf("Caro(a) encarregado(a), a vacina $vaccineName de $childName está com $daysDelayed dias em atraso. Por favor, dirija-se à Unidade Sanitária mais próxima para actualizar a caderneta digital.")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Notificação SMS / WhatsApp", fontSize = 12.sp, color = SlateMedium)
                        Text(text = "Contactar Responsável", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Destinatário: $parentContact", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = HealthBlueDark)

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    label = { Text("Texto da Mensagem") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onSent()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("✉️ Enviar Alerta SMS", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun VaccineDetailDialog(
    record: VaccineRecordEntity,
    child: ChildEntity,
    onDismiss: () -> Unit,
    onOpenCertificate: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (record.status == "admin") EmeraldSoftBg else HealthAmberLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = if (record.status == "admin") "💉" else "⏳", fontSize = 20.sp)
                        }
                        Column {
                            Text(text = record.vaccineName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                            Text(text = record.doseName, fontSize = 12.sp, color = SlateMedium)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = SlateBorder)
                Spacer(modifier = Modifier.height(12.dp))

                // Status banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when (record.status) {
                                "admin" -> EmeraldSoftBg
                                "atraso" -> HealthRedLight
                                "prox" -> HealthAmberLight
                                else -> SlateBg
                            }
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = when (record.status) {
                                "admin" -> "Estado: Administrada com Sucesso"
                                "atraso" -> "Estado: Dose em Atraso"
                                "prox" -> "Estado: Próxima Vacinação Prevista"
                                else -> "Estado: Dose Pendente no Esquema"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = when (record.status) {
                                "admin" -> EmeraldDark
                                "atraso" -> HealthRed
                                "prox" -> HealthAmber
                                else -> SlateDark
                            }
                        )
                        Text(
                            text = if (record.administeredDate != null) "Data: ${record.administeredDate}" else "Data prevista: ${record.scheduledDate}",
                            fontSize = 11.sp,
                            color = SlateMedium
                        )
                    }
                    StatusBadge(status = record.status)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Detail Rows
                DetailItem(label = "Utente", value = "${child.fullName} (${child.code})")
                DetailItem(label = "Idade Recomendada", value = record.scheduledAge)
                DetailItem(label = "Unidade Sanitária", value = record.facilityName)
                if (record.professionalName != null) {
                    DetailItem(label = "Profissional de Saúde", value = record.professionalName)
                }
                if (record.lotNumber != null) {
                    DetailItem(label = "Número do Lote", value = record.lotNumber)
                }
                if (record.notes != null && record.notes.isNotBlank()) {
                    DetailItem(label = "Observações", value = record.notes)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Clinical care advice
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "💡 Cuidados Pós-Vacinação", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SlateDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Febre ligeira ou sensibilidade no local da aplicação são normais. Mantenha o aleitamento/hidratação. Em caso de reacções adversas graves, dirija-se à US mais próxima.",
                            fontSize = 11.sp,
                            color = SlateMedium,
                            lineHeight = 15.sp
                        )
                    }
                }

                if (record.status == "admin") {
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            onDismiss()
                            onOpenCertificate()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ver Certificado Digital (QR Code)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DigitalCertificateDialog(
    child: ChildEntity,
    records: List<VaccineRecordEntity>,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    val adminDoses = records.filter { it.status == "admin" }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Certificado Oficial", fontSize = 12.sp, color = EmeraldDark, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Emblem
                Text(text = "🇲🇿", fontSize = 36.sp)
                Text(
                    text = "REPÚBLICA DE MOÇAMBIQUE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "MINISTÉRIO DA SAÚDE · MISAU",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldDark
                )
                Text(
                    text = "Certificado Digital de Vacinação PTV",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SlateDark,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Simulated QR Code Frame
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SlateBg)
                        .border(2.dp, EmeraldPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "QR Code",
                            tint = EmeraldDark,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = child.code, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Child credentials
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Utente: ${child.fullName}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateDark)
                        Text(text = "Nascimento: ${child.birthDate} · Sexo: ${child.gender}", fontSize = 11.sp, color = SlateMedium)
                        Text(text = "Encarregado(a): ${child.parentName}", fontSize = 11.sp, color = SlateMedium)
                        Text(text = "Unidade Sanitária: ${child.facilityName}", fontSize = 11.sp, color = EmeraldDark, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Doses Válidas e Autenticadas (${adminDoses.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    adminDoses.forEach { dose ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF0FDF4))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(14.dp))
                                Text(text = "${dose.vaccineName} (${dose.doseName})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SlateDark)
                            }
                            Text(text = dose.administeredDate ?: "Sim", fontSize = 10.sp, color = EmeraldDark, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Fechar")
                    }

                    Button(
                        onClick = {
                            onShare()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Partilhar")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FacilityDetailDialog(
    facility: HealthFacilityEntity,
    onDismiss: () -> Unit,
    onCall: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                            Text(text = facility.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                            Text(text = "${facility.type} · ${facility.region}", fontSize = 12.sp, color = SlateMedium)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = SlateBorder)
                Spacer(modifier = Modifier.height(12.dp))

                DetailItem(label = "Endereço", value = facility.address)
                DetailItem(label = "Horário de Vacinação PTV", value = facility.operatingHours)
                DetailItem(label = "Distância aproximada", value = facility.distance)
                DetailItem(label = "Telefone da Recepção", value = facility.phone)

                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Serviços Disponíveis:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    facility.services.split(",").forEach { s ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldSoftBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = s.trim(), fontSize = 11.sp, color = EmeraldDark, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        onCall()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ligar para a Unidade Sanitária", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = SlateMedium)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SlateDark, textAlign = TextAlign.End)
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (label, bg, color) = when (status) {
        "admin" -> Triple("Administrada", EmeraldSoftBg, EmeraldDark)
        "prox" -> Triple("Prevista", HealthAmberLight, HealthAmber)
        "atraso" -> Triple("Em atraso", HealthRedLight, HealthRed)
        else -> Triple("Pendente", SlateBg, SlateMedium)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
