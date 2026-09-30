package com.example.data.repository

import com.example.data.local.SafeGuardDatabase
import com.example.data.model.*
import com.example.data.security.SecurityManager
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class SafeGuardRepository(private val database: SafeGuardDatabase) {
    private val childDao = database.childDao()
    private val rulesDao = database.rulesDao()
    private val locationDao = database.locationDao()
    private val alertsDao = database.alertsDao()
    private val requestsDao = database.requestsDao()

    // Children
    fun getAllChildren(): Flow<List<ChildProfileEntity>> = childDao.getAllChildren()
    fun getChildById(childId: String): Flow<ChildProfileEntity?> = childDao.getChildById(childId)

    suspend fun addChild(
        name: String,
        age: Int,
        avatarEmoji: String,
        deviceModel: String,
        dailyLimitMinutes: Int = 120
    ): String {
        val newId = "child_" + UUID.randomUUID().toString().take(8)
        val child = ChildProfileEntity(
            id = newId,
            familyId = "family_anderson_01",
            name = name,
            age = age,
            avatarEmoji = avatarEmoji,
            deviceModel = deviceModel,
            onlineStatus = DeviceOnlineStatus.ONLINE,
            batteryPct = 100,
            isCharging = false,
            lastSeenTimestamp = System.currentTimeMillis(),
            todayUsedMinutes = 0,
            dailyLimitMinutes = dailyLimitMinutes,
            isDevicePaused = false,
            isBedtimeActive = false,
            isStudyModeActive = false,
            isLocationSharingEnabled = true,
            isScreenViewingActive = false,
            locationAddress = "Home",
            latitude = 37.7730,
            longitude = -122.4210
        )
        childDao.insertChild(child)

        // Seed basic rules for new child
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        days.forEach { day ->
            rulesDao.insertScreenTimeRule(
                ScreenTimeRuleEntity(
                    childId = newId,
                    dayOfWeek = day,
                    maxMinutes = dailyLimitMinutes,
                    bedTimeStart = "21:30",
                    bedTimeEnd = "07:00",
                    studyHoursStart = "08:30",
                    studyHoursEnd = "15:00",
                    isEnabled = true
                )
            )
        }
        return newId
    }

    suspend fun updateDevicePause(childId: String, isPaused: Boolean) {
        childDao.updateDevicePauseStatus(childId, isPaused)
        val action = if (isPaused) "paused" else "unpaused"
        alertsDao.insertAlert(
            SafetyAlertEntity(
                childId = childId,
                type = AlertType.PERMISSION_CHANGED,
                title = "Device $action by Parent",
                message = "Device access was $action by parent supervisor.",
                timestamp = System.currentTimeMillis(),
                severity = AlertSeverity.INFO,
                isRead = false
            )
        )
    }

    suspend fun updateBedtime(childId: String, isBedtime: Boolean) {
        childDao.updateBedtimeStatus(childId, isBedtime)
    }

    suspend fun updateStudyMode(childId: String, isStudy: Boolean) {
        childDao.updateStudyModeStatus(childId, isStudy)
    }

    suspend fun updateDailyLimit(childId: String, minutes: Int) {
        childDao.updateDailyLimit(childId, minutes)
    }

    suspend fun updateLocationSharing(childId: String, enabled: Boolean) {
        childDao.updateLocationSharing(childId, enabled)
    }

    suspend fun updateScreenViewing(childId: String, active: Boolean) {
        childDao.updateScreenViewing(childId, active)
        if (active) {
            alertsDao.insertAlert(
                SafetyAlertEntity(
                    childId = childId,
                    type = AlertType.PERMISSION_CHANGED,
                    title = "Screen Viewing Session Active",
                    message = "Official transparent screen viewing started with explicit device consent.",
                    timestamp = System.currentTimeMillis(),
                    severity = AlertSeverity.INFO,
                    isRead = false
                )
            )
        }
    }

    // Rules
    fun getScreenTimeRules(childId: String): Flow<List<ScreenTimeRuleEntity>> =
        rulesDao.getScreenTimeRules(childId)

    suspend fun updateScreenTimeRule(rule: ScreenTimeRuleEntity) {
        rulesDao.updateScreenTimeRule(rule)
    }

    // Apps
    fun getAppRules(childId: String): Flow<List<AppRuleEntity>> = rulesDao.getAppRules(childId)

    suspend fun setAppBlocked(appId: Long, blocked: Boolean) {
        rulesDao.setAppBlocked(appId, blocked)
    }

    suspend fun setAppLimit(appId: Long, limitMinutes: Int) {
        rulesDao.setAppLimit(appId, limitMinutes)
    }

    suspend fun setAppAlwaysAllowed(appId: Long, alwaysAllowed: Boolean) {
        rulesDao.setAppAlwaysAllowed(appId, alwaysAllowed)
    }

    suspend fun insertAppRule(appRule: AppRuleEntity) {
        rulesDao.insertAppRule(appRule)
    }

    // Websites
    fun getWebRules(childId: String): Flow<List<WebRuleEntity>> = rulesDao.getWebRules(childId)

    suspend fun addWebRule(childId: String, domain: String, category: String, isBlocked: Boolean) {
        rulesDao.insertWebRule(
            WebRuleEntity(
                childId = childId,
                urlOrDomain = domain.trim().lowercase(),
                category = category,
                isBlocked = isBlocked,
                isCustomRule = true
            )
        )
    }

    suspend fun setWebRuleBlocked(ruleId: Long, isBlocked: Boolean) {
        rulesDao.setWebRuleBlocked(ruleId, isBlocked)
    }

    suspend fun deleteWebRule(webRule: WebRuleEntity) {
        rulesDao.deleteWebRule(webRule)
    }

    // Geofences & Location
    fun getGeofences(childId: String): Flow<List<GeofenceEntity>> = locationDao.getGeofences(childId)

    suspend fun addGeofence(
        childId: String,
        name: String,
        address: String,
        radiusMeters: Int,
        emoji: String
    ) {
        locationDao.insertGeofence(
            GeofenceEntity(
                childId = childId,
                name = name,
                address = address,
                latitude = 37.7749 + (Math.random() - 0.5) * 0.01,
                longitude = -122.4194 + (Math.random() - 0.5) * 0.01,
                radiusMeters = radiusMeters,
                isSafeZone = true,
                notifyOnEnter = true,
                notifyOnExit = true,
                iconEmoji = emoji
            )
        )
    }

    suspend fun deleteGeofence(geofence: GeofenceEntity) {
        locationDao.deleteGeofence(geofence)
    }

    fun getLocationEvents(childId: String): Flow<List<LocationEventEntity>> =
        locationDao.getLocationEvents(childId)

    // Alerts
    fun getAlerts(childId: String): Flow<List<SafetyAlertEntity>> = alertsDao.getAlerts(childId)
    fun getAllAlerts(): Flow<List<SafetyAlertEntity>> = alertsDao.getAllAlerts()

    suspend fun markAlertRead(alertId: Long) {
        alertsDao.markAlertRead(alertId)
    }

    suspend fun markAllAlertsRead(childId: String) {
        alertsDao.markAllAlertsRead(childId)
    }

    suspend fun insertAlert(alert: SafetyAlertEntity) {
        alertsDao.insertAlert(alert)
    }

    // Requests
    fun getRequests(childId: String): Flow<List<AccessRequestEntity>> = requestsDao.getRequests(childId)
    fun getAllRequests(): Flow<List<AccessRequestEntity>> = requestsDao.getAllRequests()

    suspend fun submitRequest(
        childId: String,
        type: RequestType,
        targetName: String,
        minutes: Int,
        reason: String
    ) {
        requestsDao.insertRequest(
            AccessRequestEntity(
                childId = childId,
                type = type,
                targetName = targetName,
                requestedMinutes = minutes,
                reason = reason,
                status = RequestStatus.PENDING,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun respondToRequest(requestId: Long, childId: String, approved: Boolean, minutesGranted: Int, notes: String) {
        val newStatus = if (approved) RequestStatus.APPROVED else RequestStatus.REJECTED
        requestsDao.updateRequestStatus(requestId, newStatus, notes)

        if (approved && minutesGranted > 0) {
            // Extend daily limit or reduce used minutes
            childDao.updateDailyLimit(childId, 180) // Example updated limit
        }

        alertsDao.insertAlert(
            SafetyAlertEntity(
                childId = childId,
                type = AlertType.PERMISSION_CHANGED,
                title = if (approved) "Request Approved" else "Request Declined",
                message = if (approved) "Parent approved request for $minutesGranted mins: $notes"
                else "Parent declined request: $notes",
                timestamp = System.currentTimeMillis(),
                severity = AlertSeverity.INFO,
                isRead = false
            )
        )
    }

    // Pairing Code Operations
    suspend fun generateNewPairingCode(childId: String, childName: String, deviceModel: String): String {
        val code = SecurityManager.generatePairingCode()
        childDao.insertPairingCode(
            PairingCodeEntity(
                code = code,
                childId = childId,
                childName = childName,
                deviceModel = deviceModel,
                expiresAt = System.currentTimeMillis() + (15 * 60 * 1000) // 15 min expiry
            )
        )
        return code
    }

    suspend fun verifyAndPairCode(code: String): PairingCodeEntity? {
        val found = childDao.findPairingCode(code.trim())
        if (found != null) {
            childDao.deletePairingCode(code.trim())
        }
        return found
    }

    // Emergency SOS Trigger
    suspend fun triggerEmergencySos(childId: String, childName: String, locationStr: String) {
        val now = System.currentTimeMillis()
        alertsDao.insertAlert(
            SafetyAlertEntity(
                childId = childId,
                type = AlertType.EMERGENCY_SOS,
                title = "🚨 EMERGENCY SOS: $childName",
                message = "$childName triggered an Emergency SOS alert near $locationStr. Check in immediately!",
                timestamp = now,
                severity = AlertSeverity.CRITICAL,
                isRead = false
            )
        )
        locationDao.insertLocationEvent(
            LocationEventEntity(
                childId = childId,
                latitude = 37.7749,
                longitude = -122.4194,
                locationName = "$locationStr (SOS Location)",
                timestamp = now,
                eventType = "SOS Trigger"
            )
        )
    }

    // Privacy Data Export
    fun generateExportDataJson(child: ChildProfileEntity): String {
        val encryptedChecksum = SecurityManager.computeIntegrityHash(child.id + child.name)
        return """
        {
          "exportVersion": "1.0",
          "exportTimestamp": ${System.currentTimeMillis()},
          "account": {
            "familyId": "${child.familyId}",
            "childId": "${child.id}",
            "name": "${child.name}",
            "age": ${child.age},
            "deviceModel": "${child.deviceModel}"
          },
          "privacySettings": {
            "locationSharing": ${child.isLocationSharingEnabled},
            "screenViewing": ${child.isScreenViewingActive},
            "encryptionStandard": "AES-256-GCM / TLS 1.3",
            "integrityChecksum": "$encryptedChecksum"
          },
          "dataRetention": "30 days rolling audit logs",
          "accessTransparency": "Official Android App Usage and Location APIs only. No covert monitoring, no keylogging."
        }
        """.trimIndent()
    }
}
