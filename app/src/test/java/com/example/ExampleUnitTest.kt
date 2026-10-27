package com.example

import com.example.util.ValidationUtils
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testValidateFullName() {
        // Valid names
        assertTrue(ValidationUtils.validateFullName("Ana Macuácua").isValid)
        assertTrue(ValidationUtils.validateFullName("Carlos Tembe").isValid)
        assertTrue(ValidationUtils.validateFullName("Manuel de Jesus").isValid)

        // Invalid names (must reject)
        assertFalse(ValidationUtils.validateFullName("").isValid)
        assertFalse(ValidationUtils.validateFullName("Ana").isValid) // single word
        assertFalse(ValidationUtils.validateFullName("12345 67890").isValid) // numbers
        assertFalse(ValidationUtils.validateFullName("a b").isValid) // too short
    }

    @Test
    fun testValidateMozambiquePhone() {
        // Valid MZ phones
        assertTrue(ValidationUtils.validateMozambiquePhone("+258 84 123 4567").isValid)
        assertTrue(ValidationUtils.validateMozambiquePhone("841234567").isValid)
        assertTrue(ValidationUtils.validateMozambiquePhone("821234567").isValid)
        assertTrue(ValidationUtils.validateMozambiquePhone("861234567").isValid)
        assertTrue(ValidationUtils.validateMozambiquePhone("+258871234567").isValid)

        // Invalid phones (must reject)
        assertFalse(ValidationUtils.validateMozambiquePhone("").isValid)
        assertFalse(ValidationUtils.validateMozambiquePhone("12345").isValid)
        assertFalse(ValidationUtils.validateMozambiquePhone("991234567").isValid) // invalid MZ prefix
        assertFalse(ValidationUtils.validateMozambiquePhone("840000000").isValid) // fake repeating
    }

    @Test
    fun testValidatePassword() {
        assertTrue(ValidationUtils.validatePassword("123456").isValid)
        assertTrue(ValidationUtils.validatePassword("minhasenha").isValid)

        assertFalse(ValidationUtils.validatePassword("").isValid)
        assertFalse(ValidationUtils.validatePassword("123").isValid)
    }
}

