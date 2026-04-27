package com.antigravity.budget

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

// موديل البيانات للمعاملة
data class Transaction(
    val category: String,
    val currency: String,
    val amount: Double,
    val type: String, // "النفقات" أو "الدخل"
    val note: String,
    val date: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BudgetAppTheme {
                BudgetMainScreen()
            }
        }
    }
}

@Composable
fun BudgetAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF64FFDA),
            secondary = Color(0xFF00BFA5),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E)
        ),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetMainScreen() {
    val context = LocalContext.current
    var category by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var transactions = remember { mutableStateListOf<Transaction>() }
    
    // مشغل اختيار ملف للاستيراد
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let { importCsv(context, it, transactions) }
        }
    )

    // مشغل حفظ ملف للتصدير
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri ->
            uri?.let { exportCsv(context, it, transactions) }
        }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("مدير الميزانية الشخصية", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1E1E1E)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF121212), Color(0xFF1A1A1A))
                    )
                )
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // كرت الإدخال
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF252525))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("إضافة معاملة جديدة", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("الفئة (مثلاً: طعام)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) amount = it },
                        label = { Text("المبلغ") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("ملاحظات") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (validateInput(category, amount)) {
                                    val t = Transaction(
                                        category = category,
                                        currency = "SAR",
                                        amount = amount.toDouble(),
                                        type = "الدخل",
                                        note = note,
                                        date = getCurrentDateTime()
                                    )
                                    transactions.add(t)
                                    clearFields { category = ""; amount = ""; note = "" }
                                    Toast.makeText(context, "تمت إضافة الدخل", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, null)
                            Text(" دخل")
                        }

                        Button(
                            onClick = {
                                if (validateInput(category, amount)) {
                                    val t = Transaction(
                                        category = category,
                                        currency = "SAR",
                                        amount = -(amount.toDouble()),
                                        type = "النفقات",
                                        note = note,
                                        date = getCurrentDateTime()
                                    )
                                    transactions.add(t)
                                    clearFields { category = ""; amount = ""; note = "" }
                                    Toast.makeText(context, "تمت إضافة الصرف", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Remove, null)
                            Text(" صرف")
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // أزرار الملفات
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("text/*", "application/octet-stream")) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                ) {
                    Icon(Icons.Default.FileUpload, null)
                    Spacer(Modifier.width(8.dp))
                    Text("استيراد CSV")
                }

                Button(
                    onClick = { 
                        if (transactions.isEmpty()) {
                            Toast.makeText(context, "لا توجد بيانات لتصديرها", Toast.LENGTH_SHORT).show()
                        } else {
                            exportLauncher.launch("report_${System.currentTimeMillis()}.csv")
                        }
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64FFDA), contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.FileDownload, null)
                    Spacer(Modifier.width(8.dp))
                    Text("تصدير CSV")
                }
            }
            
            if (transactions.isNotEmpty()) {
                Text("عدد المعاملات الحالية: ${transactions.size}", color = Color.Gray, fontSize = 14.sp)
                TextButton(onClick = { transactions.clear() }) {
                    Text("مسح القائمة الحالية", color = Color.Red.copy(alpha = 0.7f))
                }
            }
        }
    }
}

fun validateInput(cat: String, amt: String): Boolean {
    return cat.isNotBlank() && amt.isNotBlank() && amt.toDoubleOrNull() != null
}

fun clearFields(clear: () -> Unit) {
    clear()
}

fun getCurrentDateTime(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
    return sdf.format(Date())
}

fun importCsv(context: Context, uri: Uri, transactions: MutableList<Transaction>) {
    try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String? = reader.readLine() // Header
            var count = 0
            while (reader.readLine().also { line = it } != null) {
                // محلل CSV متطور للتعامل مع الفواصل داخل الاقتباسات
                val parts = parseCsvLine(line!!)
                if (parts.size >= 6) {
                    val category = parts[0]
                    val currency = parts[1]
                    val amount = parts[2].toDoubleOrNull() ?: 0.0
                    val type = parts[3]
                    val note = parts[4]
                    val date = parts[5]
                    
                    transactions.add(Transaction(category, currency, amount, type, note, date))
                    count++
                }
            }
            Toast.makeText(context, "تم استيراد $count معاملة", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "خطأ في الاستيراد: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

// دالة مساعدة لتقسيم سطر CSV مع مراعاة الاقتباسات
fun parseCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    var curVal = StringBuilder()
    var inQuotes = false
    for (ch in line.toCharArray()) {
        if (inQuotes) {
            if (ch == '\"') {
                inQuotes = false
            } else {
                curVal.append(ch)
            }
        } else {
            if (ch == '\"') {
                inQuotes = true
            } else if (ch == ',') {
                result.add(curVal.toString().trim())
                curVal = StringBuilder()
            } else {
                curVal.append(ch)
            }
        }
    }
    result.add(curVal.toString().trim())
    return result
}

fun exportCsv(context: Context, uri: Uri, transactions: List<Transaction>) {
    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            val writer = OutputStreamWriter(outputStream)
            writer.write("category,currency,amount,type,note,date\n")
            transactions.forEach { t ->
                // إضافة اقتباسات إذا كانت القيمة تحتوي على فاصلة
                val category = if (t.category.contains(",")) "\"${t.category}\"" else t.category
                val note = if (t.note.contains(",")) "\"${t.note}\"" else t.note
                val type = if (t.type.contains(",")) "\"${t.type}\"" else t.type
                writer.write("$category,${t.currency},${t.amount},$type,$note,${t.date}\n")
            }
            writer.flush()
            writer.close()
            Toast.makeText(context, "تم التصدير بنجاح", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "خطأ في التصدير: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
