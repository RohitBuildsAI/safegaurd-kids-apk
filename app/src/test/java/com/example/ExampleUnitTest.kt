package com.example

import com.example.data.auth.AuthSession
import com.example.data.model.AppRole
import com.example.data.security.SecurityManager
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testSecurityManagerPairingCode() {
        val code = SecurityManager.generatePairingCode()
        assertEquals(6, code.length)
        assertTrue(code.all { it.isDigit() })
    }

    @Test
    fun testSecurityManagerIntegrityHash() {
        val payload = "child_leo_01_telemetry"
        val hash = SecurityManager.computeIntegrityHash(payload)
        assertEquals(64, hash.length) // SHA-256 produces 64 hex characters
    }

    @Test
    fun testSecurityManagerEncryptionDecryptionRoundtrip() {
        val originalText = "SafeGuardKids:Lat=37.7749,Lng=-122.4194"
        val encrypted = SecurityManager.encryptPayload(originalText)
        assertNotEquals(originalText, encrypted)
        val decrypted = SecurityManager.decryptPayload(encrypted)
        assertEquals(originalText, decrypted)
    }

    @Test
    fun testAuthSessionDefaultRole() {
        val session = AuthSession(user = null, role = AppRole.PARENT)
        assertEquals(AppRole.PARENT, session.role)
        assertNull(session.user)
    }

    @Test
    fun testAuthSessionChildRole() {
        val session = AuthSession(user = null, role = AppRole.CHILD, familyId = "fam_123", childProfileId = "child_01")
        assertEquals(AppRole.CHILD, session.role)
        assertEquals("child_01", session.childProfileId)
    }

    @Test
    fun testFirestoreUserDefaults() {
        val user = com.example.data.firestore.models.FirestoreUser()
        assertEquals("", user.userId)
        assertEquals("PARENT", user.role)
        assertTrue(user.createdAt > 0)
    }

    @Test
    fun testFirestoreFamilyStructure() {
        val family = com.example.data.firestore.models.FirestoreFamily(
            familyId = "fam_001",
            name = "Anderson Family",
            ownerId = "user_parent_01",
            inviteCode = "842915",
            memberIds = listOf("user_parent_01", "user_child_02")
        )
        assertEquals("Anderson Family", family.name)
        assertEquals(2, family.memberIds.size)
        assertEquals("842915", family.inviteCode)
    }

    @Test
    fun testFirestoreDeviceRegistrationStructure() {
        val device = com.example.data.firestore.models.FirestoreDevice(
            deviceId = "dev_123",
            familyId = "fam_001",
            childId = "child_leo_01",
            deviceName = "Leo's Pixel",
            deviceModel = "Pixel 8a",
            batteryPct = 95,
            isOnline = true
        )
        assertEquals("dev_123", device.deviceId)
        assertEquals("Pixel 8a", device.deviceModel)
        assertEquals(95, device.batteryPct)
        assertTrue(device.isOnline)
    }

    @Test
    fun testUninstallProtectionParentPermissionToggle() {
        val childId = "child_test_01"
        assertFalse(com.example.data.security.UninstallProtectionManager.isUninstallAllowedByParent(childId))

        com.example.data.security.UninstallProtectionManager.setUninstallAllowedByParent(childId, true)
        assertTrue(com.example.data.security.UninstallProtectionManager.isUninstallAllowedByParent(childId))

        com.example.data.security.UninstallProtectionManager.setUninstallAllowedByParent(childId, false)
        assertFalse(com.example.data.security.UninstallProtectionManager.isUninstallAllowedByParent(childId))
    }

    @Test
    fun testGuardianMasterPinVerification() {
        // Default PIN "2468"
        assertTrue(com.example.data.security.UninstallProtectionManager.verifyMasterPin("2468"))
        assertFalse(com.example.data.security.UninstallProtectionManager.verifyMasterPin("0000"))
        assertFalse(com.example.data.security.UninstallProtectionManager.verifyMasterPin(""))

        // Update PIN
        assertTrue(com.example.data.security.UninstallProtectionManager.updateMasterPin("9876"))
        assertTrue(com.example.data.security.UninstallProtectionManager.verifyMasterPin("9876"))
        assertFalse(com.example.data.security.UninstallProtectionManager.updateMasterPin("12")) // Too short
    }

    @Test
    fun testUninstallEligibilityStatus() {
        val childId = "child_test_status"
        com.example.data.security.UninstallProtectionManager.setUninstallAllowedByParent(childId, false)
        assertFalse(com.example.data.security.UninstallProtectionManager.isUninstallAllowedByParent(childId))

        com.example.data.security.UninstallProtectionManager.setUninstallAllowedByParent(childId, true)
        assertTrue(com.example.data.security.UninstallProtectionManager.isUninstallAllowedByParent(childId))
    }

    @Test
    fun testDeviceAdminReceiverCreation() {
        val receiver = com.example.receiver.DeviceAdminReceiver()
        assertNotNull(receiver)
    }
}

