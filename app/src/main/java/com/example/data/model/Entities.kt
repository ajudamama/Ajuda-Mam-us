package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class VaccineStatus(val code: String, val label: String) {
    ADMINISTERED("admin", "✓ Administrada"),
    UPCOMING("prox", "⏰ Próxima"),
    DELAYED("atraso", "⚠️ Em atraso"),
    PENDING("pend", "Pendente");

    companion object {
        fun fromCode(code: String): VaccineStatus =
            values().firstOrNull { it.code == code } ?: PENDING
    }
}

enum class UserRole(val code: String, val label: String) {
    PARENT("parent", "Pai / Mãe"),
    PROFESSIONAL("pro", "Profissional de Saúde"),
    ADMIN("admin", "Administrador")
}

/**
 * Entidade Paciente / Criança (Patient / Child Entity)
 * Representa o utente titular da caderneta de vacinação.
 */
@Entity(
    tableName = "children",
    indices = [
        Index(value = ["code"], unique = true),
        Index(value = ["fullName"])
    ]
)
data class ChildEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String, // e.g. "VT-MZ-000001"
    val fullName: String,
    val birthDate: String, // "07/01/2026"
    val gender: String, // "Masculino" or "Feminino"
    val ageDescription: String, // "8 meses"
    val birthPlace: String = "Maputo",
    val birthWeight: String = "3.2 kg",
    val parentName: String,
    val parentContact: String,
    val parentType: String = "Pai/Mãe", // "Pai/Mãe" or "Gestante"
    val facilityName: String = "US Polana",
    val avatarEmoji: String = "👶",
    val isPregnantMother: Boolean = false,
    val notes: String = ""
)

typealias PacienteEntity = ChildEntity
typealias PatientEntity = ChildEntity

/**
 * Entidade Registo de Vacinação (Vaccination Record Entity)
 * Representa cada dose de vacina no esquema do utente/paciente.
 */
@Entity(
    tableName = "vaccine_records",
    indices = [
        Index(value = ["childId"]),
        Index(value = ["status"]),
        Index(value = ["vaccineName"])
    ]
)
data class VaccineRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val childId: Long,
    val vaccineName: String,
    val doseName: String, // "1ª dose", "Reforço", "Dose única"
    val scheduledAge: String, // "Ao nascer", "6 semanas", "9 meses"
    val scheduledDate: String, // "09/09/2026"
    val administeredDate: String? = null,
    val status: String, // "admin", "prox", "atraso", "pend"
    val facilityName: String = "US Polana",
    val professionalName: String? = null,
    val lotNumber: String? = null,
    val expirationDate: String? = null,
    val notes: String? = null,
    val isOfflinePending: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

typealias RegistroVacinaEntity = VaccineRecordEntity
typealias VaccinationRecordEntity = VaccineRecordEntity

/**
 * Entidade Vacina / Catálogo de Vacinas (Vaccine Catalog Entity)
 * Informações oficiais sobre as vacinas do PTV (Programa Alargado de Vacinação).
 */
@Entity(
    tableName = "vaccine_catalog",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class VaccineCatalogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String,
    val targetDisease: String,
    val dosesCount: String,
    val scheduledAges: String,
    val description: String,
    val route: String = "Intramuscular / Oral",
    val isActive: Boolean = true
)

typealias VacinaEntity = VaccineCatalogEntity
typealias VaccineEntity = VaccineCatalogEntity

@Entity(tableName = "health_facilities")
data class HealthFacilityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "Unidade Sanitária", "Hospital Central", "Centro de Saúde"
    val distance: String, // "1.2 km"
    val address: String,
    val operatingHours: String,
    val phone: String,
    val services: String, // "Vacinação,Consultas,Maternidade"
    val region: String = "Maputo",
    val coveragePct: Int = 85,
    val registeredChildrenCount: Int = 1200,
    val status: String = "Activa"
)

@Entity(tableName = "health_workers")
data class HealthWorkerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val title: String, // "Enfermeiro", "Médico"
    val facilityName: String,
    val avatarInitials: String,
    val dosesCount: Int,
    val childrenCount: Int,
    val isActive: Boolean = true
)

@Entity(tableName = "vaccine_stock")
data class VaccineStockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vaccineName: String,
    val lotNumber: String,
    val quantity: Int,
    val expirationDate: String,
    val status: String = "OK" // "OK", "A vencer", "Expirado"
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subtitle: String,
    val category: String, // "Vacinação", "Utente", "Correcção", "Calendário", "Segurança"
    val timeAgo: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "user_accounts",
    indices = [
        Index(value = ["emailOrPhone"], unique = true)
    ]
)
data class UserAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emailOrPhone: String,
    val password: String = "123456",
    val role: String, // "parent", "pro", "admin"
    val parentType: String = "Pai/Mãe", // "Pai/Mãe", "Gestante", "Profissional", "Admin"
    val province: String = "Maputo",
    val phone: String = "",
    val email: String = "",
    val firebaseUid: String = "",
    val childrenCount: Int = 0,
    val status: String = "Activo",
    val createdAt: Long = System.currentTimeMillis()
)
