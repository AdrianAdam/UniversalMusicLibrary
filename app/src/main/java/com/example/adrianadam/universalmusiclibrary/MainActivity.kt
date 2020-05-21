package com.example.adrianadam.universalmusiclibrary

import android.app.ProgressDialog.show
import android.content.DialogInterface
import android.content.Intent
import android.hardware.biometrics.BiometricPrompt
import android.hardware.fingerprint.FingerprintManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var btnLogin: Button = findViewById(R.id.btnLogin)
        var btnRegister: Button = findViewById(R.id.btnRegister)
        var etEmailAddress: EditText = findViewById(R.id.etEmailAddress)
        var etPassword: EditText = findViewById(R.id.etPassword)

        var mAuth: FirebaseAuth = FirebaseAuth.getInstance()

        if(mAuth.currentUser != null)
        {
            var objIntent = Intent(this, VersionSelector::class.java)
            startActivity(objIntent)
        }

        btnLogin.setOnClickListener({
            mAuth.signInWithEmailAndPassword(etEmailAddress.text.toString(), etPassword.text.toString())
                .addOnCompleteListener(this) { task ->
                    if(task.isSuccessful)
                    {
                        var objIntent = Intent(this, VersionSelector::class.java)
                        startActivity(objIntent)
                    }
                    else
                    {
                        Log.e("Login failed", task.exception.toString())
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
        })

        btnRegister.setOnClickListener({
            mAuth.createUserWithEmailAndPassword(etEmailAddress.text.toString(), etPassword.text.toString())
                .addOnCompleteListener(this) { task ->
                    if(task.isSuccessful)
                    {
                        var objIntent = Intent(this, VersionSelector::class.java)
                        startActivity(objIntent)
                    }
                    else
                    {
                        Log.e("Login failed", task.exception.toString())
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
        })
    }
}
