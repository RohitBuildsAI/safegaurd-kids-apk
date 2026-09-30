package com.example.data.firestore.models

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class FirestoreUser(
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: String = "PARENT", // "PARENT" or "CHILD"
    val familyId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class FirestoreFamily(
    val familyId: String = "",
    val name: String = "",
    val ownerId: String = "",
    val inviteCode: String = "",
    val memberIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class FirestoreDevice(
    val deviceId: String = "",
    val familyId: String = "",
    val childId: String = "",
    val deviceName: String = "",
    val deviceModel: String = "",
    val platform: String = "Android",
    val pairedAt: Long = System.currentTimeMillis(),
    val lastSeen: Long = System.currentTimeMillis(),
    val batteryPct: Int = 100,
    val isOnline: Boolean = true
)
