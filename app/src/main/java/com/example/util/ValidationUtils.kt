package com.example.util

import java.util.regex.Pattern

/**
 * Validation utilities for strict real user data verification in Mozambique.
 * Ensures the application only accepts authentic names, valid contacts, and secure credentials.
 */
object ValidationUtils {

    // Regex for basic email format validation
    private val EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}$"
    )

    val MOZAMBIQUE_PROVINCES = listOf(
        "Maputo Cidade",
        "Maputo Província",
        "Gaza (Xai-Xai)",
        "Inhambane",
        "Sofala (Beira)",
        "Manica (Chimoio)",
        "Tete",
        "Zambézia (Quelimane)",
        "Nampula",
        "Cabo Delgado (Pemba)",
        "Niassa (Lichinga)"
    )

    /**
     * Validates full name:
     * - Must contain at least 2 distinct words (First & Last name)
     * - Must be at least 5 characters total
     * - Must contain only valid alphabetic letters, spaces, hyphens, accents (no fake spam like '123', 'asdfgh', '---')
     */
    fun validateFullName(name: String): ValidationResult {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            return ValidationResult.Invalid("O nome completo é obrigatório.")
        }
        if (trimmed.length < 5) {
            return ValidationResult.Invalid("O nome deve ter pelo menos 5 caracteres.")
        }
        val words = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.size < 2) {
            return ValidationResult.Invalid("Insira o seu nome e apelido (ex: Graça Machel ou Carlos Tembe).")
        }
        for (w in words) {
            if (w.length < 2) {
                return ValidationResult.Invalid("Cada nome deve conter pelo menos 2 letras.")
            }
            if (!w.all { it.isLetter() || it == '-' || it == '\'' }) {
                return ValidationResult.Invalid("O nome deve conter apenas letras válidas.")
            }
        }
        // Reject repeating placeholder characters like "aaaaa", "zzzzz"
        if (trimmed.lowercase().toSet().size <= 2 && trimmed.length > 5) {
            return ValidationResult.Invalid("Por favor insira um nome real válido.")
        }
        return ValidationResult.Valid
    }

    /**
     * Validates Mozambique Phone number:
     * - Accepts formats: +258 84 123 4567, 84 123 4567, 841234567, +258841234567, 82xxxxxxx, 85xxxxxxx, 86xxxxxxx, 87xxxxxxx, 21xxxxxx
     * - Checks valid telecom prefixes: 82, 83, 84, 85, 86, 87 (Vodacom, Movitel, TMCEL)
     */
    fun validateMozambiquePhone(phone: String): ValidationResult {
        val cleaned = phone.replace(Regex("[^0-9]"), "")
        if (cleaned.isBlank()) {
            return ValidationResult.Invalid("O número de telefone é obrigatório.")
        }

        // Strip country code if present (258)
        val localNumber = if (cleaned.startsWith("258") && cleaned.length >= 11) {
            cleaned.substring(3)
        } else {
            cleaned
        }

        if (localNumber.length != 9) {
            return ValidationResult.Invalid("O telefone em Moçambique deve ter 9 dígitos (ex: 84 123 4567).")
        }

        val prefix = localNumber.take(2)
        val validPrefixes = listOf("82", "83", "84", "85", "86", "87", "89", "21")
        if (prefix !in validPrefixes) {
            return ValidationResult.Invalid("Prefixo inválido. Use um número de Moçambique (82, 84, 85, 86 ou 87).")
        }

        // Avoid dummy numbers like 840000000, 841111111
        val remaining = localNumber.substring(2)
        if (remaining.toSet().size == 1) {
            return ValidationResult.Invalid("Por favor insira um número de telefone real.")
        }

        return ValidationResult.Valid
    }

    /**
     * Normalizes phone number to standard national + international format: +258 84 123 4567
     */
    fun formatMozambiquePhone(phone: String): String {
        val digits = phone.replace(Regex("[^0-9]"), "")
        val local = if (digits.startsWith("258") && digits.length >= 11) digits.substring(3) else digits
        if (local.length == 9) {
            return "+258 ${local.substring(0, 2)} ${local.substring(2, 5)} ${local.substring(5, 9)}"
        }
        return phone.trim()
    }

    /**
     * Validates email address (optional in some contexts, but if provided must be real)
     */
    fun validateEmail(email: String, isOptional: Boolean = true): ValidationResult {
        val trimmed = email.trim()
        if (trimmed.isBlank()) {
            return if (isOptional) ValidationResult.Valid else ValidationResult.Invalid("O email é obrigatório.")
        }
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            return ValidationResult.Invalid("Insira um endereço de email válido (ex: utilizador@exemplo.com).")
        }
        return ValidationResult.Valid
    }

    /**
     * Validates password:
     * - Must be at least 6 characters
     * - Cannot be blank or all whitespace
     */
    fun validatePassword(password: String): ValidationResult {
        if (password.isBlank()) {
            return ValidationResult.Invalid("A palavra-passe é obrigatória.")
        }
        if (password.length < 6) {
            return ValidationResult.Invalid("A palavra-passe deve conter pelo menos 6 caracteres.")
        }
        return ValidationResult.Valid
    }

    /**
     * Validates identifier for login (either a valid phone or a valid email)
     */
    fun validateLoginIdentifier(identifier: String): ValidationResult {
        val trimmed = identifier.trim()
        if (trimmed.isBlank()) {
            return ValidationResult.Invalid("Insira o seu número de telefone ou email registado.")
        }
        val isEmail = trimmed.contains("@")
        return if (isEmail) {
            validateEmail(trimmed, isOptional = false)
        } else {
            validateMozambiquePhone(trimmed)
        }
    }
}

sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()

    val isValid: Boolean get() = this is Valid
    val errorMessage: String? get() = (this as? Invalid)?.message
}
