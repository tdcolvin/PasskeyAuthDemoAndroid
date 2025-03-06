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
import androidx.compose.ui.Modifier
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
    Column(
        modifier = modifier
    ) {
        Text("Passkey demo app", style = MaterialTheme.typography.headlineMedium)
        Text("Authenticating to auth.tomcolvin.co.uk")

        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = "Your randomly generated username: $username"
        )


        SignUpWithPasskey(
            modifier = Modifier.padding(top = 40.dp).fillMaxWidth(),
            username = username,
            credentialManager = credentialManager,
            getPasskeyRegisterRequestJson = getPasskeyRegisterRequestJson,
            sendRegistrationResponse = sendRegistrationResponse,
            onSignedUp = onSignedIn,
            onError = { error = it }
        )
        SignInWithPasskey(
            modifier = Modifier.fillMaxWidth(),
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
    credentialManager: CredentialManager?,
    getPasskeyRegisterRequestJson: suspend (String) -> String,
    sendRegistrationResponse: suspend (String) -> String,
    onSignedUp: () -> Unit,
    onError: (Exception) -> Unit
) {
    val localActivity = LocalActivity.current
    val signUpScope = rememberCoroutineScope()

    fun doSignUp() {
        localActivity ?: return
        credentialManager ?: return

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
        Text("Sign up (=register) with Passkey")
    }
}

@Composable
fun SignInWithPasskey(
    modifier: Modifier = Modifier,
    username: String,
    credentialManager: CredentialManager?,
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
                credentialManager ?: return@launch

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
        Text("Sign in (=authenticate) with Passkey")
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