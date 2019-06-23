package com.example.lib

import org.apache.commons.codec.binary.Base64

/**
 * An example use of Apache Commons Codec [Base64.encodeBase64String] which is not part of version 1.3;
 * therefore, when called on an older Android device, the system jar in the classpath will be used
 * and a [NoSuchMethodException] will occur.
 */
fun encodeBase64String(binaryData: ByteArray?): String? {
    return Base64.encodeBase64String(binaryData)
}