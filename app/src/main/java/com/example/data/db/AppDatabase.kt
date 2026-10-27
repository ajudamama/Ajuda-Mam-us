package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ChildEntity::class,
        VaccineRecordEntity::class,
        VaccineCatalogEntity::class,
        HealthFacilityEntity::class,
        HealthWorkerEntity::class,
        VaccineStockEntity::class,
        AuditLogEntity::class,
        UserAccountEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vaccitrack_database"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            populate()
        }

        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            populate()
        }

        private fun populate() {
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.appDao())
                }
            }
        }

        suspend fun populateDatabase(dao: AppDao) {
            InitialData.getDefaultChildren().forEach { dao.insertChild(it) }
            InitialData.getDefaultVaccineRecords().forEach { dao.insertRecord(it) }
            dao.insertCatalogItems(InitialData.getDefaultVaccineCatalog())
            dao.insertFacilities(InitialData.getDefaultFacilities())
            dao.insertHealthWorkers(InitialData.getDefaultHealthWorkers())
            dao.insertStockItems(InitialData.getDefaultStock())
            dao.insertAuditLogs(InitialData.getDefaultAuditLogs())
            dao.insertUserAccounts(InitialData.getDefaultUserAccounts())
        }
    }
}
