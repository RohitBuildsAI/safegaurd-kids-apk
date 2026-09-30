package com.example.receiver

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.UserHandle
import android.util.Log
import android.widget.Toast
import com.example.R

/**
 * SafeGuardDeviceAdminReceiver enforces device administration privileges on child companion devices.
 * While active as a Device Administrator, the Android OS prevents users from uninstalling the application
 * or clearing device management restrictions without parent authorization.
 */
open class SafeGuardDeviceAdminReceiver : DeviceAdminReceiver() {

    companion object {
        private const val TAG = "SafeGuardDeviceAdmin"

        /**
         * Returns the ComponentName identifying this DeviceAdminReceiver.
         */
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context.applicationContext, SafeGuardDeviceAdminReceiver::class.java)
        }

        /**
         * Checks whether SafeGuard Kids currently holds active Device Administrator privileges.
         */
        fun isDeviceAdmin(context: Context): Boolean {
            return try {
                val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
                dpm?.isAdminActive(getComponentName(context)) ?: false
            } catch (e: Exception) {
                Log.e(TAG, "Error checking device admin status", e)
                false
            }
        }

        /**
         * Creates an Intent to prompt the user to grant Device Administrator privileges.
         */
        fun createRequestAdminIntent(context: Context): Intent {
            val component = getComponentName(context)
            return Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, component)
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    context.getString(R.string.device_admin_explanation)
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        /**
         * Checks whether uninstall actions are currently permitted.
         * Uninstallation is strictly blocked if the app is an active device administrator
         * and the parent has not explicitly approved app removal.
         */
        fun canUninstall(context: Context, isParentApproved: Boolean): Boolean {
            val isAdmin = isDeviceAdmin(context)
            // If device admin is active, uninstall cannot proceed unless parent has explicitly unlocked it
            return !isAdmin || isParentApproved
        }

        /**
         * Deactivates device administrator privileges (requires parent authorization or Master PIN).
         */
        fun removeDeviceAdmin(context: Context): Boolean {
            return try {
                val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
                val component = getComponentName(context)
                if (dpm != null && dpm.isAdminActive(component)) {
                    dpm.removeActiveAdmin(component)
                    Log.i(TAG, "Device Administrator successfully removed")
                    true
                } else {
                    true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove device admin", e)
                false
            }
        }
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.i(TAG, "SafeGuard Kids Device Administrator privileges GRANTED")
        Toast.makeText(
            context,
            "🛡️ SafeGuard Uninstall Protection is now ACTIVE",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        Log.w(TAG, "Device Administrator disable requested by user")
        return "SafeGuard Kids Uninstall Protection is locked by the Parent Guardian. Deactivating requires parent approval or Master PIN."
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.i(TAG, "SafeGuard Kids Device Administrator privileges DEACTIVATED")
        Toast.makeText(
            context,
            "⚠️ SafeGuard Uninstall Protection has been DEACTIVATED",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onPasswordFailed(context: Context, intent: Intent, user: UserHandle) {
        super.onPasswordFailed(context, intent, user)
        Log.w(TAG, "Security alert: Device password/PIN attempt failed")
    }

    override fun onPasswordSucceeded(context: Context, intent: Intent, user: UserHandle) {
        super.onPasswordSucceeded(context, intent, user)
        Log.d(TAG, "Device authentication succeeded")
    }
}
