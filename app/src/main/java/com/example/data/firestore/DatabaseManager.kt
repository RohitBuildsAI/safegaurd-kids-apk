package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.firestore.models.FirestoreDevice
import com.example.data.firestore.models.FirestoreFamily
import com.example.data.firestore.models.FirestoreUser
import com.example.data.security.SecurityManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * DatabaseManager handles cloud persistence operations against Google Cloud Firestore.
 * Always targets the custom provisioned database ID defined in firebase_applet_config.xml.
 */
class DatabaseManager(
    private val databaseId: String
) {
    companion object {
        private const val TAG = "SafeGuardDatabaseManager"

        @Volatile
        private var INSTANCE: DatabaseManager? = null

        fun getInstance(context: Context): DatabaseManager {
            return INSTANCE ?: synchronized(this) {
                val dbId = context.getString(R.string.firestore_database_id)
                val instance = DatabaseManager(dbId)
                INSTANCE = instance
                instance
            }
        }

        fun getInstance(databaseId: String): DatabaseManager {
            return INSTANCE ?: synchronized(this) {
                val instance = DatabaseManager(databaseId)
                INSTANCE = instance
                instance
            }
        }
    }

    // CRITICAL: Always initialize with custom provisioned database ID
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(databaseId)
    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be authenticated before performing database operations.")
    }

    // ========================================================
    // USERS COLLECTION
    // ========================================================

    /**
     * Creates or updates a user profile document in /users/{userId}.
     */
    suspend fun createOrUpdateUser(user: FirestoreUser): Result<Unit> {
        return try {
            val uid = user.userId.ifBlank { requireUserId() }
            val finalUser = user.copy(userId = uid)
            firestore.collection("users").document(uid).set(finalUser).await()
            Log.d(TAG, "User document saved for $uid")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Fetches a user profile document.
     */
    suspend fun getUser(userId: String): Result<FirestoreUser?> {
        return try {
            val snapshot = firestore.collection("users").document(userId).get().await()
            val user = snapshot.toObject(FirestoreUser::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user $userId: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Observes real-time updates to a user profile document.
     */
    fun observeUser(userId: String): Flow<FirestoreUser?> {
        return firestore.collection("users").document(userId)
            .snapshots()
            .map { it.toObject(FirestoreUser::class.java) }
            .catch { e ->
                Log.e(TAG, "Error observing user $userId: ${e.message}", e)
                emit(null)
            }
    }

    // ========================================================
    // FAMILIES COLLECTION
    // ========================================================

    /**
     * Creates a new Family unit with the current authenticated user as the owner.
     * Generates a 6-digit secure family invite code.
     */
    suspend fun createFamily(familyName: String, ownerId: String = ""): Result<FirestoreFamily> {
        return try {
            val uid = ownerId.ifBlank { requireUserId() }
            val familyId = "fam_" + UUID.randomUUID().toString().take(10)
            val inviteCode = SecurityManager.generatePairingCode()
            val now = System.currentTimeMillis()

            val family = FirestoreFamily(
                familyId = familyId,
                name = familyName.trim(),
                ownerId = uid,
                inviteCode = inviteCode,
                memberIds = listOf(uid),
                createdAt = now,
                updatedAt = now
            )

            // Save family document
            firestore.collection("families").document(familyId).set(family).await()

            // Update user profile with new family ID
            firestore.collection("users").document(uid).update(
                mapOf(
                    "familyId" to familyId,
                    "role" to "PARENT"
                )
            ).await()

            Log.i(TAG, "Created family $familyId with invite code $inviteCode")
            Result.success(family)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating family: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Fetches a Family document by ID.
     */
    suspend fun getFamily(familyId: String): Result<FirestoreFamily?> {
        return try {
            val snapshot = firestore.collection("families").document(familyId).get().await()
            val family = snapshot.toObject(FirestoreFamily::class.java)
            Result.success(family)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching family $familyId: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Real-time observation of a Family document.
     */
    fun observeFamily(familyId: String): Flow<FirestoreFamily?> {
        return firestore.collection("families").document(familyId)
            .snapshots()
            .map { it.toObject(FirestoreFamily::class.java) }
            .catch { e ->
                Log.e(TAG, "Error observing family $familyId: ${e.message}", e)
                emit(null)
            }
    }

    /**
     * Joins an existing family using a verified invite code.
     */
    suspend fun joinFamily(familyId: String, inviteCode: String, userId: String = ""): Result<Unit> {
        return try {
            val uid = userId.ifBlank { requireUserId() }
            val familyRef = firestore.collection("families").document(familyId)
            val snapshot = familyRef.get().await()
            val family = snapshot.toObject(FirestoreFamily::class.java)
                ?: throw IllegalArgumentException("Family not found with ID $familyId")

            if (family.inviteCode != inviteCode.trim()) {
                throw IllegalArgumentException("Invalid invite code")
            }

            // Add user to memberIds array
            familyRef.update(
                mapOf(
                    "memberIds" to FieldValue.arrayUnion(uid),
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()

            // Link user document
            firestore.collection("users").document(uid).update(
                mapOf("familyId" to familyId)
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error joining family $familyId: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ========================================================
    // DEVICES SUBCOLLECTION (/families/{familyId}/devices/{deviceId})
    // ========================================================

    /**
     * Registers a new mobile device (Parent guardian or Child companion) within a family.
     */
    suspend fun registerDevice(
        familyId: String,
        childId: String = "",
        deviceName: String,
        deviceModel: String,
        platform: String = "Android"
    ): Result<FirestoreDevice> {
        return try {
            val deviceId = "dev_" + UUID.randomUUID().toString().take(12)
            val now = System.currentTimeMillis()

            val device = FirestoreDevice(
                deviceId = deviceId,
                familyId = familyId,
                childId = childId,
                deviceName = deviceName.trim(),
                deviceModel = deviceModel.trim(),
                platform = platform,
                pairedAt = now,
                lastSeen = now,
                batteryPct = 100,
                isOnline = true
            )

            firestore.collection("families")
                .document(familyId)
                .collection("devices")
                .document(deviceId)
                .set(device)
                .await()

            Log.i(TAG, "Registered device $deviceId for family $familyId")
            Result.success(device)
        } catch (e: Exception) {
            Log.e(TAG, "Error registering device: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Updates telemetry heartbeat for a registered device (battery level, online status).
     */
    suspend fun updateDeviceHeartbeat(
        familyId: String,
        deviceId: String,
        batteryPct: Int,
        isOnline: Boolean
    ): Result<Unit> {
        return try {
            val deviceRef = firestore.collection("families")
                .document(familyId)
                .collection("devices")
                .document(deviceId)

            deviceRef.update(
                mapOf(
                    "batteryPct" to batteryPct,
                    "isOnline" to isOnline,
                    "lastSeen" to System.currentTimeMillis()
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating device heartbeat: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Observes real-time list of all registered devices in a family.
     */
    fun observeDevices(familyId: String): Flow<List<FirestoreDevice>> {
        return firestore.collection("families")
            .document(familyId)
            .collection("devices")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(FirestoreDevice::class.java)
            }
            .catch { e ->
                Log.e(TAG, "Error observing devices for family $familyId: ${e.message}", e)
                emit(emptyList())
            }
    }

    /**
     * Unregisters/removes a device from a family.
     */
    suspend fun unregisterDevice(familyId: String, deviceId: String): Result<Unit> {
        return try {
            firestore.collection("families")
                .document(familyId)
                .collection("devices")
                .document(deviceId)
                .delete()
                .await()

            Log.i(TAG, "Unregistered device $deviceId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering device $deviceId: ${e.message}", e)
            Result.failure(e)
        }
    }
}
