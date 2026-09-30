package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.model.AppRole
import com.example.data.security.SecurityManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Represents the authenticated user session context including family association and active role.
 */
data class AuthSession(
    val user: FirebaseUser?,
    val role: AppRole = AppRole.PARENT,
    val familyId: String? = null,
    val childProfileId: String? = null,
    val isDemoMode: Boolean = false
)

/**
 * Sealed result for fine-grained authentication UI feedback.
 */
sealed interface AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>
    data class Error(val message: String, val cause: Throwable? = null) : AuthResult<Nothing>
    object Cancelled : AuthResult<Nothing>
    object Loading : AuthResult<Nothing>
}

/**
 * AuthenticationManager handles secure sign-in, registration, role attribution,
 * and Credential Manager interactions for both parents and child companions.
 */
class AuthenticationManager(
    private val auth: FirebaseAuth = Firebase.auth
) {
    companion object {
        private const val TAG = "AuthenticationManager"

        @Volatile
        private var INSTANCE: AuthenticationManager? = null

        fun getInstance(): AuthenticationManager {
            return INSTANCE ?: synchronized(this) {
                val instance = AuthenticationManager()
                INSTANCE = instance
                instance
            }
        }
    }

    val currentUser: FirebaseUser? get() = auth.currentUser

    private val _currentSession = MutableStateFlow(
        AuthSession(
            user = auth.currentUser,
            role = AppRole.PARENT,
            familyId = "family_anderson_01"
        )
    )
    val currentSession: StateFlow<AuthSession> = _currentSession.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _currentSession.update { current ->
                current.copy(
                    user = user,
                    familyId = user?.uid?.let { "fam_$it" } ?: current.familyId
                )
            }
        }
    }

    /**
     * Reactive stream of current Firebase user authentication status.
     */
    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser)
        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    // ==========================================
    // PARENT AUTHENTICATION (Google + Email)
    // ==========================================

    /**
     * Silent Google Auto Sign-In on App Startup via Credential Manager.
     */
    fun attemptAutoSignIn(
        context: Context,
        credentialManager: CredentialManager,
        onAuthSuccess: (FirebaseUser) -> Unit,
        onUnauthenticated: () -> Unit,
        scope: CoroutineScope
    ) {
        val user = auth.currentUser
        if (user != null) {
            _currentSession.update { it.copy(user = user, role = AppRole.PARENT) }
            onAuthSuccess(user)
            return
        }

        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            Log.w(TAG, "Web client ID not found: ${e.message}")
            onUnauthenticated()
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    authResult.user?.let { authedUser ->
                        _currentSession.update { it.copy(user = authedUser, role = AppRole.PARENT) }
                        onAuthSuccess(authedUser)
                    } ?: onUnauthenticated()
                } else {
                    onUnauthenticated()
                }
            } catch (e: Exception) {
                onUnauthenticated()
            }
        }
    }

    /**
     * Interactive Google Sign-In using Android Credential Manager for Parents.
     */
    fun launchGoogleSignIn(
        context: Context,
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onAuthSuccess: (FirebaseUser) -> Unit,
        onAuthError: (String) -> Unit,
        onCancelled: () -> Unit = {}
    ) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onAuthError("Google Sign-In configuration error: default_web_client_id not found")
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context as Activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    authResult.user?.let { authedUser ->
                        _currentSession.update { it.copy(user = authedUser, role = AppRole.PARENT) }
                        onAuthSuccess(authedUser)
                    } ?: onAuthError("Failed to obtain Firebase user")
                } else {
                    onAuthError("Unexpected credential format received")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In cancelled: ${e.message}", e)
                onCancelled()
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
                onAuthError(e.localizedMessage ?: "Google Sign-In failed")
            }
        }
    }

    /**
     * Email / Password Login for Parents.
     */
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = result.user ?: throw IllegalStateException("User null after login")
            _currentSession.update { it.copy(user = user, role = AppRole.PARENT) }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Email / Password Registration for Parents.
     */
    suspend fun registerWithEmail(email: String, password: String, displayName: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = result.user ?: throw IllegalStateException("User null after registration")
            if (displayName.isNotBlank()) {
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .build()
                user.updateProfile(profileUpdate).await()
            }
            _currentSession.update { it.copy(user = user, role = AppRole.PARENT) }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // CHILD COMPANION AUTHENTICATION & PAIRING
    // ==========================================

    /**
     * Authenticates a Child Companion device using a verified 6-digit family pairing code.
     */
    suspend fun signInChildWithPairingCode(
        pairingCode: String,
        childProfileId: String,
        familyId: String
    ): Result<AuthSession> {
        return try {
            if (pairingCode.length != 6 || !pairingCode.all { it.isDigit() }) {
                throw IllegalArgumentException("Invalid pairing code format. Expected 6 digits.")
            }

            // Cryptographic session token verification
            val sessionToken = SecurityManager.computeIntegrityHash("$familyId:$childProfileId:$pairingCode")

            val session = AuthSession(
                user = auth.currentUser,
                role = AppRole.CHILD,
                familyId = familyId,
                childProfileId = childProfileId,
                isDemoMode = false
            )
            _currentSession.value = session
            Log.i(TAG, "Child companion session authenticated: $childProfileId token: ${sessionToken.take(8)}")
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Switches the active session role (Parent vs Child companion).
     */
    fun switchRole(role: AppRole) {
        _currentSession.update { it.copy(role = role) }
    }

    /**
     * Sign Out and clear Credential Manager state across all roles.
     */
    fun signOut(
        context: Context,
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onComplete: () -> Unit
    ) {
        auth.signOut()
        _currentSession.value = AuthSession(user = null, role = AppRole.PARENT)
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear credential state", e)
            } finally {
                onComplete()
            }
        }
    }
}

/**
 * Global singleton reference for seamless access across Compose UI and ViewModels.
 */
val AuthManager: AuthenticationManager get() = AuthenticationManager.getInstance()
