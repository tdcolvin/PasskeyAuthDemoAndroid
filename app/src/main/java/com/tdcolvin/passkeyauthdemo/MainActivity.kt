package com.tdcolvin.passkeyauthdemo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import com.tdcolvin.passkeyauthdemo.ui.theme.PasskeyAuthDemoAndroidTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PasskeyAuthDemoAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SignInWithPasskey(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun SignInWithPasskey(modifier: Modifier = Modifier) {

    val localContext = LocalContext.current
    val credentialManager = remember { CredentialManager.create(localContext) }

    val signInScope = rememberCoroutineScope()

    Button(
        modifier = modifier,
        onClick = {
            val getPublicKeyCredentialOption = GetPublicKeyCredentialOption(requestJson = """
                {
                  "challenge": "T1xCsnxM2DNL2KdK5CLa6fMhD7OBqho6syzInk_n-Uo",
                  "allowCredentials": [],
                  "timeout": 1800000,
                  "userVerification": "required",
                  "rpId": "auth.tomcolvin.co.uk"
                }
            """.trimIndent())
            val signInRequest = GetCredentialRequest(listOf(getPublicKeyCredentialOption))

            signInScope.launch {
                val result =
                    credentialManager.getCredential(context = localContext, request = signInRequest)

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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PasskeyAuthDemoAndroidTheme {
        SignInWithPasskey()
    }
}