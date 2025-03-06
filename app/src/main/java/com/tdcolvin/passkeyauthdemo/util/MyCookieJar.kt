package com.tdcolvin.passkeyauthdemo.util

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class MyCookieJar : CookieJar {
    private val cookieStore = mutableMapOf<String, List<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val storedCookies = cookieStore.getOrDefault(url.host, emptyList())
        cookieStore[url.host] = storedCookies + cookies
    }

    override fun loadForRequest(url: HttpUrl) = cookieStore[url.host] ?: emptyList()
}