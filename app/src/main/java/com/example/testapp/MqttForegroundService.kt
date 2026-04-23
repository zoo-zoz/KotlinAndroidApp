package com.example.testapp

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import java.util.concurrent.CompletableFuture

class MqttForegroundService : Service() {
    
    private val binder = LocalBinder()
    private var mqttClient: Mqtt3AsyncClient? = null
    private var connectionListener: ConnectionListener? = null
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "mqtt_service_channel"
    }
    
    inner class LocalBinder : Binder() {
        fun getService(): MqttForegroundService = this@MqttForegroundService
    }
    
    interface ConnectionListener {
        fun onConnected()
        fun onDisconnected(reason: String)
        fun onMessageReceived(topic: String, message: String)
        fun onError(error: String)
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification("MQTT 服务运行中")
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    
    fun setConnectionListener(listener: ConnectionListener) {
        this.connectionListener = listener
    }
    
    fun connect(
        host: String,
        port: Int,
        useWebSocket: Boolean,
        clientId: String,
        username: String?,
        password: String?,
        keepAlive: Int,
        cleanStart: Boolean,
        autoReconnect: Boolean,
        willTopic: String?,
        willMessage: String?,
        willQos: Int,
        willRetain: Boolean,
        wsPath: String = "/mqtt"
    ): CompletableFuture<Mqtt3ConnAck> {
        
        val builder = MqttClient.builder()
            .identifier(clientId)
            .serverHost(host)
            .serverPort(port)
            .useMqttVersion3()
        
        // WebSocket 配置 - 使用正确的 API
        if (useWebSocket) {
            val wsConfig = com.hivemq.client.mqtt.MqttWebSocketConfig.builder()
                .subprotocol("mqtt")
                .serverPath(wsPath)
                .build()
            builder.webSocketConfig(wsConfig)
        }
        
        // 自动重连配置
        if (autoReconnect) {
            builder.automaticReconnect()
                .initialDelay(1, java.util.concurrent.TimeUnit.SECONDS)
                .maxDelay(30, java.util.concurrent.TimeUnit.SECONDS)
                .applyAutomaticReconnect()
        }
        
        mqttClient = builder.buildAsync()
        
        val connectBuilder = mqttClient!!.connectWith()
            .keepAlive(keepAlive)
            .cleanSession(cleanStart)
        
        // 用户认证
        if (!username.isNullOrEmpty()) {
            connectBuilder.simpleAuth()
                .username(username)
                .password(password?.toByteArray())
                .applySimpleAuth()
        }
        
        // 遗嘱消息
        if (!willTopic.isNullOrEmpty() && !willMessage.isNullOrEmpty()) {
            connectBuilder.willPublish()
                .topic(willTopic)
                .payload(willMessage.toByteArray())
                .qos(com.hivemq.client.mqtt.datatypes.MqttQos.fromCode(willQos)!!)
                .retain(willRetain)
                .applyWillPublish()
        }
        
        return connectBuilder.send().whenComplete { _, throwable ->
            if (throwable == null) {
                updateNotification("MQTT 已连接")
                connectionListener?.onConnected()
            } else {
                val errorMsg = throwable.message ?: throwable.toString()
                android.util.Log.e("MqttService", "连接失败", throwable)
                updateNotification("MQTT 连接失败")
                connectionListener?.onError("连接失败: $errorMsg")
            }
        }
    }
    
    fun subscribe(topic: String, qos: Int) {
        mqttClient?.subscribeWith()
            ?.topicFilter(topic)
            ?.qos(com.hivemq.client.mqtt.datatypes.MqttQos.fromCode(qos)!!)
            ?.callback { publish: Mqtt3Publish ->
                val message = String(publish.payloadAsBytes)
                connectionListener?.onMessageReceived(publish.topic.toString(), message)
            }
            ?.send()
            ?.whenComplete { _, throwable ->
                if (throwable != null) {
                    connectionListener?.onError("订阅失败: ${throwable.message}")
                }
            }
    }
    
    fun publish(topic: String, message: String, qos: Int, retain: Boolean) {
        mqttClient?.publishWith()
            ?.topic(topic)
            ?.payload(message.toByteArray())
            ?.qos(com.hivemq.client.mqtt.datatypes.MqttQos.fromCode(qos)!!)
            ?.retain(retain)
            ?.send()
            ?.whenComplete { _, throwable ->
                if (throwable != null) {
                    connectionListener?.onError("发送失败: ${throwable.message}")
                }
            }
    }
    
    fun disconnect() {
        mqttClient?.disconnect()?.whenComplete { _, _ ->
            updateNotification("MQTT 已断开")
            connectionListener?.onDisconnected("用户断开")
        }
    }
    
    fun isConnected(): Boolean {
        return mqttClient?.state?.isConnected ?: false
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "MQTT 服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "MQTT 连接服务通知"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(content: String): Notification {
        val intent = Intent(this, MqttTestActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MQTT 服务")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .build()
    }
    
    private fun updateNotification(content: String) {
        val notification = createNotification(content)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mqttClient?.disconnect()
    }
}
