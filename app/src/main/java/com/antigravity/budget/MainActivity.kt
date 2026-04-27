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

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream
import java.io.OutputStream

// ... (existing Transaction and Activity setup remains similar but with new logic)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetMainScreen() {
    val context = LocalContext.current
    var category by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var transactions = remember { mutableStateListOf<Transaction>() }
    
    // مشغل اختيار ملف للاستيراد (Excel)
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let { importExcel(context, it, transactions) }
        }
    )

    // مشغل حفظ ملف للتصدير (Excel)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        onResult = { uri ->
            uri?.let { exportExcel(context, it, transactions) }
        }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("مدير الميزانية (Excel)", fontWeight = FontWeight.Bold) },
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
                                    val t = Transaction(category, "SAR", amount.toDouble(), "الدخل", note, getCurrentDateTime())
                                    transactions.add(t)
                                    category = ""; amount = ""; note = ""
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
                                    val t = Transaction(category, "SAR", -(amount.toDouble()), "النفقات", note, getCurrentDateTime())
                                    transactions.add(t)
                                    category = ""; amount = ""; note = ""
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
                    onClick = { importLauncher.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FileUpload, null)
                    Spacer(Modifier.width(8.dp))
                    Text("استيراد Excel")
                }

                Button(
                    onClick = { 
                        if (transactions.isEmpty()) {
                            Toast.makeText(context, "لا توجد بيانات لتصديرها", Toast.LENGTH_SHORT).show()
                        } else {
                            exportLauncher.launch("budget_report_${System.currentTimeMillis()}.xlsx")
                        }
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64FFDA), contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.FileDownload, null)
                    Spacer(Modifier.width(8.dp))
                    Text("تصدير Excel")
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

fun validateInput(cat: String, amt: String) = cat.isNotBlank() && amt.isNotBlank() && amt.toDoubleOrNull() != null
fun getCurrentDateTime(): String = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date())

fun importExcel(context: Context, uri: Uri, transactions: MutableList<Transaction>) {
    try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)
            var count = 0
            
            // تخطي الهيدر (Row 0)
            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i) ?: continue
                val category = row.getCell(0)?.toString() ?: ""
                val currency = row.getCell(1)?.toString() ?: "SAR"
                val amount = row.getCell(2)?.numericCellValue ?: 0.0
                val type = row.getCell(3)?.toString() ?: ""
                val note = row.getCell(4)?.toString() ?: ""
                val date = row.getCell(5)?.toString() ?: ""
                
                transactions.add(Transaction(category, currency, amount, type, note, date))
                count++
            }
            workbook.close()
            Toast.makeText(context, "تم استيراد $count معاملة بنجاح", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "خطأ في استيراد Excel: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

fun exportExcel(context: Context, uri: Uri, transactions: List<Transaction>) {
    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Transactions")
            
            // الهيدر
            val headerRow = sheet.createRow(0)
            val headers = listOf("category", "currency", "amount", "type", "note", "date")
            headers.forEachIndexed { index, title -> headerRow.createCell(index).setCellValue(title) }
            
            // البيانات
            transactions.forEachIndexed { index, t ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(t.category)
                row.createCell(1).setCellValue(t.currency)
                row.createCell(2).setCellValue(t.amount)
                row.createCell(3).setCellValue(t.type)
                row.createCell(4).setCellValue(t.note)
                row.createCell(5).setCellValue(t.date)
            }
            
            workbook.write(outputStream)
            workbook.close()
            Toast.makeText(context, "تم تصدير ملف Excel بنجاح", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "خطأ في تصدير Excel: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
