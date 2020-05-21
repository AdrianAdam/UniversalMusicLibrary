package com.example.adrianadam.universalmusiclibrary

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button

class VersionSelector : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_version)

        var btnFreeVersion: Button = findViewById(R.id.btnFreeVersion)
        var btnPaidVersion: Button = findViewById(R.id.btnPaidVersion)

        btnFreeVersion.setOnClickListener({
            var objIntent = Intent(this, MusicControllerFree::class.java)
            startActivity(objIntent)
        })

        btnPaidVersion.setOnClickListener({

        })
    }
}