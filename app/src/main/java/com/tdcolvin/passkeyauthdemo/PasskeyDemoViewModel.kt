package com.tdcolvin.passkeyauthdemo

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class PasskeyDemoViewModel @Inject constructor(
    private val okHttpClient: OkHttpClient
): ViewModel() {


    suspend fun getPasskeyAuthenticationRequestJson(username: String): String = withContext(
        Dispatchers.IO) {
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

    suspend fun sendAuthenticationResponse(authenticationResponseJson: String): String = withContext(
        Dispatchers.IO) {
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