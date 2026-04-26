package com.antigravity.redirector

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class RedirectService : Service() {

    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private val CHANNEL_ID = "RedirectServiceChannel"

    companion object {
        var targetUrl: String = "https://google.com"
        const val PORT = 8080
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val inputUrl = intent?.getStringExtra("TARGET_URL")
        if (inputUrl != null) {
            targetUrl = inputUrl
        }

        startForeground(1, createNotification())
        startServer()

        return START_STICKY
    }

    private fun startServer() {
        if (isRunning) return
        isRunning = true
        thread {
            try {
                serverSocket = ServerSocket(PORT)
                serverSocket?.reuseAddress = true
                while (isRunning) {
                    val socket = serverSocket?.accept()
                    if (socket != null) {
                        thread { handleClient(socket) }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun handleClient(socket: Socket) {
        try {
            val output: OutputStream = socket.getOutputStream()
            // Standard HTTP 301 Redirect Response
            val response = "HTTP/1.1 301 Moved Permanently\r\n" +
                    "Location: $targetUrl\r\n" +
                    "Connection: close\r\n\r\n"
            output.write(response.toByteArray())
            output.flush()
            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Redirect Server Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("خادم إعادة التوجيه يعمل")
            .setContentText("يتم التوجيه الآن إلى: $targetUrl")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }

    override fun onDestroy() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
