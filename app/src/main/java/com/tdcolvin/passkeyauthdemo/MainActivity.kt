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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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

    val username = remember { String((0..5).map{"abcdefghijklmnopqrstuvwxyz0123456789".random()}.toCharArray()) }

    Column(
        modifier = modifier
    ) {
        Text("Username: $username")
        SignUpWithPasskey(
            credentialManager = credentialManager,
            username = username,
            getPasskeyRegisterRequestJson = viewModel::getPasskeyRegisterRequestJson,
            sendRegistrationResponse = viewModel::sendRegistrationResponse
        )
        SignInWithPasskey(
            credentialManager = credentialManager,
            getPasskeyRegisterRequestJson = viewModel::getPasskeyRegisterRequestJson
        )
    }
}


@Composable
fun SignUpWithPasskey(
    modifier: Modifier = Modifier,
    username: String,
    credentialManager: CredentialManager,
    getPasskeyRegisterRequestJson: suspend (String) -> String,
    sendRegistrationResponse: suspend (String) -> String
) {
    val localActivity = LocalActivity.current
    val signUpScope = rememberCoroutineScope()

    fun doSignUp() {
        localActivity ?: return

        signUpScope.launch {
            val createPublicKeyCredentialRequest = CreatePublicKeyCredentialRequest(
                requestJson = getPasskeyRegisterRequestJson(username),
                preferImmediatelyAvailableCredentials = false
            )
            val createCredentialResponse = credentialManager.createCredential(
                context = localActivity,
                request = createPublicKeyCredentialRequest
            )

            if (createCredentialResponse !is CreatePublicKeyCredentialResponse) {
                return@launch
            }
            sendRegistrationResponse(createCredentialResponse.registrationResponseJson)
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
    credentialManager: CredentialManager,
    getPasskeyRegisterRequestJson: suspend (String) -> String,
) {
    val localActivity = LocalActivity.current
    val signInScope = rememberCoroutineScope()

    Button(
        modifier = modifier,
        onClick = {
            signInScope.launch {
                localActivity ?: return@launch

                val getPublicKeyCredentialOption = GetPublicKeyCredentialOption(requestJson = getPasskeyRegisterRequestJson("tom"))
                val signInRequest = GetCredentialRequest(listOf(getPublicKeyCredentialOption))
                
                val result =
                    credentialManager.getCredential(context = localActivity, request = signInRequest)

                val credential = result.credential

                if (credential !is PublicKeyCredential) {
                    throw Exception("Incorrect credential type")
                }

                val responseJson = credential.authenticationResponseJson
                    // Share responseJson i.e. a GetCredentialResponse on your server to
                    // validate and  authenticate
                Log.v("passkey", responseJson)
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
        response.body?.string() ?: throw Exception("No response body")
    }
}