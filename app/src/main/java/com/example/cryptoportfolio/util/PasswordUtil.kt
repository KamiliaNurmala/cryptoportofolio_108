package com.example.cryptoportfolio.util

import java.security.MessageDigest

object PasswordUtil {

    /**
     * Hash password menggunakan SHA-256
     * @param password Plain text password
     * @return Hashed password dalam format hexadecimal
     */
    fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    /**
     * Validasi password strength
     * @param password Password yang akan divalidasi
     * @return true jika password memenuhi kriteria minimum
     */
    fun validatePassword(password: String): Boolean {
        // Minimal 6 karakter
        return password.length >= 6
    }

}