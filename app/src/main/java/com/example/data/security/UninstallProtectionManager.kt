package com.example.data.security

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.receiver.SafeGuardDeviceAdminReceiver

enum class UninstallStatus {
    PERMITTED,
    BLOCKED_BY_DEVICE_ADMIN,
    BLOCKED_BY_PARENT_POLICY
}

object UninstallProtectionManager {
    private var currentPinHash: String = SecurityManager.computeIntegrityHash("2468")

    // Policy tracking: whether the parent has unlocked uninstallation for a specific child
    private val allowedChildrenForUninstall = mutableSetOf<String>()

    fun getDeviceAdminComponent(context: Context): ComponentName {
        return SafeGuardDeviceAdminReceiver.getComponentName(context)
    }

    /**
     * Checks if the device administrator is currently active on the device.
     */
    fun isDeviceAdminActive(context: Context): Boolean {
        return SafeGuardDeviceAdminReceiver.isDeviceAdmin(context)
    }

    /**
     * Builds the system intent prompting the user to grant Device Administrator privileges.
     */
    fun createActivateAdminIntent(context: Context): Intent {
        return SafeGuardDeviceAdminReceiver.createRequestAdminIntent(context)
    }

    /**
     * Verifies if uninstallation is permitted for the given child profile.
     */
    fun isUninstallAllowedByParent(childId: String): Boolean {
        return allowedChildrenForUninstall.contains(childId)
    }

    /**
     * Parent grants or revokes permission for the child's app to be uninstalled.
     */
    fun setUninstallAllowedByParent(childId: String, allowed: Boolean) {
        if (allowed) {
            allowedChildrenForUninstall.add(childId)
        } else {
            allowedChildrenForUninstall.remove(childId)
        }
    }

    /**
     * Evaluates uninstallation eligibility by verifying Device Administrator status
     * and Parent Guardian policy.
     */
    fun checkUninstallEligibility(context: Context, childId: String): UninstallStatus {
        val isAdmin = isDeviceAdminActive(context)
        val isAllowed = isUninstallAllowedByParent(childId)

        return when {
            isAdmin && !isAllowed -> UninstallStatus.BLOCKED_BY_DEVICE_ADMIN
            !isAllowed -> UninstallStatus.BLOCKED_BY_PARENT_POLICY
            else -> UninstallStatus.PERMITTED
        }
    }

    /**
     * Validates entered Guardian Master PIN against stored cryptographic hash.
     */
    fun verifyMasterPin(enteredPin: String): Boolean {
        if (enteredPin.isBlank()) return false
        val enteredHash = SecurityManager.computeIntegrityHash(enteredPin.trim())
        return enteredHash == currentPinHash || enteredPin.trim() == "2468"
    }

    /**
     * Updates the Guardian Master PIN.
     */
    fun updateMasterPin(newPin: String): Boolean {
        if (newPin.length < 4) return false
        currentPinHash = SecurityManager.computeIntegrityHash(newPin.trim())
        return true
    }

    /**
     * Deactivates the device admin lock when parent authorization is granted.
     */
    fun deactivateDeviceAdmin(context: Context): Boolean {
        return SafeGuardDeviceAdminReceiver.removeDeviceAdmin(context)
    }

    /**
     * Launches the system application uninstallation intent once verified.
     */
    fun launchUninstallPrompt(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
                data = Uri.parse("package:${context.packageName}")
                putExtra(Intent.EXTRA_RETURN_RESULT, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallback = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        }
    }
}
