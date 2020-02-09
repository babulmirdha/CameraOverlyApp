package com.provatsoft.cameraoverlyapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        takeNIDButton.setOnClickListener {
            startActivity(Intent(this,CaptureRectangleActivity::class.java))
        }


        takePhotoButton.setOnClickListener {
            startActivity(Intent(this,CaptureCircleActivity::class.java))
        }
    }
}
