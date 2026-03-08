package com.tdcolvin.passkeyauthdemo.ui.signup

import android.app.Activity
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

data class SignUpUiState(
    val getRegistrationOptionsJsonResult: Result<String>? = null,
    val isGettingRegistrationOptionsJson: Boolean = false,

    val createPasskeyResult: Result<String>? = null,
    val isCreatingPasskey: Boolean = false,

    val sendRegistrationResponseToServerResult: Result<Unit>? = null,
    val isSendingRegistrationResponseToServer: Boolean = false
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val credentialManager: CredentialManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState get() = _uiState.asStateFlow()

    fun getPasskeyRegistrationOptionsJson(username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isGettingRegistrationOptionsJson = true) }

            val result = runCatching {
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
                response.body.string()
            }

            _uiState.update { it.copy(
                getRegistrationOptionsJsonResult = result,
                isGettingRegistrationOptionsJson = false
            ) }
        }
    }

    fun createPasskeyFromRegistrationOptions(activity: Activity) {
        _uiState.update { it.copy(isCreatingPasskey = true) }

        viewModelScope.launch(Dispatchers.IO) {
            val registrationResponse = runCatching {
                val registerRequestJson = _uiState.value.getRegistrationOptionsJsonResult?.getOrNull()
                    ?: throw Exception("No registration options available")

                val createPublicKeyCredentialRequest = CreatePublicKeyCredentialRequest(
                    requestJson = registerRequestJson,
                    preferImmediatelyAvailableCredentials = false
                )
                val createCredentialResponse = credentialManager.createCredential(
                    context = activity,
                    request = createPublicKeyCredentialRequest
                )

                if (createCredentialResponse !is CreatePublicKeyCredentialResponse) {
                    throw Exception("Incorrect response type")
                }

                createCredentialResponse.registrationResponseJson
            }

            _uiState.update { it.copy(
                createPasskeyResult = registrationResponse,
                isCreatingPasskey = false
            ) }
        }
    }

    fun sendRegistrationResponseToServer() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSendingRegistrationResponseToServer = true) }

            val result = runCatching {
                val registrationResponseJson = _uiState.value.createPasskeyResult?.getOrNull()
                    ?: throw Exception("No registration response available")

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
                    throw Exception("Registration failed: ${response.body.string()}")
                }
            }

            _uiState.update { it.copy(
                sendRegistrationResponseToServerResult = result,
                isSendingRegistrationResponseToServer = false
            ) }
        }
    }
}