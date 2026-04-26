package com.antigravity.redirector

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.net.NetworkInterface
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF00E5FF),
                    background = Color(0xFF121212)
                )
            ) {
                RedirectorApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedirectorApp() {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    var targetUrl by remember { mutableStateOf("https://google.com") }
    var isRunning by remember { mutableStateOf(false) }
    var localIp by remember { mutableStateOf(getLocalIpAddress()) }
    val redirectLink = "http://$localIp:8080"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A237E), Color(0xFF121212))
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = Color(0xFF00E5FF),
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "مُوجه المواقع Native",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            OutlinedTextField(
                value = targetUrl,
                onValueChange = { targetUrl = it },
                label = { Text("الرابط المستهدف") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedBorderColor = Color.Gray,
                    containerColor = Color.Black.copy(alpha = 0.3f)
                )
            )

            Button(
                onClick = {
                    val intent = Intent(context, RedirectService::class.java).apply {
                        putExtra("TARGET_URL", targetUrl)
                    }
                    if (isRunning) {
                        context.stopService(intent)
                    } else {
                        context.startForegroundService(intent)
                    }
                    isRunning = !isRunning
                    localIp = getLocalIpAddress()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Color.Red.copy(alpha = 0.7f) else Color(0xFF00E5FF),
                    contentColor = Color.Black
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isRunning) "إيقاف التوجيه" else "بدء التوجيه",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isRunning) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("الرابط المباشر للمشاركة:", color = Color.Gray, fontSize = 14.sp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = redirectLink,
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(redirectLink))
                            }) {
                                Icon(Icons.Default.ContentCopy, "نسخ", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getLocalIpAddress(): String {
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val iface = interfaces.nextElement()
            val addresses = iface.inetAddresses
            while (addresses.hasMoreElements()) {
                val addr = addresses.nextElement()
                if (!addr.isLoopbackAddress && addr.address.size == 4) {
                    return addr.hostAddress
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return "127.0.0.1"
}
