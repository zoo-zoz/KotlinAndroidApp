package com.example.testapp

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MqttTestActivity : AppCompatActivity() {
    
    private var mqttService: MqttForegroundService? = null
    private var serviceBound = false
    
    // UI 组件
    private lateinit var rgProtocol: RadioGroup
    private lateinit var etHost: EditText
    private lateinit var etPort: EditText
    private lateinit var etWsPath: EditText
    private lateinit var etClientId: EditText
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etKeepAlive: EditText
    private lateinit var swCleanStart: Switch
    private lateinit var swAutoReconnect: Switch
    
    private lateinit var etWillTopic: EditText
    private lateinit var etWillMessage: EditText
    private lateinit var spWillQos: Spinner
    private lateinit var swWillRetain: Switch
    
    private lateinit var etSubTopic: EditText
    private lateinit var spSubQos: Spinner
    private lateinit var etPubTopic: EditText
    private lateinit var etPubMessage: EditText
    private lateinit var spPubQos: Spinner
    private lateinit var swPubRetain: Switch
    
    private lateinit var btnConnect: Button
    private lateinit var btnDisconnect: Button
    private lateinit var btnSubscribe: Button
    private lateinit var btnPublish: Button
    private lateinit var btnClearLog: Button
    
    private lateinit var tvStatus: TextView
    private lateinit var tvLog: TextView
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MqttForegroundService.LocalBinder
            mqttService = binder.getService()
            serviceBound = true
            
            mqttService?.setConnectionListener(object : MqttForegroundService.ConnectionListener {
                override fun onConnected() {
                    runOnUiThread {
                        tvStatus.text = "状态: 已连接"
                        tvStatus.setTextColor(ContextCompat.getColor(this@MqttTestActivity, android.R.color.holo_green_dark))
                        btnConnect.isEnabled = false
                        btnDisconnect.isEnabled = true
                        btnSubscribe.isEnabled = true
                        btnPublish.isEnabled = true
                        appendLog("✓ 连接成功")
                    }
                }
                
                override fun onDisconnected(reason: String) {
                    runOnUiThread {
                        tvStatus.text = "状态: 已断开"
                        tvStatus.setTextColor(ContextCompat.getColor(this@MqttTestActivity, android.R.color.holo_red_dark))
                        btnConnect.isEnabled = true
                        btnDisconnect.isEnabled = false
                        btnSubscribe.isEnabled = false
                        btnPublish.isEnabled = false
                        appendLog("✗ 连接断开: $reason")
                    }
                }
                
                override fun onMessageReceived(topic: String, message: String) {
                    runOnUiThread {
                        appendLog("← 收到消息 [$topic]: $message")
                    }
                }
                
                override fun onError(error: String) {
                    runOnUiThread {
                        appendLog("✗ 错误: $error")
                        Toast.makeText(this@MqttTestActivity, error, Toast.LENGTH_SHORT).show()
                    }
                }
            })
            
            updateConnectionStatus()
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            mqttService = null
            serviceBound = false
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mqtt_test)
        
        // 启用返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        initViews()
        setupListeners()
        
        // 绑定服务
        val intent = Intent(this, MqttForegroundService::class.java)
        startService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    private fun initViews() {
        rgProtocol = findViewById(R.id.rgProtocol)
        etHost = findViewById(R.id.etHost)
        etPort = findViewById(R.id.etPort)
        etWsPath = findViewById(R.id.etWsPath)
        etClientId = findViewById(R.id.etClientId)
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        etKeepAlive = findViewById(R.id.etKeepAlive)
        swCleanStart = findViewById(R.id.swCleanStart)
        swAutoReconnect = findViewById(R.id.swAutoReconnect)
        
        etWillTopic = findViewById(R.id.etWillTopic)
        etWillMessage = findViewById(R.id.etWillMessage)
        spWillQos = findViewById(R.id.spWillQos)
        swWillRetain = findViewById(R.id.swWillRetain)
        
        etSubTopic = findViewById(R.id.etSubTopic)
        spSubQos = findViewById(R.id.spSubQos)
        etPubTopic = findViewById(R.id.etPubTopic)
        etPubMessage = findViewById(R.id.etPubMessage)
        spPubQos = findViewById(R.id.spPubQos)
        swPubRetain = findViewById(R.id.swPubRetain)
        
        btnConnect = findViewById(R.id.btnConnect)
        btnDisconnect = findViewById(R.id.btnDisconnect)
        btnSubscribe = findViewById(R.id.btnSubscribe)
        btnPublish = findViewById(R.id.btnPublish)
        btnClearLog = findViewById(R.id.btnClearLog)
        
        tvStatus = findViewById(R.id.tvStatus)
        tvLog = findViewById(R.id.tvLog)
        
        // 设置默认值
        etHost.setText("192.168.5.238")
        etPort.setText("1883")
        etWsPath.setText("/mqtt")
        etClientId.setText("AndroidClient_${System.currentTimeMillis()}")
        etKeepAlive.setText("60")
        swCleanStart.isChecked = true
        swAutoReconnect.isChecked = true
        
        etSubTopic.setText("test/topic")
        etPubTopic.setText("test/topic")
        etPubMessage.setText("Hello MQTT")
        
        // 设置 QoS Spinner
        val qosAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayOf("QoS 0", "QoS 1", "QoS 2"))
        qosAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spWillQos.adapter = qosAdapter
        spSubQos.adapter = qosAdapter
        spPubQos.adapter = qosAdapter
        
        btnDisconnect.isEnabled = false
        btnSubscribe.isEnabled = false
        btnPublish.isEnabled = false
        
        // WebSocket 路径初始可见性
        updateWsPathVisibility()
    }
    
    private fun setupListeners() {
        rgProtocol.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbTcp -> {
                    etPort.setText("1883")
                    updateWsPathVisibility()
                }
                R.id.rbWebSocket -> {
                    etPort.setText("8083")
                    updateWsPathVisibility()
                }
            }
        }
        
        btnConnect.setOnClickListener { connectMqtt() }
        btnDisconnect.setOnClickListener { disconnectMqtt() }
        btnSubscribe.setOnClickListener { subscribeTopic() }
        btnPublish.setOnClickListener { publishMessage() }
        btnClearLog.setOnClickListener { 
            tvLog.text = ""
            appendLog("日志已清空")
        }
    }
    
    private fun updateWsPathVisibility() {
        val isWebSocket = rgProtocol.checkedRadioButtonId == R.id.rbWebSocket
        etWsPath.visibility = if (isWebSocket) android.view.View.VISIBLE else android.view.View.GONE
    }
    
    private fun connectMqtt() {
        val host = etHost.text.toString().trim()
        val port = etPort.text.toString().toIntOrNull() ?: 1883
        val useWebSocket = rgProtocol.checkedRadioButtonId == R.id.rbWebSocket
        val wsPath = if (useWebSocket) etWsPath.text.toString().trim() else "/mqtt"
        val clientId = etClientId.text.toString().trim()
        val username = etUsername.text.toString().trim().takeIf { it.isNotEmpty() }
        val password = etPassword.text.toString().trim().takeIf { it.isNotEmpty() }
        val keepAlive = etKeepAlive.text.toString().toIntOrNull() ?: 60
        val cleanStart = swCleanStart.isChecked
        val autoReconnect = swAutoReconnect.isChecked
        
        val willTopic = etWillTopic.text.toString().trim().takeIf { it.isNotEmpty() }
        val willMessage = etWillMessage.text.toString().trim().takeIf { it.isNotEmpty() }
        val willQos = spWillQos.selectedItemPosition
        val willRetain = swWillRetain.isChecked
        
        if (host.isEmpty() || clientId.isEmpty()) {
            Toast.makeText(this, "请填写主机地址和客户端ID", Toast.LENGTH_SHORT).show()
            return
        }
        
        appendLog("→ 正在连接...")
        appendLog("  协议: ${if (useWebSocket) "WebSocket" else "TCP"}")
        appendLog("  地址: $host:$port")
        if (useWebSocket) {
            appendLog("  路径: $wsPath")
        }
        appendLog("  客户端ID: $clientId")
        appendLog("  Keep Alive: ${keepAlive}s")
        appendLog("  Clean Start: $cleanStart")
        appendLog("  自动重连: $autoReconnect")
        
        mqttService?.connect(
            host, port, useWebSocket, clientId, username, password,
            keepAlive, cleanStart, autoReconnect,
            willTopic, willMessage, willQos, willRetain,
            wsPath
        )
    }
    
    private fun disconnectMqtt() {
        mqttService?.disconnect()
        appendLog("→ 断开连接...")
    }
    
    private fun subscribeTopic() {
        val topic = etSubTopic.text.toString().trim()
        val qos = spSubQos.selectedItemPosition
        
        if (topic.isEmpty()) {
            Toast.makeText(this, "请输入订阅主题", Toast.LENGTH_SHORT).show()
            return
        }
        
        mqttService?.subscribe(topic, qos)
        appendLog("→ 订阅主题: $topic (QoS $qos)")
    }
    
    private fun publishMessage() {
        val topic = etPubTopic.text.toString().trim()
        val message = etPubMessage.text.toString()
        val qos = spPubQos.selectedItemPosition
        val retain = swPubRetain.isChecked
        
        if (topic.isEmpty()) {
            Toast.makeText(this, "请输入发布主题", Toast.LENGTH_SHORT).show()
            return
        }
        
        mqttService?.publish(topic, message, qos, retain)
        appendLog("→ 发布消息 [$topic]: $message (QoS $qos, Retain: $retain)")
    }
    
    private fun updateConnectionStatus() {
        val isConnected = mqttService?.isConnected() ?: false
        if (isConnected) {
            tvStatus.text = "状态: 已连接"
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
            btnConnect.isEnabled = false
            btnDisconnect.isEnabled = true
            btnSubscribe.isEnabled = true
            btnPublish.isEnabled = true
        } else {
            tvStatus.text = "状态: 未连接"
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            btnConnect.isEnabled = true
            btnDisconnect.isEnabled = false
            btnSubscribe.isEnabled = false
            btnPublish.isEnabled = false
        }
    }
    
    private fun appendLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        tvLog.append("[$timestamp] $message\n")
        
        // 自动滚动到底部
        val scrollView = findViewById<ScrollView>(R.id.scrollViewLog)
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }
}
