package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // === Children ===
    @Query("SELECT * FROM children ORDER BY id ASC")
    fun getAllChildren(): Flow<List<ChildEntity>>

    @Query("SELECT * FROM children WHERE id = :id")
    suspend fun getChildById(id: Long): ChildEntity?

    @Query("SELECT * FROM children WHERE code = :code LIMIT 1")
    suspend fun getChildByCode(code: String): ChildEntity?

    @Query("SELECT * FROM children WHERE fullName LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%'")
    fun searchChildren(query: String): Flow<List<ChildEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChild(child: ChildEntity): Long

    @Update
    suspend fun updateChild(child: ChildEntity)

    @Delete
    suspend fun deleteChild(child: ChildEntity)

    @Query("DELETE FROM children WHERE id = :id")
    suspend fun deleteChildById(id: Long)

    // === Vaccine Records ===
    @Query("SELECT * FROM vaccine_records WHERE childId = :childId ORDER BY id ASC")
    fun getRecordsForChild(childId: Long): Flow<List<VaccineRecordEntity>>

    @Query("SELECT * FROM vaccine_records ORDER BY id DESC")
    fun getAllRecords(): Flow<List<VaccineRecordEntity>>

    @Query("SELECT * FROM vaccine_records WHERE status = 'atraso'")
    fun getDelayedRecords(): Flow<List<VaccineRecordEntity>>

    @Query("SELECT * FROM vaccine_records WHERE status = 'prox'")
    fun getUpcomingRecords(): Flow<List<VaccineRecordEntity>>

    @Query("SELECT * FROM vaccine_records WHERE status = 'admin'")
    fun getAdministeredRecords(): Flow<List<VaccineRecordEntity>>

    @Query("SELECT * FROM vaccine_records WHERE isOfflinePending = 1")
    fun getOfflinePendingRecords(): Flow<List<VaccineRecordEntity>>

    @Query("SELECT * FROM vaccine_records WHERE id = :id")
    suspend fun getRecordById(id: Long): VaccineRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: VaccineRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<VaccineRecordEntity>)

    @Update
    suspend fun updateRecord(record: VaccineRecordEntity)

    @Delete
    suspend fun deleteRecord(record: VaccineRecordEntity)

    @Query("DELETE FROM vaccine_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM vaccine_records WHERE childId = :childId")
    suspend fun deleteRecordsForChild(childId: Long)

    @Query("UPDATE vaccine_records SET isOfflinePending = 0 WHERE isOfflinePending = 1")
    suspend fun markAllOfflineRecordsSynced()

    // === Vaccine Catalog ===
    @Query("SELECT * FROM vaccine_catalog ORDER BY id ASC")
    fun getVaccineCatalog(): Flow<List<VaccineCatalogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatalogItems(items: List<VaccineCatalogEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatalogItem(item: VaccineCatalogEntity): Long

    @Update
    suspend fun updateCatalogItem(item: VaccineCatalogEntity)

    @Delete
    suspend fun deleteCatalogItem(item: VaccineCatalogEntity)

    @Query("DELETE FROM vaccine_catalog WHERE id = :id")
    suspend fun deleteCatalogItemById(id: Long)

    // === Health Facilities ===
    @Query("SELECT * FROM health_facilities ORDER BY id ASC")
    fun getAllFacilities(): Flow<List<HealthFacilityEntity>>

    @Query("SELECT * FROM health_facilities WHERE name LIKE '%' || :query || '%' OR region LIKE '%' || :query || '%'")
    fun searchFacilities(query: String): Flow<List<HealthFacilityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFacilities(facilities: List<HealthFacilityEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFacility(facility: HealthFacilityEntity): Long

    @Update
    suspend fun updateFacility(facility: HealthFacilityEntity)

    @Delete
    suspend fun deleteFacility(facility: HealthFacilityEntity)

    @Query("DELETE FROM health_facilities WHERE id = :id")
    suspend fun deleteFacilityById(id: Long)

    // === Health Workers ===
    @Query("SELECT * FROM health_workers ORDER BY id ASC")
    fun getAllHealthWorkers(): Flow<List<HealthWorkerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthWorkers(workers: List<HealthWorkerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthWorker(worker: HealthWorkerEntity): Long

    @Update
    suspend fun updateHealthWorker(worker: HealthWorkerEntity)

    @Delete
    suspend fun deleteHealthWorker(worker: HealthWorkerEntity)

    @Query("DELETE FROM health_workers WHERE id = :id")
    suspend fun deleteHealthWorkerById(id: Long)

    @Query("DELETE FROM health_workers WHERE name = :name")
    suspend fun deleteHealthWorkerByName(name: String)

    // === Vaccine Stock ===
    @Query("SELECT * FROM vaccine_stock ORDER BY id ASC")
    fun getAllStock(): Flow<List<VaccineStockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockItems(items: List<VaccineStockEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockItem(item: VaccineStockEntity): Long

    @Update
    suspend fun updateStockItem(item: VaccineStockEntity)

    @Delete
    suspend fun deleteStockItem(item: VaccineStockEntity)

    @Query("DELETE FROM vaccine_stock WHERE id = :id")
    suspend fun deleteStockItemById(id: Long)

    // === Audit Logs ===
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLogs(logs: List<AuditLogEntity>)

    @Delete
    suspend fun deleteAuditLog(log: AuditLogEntity)

    @Query("DELETE FROM audit_logs")
    suspend fun clearAllAuditLogs()

    @Query("DELETE FROM audit_logs WHERE id = :id")
    suspend fun deleteAuditLogById(id: Long)

    // === User Accounts ===
    @Query("SELECT * FROM user_accounts ORDER BY id ASC")
    fun getAllUserAccounts(): Flow<List<UserAccountEntity>>

    @Query("SELECT * FROM user_accounts WHERE LOWER(TRIM(emailOrPhone)) = LOWER(TRIM(:identifier)) OR LOWER(TRIM(phone)) = LOWER(TRIM(:identifier)) OR LOWER(TRIM(email)) = LOWER(TRIM(:identifier)) LIMIT 1")
    suspend fun findUserByIdentifier(identifier: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccounts(users: List<UserAccountEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccount(user: UserAccountEntity): Long

    @Update
    suspend fun updateUserAccount(user: UserAccountEntity)

    @Delete
    suspend fun deleteUserAccount(user: UserAccountEntity)

    @Query("DELETE FROM user_accounts WHERE id = :id")
    suspend fun deleteUserAccountById(id: Long)

    // === Global Reset for Real-time Accounting ===
    @Query("DELETE FROM children")
    suspend fun clearAllChildren()

    @Query("DELETE FROM vaccine_records")
    suspend fun clearAllVaccineRecords()

    @Query("UPDATE health_workers SET dosesCount = 0, childrenCount = 0")
    suspend fun resetHealthWorkersMetrics()

    @Query("UPDATE health_facilities SET registeredChildrenCount = 0, coveragePct = 0")
    suspend fun resetHealthFacilitiesMetrics()

    @Query("UPDATE user_accounts SET childrenCount = 0")
    suspend fun resetUserAccountsMetrics()
}
