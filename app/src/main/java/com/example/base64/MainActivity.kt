package com.example.base64

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) = super.onCreate(savedInstanceState).also {
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.text).text = base64RoundTrip("Hello World")
    }
}
