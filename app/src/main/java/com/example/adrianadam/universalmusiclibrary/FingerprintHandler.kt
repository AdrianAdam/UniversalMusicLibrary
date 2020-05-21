package com.example.adrianadam.universalmusiclibrary

import android.widget.Toast
import android.content.Intent
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.annotation.TargetApi
import android.content.Context
import android.os.CancellationSignal


@TargetApi(Build.VERSION_CODES.M)
class FingerprintHandler(private val context: Context) : FingerprintManager.AuthenticationCallback() {

    fun startAuth(fingerprintManager: FingerprintManager, cryptoObject: FingerprintManager.CryptoObject) {
        val cancellationSignal = CancellationSignal()
        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, this, null)
    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        this.updateFailed("There was an Auth error $errString", false)
    }

    override fun onAuthenticationFailed() {
        this.updateFailed("Auth failed", false)
    }

    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence) {
        this.updateFailed("Error $helpString", false)
    }

    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
        this.updateSuccessfull("Auth successful", true)
    }

    private fun updateSuccessfull(s: String, b: Boolean) {
        Toast.makeText(context, s, Toast.LENGTH_LONG).show()

        val intent = Intent(context, MainActivity::class.java)
        context.startActivity(intent)
    }

    private fun updateFailed(s: String, b: Boolean) {
        Toast.makeText(context, s, Toast.LENGTH_LONG).show()
    }
}
