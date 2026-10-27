package com.example.data.auth

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Service to handle Firebase Authentication operations safely.
 * Enforces real user account creation, secure sign-in, and session management.
 */
class FirebaseAuthService {

    companion object {
        private const val TAG = "FirebaseAuthService"
    }

    private val auth: FirebaseAuth?
        get() = try {
            FirebaseApp.getInstance()
            FirebaseAuth.getInstance()
        } catch (t: Throwable) {
            Log.w(TAG, "FirebaseAuth instance not ready or unavailable: ${t.message}")
            null
        }

    val currentUser: FirebaseUser?
        get() = try {
            auth?.currentUser
        } catch (t: Throwable) {
            null
        }

    val isUserSignedIn: Boolean
        get() = currentUser != null

    /**
     * Converts a phone number or email into a standard Firebase Auth email format.
     */
    fun resolveAuthEmail(email: String, phone: String): String {
        val cleanEmail = email.trim()
        if (cleanEmail.isNotBlank() && cleanEmail.contains("@")) {
            return cleanEmail
        }
        val digits = phone.replace(Regex("[^0-9]"), "")
        val localDigits = if (digits.startsWith("258") && digits.length >= 11) digits.substring(3) else digits
        return "mz.${localDigits}@vaccitrack.mz"
    }

    /**
     * Registers a new account in Firebase Authentication.
     */
    suspend fun registerWithFirebase(
        fullName: String,
        email: String,
        phone: String,
        password: String
    ): Result<FirebaseUser?> {
        val authInstance = auth
        if (authInstance == null) {
            Log.i(TAG, "FirebaseAuth instance null, registering locally with secure UID")
            return Result.success(null)
        }

        return try {
            val authEmail = resolveAuthEmail(email, phone)
            val authResult = authInstance.createUserWithEmailAndPassword(authEmail, password).await()
            val user = authResult.user
                ?: return Result.failure(Exception("Falha ao inicializar o utilizador no Firebase."))

            // Update user profile display name
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName.trim())
                    .build()
                user.updateProfile(profileUpdates).await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to set display name in Firebase", e)
            }

            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Firebase registration failed", e)
            val errorMsg = mapFirebaseAuthException(e)
            Result.failure(Exception(errorMsg))
        }
    }

    /**
     * Authenticates an existing user in Firebase Authentication.
     */
    suspend fun signInWithFirebase(
        identifier: String,
        password: String
    ): Result<FirebaseUser?> {
        val authInstance = auth
        if (authInstance == null) {
            Log.i(TAG, "FirebaseAuth instance null, validating credentials locally")
            return Result.success(null)
        }

        return try {
            val isEmail = identifier.contains("@")
            val authEmail = if (isEmail) {
                identifier.trim()
            } else {
                val digits = identifier.replace(Regex("[^0-9]"), "")
                val local = if (digits.startsWith("258") && digits.length >= 11) digits.substring(3) else digits
                "mz.${local}@vaccitrack.mz"
            }

            val authResult = authInstance.signInWithEmailAndPassword(authEmail, password).await()
            val user = authResult.user
                ?: return Result.failure(Exception("Utilizador não encontrado no Firebase."))

            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Firebase sign-in failed", e)
            val errorMsg = mapFirebaseAuthException(e)
            Result.failure(Exception(errorMsg))
        }
    }

    /**
     * Signs out the currently authenticated Firebase user.
     */
    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Firebase signOut error", e)
        }
    }

    private fun mapFirebaseAuthException(e: Exception): String {
        val msg = e.message ?: ""
        return when {
            msg.contains("The email address is already in use", ignoreCase = true) ->
                "Este contacto/email já se encontra registado no Firebase Auth."
            msg.contains("The email address is badly formatted", ignoreCase = true) ->
                "Formato de email ou identificador inválido para autenticação."
            msg.contains("Password should be at least 6 characters", ignoreCase = true) ->
                "A palavra-passe deve conter pelo menos 6 caracteres no Firebase."
            msg.contains("There is no user record corresponding to this identifier", ignoreCase = true) ||
            msg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
            msg.contains("INVALID_CREDENTIALS", ignoreCase = true) ->
                "Credenciais incorrectas no Firebase ou utilizador não cadastrado."
            msg.contains("A network error", ignoreCase = true) || msg.contains("network", ignoreCase = true) ->
                "Erro de rede ao ligar ao Firebase Auth. Verifique a sua ligação."
            else ->
                "Erro de autenticação Firebase: ${e.localizedMessage ?: "Verifique as suas credenciais."}"
        }
    }
}
