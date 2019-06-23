package com.example.base64

import org.junit.Assert.*
import org.junit.Test

class Base64RoundTripKtTest {
    @Test
    fun helloWorld() {
        assertEquals("Hello World", base64RoundTrip("Hello World"))
    }
}