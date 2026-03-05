package com.tdcolvin.passkeyauthdemo.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

data class SignUpUiState(
    val getRegistrationOptionsJsonResult: Result<String>? = null,
    val isGettingRegistrationOptionsJson: Boolean = false,
    val getPasskeyJsonResult: Result<String>? = null
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val okHttpClient: OkHttpClient
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
        response.body.string()
    }
}