package com.tdcolvin.passkeyauthdemo.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

fun String.prettyPrintJson(): String {
    // 1. Configure the Json instance for pretty printing
    val json = Json { prettyPrint = true }

    // 2. Parse the string into a generic JsonElement
    val jsonElement = json.parseToJsonElement(this)

    // 3. Encode it back to a string with the pretty configuration
    return json.encodeToString(JsonElement.serializer(), jsonElement)
}