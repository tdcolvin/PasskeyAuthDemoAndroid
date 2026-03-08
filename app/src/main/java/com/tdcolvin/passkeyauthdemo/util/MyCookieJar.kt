package com.tdcolvin.passkeyauthdemo.util

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class MyCookieJar : CookieJar {
    private val cookieStore = mutableMapOf<String, List<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val currentCookies = cookieStore[url.host]?.toMutableList() ?: mutableListOf()

        cookies.forEach { newCookie ->
            // Remove any old cookie with the same name, as they are not otherwise overwritten
            currentCookies.removeAll { it.name == newCookie.name }
            currentCookies.add(newCookie)
        }

        cookieStore[url.host] = currentCookies
    }

    override fun loadForRequest(url: HttpUrl) = cookieStore[url.host] ?: emptyList()
}