@file:Suppress("unused")

package com.example.lib

import org.apache.commons.codec.binary.Base64.encodeBase64

fun encodeBase64StringCompat(binaryData: ByteArray?): String? {
    return if(binaryData == null) null else {
        java.lang.String(encodeBase64(binaryData)) as String
    }
}
