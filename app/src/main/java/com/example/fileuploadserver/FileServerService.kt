package com.example.fileuploadserver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log

class FileServerService : Service() {

    private var server: FileUploadServer? = null
    private val port = 8080
    private val channelId = "file_server_channel"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val folderUri = intent?.getParcelableExtra<Uri>("folderUri")
        if (folderUri == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            server = FileUploadServer(applicationContext, port, folderUri)
            server?.start()
        } catch (e: Exception) {
            Log.e("FileServerService", "Failed to start server: ${e.message}")
            stopSelf()
            return START_NOT_STICKY
        }

        createNotificationChannel()
        val notification = Notification.Builder(this, channelId)
            .setContentTitle("📡 File Server Running")
            .setContentText("Upload at http://${getLocalIpAddress()}:$port/upload")
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .build()

        startForeground(1, notification)
        return START_STICKY
    }

    override fun onDestroy() {
        server?.stop()
        stopForeground(true)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun getLocalIpAddress(): String {
        val wm = applicationContext.getSystemService(WIFI_SERVICE) as android.net.wifi.WifiManager
        return android.text.format.Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "File Server Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}