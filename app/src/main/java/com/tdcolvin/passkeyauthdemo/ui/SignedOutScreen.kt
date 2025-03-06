package com.tdcolvin.passkeyauthdemo.ui

import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import kotlinx.coroutines.launch

@Composable
fun SignedOutScreen(
    modifier: Modifier = Modifier,
    username: String,
    credentialManager: CredentialManager,
    getPasskeyRegisterRequestJson: suspend (String) -> String,
    sendRegistrationResponse: suspend (String) -> String,
    getPasskeyAuthenticationRequestJson: suspend (String) -> String,
    sendAuthenticationResponse: suspend (String) -> String,
    onSignedIn: () -> Unit
) {
    var error by remember { mutableStateOf<Exception?>(null) }
    Column(
        modifier = modifier
    ) {
        Text("Username: $username")
        SignUpWithPasskey(
            username = username,
            credentialManager = credentialManager,
            getPasskeyRegisterRequestJson = getPasskeyRegisterRequestJson,
            sendRegistrationResponse = sendRegistrationResponse,
            onSignedUp = onSignedIn,
            onError = { error = it }
        )
        SignInWithPasskey(
            username = username,
            credentialManager = credentialManager,
            getPasskeyAuthenticationRequestJson = getPasskeyAuthenticationRequestJson,
            sendAuthenticationResponse = sendAuthenticationResponse,
            onSignedIn = onSignedIn,
            onError = { error = it }
        )
    }
}




@Composable
fun SignUpWithPasskey(
    modifier: Modifier = Modifier,
    username: String,
    credentialManager: CredentialManager,
    getPasskeyRegisterRequestJson: suspend (String) -> String,
    sendRegistrationResponse: suspend (String) -> String,
    onSignedUp: () -> Unit,
    onError: (Exception) -> Unit
) {
    val localActivity = LocalActivity.current
    val signUpScope = rememberCoroutineScope()

    fun doSignUp() {
        localActivity ?: return

        signUpScope.launch {
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
        }
    }

    Button(
        modifier = modifier,
        onClick = { doSignUp() }
    ) {
        Text("Sign up with Passkey")
    }
}

@Composable
fun SignInWithPasskey(
    modifier: Modifier = Modifier,
    username: String,
    credentialManager: CredentialManager,
    getPasskeyAuthenticationRequestJson: suspend (String) -> String,
    sendAuthenticationResponse: suspend (String) -> String,
    onSignedIn: () -> Unit,
    onError: (Exception) -> Unit
) {
    val localActivity = LocalActivity.current
    val signInScope = rememberCoroutineScope()

    Button(
        modifier = modifier,
        onClick = {
            signInScope.launch {
                localActivity ?: return@launch

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
            }
        }
    ) {
        Text("Sign in with Passkey")
    }
}