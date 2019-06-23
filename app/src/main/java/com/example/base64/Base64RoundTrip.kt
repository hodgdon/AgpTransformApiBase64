package com.example.base64

import com.example.lib.encodeBase64String
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.encodeUtf8

/** @return [input] */
fun base64RoundTrip(input: String): String {
    val encoded = encodeBase64String(input.encodeUtf8().toByteArray())
    return encoded!!.decodeBase64()!!.utf8()
}