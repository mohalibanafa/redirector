package com.antigravity.redirector

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class RedirectService : Service() {

    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private val CHANNEL_ID = "RedirectServiceChannel"

    companion object {
        var targetUrl: String = "127.0.0.1:5000"
        var listenPort: Int = 8080
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val inputUrl = intent?.getStringExtra("TARGET_URL")
        if (inputUrl != null && inputUrl.isNotBlank()) {
            targetUrl = inputUrl
        }
        
        val port = intent?.getIntExtra("LISTEN_PORT", 8080)
        if (port != null && port > 0) {
            listenPort = port
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
                serverSocket = ServerSocket(listenPort)
                serverSocket?.reuseAddress = true
                while (isRunning) {
                    val clientSocket = serverSocket?.accept()
                    if (clientSocket != null) {
                        thread { handleTcpProxy(clientSocket) }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                isRunning = false
            }
        }
    }

    private fun getHostAndPort(url: String): Pair<String, Int> {
        try {
            var cleanUrl = url.trim()
            if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
                cleanUrl = "http://$cleanUrl" // إضافة مؤقتة للتمكن من استخراج المكونات
            }
            val uri = java.net.URI(cleanUrl)
            val host = uri.host ?: "127.0.0.1"
            val port = if (uri.port != -1) uri.port else if (uri.scheme == "https") 443 else 80
            return Pair(host, port)
        } catch (e: Exception) {
            return Pair("127.0.0.1", 80)
        }
    }

    private fun handleTcpProxy(clientSocket: Socket) {
        var targetSocket: Socket? = null
        try {
            val (targetHost, targetPort) = getHostAndPort(targetUrl)
            
            // فتح اتصال مع المنفذ المحلي المراد بثه
            targetSocket = Socket(targetHost, targetPort)

            val clientIn = clientSocket.getInputStream()
            val clientOut = clientSocket.getOutputStream()
            val targetIn = targetSocket.getInputStream()
            val targetOut = targetSocket.getOutputStream()

            // مسار 1: من الهدف إلى العميل (في خيط منفصل)
            thread {
                try {
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (targetIn.read(buffer).also { read = it } != -1) {
                        clientOut.write(buffer, 0, read)
                        clientOut.flush()
                    }
                } catch (e: Exception) {
                } finally {
                    try { clientSocket.close() } catch (e: Exception) {}
                    try { targetSocket.close() } catch (e: Exception) {}
                }
            }

            // مسار 2: من العميل إلى الهدف (في نفس الخيط)
            try {
                val buffer = ByteArray(8192)
                var read: Int
                while (clientIn.read(buffer).also { read = it } != -1) {
                    targetOut.write(buffer, 0, read)
                    targetOut.flush()
                }
            } catch (e: Exception) {
            } finally {
                try { clientSocket.close() } catch (e: Exception) {}
                try { targetSocket?.close() } catch (e: Exception) {}
            }

        } catch (e: Exception) {
            try { clientSocket.close() } catch (ex: Exception) {}
            try { targetSocket?.close() } catch (ex: Exception) {}
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "إعادة البث النشطة",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("يتم بث المنفذ $listenPort")
            .setContentText("يتم توجيه البيانات إلى: $targetUrl")
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
