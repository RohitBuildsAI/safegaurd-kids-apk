package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SafeGuardDatabase
import com.example.data.model.*
import com.example.data.repository.SafeGuardRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ParentNavDestination {
    DASHBOARD,
    SCREEN_TIME,
    APPS,
    WEBSITES,
    LOCATION,
    ALERTS,
    REQUESTS,
    PAIRING,
    PRIVACY
}

enum class ChildNavDestination {
    DASHBOARD,
    REQUESTS,
    SAFETY,
    PRIVACY
}

data class SafeGuardUiState(
    val currentRole: AppRole = AppRole.PARENT,
    val parentDestination: ParentNavDestination = ParentNavDestination.DASHBOARD,
    val childDestination: ChildNavDestination = ChildNavDestination.DASHBOARD,
    val children: List<ChildProfileEntity> = emptyList(),
    val selectedChildId: String = "child_leo_01",
    val activeChild: ChildProfileEntity? = null,
    val screenTimeRules: List<ScreenTimeRuleEntity> = emptyList(),
    val appRules: List<AppRuleEntity> = emptyList(),
    val webRules: List<WebRuleEntity> = emptyList(),
    val geofences: List<GeofenceEntity> = emptyList(),
    val locationEvents: List<LocationEventEntity> = emptyList(),
    val alerts: List<SafetyAlertEntity> = emptyList(),
    val requests: List<AccessRequestEntity> = emptyList(),
    val activePairingCode: String? = null,
    val pairingSuccessMessage: String? = null,
    val exportedDataJson: String? = null,
    val isSosActive: Boolean = false,
    val timeViewPeriod: String = "Today", // "Today", "Week", "Month"
    val safeSearchEnabled: Boolean = true,
    val studyBrowsingMode: Boolean = false,
    val isUninstallProtectionActive: Boolean = true,
    val isUninstallAllowedByParent: Boolean = false,
    val uninstallAttemptNotice: String? = null
)

class SafeGuardViewModel(application: Application) : AndroidViewModel(application) {
    private val database = SafeGuardDatabase.getDatabase(application, viewModelScope)
    private val repository = SafeGuardRepository(database)

    private val _uiState = MutableStateFlow(SafeGuardUiState())
    val uiState: StateFlow<SafeGuardUiState> = _uiState.asStateFlow()

    init {
        // Observe all children
        viewModelScope.launch {
            repository.getAllChildren().collect { childrenList ->
                _uiState.update { state ->
                    val currentSelectedId = if (childrenList.any { it.id == state.selectedChildId }) {
                        state.selectedChildId
                    } else {
                        childrenList.firstOrNull()?.id ?: ""
                    }
                    val active = childrenList.firstOrNull { it.id == currentSelectedId }
                    state.copy(
                        children = childrenList,
                        selectedChildId = currentSelectedId,
                        activeChild = active
                    )
                }
                // Once we have children, observe child-specific data
                val activeId = _uiState.value.selectedChildId
                if (activeId.isNotEmpty()) {
                    loadChildSpecificData(activeId)
                }
            }
        }

        // Observe global alerts
        viewModelScope.launch {
            repository.getAllAlerts().collect { alertList ->
                _uiState.update { it.copy(alerts = alertList) }
            }
        }

        // Observe global requests
        viewModelScope.launch {
            repository.getAllRequests().collect { reqList ->
                _uiState.update { it.copy(requests = reqList) }
            }
        }
    }

    private fun loadChildSpecificData(childId: String) {
        viewModelScope.launch {
            repository.getScreenTimeRules(childId).collect { rules ->
                _uiState.update { it.copy(screenTimeRules = rules) }
            }
        }
        viewModelScope.launch {
            repository.getAppRules(childId).collect { apps ->
                _uiState.update { it.copy(appRules = apps) }
            }
        }
        viewModelScope.launch {
            repository.getWebRules(childId).collect { webs ->
                _uiState.update { it.copy(webRules = webs) }
            }
        }
        viewModelScope.launch {
            repository.getGeofences(childId).collect { fences ->
                _uiState.update { it.copy(geofences = fences) }
            }
        }
        viewModelScope.launch {
            repository.getLocationEvents(childId).collect { events ->
                _uiState.update { it.copy(locationEvents = events) }
            }
        }
    }

    fun selectRole(role: AppRole) {
        _uiState.update { it.copy(currentRole = role) }
    }

    fun selectParentDestination(dest: ParentNavDestination) {
        _uiState.update { it.copy(parentDestination = dest) }
    }

    fun selectChildDestination(dest: ChildNavDestination) {
        _uiState.update { it.copy(childDestination = dest) }
    }

    fun selectChild(childId: String) {
        _uiState.update { state ->
            val child = state.children.firstOrNull { it.id == childId }
            state.copy(selectedChildId = childId, activeChild = child)
        }
        loadChildSpecificData(childId)
    }

    fun setTimeViewPeriod(period: String) {
        _uiState.update { it.copy(timeViewPeriod = period) }
    }

    fun toggleDevicePause(childId: String) {
        val current = _uiState.value.activeChild?.isDevicePaused ?: false
        viewModelScope.launch {
            repository.updateDevicePause(childId, !current)
        }
    }

    fun toggleBedtime(childId: String) {
        val current = _uiState.value.activeChild?.isBedtimeActive ?: false
        viewModelScope.launch {
            repository.updateBedtime(childId, !current)
        }
    }

    fun toggleStudyMode(childId: String) {
        val current = _uiState.value.activeChild?.isStudyModeActive ?: false
        viewModelScope.launch {
            repository.updateStudyMode(childId, !current)
        }
    }

    fun setDailyLimit(childId: String, minutes: Int) {
        viewModelScope.launch {
            repository.updateDailyLimit(childId, minutes)
        }
    }

    fun toggleLocationSharing(childId: String) {
        val current = _uiState.value.activeChild?.isLocationSharingEnabled ?: true
        viewModelScope.launch {
            repository.updateLocationSharing(childId, !current)
        }
    }

    fun toggleScreenViewing(childId: String) {
        val current = _uiState.value.activeChild?.isScreenViewingActive ?: false
        viewModelScope.launch {
            repository.updateScreenViewing(childId, !current)
        }
    }

    fun toggleSafeSearch() {
        _uiState.update { it.copy(safeSearchEnabled = !it.safeSearchEnabled) }
    }

    fun toggleStudyBrowsing() {
        _uiState.update { it.copy(studyBrowsingMode = !it.studyBrowsingMode) }
    }

    // Apps Management
    fun setAppBlocked(appId: Long, blocked: Boolean) {
        viewModelScope.launch {
            repository.setAppBlocked(appId, blocked)
        }
    }

    fun setAppLimit(appId: Long, minutes: Int) {
        viewModelScope.launch {
            repository.setAppLimit(appId, minutes)
        }
    }

    fun setAppAlwaysAllowed(appId: Long, alwaysAllowed: Boolean) {
        viewModelScope.launch {
            repository.setAppAlwaysAllowed(appId, alwaysAllowed)
        }
    }

    // Web Management
    fun addWebRule(domain: String, category: String, isBlocked: Boolean) {
        val childId = _uiState.value.selectedChildId
        viewModelScope.launch {
            repository.addWebRule(childId, domain, category, isBlocked)
        }
    }

    fun toggleWebRuleBlocked(ruleId: Long, currentBlocked: Boolean) {
        viewModelScope.launch {
            repository.setWebRuleBlocked(ruleId, !currentBlocked)
        }
    }

    fun deleteWebRule(rule: WebRuleEntity) {
        viewModelScope.launch {
            repository.deleteWebRule(rule)
        }
    }

    // Geofences
    fun addGeofence(name: String, address: String, radius: Int, emoji: String) {
        val childId = _uiState.value.selectedChildId
        viewModelScope.launch {
            repository.addGeofence(childId, name, address, radius, emoji)
        }
    }

    fun deleteGeofence(geofence: GeofenceEntity) {
        viewModelScope.launch {
            repository.deleteGeofence(geofence)
        }
    }

    // Requests
    fun submitChildRequest(type: RequestType, targetName: String, minutes: Int, reason: String) {
        val childId = _uiState.value.selectedChildId
        viewModelScope.launch {
            repository.submitRequest(childId, type, targetName, minutes, reason)
        }
    }

    fun respondToRequest(requestId: Long, approved: Boolean, minutes: Int, notes: String) {
        val childId = _uiState.value.selectedChildId
        viewModelScope.launch {
            repository.respondToRequest(requestId, childId, approved, minutes, notes)
        }
    }

    // Alerts
    fun markAlertRead(alertId: Long) {
        viewModelScope.launch {
            repository.markAlertRead(alertId)
        }
    }

    fun markAllAlertsRead() {
        val childId = _uiState.value.selectedChildId
        viewModelScope.launch {
            repository.markAllAlertsRead(childId)
        }
    }

    // Emergency SOS Trigger
    fun triggerSos() {
        val child = _uiState.value.activeChild ?: return
        viewModelScope.launch {
            repository.triggerEmergencySos(child.id, child.name, child.locationAddress)
            _uiState.update { it.copy(isSosActive = true) }
        }
    }

    fun dismissSos() {
        _uiState.update { it.copy(isSosActive = false) }
    }

    // Device Pairing
    fun generatePairingCode() {
        val child = _uiState.value.activeChild ?: return
        viewModelScope.launch {
            val code = repository.generateNewPairingCode(child.id, child.name, child.deviceModel)
            _uiState.update { it.copy(activePairingCode = code, pairingSuccessMessage = null) }
        }
    }

    fun verifyPairingCode(code: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val matched = repository.verifyAndPairCode(code)
            if (matched != null) {
                _uiState.update {
                    it.copy(pairingSuccessMessage = "Successfully paired device with ${matched.childName}'s ${matched.deviceModel}!")
                }
                onResult(true, "Device paired successfully")
            } else {
                onResult(false, "Invalid or expired pairing code. Please generate a new code on the child device.")
            }
        }
    }

    // Add Child
    fun addNewChild(name: String, age: Int, emoji: String, device: String, limitMins: Int) {
        viewModelScope.launch {
            val newId = repository.addChild(name, age, emoji, device, limitMins)
            selectChild(newId)
        }
    }

    // Export Data
    fun exportChildData() {
        val child = _uiState.value.activeChild ?: return
        val json = repository.generateExportDataJson(child)
        _uiState.update { it.copy(exportedDataJson = json) }
    }

    fun clearExportedData() {
        _uiState.update { it.copy(exportedDataJson = null) }
    }

    // Uninstall Protection Controls
    fun setParentAllowUninstall(childId: String, allow: Boolean) {
        com.example.data.security.UninstallProtectionManager.setUninstallAllowedByParent(childId, allow)
        _uiState.update { it.copy(isUninstallAllowedByParent = allow) }
        viewModelScope.launch {
            repository.insertAlert(
                SafetyAlertEntity(
                    childId = childId,
                    type = AlertType.PERMISSION_CHANGED,
                    title = if (allow) "Uninstall Permission Granted" else "Uninstall Protection Re-Locked",
                    message = if (allow)
                        "Parent allowed app uninstallation on child's phone."
                    else
                        "Parent locked app uninstallation. SafeGuard cannot be removed without parent approval.",
                    timestamp = System.currentTimeMillis(),
                    severity = if (allow) AlertSeverity.WARNING else AlertSeverity.INFO,
                    isRead = false
                )
            )
        }
    }

    fun requestUninstallByChild(childId: String, reason: String) {
        viewModelScope.launch {
            repository.insertAlert(
                SafetyAlertEntity(
                    childId = childId,
                    type = AlertType.PERMISSION_CHANGED,
                    title = "⚠️ App Removal Attempt Detected",
                    message = "Child requested to uninstall SafeGuard Kids: \"$reason\". App remains protected by Guardian Device Administrator until parent approval.",
                    timestamp = System.currentTimeMillis(),
                    severity = AlertSeverity.CRITICAL,
                    isRead = false
                )
            )
            repository.submitRequest(
                childId = childId,
                type = RequestType.PERMISSION_REQUEST,
                targetName = "SafeGuard App Removal",
                minutes = 0,
                reason = reason
            )
            _uiState.update {
                it.copy(uninstallAttemptNotice = "Uninstall request sent to Parent Guardian. SafeGuard Kids will remain locked and protected until parent approval.")
            }
        }
    }

    fun dismissUninstallNotice() {
        _uiState.update { it.copy(uninstallAttemptNotice = null) }
    }
}
