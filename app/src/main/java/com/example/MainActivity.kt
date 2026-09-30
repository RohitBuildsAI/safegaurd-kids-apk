package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.auth.AuthManager
import com.example.data.model.AppRole
import com.example.ui.components.ChildBottomNavigationBar
import com.example.ui.components.ParentBottomNavigationBar
import com.example.ui.components.SafeGuardTopAppBar
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.child.ChildCompanionScreen
import com.example.ui.screens.parent.*
import com.example.ui.theme.SafeGuardKidsTheme
import com.example.viewmodel.*
import com.google.firebase.auth.FirebaseUser

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SafeGuardKidsTheme {
                MainRootNavigation()
            }
        }
    }
}

@Composable
fun MainRootNavigation(
    viewModel: SafeGuardViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val currentUser by AuthManager.authStateFlow().collectAsState(initial = AuthManager.currentUser)
    var isGuestDemoMode by remember { mutableStateOf(false) }

    // Attempt silent auto-sign-in on startup
    LaunchedEffect(Unit) {
        AuthManager.attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = { /* Automatically picked up by authStateFlow */ },
            onUnauthenticated = { /* Stays on AuthScreen */ },
            scope = coroutineScope
        )
    }

    if (currentUser == null && !isGuestDemoMode) {
        AuthScreen(
            onAuthSuccess = { /* authStateFlow updates automatically */ },
            onChildPairingSuccess = {
                viewModel.selectRole(AppRole.CHILD)
                isGuestDemoMode = true
            },
            onSkipToDemo = { isGuestDemoMode = true }
        )
    } else {
        SafeGuardApp(
            currentUser = currentUser,
            onSignOut = {
                AuthManager.signOut(context, credentialManager, coroutineScope) {
                    isGuestDemoMode = false
                }
            },
            viewModel = viewModel
        )
    }
}

@Composable
fun SafeGuardApp(
    currentUser: FirebaseUser? = null,
    onSignOut: () -> Unit = {},
    viewModel: SafeGuardViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddChildDialog by remember { mutableStateOf(false) }

    // Sub-screen BackHandler: Pressing back in sub-screens navigates back to Dashboard
    if (state.currentRole == AppRole.PARENT && state.parentDestination != ParentNavDestination.DASHBOARD) {
        BackHandler {
            viewModel.selectParentDestination(ParentNavDestination.DASHBOARD)
        }
    } else if (state.currentRole == AppRole.CHILD && state.childDestination != ChildNavDestination.DASHBOARD) {
        BackHandler {
            viewModel.selectChildDestination(ChildNavDestination.DASHBOARD)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SafeGuardTopAppBar(
                currentRole = state.currentRole,
                children = state.children,
                selectedChildId = state.selectedChildId,
                unreadAlertsCount = state.alerts.count { !it.isRead },
                pendingRequestsCount = state.requests.count { it.status == com.example.data.model.RequestStatus.PENDING },
                onSelectRole = { viewModel.selectRole(it) },
                onSelectChild = { viewModel.selectChild(it) },
                onAddChildClick = { showAddChildDialog = true },
                onPrivacyClick = {
                    if (state.currentRole == AppRole.PARENT) {
                        viewModel.selectParentDestination(ParentNavDestination.PRIVACY)
                    } else {
                        viewModel.selectChildDestination(ChildNavDestination.PRIVACY)
                    }
                }
            )
        },
        bottomBar = {
            if (state.currentRole == AppRole.PARENT) {
                ParentBottomNavigationBar(
                    currentDestination = state.parentDestination,
                    unreadAlertsCount = state.alerts.count { !it.isRead },
                    pendingRequestsCount = state.requests.count { it.status == com.example.data.model.RequestStatus.PENDING },
                    onNavigate = { viewModel.selectParentDestination(it) }
                )
            } else {
                ChildBottomNavigationBar(
                    currentDestination = state.childDestination,
                    onNavigate = { viewModel.selectChildDestination(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.currentRole == AppRole.PARENT) {
                when (state.parentDestination) {
                    ParentNavDestination.DASHBOARD -> {
                        ParentDashboardScreen(
                            state = state,
                            onTogglePause = { viewModel.toggleDevicePause(it) },
                            onToggleBedtime = { viewModel.toggleBedtime(it) },
                            onToggleStudyMode = { viewModel.toggleStudyMode(it) },
                            onToggleScreenViewing = { viewModel.toggleScreenViewing(it) },
                            onNavigate = { viewModel.selectParentDestination(it) },
                            onDismissSos = { viewModel.dismissSos() }
                        )
                    }
                    ParentNavDestination.SCREEN_TIME -> {
                        ScreenTimeManagementScreen(
                            state = state,
                            onSetDailyLimit = { id, mins -> viewModel.setDailyLimit(id, mins) },
                            onTogglePause = { viewModel.toggleDevicePause(it) },
                            onToggleBedtime = { viewModel.toggleBedtime(it) },
                            onToggleStudyMode = { viewModel.toggleStudyMode(it) }
                        )
                    }
                    ParentNavDestination.APPS, ParentNavDestination.WEBSITES -> {
                        AppManagementScreen(
                            state = state,
                            onSetAppBlocked = { id, blocked -> viewModel.setAppBlocked(id, blocked) },
                            onSetAppLimit = { id, mins -> viewModel.setAppLimit(id, mins) },
                            onSetAppAlwaysAllowed = { id, allowed -> viewModel.setAppAlwaysAllowed(id, allowed) },
                            onAddWebRule = { domain, cat, blocked -> viewModel.addWebRule(domain, cat, blocked) },
                            onToggleWebBlocked = { id, cur -> viewModel.toggleWebRuleBlocked(id, cur) },
                            onDeleteWebRule = { viewModel.deleteWebRule(it) },
                            onToggleSafeSearch = { viewModel.toggleSafeSearch() },
                            onToggleStudyBrowsing = { viewModel.toggleStudyBrowsing() }
                        )
                    }
                    ParentNavDestination.LOCATION -> {
                        LocationGeofenceScreen(
                            state = state,
                            onToggleLocationSharing = { viewModel.toggleLocationSharing(it) },
                            onAddGeofence = { name, addr, rad, emoji -> viewModel.addGeofence(name, addr, rad, emoji) },
                            onDeleteGeofence = { viewModel.deleteGeofence(it) }
                        )
                    }
                    ParentNavDestination.ALERTS, ParentNavDestination.REQUESTS -> {
                        SafetyAlertsScreen(
                            state = state,
                            onMarkAlertRead = { viewModel.markAlertRead(it) },
                            onMarkAllAlertsRead = { viewModel.markAllAlertsRead() },
                            onRespondToRequest = { reqId, approved, mins, notes ->
                                viewModel.respondToRequest(reqId, approved, mins, notes)
                            }
                        )
                    }
                    ParentNavDestination.PAIRING -> {
                        DevicePairingScreen(
                            state = state,
                            onGenerateCode = { viewModel.generatePairingCode() },
                            onVerifyCode = { code, cb -> viewModel.verifyPairingCode(code, cb) }
                        )
                    }
                    ParentNavDestination.PRIVACY -> {
                        PrivacyCenterScreen(
                            state = state,
                            currentUser = currentUser,
                            onSignOut = onSignOut,
                            onToggleAllowUninstall = { childId, allow ->
                                viewModel.setParentAllowUninstall(childId, allow)
                            },
                            onExportData = { viewModel.exportChildData() },
                            onClearExportData = { viewModel.clearExportedData() }
                        )
                    }
                }
            } else {
                // CHILD COMPANION EXPERIENCE
                when (state.childDestination) {
                    ChildNavDestination.DASHBOARD -> {
                        ChildCompanionScreen(
                            state = state,
                            onSubmitRequest = { type, target, mins, reason ->
                                viewModel.submitChildRequest(type, target, mins, reason)
                            },
                            onRequestUninstall = { reason ->
                                viewModel.requestUninstallByChild(state.selectedChildId, reason)
                            },
                            onDismissUninstallNotice = {
                                viewModel.dismissUninstallNotice()
                            },
                            onTriggerSos = { viewModel.triggerSos() },
                            onDismissSos = { viewModel.dismissSos() },
                            onNavigate = { viewModel.selectChildDestination(it) }
                        )
                    }
                    ChildNavDestination.REQUESTS -> {
                        SafetyAlertsScreen(
                            state = state,
                            onMarkAlertRead = { viewModel.markAlertRead(it) },
                            onMarkAllAlertsRead = { viewModel.markAllAlertsRead() },
                            onRespondToRequest = { reqId, approved, mins, notes ->
                                viewModel.respondToRequest(reqId, approved, mins, notes)
                            }
                        )
                    }
                    ChildNavDestination.SAFETY -> {
                        LocationGeofenceScreen(
                            state = state,
                            onToggleLocationSharing = { viewModel.toggleLocationSharing(it) },
                            onAddGeofence = { name, addr, rad, emoji -> viewModel.addGeofence(name, addr, rad, emoji) },
                            onDeleteGeofence = { viewModel.deleteGeofence(it) }
                        )
                    }
                    ChildNavDestination.PRIVACY -> {
                        PrivacyCenterScreen(
                            state = state,
                            currentUser = currentUser,
                            onSignOut = onSignOut,
                            onExportData = { viewModel.exportChildData() },
                            onClearExportData = { viewModel.clearExportedData() }
                        )
                    }
                }
            }
        }
    }

    if (showAddChildDialog) {
        AddChildDialog(
            onDismiss = { showAddChildDialog = false },
            onConfirmAdd = { name, age, emoji, device, limit ->
                viewModel.addNewChild(name, age, emoji, device, limit)
            }
        )
    }
}
