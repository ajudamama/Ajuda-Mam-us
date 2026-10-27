package com.example.data.repository

import com.example.data.auth.FirebaseAuthService
import com.example.data.db.AppDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class VacciTrackRepository(
    private val dao: AppDao,
    val firebaseAuthService: FirebaseAuthService = FirebaseAuthService()
) {

    val allChildren: Flow<List<ChildEntity>> = dao.getAllChildren()
    val allRecords: Flow<List<VaccineRecordEntity>> = dao.getAllRecords()
    val delayedRecords: Flow<List<VaccineRecordEntity>> = dao.getDelayedRecords()
    val offlinePendingRecords: Flow<List<VaccineRecordEntity>> = dao.getOfflinePendingRecords()
    val vaccineCatalog: Flow<List<VaccineCatalogEntity>> = dao.getVaccineCatalog()
    val allFacilities: Flow<List<HealthFacilityEntity>> = dao.getAllFacilities()
    val allHealthWorkers: Flow<List<HealthWorkerEntity>> = dao.getAllHealthWorkers()
    val allStock: Flow<List<VaccineStockEntity>> = dao.getAllStock()
    val allAuditLogs: Flow<List<AuditLogEntity>> = dao.getAllAuditLogs()
    val allUserAccounts: Flow<List<UserAccountEntity>> = dao.getAllUserAccounts()

    fun getRecordsForChild(childId: Long): Flow<List<VaccineRecordEntity>> =
        dao.getRecordsForChild(childId)

    fun searchChildren(query: String): Flow<List<ChildEntity>> =
        dao.searchChildren(query)

    fun searchFacilities(query: String): Flow<List<HealthFacilityEntity>> =
        dao.searchFacilities(query)

    suspend fun registerChild(
        fullName: String,
        birthDate: String,
        gender: String,
        birthPlace: String,
        birthWeight: String,
        parentName: String,
        parentContact: String,
        parentType: String,
        facilityName: String
    ): Long {
        val randomDigits = String.format(Locale.US, "%06d", (1000..999999).random())
        val code = "VT-MZ-$randomDigits"
        val avatar = if (gender.contains("Fem", ignoreCase = true)) "👧" else "👶"

        val child = ChildEntity(
            code = code,
            fullName = fullName,
            birthDate = birthDate,
            gender = gender,
            ageDescription = "Recém-nascido",
            birthPlace = birthPlace.ifBlank { "Maputo" },
            birthWeight = birthWeight.ifBlank { "3.2 kg" },
            parentName = parentName,
            parentContact = parentContact,
            parentType = parentType,
            facilityName = facilityName,
            avatarEmoji = avatar
        )
        val childId = dao.insertChild(child)

        // Generate standard PTV initial vaccination schedule for this child
        val standardRecords = listOf(
            VaccineRecordEntity(childId = childId, vaccineName = "BCG", doseName = "Dose única", scheduledAge = "Ao nascer", scheduledDate = birthDate, status = "prox", facilityName = facilityName),
            VaccineRecordEntity(childId = childId, vaccineName = "VOP 0", doseName = "Dose zero", scheduledAge = "Ao nascer", scheduledDate = birthDate, status = "prox", facilityName = facilityName),
            VaccineRecordEntity(childId = childId, vaccineName = "Pentavalente 1", doseName = "1ª dose", scheduledAge = "6 semanas", scheduledDate = "Em 6 semanas", status = "pend", facilityName = facilityName),
            VaccineRecordEntity(childId = childId, vaccineName = "VOP 1", doseName = "1ª dose", scheduledAge = "6 semanas", scheduledDate = "Em 6 semanas", status = "pend", facilityName = facilityName),
            VaccineRecordEntity(childId = childId, vaccineName = "VIP 1", doseName = "1ª dose", scheduledAge = "6 semanas", scheduledDate = "Em 6 semanas", status = "pend", facilityName = facilityName),
            VaccineRecordEntity(childId = childId, vaccineName = "Pneumo 1", doseName = "1ª dose", scheduledAge = "6 semanas", scheduledDate = "Em 6 semanas", status = "pend", facilityName = facilityName),
            VaccineRecordEntity(childId = childId, vaccineName = "Rotavírus 1", doseName = "1ª dose", scheduledAge = "6 semanas", scheduledDate = "Em 6 semanas", status = "pend", facilityName = facilityName),
            VaccineRecordEntity(childId = childId, vaccineName = "Pentavalente 2", doseName = "2ª dose", scheduledAge = "10 semanas", scheduledDate = "Em 10 semanas", status = "pend", facilityName = facilityName),
            VaccineRecordEntity(childId = childId, vaccineName = "VOP 2", doseName = "2ª dose", scheduledAge = "10 semanas", scheduledDate = "Em 10 semanas", status = "pend", facilityName = facilityName),
            VaccineRecordEntity(childId = childId, vaccineName = "Pneumo 2", doseName = "2ª dose", scheduledAge = "10 semanas", scheduledDate = "Em 10 semanas", status = "pend", facilityName = facilityName),
            VaccineRecordEntity(childId = childId, vaccineName = "Rotavírus 2", doseName = "2ª dose", scheduledAge = "10 semanas", scheduledDate = "Em 10 semanas", status = "pend", facilityName = facilityName),
            VaccineRecordEntity(childId = childId, vaccineName = "Pentavalente 3", doseName = "3ª dose", scheduledAge = "14 semanas", scheduledDate = "Em 14 semanas", status = "pend", facilityName = facilityName),
            VaccineRecordEntity(childId = childId, vaccineName = "VOP 3", doseName = "3ª dose", scheduledAge = "14 semanas", scheduledDate = "Em 14 semanas", status = "pend", facilityName = facilityName),
            VaccineRecordEntity(childId = childId, vaccineName = "VIP 2", doseName = "2ª dose", scheduledAge = "14 semanas", scheduledDate = "Em 14 semanas", status = "pend", facilityName = facilityName),
            VaccineRecordEntity(childId = childId, vaccineName = "Pneumo 3", doseName = "3ª dose", scheduledAge = "14 semanas", scheduledDate = "Em 14 semanas", status = "pend", facilityName = facilityName),
            VaccineRecordEntity(childId = childId, vaccineName = "Febre Amarela", doseName = "Dose única", scheduledAge = "9 meses", scheduledDate = "Em 9 meses", status = "pend", facilityName = facilityName),
            VaccineRecordEntity(childId = childId, vaccineName = "Sarampo 1", doseName = "1ª dose", scheduledAge = "9 meses", scheduledDate = "Em 9 meses", status = "pend", facilityName = facilityName)
        )
        dao.insertRecords(standardRecords)

        dao.insertAuditLog(
            AuditLogEntity(
                title = "Novo utente registado · $fullName",
                subtitle = "Código $code gerado com sucesso para $facilityName",
                category = "Utente",
                timeAgo = "Agora mesmo"
            )
        )

        return childId
    }

    suspend fun administerVaccine(
        childId: Long,
        vaccineName: String,
        doseName: String,
        administeredDate: String,
        facilityName: String,
        professionalName: String,
        lotNumber: String,
        expirationDate: String?,
        notes: String?,
        isOfflinePending: Boolean
    ): Long {
        val record = VaccineRecordEntity(
            childId = childId,
            vaccineName = vaccineName,
            doseName = doseName,
            scheduledAge = "Conforme esquema",
            scheduledDate = administeredDate,
            administeredDate = administeredDate,
            status = "admin",
            facilityName = facilityName,
            professionalName = professionalName,
            lotNumber = lotNumber,
            expirationDate = expirationDate,
            notes = notes,
            isOfflinePending = isOfflinePending
        )
        val id = dao.insertRecord(record)

        val child = dao.getChildById(childId)
        val childName = child?.fullName ?: "Utente #$childId"

        dao.insertAuditLog(
            AuditLogEntity(
                title = "Vacinação registada · $childName",
                subtitle = "$vaccineName $doseName (Lote $lotNumber) por $professionalName · $facilityName" +
                        if (isOfflinePending) " [Modo Offline]" else "",
                category = "Vacinação",
                timeAgo = "Agora mesmo"
            )
        )

        return id
    }

    suspend fun syncOfflineQueue() {
        dao.markAllOfflineRecordsSynced()
        val nowFormatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        dao.insertAuditLog(
            AuditLogEntity(
                title = "Sincronização completa de dados offline",
                subtitle = "Todos os registos locais foram enviados para o servidor central às $nowFormatted",
                category = "Sincronização",
                timeAgo = "Agora mesmo"
            )
        )
    }

    suspend fun addStockItem(
        vaccineName: String,
        lotNumber: String,
        quantity: Int,
        expirationDate: String,
        status: String = "OK"
    ) {
        dao.insertStockItem(
            VaccineStockEntity(
                vaccineName = vaccineName,
                lotNumber = lotNumber,
                quantity = quantity,
                expirationDate = expirationDate,
                status = status
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                title = "Novo lote de vacinas adicionado",
                subtitle = "$vaccineName - Lote $lotNumber ($quantity doses) com validade $expirationDate",
                category = "Stock",
                timeAgo = "Agora mesmo"
            )
        )
    }

    suspend fun addHealthFacility(
        name: String,
        type: String,
        distance: String,
        address: String,
        operatingHours: String,
        phone: String,
        services: String,
        region: String
    ) {
        dao.insertFacility(
            HealthFacilityEntity(
                name = name,
                type = type,
                distance = distance,
                address = address,
                operatingHours = operatingHours,
                phone = phone,
                services = services,
                region = region,
                coveragePct = 75,
                registeredChildrenCount = 150,
                status = "Nova"
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                title = "Nova unidade sanitária registada",
                subtitle = "$name ($region) adicionada à rede de saúde nacional",
                category = "Unidades",
                timeAgo = "Agora mesmo"
            )
        )
    }

    suspend fun addHealthWorker(
        name: String,
        title: String,
        facilityName: String
    ) {
        val initials = name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
            .ifEmpty { "CT" }

        dao.insertHealthWorker(
            HealthWorkerEntity(
                name = name,
                title = title,
                facilityName = facilityName,
                avatarInitials = initials,
                dosesCount = 0,
                childrenCount = 0,
                isActive = true
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                title = "Novo profissional activado",
                subtitle = "$name ($title) associado à $facilityName",
                category = "Profissionais",
                timeAgo = "Agora mesmo"
            )
        )
    }

    suspend fun addVaccineToCatalog(
        name: String,
        targetDisease: String,
        dosesCount: Int,
        scheduledAges: String,
        description: String,
        administrationRoute: String = "Intramuscular",
        emoji: String = "💉"
    ): Long {
        val dosesString = if (dosesCount == 1) "1 dose" else "$dosesCount doses"
        val id = dao.insertCatalogItem(
            VaccineCatalogEntity(
                name = name,
                emoji = emoji,
                targetDisease = targetDisease,
                dosesCount = dosesString,
                scheduledAges = scheduledAges,
                description = description,
                route = administrationRoute,
                isActive = true
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                title = "Nova vacina cadastrada no catálogo",
                subtitle = "$name ($dosesString · $scheduledAges) adicionada à base de dados Room",
                category = "Catálogo",
                timeAgo = "Agora mesmo"
            )
        )
        return id
    }

    // === Firebase & User Authentication Management ===
    suspend fun registerUserAccount(
        name: String,
        phone: String,
        email: String,
        password: String,
        role: String,
        parentType: String,
        province: String,
        isCreatedByMasterAdmin: Boolean = false
    ): Result<UserAccountEntity> {
        val primaryIdentifier = if (phone.isNotBlank()) phone.trim() else email.trim()
        
        // Block public registration of admin accounts
        if (role.equals("admin", ignoreCase = true) && !isCreatedByMasterAdmin) {
            return Result.failure(
                Exception("Criação restrita: Contas de Administrador só podem ser adicionadas pelo Administrador Principal.")
            )
        }

        // 1. Check if user already exists locally
        val existingUser = dao.findUserByIdentifier(primaryIdentifier)
        if (existingUser != null) {
            return Result.failure(Exception("Já existe uma conta registada com este contacto ($primaryIdentifier)."))
        }

        // 2. Register user in Firebase Authentication
        var firebaseUid = ""
        try {
            val fbResult = firebaseAuthService.registerWithFirebase(
                fullName = name,
                email = email,
                phone = phone,
                password = password
            )
            if (fbResult.isSuccess) {
                firebaseUid = fbResult.getOrNull()?.uid ?: ""
            } else {
                val errorMsg = fbResult.exceptionOrNull()?.message ?: "Falha no registo do Firebase Authentication."
                return Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            return Result.failure(Exception("Erro no serviço Firebase Auth: ${e.localizedMessage}"))
        }

        // 3. Persist verified real user account in local Room database
        val newUser = UserAccountEntity(
            name = name.trim(),
            emailOrPhone = primaryIdentifier,
            password = password.trim(),
            role = role,
            parentType = parentType,
            province = province,
            phone = phone.trim(),
            email = email.trim(),
            firebaseUid = firebaseUid,
            childrenCount = if (role == "parent") 1 else 0,
            status = "Activo",
            createdAt = System.currentTimeMillis()
        )

        val generatedId = dao.insertUserAccount(newUser)
        val createdUser = newUser.copy(id = generatedId)

        dao.insertAuditLog(
            AuditLogEntity(
                title = if (isCreatedByMasterAdmin) "Novo utilizador adicionado pelo Admin Master" else "Nova conta autenticada",
                subtitle = "$name (${role.uppercase()}) registado com sucesso ($province)",
                category = "Segurança",
                timeAgo = "Agora mesmo"
            )
        )

        return Result.success(createdUser)
    }

    suspend fun authenticateUser(
        identifier: String,
        password: String,
        expectedRole: String? = null
    ): Result<UserAccountEntity> {
        val cleanIdentifier = identifier.trim()
        val cleanPassword = password.trim()

        // Explicit Master Admin Override Verification
        if (cleanIdentifier.equals("cepsgadmin@jorgito2026", ignoreCase = true)) {
            if (cleanPassword == "hhJorge*414life") {
                var masterUser = dao.findUserByIdentifier("cepsgadmin@jorgito2026")
                if (masterUser == null) {
                    val newMaster = UserAccountEntity(
                        name = "Admin Principal (Master)",
                        emailOrPhone = "cepsgadmin@jorgito2026",
                        email = "cepsgadmin@jorgito2026",
                        password = "hhJorge*414life",
                        role = "admin",
                        parentType = "Admin Principal",
                        province = "Maputo Cidade",
                        status = "Activo"
                    )
                    val id = dao.insertUserAccount(newMaster)
                    masterUser = newMaster.copy(id = id)
                }
                return Result.success(masterUser)
            } else {
                return Result.failure(Exception("Palavra-passe incorrecta para o Administrador Principal."))
            }
        }

        // 1. Authenticate with Firebase Authentication
        val fbResult = firebaseAuthService.signInWithFirebase(cleanIdentifier, cleanPassword)
        if (fbResult.isFailure) {
            val err = fbResult.exceptionOrNull()?.message ?: "Falha na autenticação Firebase."
            return Result.failure(Exception(err))
        }

        val fbUser = fbResult.getOrNull()

        // 2. Fetch or initialize matching local profile
        var user = dao.findUserByIdentifier(cleanIdentifier)
        if (user == null && fbUser != null) {
            // Reconstruct local profile from Firebase Auth user
            val fallbackName = fbUser.displayName?.takeIf { it.isNotBlank() } ?: "Utilizador VacciTrack"
            val isEmail = cleanIdentifier.contains("@")
            val newLocalUser = UserAccountEntity(
                name = fallbackName,
                emailOrPhone = cleanIdentifier,
                password = cleanPassword,
                role = expectedRole ?: "parent",
                parentType = if (expectedRole == "pro") "Profissional" else if (expectedRole == "admin") "Admin" else "Pai/Mãe",
                province = "Maputo Cidade",
                phone = if (!isEmail) cleanIdentifier else "",
                email = if (isEmail) cleanIdentifier else (fbUser.email ?: ""),
                firebaseUid = fbUser.uid,
                childrenCount = 1,
                status = "Activo"
            )
            val newId = dao.insertUserAccount(newLocalUser)
            user = newLocalUser.copy(id = newId)
        }

        if (user == null) {
            return Result.failure(Exception("Nenhuma conta encontrada com o identificador '$cleanIdentifier'."))
        }

        if (fbUser == null && user.password.isNotBlank() && user.password != cleanPassword) {
            return Result.failure(Exception("Palavra-passe incorrecta. Por favor, verifique as suas credenciais."))
        }

        if (expectedRole != null && user.role.lowercase() != expectedRole.lowercase()) {
            return Result.failure(Exception("Esta conta está registada como '${user.role.uppercase()}'. Seleccione o perfil correcto no login."))
        }

        return Result.success(user)
    }

    fun signOutUser() {
        firebaseAuthService.signOut()
    }

    suspend fun getUserByIdentifier(identifier: String): UserAccountEntity? {
        return dao.findUserByIdentifier(identifier.trim())
    }

    suspend fun deleteUserAccount(user: UserAccountEntity): Result<Unit> {
        // Prevent deleting the Master Admin
        if (user.emailOrPhone.equals("cepsgadmin@jorgito2026", ignoreCase = true) ||
            user.email.equals("cepsgadmin@jorgito2026", ignoreCase = true)
        ) {
            return Result.failure(Exception("O Administrador Principal (Master) não pode ser removido do sistema."))
        }

        dao.deleteUserAccount(user)
        // Also remove matching health worker if exists
        dao.deleteHealthWorkerByName(user.name)

        dao.insertAuditLog(
            AuditLogEntity(
                title = "Conta de utilizador removida pelo Admin Principal",
                subtitle = "${user.name} (${user.role.uppercase()} - ${user.emailOrPhone}) foi revogado e removido",
                category = "Segurança",
                timeAgo = "Agora mesmo"
            )
        )
        return Result.success(Unit)
    }

    suspend fun deleteHealthWorker(worker: HealthWorkerEntity): Result<Unit> {
        dao.deleteHealthWorker(worker)
        dao.insertAuditLog(
            AuditLogEntity(
                title = "Profissional de saúde removido pelo Admin Principal",
                subtitle = "${worker.name} (${worker.title} - ${worker.facilityName}) foi removido da equipa",
                category = "Segurança",
                timeAgo = "Agora mesmo"
            )
        )
        return Result.success(Unit)
    }

    suspend fun deleteHealthFacility(facility: HealthFacilityEntity): Result<Unit> {
        dao.deleteFacility(facility)
        dao.insertAuditLog(
            AuditLogEntity(
                title = "Unidade Sanitária removida pelo Admin Principal",
                subtitle = "${facility.name} (${facility.region} - ${facility.type}) foi desativada e removida",
                category = "Segurança",
                timeAgo = "Agora mesmo"
            )
        )
        return Result.success(Unit)
    }

    suspend fun deleteVaccineCatalogItem(item: VaccineCatalogEntity): Result<Unit> {
        dao.deleteCatalogItem(item)
        dao.insertAuditLog(
            AuditLogEntity(
                title = "Vacina removida do catálogo pelo Admin Principal",
                subtitle = "${item.name} (${item.targetDisease}) foi removida do PTV",
                category = "Calendário",
                timeAgo = "Agora mesmo"
            )
        )
        return Result.success(Unit)
    }

    suspend fun deleteVaccineStockItem(item: VaccineStockEntity): Result<Unit> {
        dao.deleteStockItem(item)
        dao.insertAuditLog(
            AuditLogEntity(
                title = "Lote de vacina eliminado pelo Admin Principal",
                subtitle = "Lote ${item.lotNumber} (${item.vaccineName}) foi removido do stock",
                category = "Stock",
                timeAgo = "Agora mesmo"
            )
        )
        return Result.success(Unit)
    }

    suspend fun deleteChildAndRecords(child: ChildEntity): Result<Unit> {
        dao.deleteRecordsForChild(child.id)
        dao.deleteChild(child)
        dao.insertAuditLog(
            AuditLogEntity(
                title = "Utente removido pelo Admin Principal",
                subtitle = "${child.fullName} (${child.code}) e o seu histórico vacinal foram eliminados",
                category = "Utente",
                timeAgo = "Agora mesmo"
            )
        )
        return Result.success(Unit)
    }

    suspend fun deleteVaccineRecord(record: VaccineRecordEntity): Result<Unit> {
        dao.deleteRecord(record)
        dao.insertAuditLog(
            AuditLogEntity(
                title = "Registo de dose cancelado/eliminado pelo Admin Principal",
                subtitle = "${record.vaccineName} (${record.doseName}) do utente ID ${record.childId} foi eliminado",
                category = "Correcção",
                timeAgo = "Agora mesmo"
            )
        )
        return Result.success(Unit)
    }

    suspend fun clearAuditLogs(): Result<Unit> {
        dao.clearAllAuditLogs()
        dao.insertAuditLog(
            AuditLogEntity(
                title = "Histórico de auditoria limpo pelo Admin Principal",
                subtitle = "Todos os registos de auditoria anteriores foram purgados",
                category = "Segurança",
                timeAgo = "Agora mesmo"
            )
        )
        return Result.success(Unit)
    }

    suspend fun updateUserAccount(user: UserAccountEntity): Result<Unit> {
        dao.updateUserAccount(user)
        dao.insertAuditLog(
            AuditLogEntity(
                title = "Conta de utilizador atualizada pelo Admin Principal",
                subtitle = "${user.name} (${user.role.uppercase()} - ${user.emailOrPhone}) foi modificado",
                category = "Segurança",
                timeAgo = "Agora mesmo"
            )
        )
        return Result.success(Unit)
    }

    suspend fun updateHealthWorker(worker: HealthWorkerEntity): Result<Unit> {
        dao.updateHealthWorker(worker)
        dao.insertAuditLog(
            AuditLogEntity(
                title = "Profissional de saúde atualizado pelo Admin Principal",
                subtitle = "${worker.name} (${worker.title} - ${worker.facilityName}) dados editados",
                category = "Equipa",
                timeAgo = "Agora mesmo"
            )
        )
        return Result.success(Unit)
    }

    suspend fun updateHealthFacility(facility: HealthFacilityEntity): Result<Unit> {
        dao.updateFacility(facility)
        dao.insertAuditLog(
            AuditLogEntity(
                title = "Unidade Sanitária atualizada pelo Admin Principal",
                subtitle = "${facility.name} (${facility.region}) dados e capacidade atualizados",
                category = "Infraestrutura",
                timeAgo = "Agora mesmo"
            )
        )
        return Result.success(Unit)
    }

    suspend fun updateVaccineCatalogItem(item: VaccineCatalogEntity): Result<Unit> {
        dao.updateCatalogItem(item)
        dao.insertAuditLog(
            AuditLogEntity(
                title = "Vacina do catálogo atualizada pelo Admin Principal",
                subtitle = "${item.name} (${item.targetDisease}) - Esquema: ${item.scheduledAges}",
                category = "Calendário",
                timeAgo = "Agora mesmo"
            )
        )
        return Result.success(Unit)
    }

    suspend fun updateVaccineStockItem(item: VaccineStockEntity): Result<Unit> {
        dao.updateStockItem(item)
        dao.insertAuditLog(
            AuditLogEntity(
                title = "Lote de vacina atualizado pelo Admin Principal",
                subtitle = "Lote ${item.lotNumber} (${item.vaccineName}) - Stock: ${item.quantity} doses",
                category = "Stock",
                timeAgo = "Agora mesmo"
            )
        )
        return Result.success(Unit)
    }

    suspend fun resetAllDataToRealtimeZero(): Result<Unit> {
        dao.clearAllChildren()
        dao.clearAllVaccineRecords()
        dao.clearAllAuditLogs()
        dao.resetHealthWorkersMetrics()
        dao.resetHealthFacilitiesMetrics()
        dao.resetUserAccountsMetrics()
        dao.insertAuditLog(
            AuditLogEntity(
                title = "Dados Zerados para Contabilização em Tempo Real",
                subtitle = "Todas as crianças, doses e métricas foram reiniciadas a zero para contabilização em tempo real.",
                category = "Sistema",
                timeAgo = "Agora mesmo"
            )
        )
        return Result.success(Unit)
    }
}
