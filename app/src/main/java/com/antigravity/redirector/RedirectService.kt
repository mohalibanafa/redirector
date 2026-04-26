package com.antigravity.redirector

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.Executors

class RedirectService : Service() {

    private var server: HttpServer? = null
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
        if (server == null) {
            try {
                server = HttpServer.create(InetSocketAddress(PORT), 0)
                server?.createContext("/", RedirectHandler())
                server?.executor = Executors.newCachedThreadPool()
                server?.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private class RedirectHandler : HttpHandler {
        override fun handle(t: HttpExchange) {
            val response = "Redirecting to $targetUrl..."
            t.responseHeaders.set("Location", targetUrl)
            t.sendResponseHeaders(301, response.length.toLong())
            val os = t.responseBody
            os.write(response.toByteArray())
            os.close()
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
        server?.stop(0)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
