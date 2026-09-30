package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChildDao {
    @Query("SELECT * FROM child_profiles")
    fun getAllChildren(): Flow<List<ChildProfileEntity>>

    @Query("SELECT * FROM child_profiles WHERE id = :childId")
    fun getChildById(childId: String): Flow<ChildProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChild(child: ChildProfileEntity)

    @Update
    suspend fun updateChild(child: ChildProfileEntity)

    @Query("UPDATE child_profiles SET isDevicePaused = :isPaused WHERE id = :childId")
    suspend fun updateDevicePauseStatus(childId: String, isPaused: Boolean)

    @Query("UPDATE child_profiles SET isBedtimeActive = :isBedtime WHERE id = :childId")
    suspend fun updateBedtimeStatus(childId: String, isBedtime: Boolean)

    @Query("UPDATE child_profiles SET isStudyModeActive = :isStudy WHERE id = :childId")
    suspend fun updateStudyModeStatus(childId: String, isStudy: Boolean)

    @Query("UPDATE child_profiles SET isLocationSharingEnabled = :enabled WHERE id = :childId")
    suspend fun updateLocationSharing(childId: String, enabled: Boolean)

    @Query("UPDATE child_profiles SET isScreenViewingActive = :active WHERE id = :childId")
    suspend fun updateScreenViewing(childId: String, active: Boolean)

    @Query("UPDATE child_profiles SET dailyLimitMinutes = :minutes WHERE id = :childId")
    suspend fun updateDailyLimit(childId: String, minutes: Int)

    @Query("UPDATE child_profiles SET todayUsedMinutes = todayUsedMinutes + :extraMinutes WHERE id = :childId")
    suspend fun addUsedMinutes(childId: String, extraMinutes: Int)

    @Query("DELETE FROM child_profiles WHERE id = :childId")
    suspend fun deleteChild(childId: String)

    // Pairing Codes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPairingCode(code: PairingCodeEntity)

    @Query("SELECT * FROM pairing_codes WHERE code = :code LIMIT 1")
    suspend fun findPairingCode(code: String): PairingCodeEntity?

    @Query("DELETE FROM pairing_codes WHERE code = :code")
    suspend fun deletePairingCode(code: String)
}

@Dao
interface RulesDao {
    @Query("SELECT * FROM screen_time_rules WHERE childId = :childId")
    fun getScreenTimeRules(childId: String): Flow<List<ScreenTimeRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreenTimeRule(rule: ScreenTimeRuleEntity)

    @Update
    suspend fun updateScreenTimeRule(rule: ScreenTimeRuleEntity)

    // Apps
    @Query("SELECT * FROM app_rules WHERE childId = :childId")
    fun getAppRules(childId: String): Flow<List<AppRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppRule(appRule: AppRuleEntity)

    @Update
    suspend fun updateAppRule(appRule: AppRuleEntity)

    @Query("UPDATE app_rules SET isBlocked = :blocked WHERE id = :appId")
    suspend fun setAppBlocked(appId: Long, blocked: Boolean)

    @Query("UPDATE app_rules SET limitMinutes = :limitMinutes WHERE id = :appId")
    suspend fun setAppLimit(appId: Long, limitMinutes: Int)

    @Query("UPDATE app_rules SET isAlwaysAllowed = :alwaysAllowed WHERE id = :appId")
    suspend fun setAppAlwaysAllowed(appId: Long, alwaysAllowed: Boolean)

    // Websites
    @Query("SELECT * FROM web_rules WHERE childId = :childId")
    fun getWebRules(childId: String): Flow<List<WebRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWebRule(webRule: WebRuleEntity)

    @Delete
    suspend fun deleteWebRule(webRule: WebRuleEntity)

    @Query("UPDATE web_rules SET isBlocked = :isBlocked WHERE id = :ruleId")
    suspend fun setWebRuleBlocked(ruleId: Long, isBlocked: Boolean)
}

@Dao
interface LocationDao {
    @Query("SELECT * FROM geofences WHERE childId = :childId")
    fun getGeofences(childId: String): Flow<List<GeofenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeofence(geofence: GeofenceEntity)

    @Delete
    suspend fun deleteGeofence(geofence: GeofenceEntity)

    @Query("SELECT * FROM location_events WHERE childId = :childId ORDER BY timestamp DESC LIMIT 50")
    fun getLocationEvents(childId: String): Flow<List<LocationEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationEvent(event: LocationEventEntity)
}

@Dao
interface AlertsDao {
    @Query("SELECT * FROM safety_alerts WHERE childId = :childId ORDER BY timestamp DESC")
    fun getAlerts(childId: String): Flow<List<SafetyAlertEntity>>

    @Query("SELECT * FROM safety_alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<SafetyAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: SafetyAlertEntity)

    @Query("UPDATE safety_alerts SET isRead = 1 WHERE id = :alertId")
    suspend fun markAlertRead(alertId: Long)

    @Query("UPDATE safety_alerts SET isRead = 1 WHERE childId = :childId")
    suspend fun markAllAlertsRead(childId: String)

    @Query("DELETE FROM safety_alerts WHERE childId = :childId")
    suspend fun clearAlerts(childId: String)
}

@Dao
interface RequestsDao {
    @Query("SELECT * FROM access_requests WHERE childId = :childId ORDER BY createdAt DESC")
    fun getRequests(childId: String): Flow<List<AccessRequestEntity>>

    @Query("SELECT * FROM access_requests ORDER BY createdAt DESC")
    fun getAllRequests(): Flow<List<AccessRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: AccessRequestEntity)

    @Query("UPDATE access_requests SET status = :status, parentResponseNotes = :notes WHERE id = :requestId")
    suspend fun updateRequestStatus(requestId: Long, status: RequestStatus, notes: String)
}
