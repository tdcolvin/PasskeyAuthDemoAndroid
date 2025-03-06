package com.tdcolvin.passkeyauthdemo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tdcolvin.passkeyauthdemo.ui.theme.PasskeyAuthDemoAndroidTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PasskeyAuthDemoAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PasskeyDemoScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun PasskeyDemoScreen(
    modifier: Modifier = Modifier,
    viewModel: PasskeyDemoViewModel = viewModel()
) {
    val localContext = LocalContext.current

    val credentialManager = remember { CredentialManager.create(localContext) }

    val username = remember {
        String((0..5).map { "abcdefghijklmnopqrstuvwxyz0123456789".random() }.toCharArray())
    }
    var signedIn by remember { mutableStateOf(false) }

    if (signedIn) {
        SignedInScreen(
            modifier = modifier,
            username = username,
            onSignOut = { signedIn = false }
        )
    }
    else {
        SignedOutScreen(
            modifier = modifier,
            username = username,
            credentialManager = credentialManager,
            getPasskeyRegisterRequestJson = viewModel::getPasskeyRegisterRequestJson,
            sendRegistrationResponse = viewModel::sendRegistrationResponse,
            getPasskeyAuthenticationRequestJson = viewModel::getPasskeyAuthenticationRequestJson,
            sendAuthenticationResponse = viewModel::sendAuthenticationResponse,
            onSignedIn = { signedIn = true }
        )
    }
}

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
fun SignedInScreen(
    modifier: Modifier = Modifier,
    username: String,
    onSignOut: () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            modifier = Modifier.weight(1f),
            text = "Successfully signed in as $username"
        )

        Button(onClick = onSignOut) {
            Text("Sign out")
        }
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



class PasskeyDemoViewModel: ViewModel() {
    private val okHttpClient = OkHttpClient.Builder().cookieJar(MyCookieJar()).build()

    suspend fun getPasskeyRegisterRequestJson(username: String): String = withContext(Dispatchers.IO) {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("auth.tomcolvin.co.uk")
            .addPathSegment("generate-registration-options")
            .addQueryParameter("username", username)
            .build()
        val request = Request.Builder()
            .url(url)
            .build()

        val response = okHttpClient.newCall(request).execute()
        response.body?.string() ?: throw Exception("No response body")
    }

    suspend fun sendRegistrationResponse(
        registrationResponseJson: String,
    ) = withContext(Dispatchers.IO) {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("auth.tomcolvin.co.uk")
            .addPathSegment("verify-registration")
            .build()
        val request = Request.Builder()
            .url(url)
            .post(registrationResponseJson.toRequestBody("application/json".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (response.code != 200) {
            throw Exception("Registration failed: ${response.body?.string()}")
        }
        response.body?.string() ?: "{}"
    }

    suspend fun getPasskeyAuthenticationRequestJson(username: String): String = withContext(Dispatchers.IO) {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("auth.tomcolvin.co.uk")
            .addPathSegment("generate-authentication-options")
            .addQueryParameter("username", username)
            .build()
        val request = Request.Builder()
            .url(url)
            .build()

        val response = okHttpClient.newCall(request).execute()
        response.body?.string() ?: throw Exception("No response body")
    }

    suspend fun sendAuthenticationResponse(authenticationResponseJson: String): String = withContext(Dispatchers.IO) {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("auth.tomcolvin.co.uk")
            .addPathSegment("verify-authentication")
            .build()
        val request = Request.Builder()
            .url(url)
            .post(authenticationResponseJson.toRequestBody("application/json".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()
        response.body?.string() ?: throw Exception("No response body")
    }
}