package com.tdcolvin.passkeyauthdemo.ui

import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import com.tdcolvin.passkeyauthdemo.ui.theme.PasskeyAuthDemoAndroidTheme
import kotlinx.coroutines.launch

@Composable
fun SignedOutScreen(
    modifier: Modifier = Modifier,
    username: String,
    credentialManager: CredentialManager?,
    getPasskeyRegisterRequestJson: suspend (String) -> String,
    sendRegistrationResponse: suspend (String) -> String,
    getPasskeyAuthenticationRequestJson: suspend (String) -> String,
    sendAuthenticationResponse: suspend (String) -> String,
    onSignedIn: () -> Unit
) {
    var error by remember { mutableStateOf<Exception?>(null) }
    var isWorking by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Passkey demo app",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium
        )

        Text(text = "Authenticating to auth.tomcolvin.co.uk", textAlign = TextAlign.Center)

        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = "Your randomly generated username: $username",
            textAlign = TextAlign.Center
        )


        SignUpWithPasskey(
            modifier = Modifier.padding(top = 40.dp).fillMaxWidth(),
            enabled = !isWorking,
            username = username,
            credentialManager = credentialManager,
            getPasskeyRegisterRequestJson = getPasskeyRegisterRequestJson,
            sendRegistrationResponse = sendRegistrationResponse,
            onBeginSignUp = {
                error = null
                isWorking = true
            },
            onSignedUp = {
                onSignedIn()
                isWorking = false
            },
            onError = {
                error = it
                isWorking = false
            }
        )
        SignInWithPasskey(
            modifier = Modifier.fillMaxWidth(),
            enabled = !isWorking,
            username = username,
            credentialManager = credentialManager,
            getPasskeyAuthenticationRequestJson = getPasskeyAuthenticationRequestJson,
            sendAuthenticationResponse = sendAuthenticationResponse,
            onBeginSignIn = {
                error = null
                isWorking = true
            },
            onSignedIn = {
                onSignedIn()
                isWorking = false
            },
            onError = {
                error = it
                isWorking = false
            }
        )

        error?.message?.let { errorMessage ->
            Text(modifier = Modifier.padding(top = 40.dp), text = errorMessage)
        }
    }
}

@Composable
fun SignUpWithPasskey(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    username: String,
    credentialManager: CredentialManager?,
    getPasskeyRegisterRequestJson: suspend (String) -> String,
    sendRegistrationResponse: suspend (String) -> String,
    onBeginSignUp: () -> Unit,
    onSignedUp: () -> Unit,
    onError: (Exception) -> Unit
) {
    val localActivity = LocalActivity.current
    val signUpScope = rememberCoroutineScope()

    var isRegistering by remember { mutableStateOf(false) }

    fun doSignUp() {
        localActivity ?: return
        credentialManager ?: return

        signUpScope.launch {
            onBeginSignUp()
            isRegistering = true

            try {
                val registerRequestJson = getPasskeyRegisterRequestJson(username)

                val createPublicKeyCredentialRequest = CreatePublicKeyCredentialRequest(
                    requestJson = registerRequestJson,
                    preferImmediatelyAvailableCredentials = false
                )
                val createCredentialResponse = credentialManager.createCredential(
                    context = localActivity,
                    request = createPublicKeyCredentialRequest
                )

                if (createCredentialResponse !is CreatePublicKeyCredentialResponse) {
                    throw Exception("Incorrect response type")
                }

                sendRegistrationResponse(createCredentialResponse.registrationResponseJson)

                onSignedUp()
            }
            catch (e: Exception) {
                onError(e)
            }
            finally {
                isRegistering = false
            }
        }
    }

    Button(
        modifier = modifier,
        onClick = { if (enabled) doSignUp() }
    ) {
        Text(if (isRegistering) "Registering..." else "Sign up (=register) with Passkey")
    }
}

@Composable
fun SignInWithPasskey(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    username: String,
    credentialManager: CredentialManager?,
    getPasskeyAuthenticationRequestJson: suspend (String) -> String,
    sendAuthenticationResponse: suspend (String) -> String,
    onBeginSignIn: () -> Unit,
    onSignedIn: () -> Unit,
    onError: (Exception) -> Unit
) {
    val localActivity = LocalActivity.current
    val signInScope = rememberCoroutineScope()

    var isAuthenticating by remember { mutableStateOf(false) }

    fun doSignIn() {
        localActivity ?: return
        credentialManager ?: return

        onBeginSignIn()
        isAuthenticating = true

        signInScope.launch {
            try {
                val authenticationRequestJson = getPasskeyAuthenticationRequestJson(username)
                Log.v("passkey", "Authentication request JSON: $authenticationRequestJson")

                val getPublicKeyCredentialOption = GetPublicKeyCredentialOption(
                    requestJson = authenticationRequestJson
                )
                val signInRequest = GetCredentialRequest(listOf(getPublicKeyCredentialOption))

                val result = credentialManager.getCredential(
                    context = localActivity,
                    request = signInRequest
                )

                val credential = result.credential

                if (credential !is PublicKeyCredential) {
                    throw Exception("Incorrect credential type")
                }

                val responseJson = credential.authenticationResponseJson

                sendAuthenticationResponse(responseJson)

                onSignedIn()
            }
            catch (e: Exception) {
                onError(e)
            }
            finally {
                isAuthenticating = false
            }
        }
    }

    Button(
        modifier = modifier,
        enabled = enabled,
        onClick = { if (enabled) doSignIn() }
    ) {
        Text(if (isAuthenticating) "Authenticating..." else "Sign in (=authenticate) with Passkey")
    }
}

@Preview(showBackground = false)
@Composable
fun SignedOutScreen_Preview() {
    PasskeyAuthDemoAndroidTheme {
        Surface {
            SignedOutScreen(
                username = "test",
                credentialManager = null,
                sendRegistrationResponse = { "" },
                sendAuthenticationResponse = { "" },
                getPasskeyRegisterRequestJson = { "" },
                getPasskeyAuthenticationRequestJson = { "" },
                onSignedIn = { }
            )
        }
    }
}