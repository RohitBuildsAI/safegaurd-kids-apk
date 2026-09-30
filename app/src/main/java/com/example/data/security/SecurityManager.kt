package com.example.data.security

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object SecurityManager {
    private const val AES_GCM_NOPADDING = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    // App-level session secret key derived for device-to-parent encryption
    private val masterKeyBytes: ByteArray by lazy {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.digest("SafeGuardKids-E2EE-MasterSecret-2026".toByteArray(StandardCharsets.UTF_8))
    }

    private val secretKey: SecretKey by lazy {
        SecretKeySpec(masterKeyBytes, "AES")
    }

    /**
     * Encrypts sensitive child activity payloads using AES-256-GCM.
     */
    fun encryptPayload(plainText: String): String {
        return try {
            val cipher = Cipher.getInstance(AES_GCM_NOPADDING)
            val iv = ByteArray(GCM_IV_LENGTH)
            SecureRandom().nextBytes(iv)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
            Base64.getEncoder().encodeToString(combined)
        } catch (e: Exception) {
            plainText
        }
    }

    /**
     * Decrypts an AES-256-GCM ciphertext payload.
     */
    fun decryptPayload(cipherText: String): String {
        return try {
            val combined = Base64.getDecoder().decode(cipherText)
            if (combined.size < GCM_IV_LENGTH) return cipherText
            val iv = ByteArray(GCM_IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH)
            val encryptedBytes = ByteArray(combined.size - GCM_IV_LENGTH)
            System.arraycopy(combined, GCM_IV_LENGTH, encryptedBytes, 0, encryptedBytes.size)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            val cipher = Cipher.getInstance(AES_GCM_NOPADDING)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            cipherText
        }
    }

    /**
     * Computes SHA-256 digest for audit log integrity verification.
     */
    fun computeIntegrityHash(content: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(content.toByteArray(StandardCharsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Generates a secure random 6-digit pairing code.
     */
    fun generatePairingCode(): String {
        val random = SecureRandom()
        val code = 100000 + random.nextInt(900000)
        return code.toString()
    }
}
