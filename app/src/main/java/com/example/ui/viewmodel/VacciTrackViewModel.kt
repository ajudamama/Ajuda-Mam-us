package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.VacciTrackRepository
import com.example.data.supabase.SupabaseConfig
import com.example.data.supabase.SupabaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

enum class AppDestination {
    SPLASH,
    LOGIN,
    REGISTER,
    DEMO_SELECTOR,
    PARENT_PORTAL,
    PRO_PORTAL,
    ADMIN_PORTAL
}

enum class ParentTab(val label: String) {
    HOME("Início"),
    CADERNETA("Caderneta"),
    CALENDARIO("Calendário"),
    HISTORICO("Histórico"),
    ALERTAS("Alertas"),
    VACINAS("Vacinas"),
    UNIDADES("Unidades")
}

enum class ProTab(val label: String) {
    DASHBOARD("Dashboard"),
    REGISTAR("Registar Utente"),
    VACINAR("Registar Dose"),
    CONSULTAR("Consultar Histórico"),
    ATRASO("Doses em Atraso"),
    DOSES("Gestão de Doses"),
    SYNC("Sincronização"),
    RELATORIOS("Relatórios")
}

enum class AdminTab(val label: String) {
    DASHBOARD("Dashboard"),
    USERS("Utilizadores"),
    PROS("Profissionais"),
    UNITS("Unidades"),
    VACCINES("Vacinas"),
    CALENDARS("Calendários"),
    REPORTS("Relatórios"),
    AUDIT("Auditoria"),
    CONFIG("Configurações")
}

data class UiMessage(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val isError: Boolean = false
)

class VacciTrackViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VacciTrackRepository
    val supabaseService: SupabaseService

    // Supabase Configuration State
    private val _supabaseProjectUrl = MutableStateFlow(SupabaseConfig.getProjectUrl(application))
    val supabaseProjectUrl: StateFlow<String> = _supabaseProjectUrl.asStateFlow()

    private val _supabaseAnonKey = MutableStateFlow(SupabaseConfig.getAnonKey(application))
    val supabaseAnonKey: StateFlow<String> = _supabaseAnonKey.asStateFlow()

    private val _supabaseStatus = MutableStateFlow("Chave Supabase Configurada")
    val supabaseStatus: StateFlow<String> = _supabaseStatus.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = VacciTrackRepository(database.appDao())
        supabaseService = SupabaseService(application)
    }

    // App Navigation State
    private val _destination = MutableStateFlow(AppDestination.SPLASH)
    val destination: StateFlow<AppDestination> = _destination.asStateFlow()

    private val _currentRole = MutableStateFlow(UserRole.PARENT)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    // Authenticated User Profile
    private val _currentUser = MutableStateFlow<UserAccountEntity?>(null)
    val currentUser: StateFlow<UserAccountEntity?> = _currentUser.asStateFlow()

    // Sub-Tabs
    private val _parentTab = MutableStateFlow(ParentTab.HOME)
    val parentTab: StateFlow<ParentTab> = _parentTab.asStateFlow()

    private val _proTab = MutableStateFlow(ProTab.DASHBOARD)
    val proTab: StateFlow<ProTab> = _proTab.asStateFlow()

    private val _adminTab = MutableStateFlow(AdminTab.DASHBOARD)
    val adminTab: StateFlow<AdminTab> = _adminTab.asStateFlow()

    // Selected Child in Parent View
    private val _selectedChildId = MutableStateFlow<Long>(1L)
    val selectedChildId: StateFlow<Long> = _selectedChildId.asStateFlow()

    // Queries & Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Transient UI Notification
    private val _uiMessage = MutableStateFlow<UiMessage?>(null)
    val uiMessage: StateFlow<UiMessage?> = _uiMessage.asStateFlow()

    // Sync in-progress animation
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // Data streams from repository
    val children: StateFlow<List<ChildEntity>> = repository.allChildren
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVaccineRecords: StateFlow<List<VaccineRecordEntity>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val delayedRecords: StateFlow<List<VaccineRecordEntity>> = repository.delayedRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offlinePendingRecords: StateFlow<List<VaccineRecordEntity>> = repository.offlinePendingRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaccineCatalog: StateFlow<List<VaccineCatalogEntity>> = repository.vaccineCatalog
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val facilities: StateFlow<List<HealthFacilityEntity>> = repository.allFacilities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val healthWorkers: StateFlow<List<HealthWorkerEntity>> = repository.allHealthWorkers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockItems: StateFlow<List<VaccineStockEntity>> = repository.allStock
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userAccounts: StateFlow<List<UserAccountEntity>> = repository.allUserAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Child's records stream
    val currentChildRecords: StateFlow<List<VaccineRecordEntity>> = _selectedChildId
        .flatMapLatest { childId ->
            repository.getRecordsForChild(childId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation Actions
    fun navigateTo(dest: AppDestination) {
        _destination.value = dest
    }

    fun setRole(role: UserRole) {
        _currentRole.value = role
        when (role) {
            UserRole.PARENT -> _destination.value = AppDestination.PARENT_PORTAL
            UserRole.PROFESSIONAL -> _destination.value = AppDestination.PRO_PORTAL
            UserRole.ADMIN -> _destination.value = AppDestination.ADMIN_PORTAL
        }
    }

    /**
     * Authenticate user with real credentials.
     * Prevents entry without a registered account in Room DB.
     */
    fun loginWithCredentials(
        identifier: String,
        password: String,
        role: UserRole,
        onSuccess: (UserAccountEntity) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val roleCode = when (role) {
                UserRole.PARENT -> "parent"
                UserRole.PROFESSIONAL -> "pro"
                UserRole.ADMIN -> "admin"
            }

            val result = repository.authenticateUser(
                identifier = identifier,
                password = password,
                expectedRole = roleCode
            )

            result.onSuccess { user ->
                _currentUser.value = user
                _currentRole.value = role
                launch(Dispatchers.Main) {
                    showMessage("Bem-vindo(a), ${user.name}!")
                    when (role) {
                        UserRole.PARENT -> _destination.value = AppDestination.PARENT_PORTAL
                        UserRole.PROFESSIONAL -> _destination.value = AppDestination.PRO_PORTAL
                        UserRole.ADMIN -> _destination.value = AppDestination.ADMIN_PORTAL
                    }
                    onSuccess(user)
                }
            }.onFailure { ex ->
                launch(Dispatchers.Main) {
                    val errorMsg = ex.message ?: "Falha ao iniciar sessão. Verifique as credenciais."
                    showMessage(errorMsg, isError = true)
                    onError(errorMsg)
                }
            }
        }
    }

    /**
     * Register a new user with verified real data.
     * Persists directly to Room database and initiates an authenticated session.
     */
    fun registerNewAccount(
        name: String,
        phone: String,
        email: String,
        password: String,
        role: UserRole,
        parentType: String,
        province: String,
        onSuccess: (UserAccountEntity) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val roleCode = when (role) {
                UserRole.PARENT -> "parent"
                UserRole.PROFESSIONAL -> "pro"
                UserRole.ADMIN -> "admin"
            }

            val result = repository.registerUserAccount(
                name = name,
                phone = phone,
                email = email,
                password = password,
                role = roleCode,
                parentType = parentType,
                province = province
            )

            result.onSuccess { user ->
                _currentUser.value = user
                _currentRole.value = role

                // If user is a Parent or Gestante, create an associated child/pregnancy record if needed
                if (role == UserRole.PARENT) {
                    val isPregnant = parentType.equals("Gestante", ignoreCase = true)
                    val initialChildName = if (isPregnant) "Bebé de ${user.name.split(" ").first()}" else "Criança de ${user.name.split(" ").first()}"
                    val childId = repository.registerChild(
                        fullName = initialChildName,
                        birthDate = if (isPregnant) "Previsão 2026" else "01/01/2026",
                        gender = "Feminino",
                        birthPlace = province,
                        birthWeight = "3.1 kg",
                        parentName = user.name,
                        parentContact = if (user.phone.isNotBlank()) user.phone else user.emailOrPhone,
                        parentType = parentType,
                        facilityName = "US Central - $province"
                    )
                    _selectedChildId.value = childId
                }

                launch(Dispatchers.Main) {
                    showMessage("Conta criada com sucesso! Bem-vindo(a), ${user.name}.")
                    when (role) {
                        UserRole.PARENT -> _destination.value = AppDestination.PARENT_PORTAL
                        UserRole.PROFESSIONAL -> _destination.value = AppDestination.PRO_PORTAL
                        UserRole.ADMIN -> _destination.value = AppDestination.ADMIN_PORTAL
                    }
                    onSuccess(user)
                }
            }.onFailure { ex ->
                launch(Dispatchers.Main) {
                    val errorMsg = ex.message ?: "Erro ao registar conta. Tente novamente."
                    showMessage(errorMsg, isError = true)
                    onError(errorMsg)
                }
            }
        }
    }

    /**
     * Creation of secondary Admin or Pro accounts initiated by the Master Admin.
     */
    fun createAdminOrWorkerAccountByMaster(
        name: String,
        phone: String,
        email: String,
        password: String,
        role: String, // "admin" or "pro"
        province: String,
        onSuccess: (UserAccountEntity) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.registerUserAccount(
                name = name,
                phone = phone,
                email = email,
                password = password,
                role = role,
                parentType = if (role == "admin") "Administrador" else "Profissional",
                province = province,
                isCreatedByMasterAdmin = true
            )

            result.onSuccess { user ->
                launch(Dispatchers.Main) {
                    val roleLabel = if (role == "admin") "Administrador" else "Profissional"
                    showMessage("$roleLabel cadastrado com sucesso pelo Admin Principal!")
                    onSuccess(user)
                }
            }.onFailure { ex ->
                launch(Dispatchers.Main) {
                    val errorMsg = ex.message ?: "Erro ao criar conta."
                    showMessage(errorMsg, isError = true)
                    onError(errorMsg)
                }
            }
        }
    }

    /**
     * Remove user account (Admin, Pro, Parent) by Master Admin.
     */
    fun deleteUserAccount(
        user: UserAccountEntity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.deleteUserAccount(user)
            result.onSuccess {
                launch(Dispatchers.Main) {
                    showMessage("Conta de ${user.name} removida com sucesso!")
                    onSuccess()
                }
            }.onFailure { ex ->
                launch(Dispatchers.Main) {
                    val errorMsg = ex.message ?: "Erro ao remover conta."
                    showMessage(errorMsg, isError = true)
                    onError(errorMsg)
                }
            }
        }
    }

    /**
     * Remove health worker from staff team by Master Admin.
     */
    fun deleteHealthWorker(
        worker: HealthWorkerEntity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.deleteHealthWorker(worker)
            result.onSuccess {
                launch(Dispatchers.Main) {
                    showMessage("Profissional ${worker.name} removido da equipa de saúde!")
                    onSuccess()
                }
            }.onFailure { ex ->
                launch(Dispatchers.Main) {
                    val errorMsg = ex.message ?: "Erro ao remover profissional."
                    showMessage(errorMsg, isError = true)
                    onError(errorMsg)
                }
            }
        }
    }

    /**
     * Remove health facility by Master Admin.
     */
    fun deleteHealthFacility(
        facility: HealthFacilityEntity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.deleteHealthFacility(facility)
            result.onSuccess {
                launch(Dispatchers.Main) {
                    showMessage("Unidade Sanitária ${facility.name} removida com sucesso!")
                    onSuccess()
                }
            }.onFailure { ex ->
                launch(Dispatchers.Main) {
                    val errorMsg = ex.message ?: "Erro ao remover Unidade Sanitária."
                    showMessage(errorMsg, isError = true)
                    onError(errorMsg)
                }
            }
        }
    }

    /**
     * Remove vaccine from catalog by Master Admin.
     */
    fun deleteVaccineCatalogItem(
        item: VaccineCatalogEntity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.deleteVaccineCatalogItem(item)
            result.onSuccess {
                launch(Dispatchers.Main) {
                    showMessage("Vacina ${item.name} removida do catálogo!")
                    onSuccess()
                }
            }.onFailure { ex ->
                launch(Dispatchers.Main) {
                    val errorMsg = ex.message ?: "Erro ao remover vacina."
                    showMessage(errorMsg, isError = true)
                    onError(errorMsg)
                }
            }
        }
    }

    /**
     * Remove child and vaccination history by Master Admin.
     */
    fun deleteChild(
        child: ChildEntity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.deleteChildAndRecords(child)
            result.onSuccess {
                launch(Dispatchers.Main) {
                    showMessage("Utente ${child.fullName} (${child.code}) removido do sistema!")
                    onSuccess()
                }
            }.onFailure { ex ->
                launch(Dispatchers.Main) {
                    val errorMsg = ex.message ?: "Erro ao remover utente."
                    showMessage(errorMsg, isError = true)
                    onError(errorMsg)
                }
            }
        }
    }

    /**
     * Clear all audit logs.
     */
    fun clearAuditLogs(
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.clearAuditLogs()
            result.onSuccess {
                launch(Dispatchers.Main) {
                    showMessage("Histórico de auditoria limpo com sucesso!")
                    onSuccess()
                }
            }.onFailure { ex ->
                launch(Dispatchers.Main) {
                    val errorMsg = ex.message ?: "Erro ao limpar auditoria."
                    showMessage(errorMsg, isError = true)
                    onError(errorMsg)
                }
            }
        }
    }

    /**
     * Edit / Update User Account by Master Admin.
     */
    fun updateUserAccount(
        user: UserAccountEntity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.updateUserAccount(user)
            result.onSuccess {
                launch(Dispatchers.Main) {
                    showMessage("Conta de ${user.name} atualizada com sucesso!")
                    onSuccess()
                }
            }.onFailure { ex ->
                launch(Dispatchers.Main) {
                    val errorMsg = ex.message ?: "Erro ao atualizar conta."
                    showMessage(errorMsg, isError = true)
                    onError(errorMsg)
                }
            }
        }
    }

    /**
     * Edit / Update Health Worker by Master Admin.
     */
    fun updateHealthWorker(
        worker: HealthWorkerEntity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.updateHealthWorker(worker)
            result.onSuccess {
                launch(Dispatchers.Main) {
                    showMessage("Profissional ${worker.name} atualizado!")
                    onSuccess()
                }
            }.onFailure { ex ->
                launch(Dispatchers.Main) {
                    val errorMsg = ex.message ?: "Erro ao atualizar profissional."
                    showMessage(errorMsg, isError = true)
                    onError(errorMsg)
                }
            }
        }
    }

    /**
     * Edit / Update Health Facility by Master Admin.
     */
    fun updateHealthFacility(
        facility: HealthFacilityEntity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.updateHealthFacility(facility)
            result.onSuccess {
                launch(Dispatchers.Main) {
                    showMessage("Unidade Sanitária ${facility.name} atualizada!")
                    onSuccess()
                }
            }.onFailure { ex ->
                launch(Dispatchers.Main) {
                    val errorMsg = ex.message ?: "Erro ao atualizar unidade."
                    showMessage(errorMsg, isError = true)
                    onError(errorMsg)
                }
            }
        }
    }

    /**
     * Edit / Update Vaccine Catalog Item by Master Admin.
     */
    fun updateVaccineCatalogItem(
        item: VaccineCatalogEntity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.updateVaccineCatalogItem(item)
            result.onSuccess {
                launch(Dispatchers.Main) {
                    showMessage("Vacina ${item.name} atualizada com sucesso!")
                    onSuccess()
                }
            }.onFailure { ex ->
                launch(Dispatchers.Main) {
                    val errorMsg = ex.message ?: "Erro ao atualizar vacina."
                    showMessage(errorMsg, isError = true)
                    onError(errorMsg)
                }
            }
        }
    }

    /**
     * Edit / Update Vaccine Stock Item by Master Admin.
     */
    fun updateVaccineStockItem(
        item: VaccineStockEntity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.updateVaccineStockItem(item)
            result.onSuccess {
                launch(Dispatchers.Main) {
                    showMessage("Lote ${item.lotNumber} atualizado no stock!")
                    onSuccess()
                }
            }.onFailure { ex ->
                launch(Dispatchers.Main) {
                    val errorMsg = ex.message ?: "Erro ao atualizar stock."
                    showMessage(errorMsg, isError = true)
                    onError(errorMsg)
                }
            }
        }
    }

    /**
     * Terminate session and return to Welcome/Login screen.
     */
    fun logout() {
        repository.signOutUser()
        _currentUser.value = null
        _destination.value = AppDestination.SPLASH
        showMessage("Sessão terminada com segurança.")
    }

    fun setParentTab(tab: ParentTab) {
        _parentTab.value = tab
    }

    fun setProTab(tab: ProTab) {
        _proTab.value = tab
    }

    fun setAdminTab(tab: AdminTab) {
        _adminTab.value = tab
    }

    fun selectChild(childId: Long) {
        _selectedChildId.value = childId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun showMessage(msg: String, isError: Boolean = false) {
        _uiMessage.value = UiMessage(text = msg, isError = isError)
    }

    // Business Actions
    fun registerNewChild(
        fullName: String,
        birthDate: String,
        gender: String,
        birthPlace: String,
        birthWeight: String,
        parentName: String,
        parentContact: String,
        parentType: String,
        facilityName: String,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newId = repository.registerChild(
                    fullName = fullName,
                    birthDate = birthDate,
                    gender = gender,
                    birthPlace = birthPlace,
                    birthWeight = birthWeight,
                    parentName = parentName,
                    parentContact = parentContact,
                    parentType = parentType,
                    facilityName = facilityName
                )
                _selectedChildId.value = newId
                showMessage("Utente $fullName registado com sucesso!")
                onSuccess(newId)
            } catch (e: Exception) {
                showMessage("Erro ao registar: ${e.message}", isError = true)
            }
        }
    }

    fun administerVaccine(
        childId: Long,
        vaccineName: String,
        doseName: String,
        administeredDate: String,
        facilityName: String,
        professionalName: String,
        lotNumber: String,
        expirationDate: String?,
        notes: String?,
        isOfflinePending: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.administerVaccine(
                    childId = childId,
                    vaccineName = vaccineName,
                    doseName = doseName,
                    administeredDate = administeredDate,
                    facilityName = facilityName,
                    professionalName = professionalName,
                    lotNumber = lotNumber,
                    expirationDate = expirationDate,
                    notes = notes,
                    isOfflinePending = isOfflinePending
                )
                val statusText = if (isOfflinePending) "guardada offline" else "registada no sistema"
                showMessage("Dose de $vaccineName $statusText com sucesso!")
                onSuccess()
            } catch (e: Exception) {
                showMessage("Erro ao registar vacina: ${e.message}", isError = true)
            }
        }
    }

    fun syncOfflineRecords() {
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncing.value = true
            delay(1200) // Realistic network sync roundtrip
            repository.syncOfflineQueue()
            _isSyncing.value = false
            showMessage("Sincronização concluída com sucesso!")
        }
    }

    fun addStockItem(
        vaccineName: String,
        lotNumber: String,
        quantity: Int,
        expirationDate: String,
        status: String = "OK",
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.addStockItem(vaccineName, lotNumber, quantity, expirationDate, status)
                showMessage("Lote $lotNumber adicionado ao stock!")
                onSuccess()
            } catch (e: Exception) {
                showMessage("Erro ao adicionar lote: ${e.message}", isError = true)
            }
        }
    }

    fun addFacility(
        name: String,
        type: String,
        distance: String,
        address: String,
        operatingHours: String,
        phone: String,
        services: String,
        region: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.addHealthFacility(name, type, distance, address, operatingHours, phone, services, region)
                showMessage("Unidade $name adicionada!")
                onSuccess()
            } catch (e: Exception) {
                showMessage("Erro ao adicionar unidade: ${e.message}", isError = true)
            }
        }
    }

    fun addHealthWorker(
        name: String,
        title: String,
        facilityName: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.addHealthWorker(name, title, facilityName)
                showMessage("Profissional $name adicionado com sucesso!")
                onSuccess()
            } catch (e: Exception) {
                showMessage("Erro ao adicionar profissional: ${e.message}", isError = true)
            }
        }
    }

    fun registerNewVaccine(
        name: String,
        targetDisease: String,
        dosesCount: Int,
        scheduledAges: String,
        description: String,
        administrationRoute: String = "Intramuscular",
        emoji: String = "💉",
        onSuccess: (Long) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newId = repository.addVaccineToCatalog(
                    name = name.trim(),
                    targetDisease = targetDisease.trim(),
                    dosesCount = dosesCount,
                    scheduledAges = scheduledAges.trim(),
                    description = description.trim(),
                    administrationRoute = administrationRoute,
                    emoji = emoji
                )
                showMessage("Vacina ${name.trim()} cadastrada com sucesso na base de dados!")
                onSuccess(newId)
            } catch (e: Exception) {
                showMessage("Erro ao cadastrar vacina: ${e.message}", isError = true)
            }
        }
    }

    // ----------------- SUPABASE INTEGRATION METHODS -----------------

    fun saveSupabaseSettings(projectUrl: String, anonKey: String) {
        SupabaseConfig.saveConfig(getApplication(), projectUrl, anonKey)
        _supabaseProjectUrl.value = projectUrl.trim()
        _supabaseAnonKey.value = anonKey.trim()
        _supabaseStatus.value = "Configurações guardadas"
        showMessage("Configuração do Supabase guardada com sucesso!")
    }

    fun testSupabaseConnection() {
        viewModelScope.launch {
            _isSyncing.value = true
            _supabaseStatus.value = "A testar ligação ao Supabase..."
            val result = supabaseService.testConnection()
            _isSyncing.value = false
            result.onSuccess { msg ->
                _supabaseStatus.value = "Ligação activa ✅"
                showMessage(msg)
            }.onFailure { err ->
                _supabaseStatus.value = "Erro de ligação: ${err.message}"
                showMessage(err.message ?: "Falha ao contactar Supabase", isError = true)
            }
        }
    }

    fun syncAllToSupabase() {
        viewModelScope.launch(Dispatchers.IO) {
            if (!supabaseService.isConfigured) {
                launch(Dispatchers.Main) {
                    showMessage("Por favor configure o URL do Supabase antes de sincronizar.", isError = true)
                }
                return@launch
            }

            _isSyncing.value = true
            _supabaseStatus.value = "A sincronizar tabelas com Supabase..."

            try {
                // Sync Children
                val childList = children.value
                val childJsonArray = JSONArray()
                childList.forEach { c ->
                    val obj = JSONObject().apply {
                        put("id", c.id)
                        put("code", c.code)
                        put("full_name", c.fullName)
                        put("birth_date", c.birthDate)
                        put("gender", c.gender)
                        put("parent_name", c.parentName)
                        put("parent_contact", c.parentContact)
                        put("facility_name", c.facilityName)
                        put("birth_place", c.birthPlace)
                    }
                    childJsonArray.put(obj)
                }

                if (childJsonArray.length() > 0) {
                    supabaseService.upsertTable("children", childJsonArray)
                }

                // Sync Vaccine Records
                val recordList = allVaccineRecords.value
                val recordJsonArray = JSONArray()
                recordList.forEach { r ->
                    val obj = JSONObject().apply {
                        put("id", r.id)
                        put("child_id", r.childId)
                        put("vaccine_name", r.vaccineName)
                        put("dose_name", r.doseName)
                        put("scheduled_date", r.scheduledDate)
                        put("administered_date", r.administeredDate ?: JSONObject.NULL)
                        put("status", r.status)
                        put("facility_name", r.facilityName)
                        put("professional_name", r.professionalName ?: JSONObject.NULL)
                        put("lot_number", r.lotNumber ?: JSONObject.NULL)
                        put("is_offline_pending", r.isOfflinePending)
                    }
                    recordJsonArray.put(obj)
                }

                if (recordJsonArray.length() > 0) {
                    supabaseService.upsertTable("vaccine_records", recordJsonArray)
                }

                // Sync Stock
                val stockList = stockItems.value
                val stockJsonArray = JSONArray()
                stockList.forEach { s ->
                    val obj = JSONObject().apply {
                        put("id", s.id)
                        put("vaccine_name", s.vaccineName)
                        put("lot_number", s.lotNumber)
                        put("quantity", s.quantity)
                        put("expiration_date", s.expirationDate)
                        put("status", s.status)
                    }
                    stockJsonArray.put(obj)
                }

                if (stockJsonArray.length() > 0) {
                    supabaseService.upsertTable("vaccine_inventory", stockJsonArray)
                }

                repository.syncOfflineQueue()

                launch(Dispatchers.Main) {
                    _supabaseStatus.value = "Sincronizado com Supabase ✅"
                    showMessage("Sincronização com Supabase concluída com sucesso!")
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    _supabaseStatus.value = "Erro na sincronização: ${e.message}"
                    showMessage("Erro ao sincronizar com Supabase: ${e.message}", isError = true)
                }
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun resetAllDataToRealtimeZero(onSuccess: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.resetAllDataToRealtimeZero()
            launch(Dispatchers.Main) {
                if (result.isSuccess) {
                    _selectedChildId.value = 0L
                    showMessage("Sistema zerado com sucesso! Contabilização em tempo real ativa.")
                    onSuccess()
                } else {
                    showMessage("Erro ao zerar dados do sistema.", isError = true)
                }
            }
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VacciTrackViewModel(application) as T
            }
        }
    }
}
