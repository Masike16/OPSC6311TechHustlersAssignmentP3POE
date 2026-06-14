package com.example.easebudgetv1.utils

import java.security.MessageDigest

object HashUtils {
    /**
     * Hashes the input string using SHA-256.
     */
    fun sha256(input: String): String {
        return MessageDigest
            .getInstance("SHA-256")
            .digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }
}
