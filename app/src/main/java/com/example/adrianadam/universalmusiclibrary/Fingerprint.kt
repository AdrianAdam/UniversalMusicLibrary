package com.example.adrianadam.universalmusiclibrary

import android.Manifest
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.app.KeyguardManager
import android.hardware.fingerprint.FingerprintManager
import android.widget.TextView
import javax.crypto.Cipher
import android.widget.Toast
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.os.Build
import android.security.keystore.KeyProperties
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.security.keystore.KeyGenParameterSpec
import java.security.*
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey


class Fingerprint: AppCompatActivity() {

    lateinit var textView: TextView

    lateinit var fingerprintManager: FingerprintManager
    lateinit var keyguardManager: KeyguardManager

    lateinit var keyStore: KeyStore
    lateinit var cipher: Cipher
    val KEY_NAME = "AndroidKey"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fingerprint)

        var objIntent = Intent(this, MainActivity::class.java)
        startActivity(objIntent)

        textView = findViewById(R.id.textFingerprint)
        textView.text = "You have problems with the fingerprint settings"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintManager = getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
            keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.USE_FINGERPRINT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, "Permission not granted to use fingerprint scanner", Toast.LENGTH_LONG).show()
            } else if (!keyguardManager.isKeyguardSecure) {
                Toast.makeText(this, "Secure your phone with PIN or LOCK", Toast.LENGTH_LONG).show()
            } else if (!fingerprintManager.hasEnrolledFingerprints()) {
                Toast.makeText(this, "You have no saved fingerprints", Toast.LENGTH_LONG).show()
            } else {
                textView.text = "Place your finger on the fingerprint scanner to proceed"

                generateKey()

                if (cipherInit()) {
                    val cryptoObject = FingerprintManager.CryptoObject(cipher)
                    val fingerprintHandler = FingerprintHandler(this)
                    fingerprintHandler.startAuth(fingerprintManager, cryptoObject)
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore")
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

            keyStore.load(null)
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                        KeyProperties.ENCRYPTION_PADDING_PKCS7
                    )
                    .build()
            )
            keyGenerator.generateKey()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun cipherInit(): Boolean {
        try {
            cipher =
                    Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get Cipher", e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("Failed to get Cipher", e)
        }

        try {
            keyStore.load(null)

            val key = keyStore.getKey(KEY_NAME, null) as SecretKey

            cipher.init(Cipher.ENCRYPT_MODE, key)

            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }
}