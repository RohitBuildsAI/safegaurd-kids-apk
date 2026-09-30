package com.example.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.data.auth.AuthManager
import com.example.data.model.AppRole
import com.example.ui.theme.*
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: (FirebaseUser) -> Unit,
    onChildPairingSuccess: () -> Unit = {},
    onSkipToDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val credentialManager = remember { CredentialManager.create(context) }

    var selectedRoleTab by remember { mutableStateOf(AppRole.PARENT) }
    var isRegistering by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var pairingCode by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // App Shield Logo
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SafeGuardBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "SafeGuard Kids Logo",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "SafeGuard Kids",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (selectedRoleTab == AppRole.PARENT)
                        (if (isRegistering) "Create Family Guardian Account" else "Sign In to Family Dashboard")
                    else
                        "Connect Child Companion Device",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Role Segmented Tabs (Parent vs Child)
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_role_selector")
            ) {
                SegmentedButton(
                    selected = selectedRoleTab == AppRole.PARENT,
                    onClick = {
                        selectedRoleTab = AppRole.PARENT
                        errorMessage = null
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = { Icon(Icons.Default.SupervisorAccount, contentDescription = null) }
                ) {
                    Text("Parent Guardian", fontWeight = FontWeight.Bold)
                }

                SegmentedButton(
                    selected = selectedRoleTab == AppRole.CHILD,
                    onClick = {
                        selectedRoleTab = AppRole.CHILD
                        errorMessage = null
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = { Icon(Icons.Default.Smartphone, contentDescription = null) }
                ) {
                    Text("Child Companion", fontWeight = FontWeight.Bold)
                }
            }

            if (selectedRoleTab == AppRole.PARENT) {
                // ==============================
                // PARENT SIGN IN & REGISTRATION
                // ==============================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Google Sign-In via Credential Manager
                        Button(
                            onClick = {
                                errorMessage = null
                                isLoading = true
                                AuthManager.launchGoogleSignIn(
                                    context = context,
                                    credentialManager = credentialManager,
                                    scope = coroutineScope,
                                    onAuthSuccess = { user ->
                                        isLoading = false
                                        onAuthSuccess(user)
                                    },
                                    onAuthError = { err ->
                                        isLoading = false
                                        errorMessage = err
                                    },
                                    onCancelled = {
                                        isLoading = false
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("button_google_signin"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "G",
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF4285F4),
                                        fontSize = 15.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Sign in with Google",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Divider with text
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f))
                            Text(
                                text = "OR WITH EMAIL",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f))
                        }

                        // Name field (if registering)
                        if (isRegistering) {
                            OutlinedTextField(
                                value = displayName,
                                onValueChange = { displayName = it },
                                label = { Text("Parent / Guardian Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_auth_name")
                            )
                        }

                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_auth_email")
                        )

                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password (min 6 characters)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password visibility"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_auth_password")
                        )

                        // Submit Email Button
                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank()) {
                                    errorMessage = "Please enter both email and password."
                                    return@Button
                                }
                                if (password.length < 6) {
                                    errorMessage = "Password must be at least 6 characters."
                                    return@Button
                                }
                                errorMessage = null
                                isLoading = true
                                coroutineScope.launch {
                                    val result = if (isRegistering) {
                                        AuthManager.registerWithEmail(email, password, displayName)
                                    } else {
                                        AuthManager.signInWithEmail(email, password)
                                    }
                                    isLoading = false
                                    result.fold(
                                        onSuccess = { user -> onAuthSuccess(user) },
                                        onFailure = { err ->
                                            errorMessage = err.localizedMessage ?: "Authentication failed"
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("button_submit_email_auth"),
                            colors = ButtonDefaults.buttonColors(containerColor = SafeGuardBlue),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (isRegistering) "Create Family Account" else "Sign In as Parent",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Switch between Sign In and Register
                        TextButton(
                            onClick = {
                                isRegistering = !isRegistering
                                errorMessage = null
                            },
                            modifier = Modifier.testTag("button_toggle_auth_mode")
                        ) {
                            Text(
                                text = if (isRegistering) "Already have an account? Sign In" else "New to SafeGuard? Create Account",
                                color = SafeGuardCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                // ==============================
                // CHILD COMPANION PAIRING FLOW
                // ==============================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(SafeGuardCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = SafeGuardCyan,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Text(
                            text = "Link Companion Device",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Enter the 6-digit pairing code generated in the Parent App (or use demo code 842915).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        OutlinedTextField(
                            value = pairingCode,
                            onValueChange = { if (it.length <= 6) pairingCode = it },
                            placeholder = { Text("842915") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_child_pairing_code"),
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = TextAlign.Center,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 8.sp
                            )
                        )

                        Button(
                            onClick = {
                                if (pairingCode.length != 6) {
                                    errorMessage = "Please enter a valid 6-digit pairing code."
                                    return@Button
                                }
                                errorMessage = null
                                isLoading = true
                                coroutineScope.launch {
                                    val result = AuthManager.signInChildWithPairingCode(
                                        pairingCode = pairingCode,
                                        childProfileId = "child_leo_01",
                                        familyId = "family_anderson_01"
                                    )
                                    isLoading = false
                                    result.fold(
                                        onSuccess = {
                                            onChildPairingSuccess()
                                        },
                                        onFailure = { err ->
                                            errorMessage = err.localizedMessage ?: "Invalid pairing code"
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("button_connect_child_companion"),
                            colors = ButtonDefaults.buttonColors(containerColor = SafeGuardCyan),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Link, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Connect Companion", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Error Message Banner
            errorMessage?.let { error ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AlertRose.copy(alpha = 0.12f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(AlertRose.copy(alpha = 0.4f))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = AlertRose)
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = AlertRose,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Instant Demo / Guest Bypass for testing/evaluation
            OutlinedButton(
                onClick = onSkipToDemo,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("button_guest_demo"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Explore Preloaded Family Demo (Offline)")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
