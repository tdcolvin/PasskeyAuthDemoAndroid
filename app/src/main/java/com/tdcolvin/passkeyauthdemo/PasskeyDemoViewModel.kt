package com.tdcolvin.passkeyauthdemo

import androidx.lifecycle.ViewModel
import com.tdcolvin.passkeyauthdemo.util.MyCookieJar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

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