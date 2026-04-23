package com.example.testapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnMqttTest).setOnClickListener {
            startActivity(Intent(this, MqttTestActivity::class.java))
        }

        findViewById<Button>(R.id.btnChartTest).setOnClickListener {
            startActivity(Intent(this, ChartTestActivity::class.java))
        }
        
        findViewById<Button>(R.id.btnIoTDashboard).setOnClickListener {
            startActivity(Intent(this, IoTDashboardActivity::class.java))
        }
        
        findViewById<Button>(R.id.btnPathAnimation).setOnClickListener {
            startActivity(Intent(this, PathAnimationActivity::class.java))
        }
    }
}
