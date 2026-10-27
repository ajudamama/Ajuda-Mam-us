package com.example.data.supabase

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Configuration and credentials for Supabase Integration.
 */
object SupabaseConfig {
    private const val PREFS_NAME = "supabase_prefs"
    private const val KEY_PROJECT_URL = "supabase_project_url"
    private const val KEY_ANON_KEY = "supabase_anon_key"

    // Default credentials provided by user
    const val DEFAULT_ANON_KEY = "sb_publishable_-KG_PwSHwv1dfJOdzEPKLg_mMD0sEWV"
    const val DEFAULT_PROJECT_URL = "https://xrtvhmqhqvrhcgweawsr.supabase.co"

    fun getProjectUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_PROJECT_URL, "") ?: ""
        return saved.ifBlank { DEFAULT_PROJECT_URL }
    }

    fun getAnonKey(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_ANON_KEY, "") ?: ""
        return saved.ifBlank { DEFAULT_ANON_KEY }
    }

    fun saveConfig(context: Context, projectUrl: String, anonKey: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_PROJECT_URL, projectUrl.trim().removeSuffix("/"))
            .putString(KEY_ANON_KEY, anonKey.trim())
            .apply()
    }
}

/**
 * Lightweight and robust Supabase Service for VacciTrack.
 * Handles Authentication (GoTrue /auth/v1) and PostgreSQL Table Sync (PostgREST /rest/v1).
 */
class SupabaseService(private val context: Context) {

    companion object {
        private const val TAG = "SupabaseService"
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val projectUrl: String
        get() = SupabaseConfig.getProjectUrl(context)

    val anonKey: String
        get() = SupabaseConfig.getAnonKey(context)

    val isConfigured: Boolean
        get() = projectUrl.isNotBlank() && anonKey.isNotBlank() && projectUrl.startsWith("http")

    /**
     * Test connection to the configured Supabase instance
     */
    suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(
                Exception("Supabase ainda não está totalmente configurado. Por favor insira o URL do Projeto (ex: https://xyz.supabase.co).")
            )
        }

        try {
            val url = "$projectUrl/rest/v1/"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code == 200 || response.code == 404) {
                    Result.success("Ligação ao Supabase estabelecida com sucesso!")
                } else {
                    Result.failure(Exception("Supabase respondeu com código ${response.code}: ${response.message}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Connection test failed", e)
            Result.failure(Exception("Erro ao ligar ao Supabase: ${e.localizedMessage}"))
        }
    }

    /**
     * Sign Up a user in Supabase Auth (/auth/v1/signup)
     */
    suspend fun signUp(email: String, password: String, metadata: Map<String, Any> = emptyMap()): Result<String> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(Exception("Supabase Project URL não configurado."))
        }

        try {
            val bodyJson = JSONObject().apply {
                put("email", email.trim())
                put("password", password)
                if (metadata.isNotEmpty()) {
                    put("data", JSONObject(metadata))
                }
            }

            val request = Request.Builder()
                .url("$projectUrl/auth/v1/signup")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .post(bodyJson.toString().toRequestBody(JSON_MEDIA))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val resBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val json = JSONObject(resBody)
                    val userId = json.optJSONObject("user")?.optString("id") 
                        ?: json.optString("id", "")
                    Result.success(userId)
                } else {
                    val errorMsg = try {
                        JSONObject(resBody).optString("msg", JSONObject(resBody).optString("error_description", response.message))
                    } catch (e: Exception) {
                        response.message
                    }
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign In with password in Supabase Auth (/auth/v1/token?grant_type=password)
     */
    suspend fun signIn(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(Exception("Supabase Project URL não configurado."))
        }

        try {
            val bodyJson = JSONObject().apply {
                put("email", email.trim())
                put("password", password)
            }

            val request = Request.Builder()
                .url("$projectUrl/auth/v1/token?grant_type=password")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .post(bodyJson.toString().toRequestBody(JSON_MEDIA))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val resBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val json = JSONObject(resBody)
                    val accessToken = json.optString("access_token")
                    Result.success(accessToken)
                } else {
                    val errorMsg = try {
                        JSONObject(resBody).optString("error_description", JSONObject(resBody).optString("msg", response.message))
                    } catch (e: Exception) {
                        response.message
                    }
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upsert records to a Supabase PostgREST table (/rest/v1/<table>)
     */
    suspend fun upsertTable(tableName: String, jsonArrayPayload: JSONArray): Result<Int> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(Exception("Supabase URL não configurado."))
        }

        try {
            val request = Request.Builder()
                .url("$projectUrl/rest/v1/$tableName")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(jsonArrayPayload.toString().toRequestBody(JSON_MEDIA))
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code == 201 || response.code == 200) {
                    Result.success(jsonArrayPayload.length())
                } else {
                    val errorBody = response.body?.string() ?: response.message
                    Result.failure(Exception("Erro na tabela $tableName: $errorBody (HTTP ${response.code})"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Upsert to $tableName failed", e)
            Result.failure(e)
        }
    }
}
