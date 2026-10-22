package com.masroufi.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val bg = Color(0xFF101318)
private val panel = Color(0xFF1C222B)
private val green = Color(0xFF64E6A5)

data class Expense(
    val id: Long,
    val amount: Double,
    val category: String,
    val note: String,
    val date: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Masroufi(applicationContext) }
    }
}

@Composable
fun Masroufi(context: Context) {
    val prefs = remember {
        context.getSharedPreferences("masroufi", Context.MODE_PRIVATE)
    }
    val expenses = remember { mutableStateListOf<Expense>() }
    var budget by remember {
        mutableDoubleStateOf(prefs.getFloat("budget", 4000f).toDouble())
    }
    var addDialog by remember { mutableStateOf(false) }
    var budgetDialog by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }

    val today = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
    val month = today.take(7)
    val monthly = expenses.filter { it.date.startsWith(month) }
    val spent = monthly.sumOf { it.amount }

    fun save() {
        val arr = JSONArray()
        expenses.forEach { e ->
            arr.put(JSONObject().apply {
                put("id", e.id)
                put("amount", e.amount)
                put("category", e.category)
                put("note", e.note)
                put("date", e.date)
            })
        }
        prefs.edit().putString("expenses", arr.toString()).apply()
    }

    LaunchedEffect(Unit) {
        val arr = JSONArray(prefs.getString("expenses", "[]"))
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            expenses.add(Expense(
                o.optLong("id"),
                o.optDouble("amount"),
                o.optString("category", "Ø£Ø®Ø±Ù‰"),
                o.optString("note", ""),
                o.optString("date", today)
            ))
        }
        initialized = true
    }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = green, background = bg, surface = panel,
        onPrimary = bg, onBackground = Color.White, onSurface = Color.White
    )) {
        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl
        ) {
            Scaffold(
                containerColor = bg,
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { addDialog = true },
                        containerColor = green
                    ) { Icon(Icons.Default.Add, "Ø¥Ø¶Ø§ÙØ© Ù…ØµØ±ÙˆÙ") }
                }
            ) { padding ->
                LazyColumn(
                    Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text("Ù…ØµØ±ÙˆÙÙŠ", style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold)
                        Text("ØªØ³ÙŠÙŠØ± Ø§Ù„Ù…ØµØ±ÙˆÙ Ø§Ù„Ø´Ù‡Ø±ÙŠ Ø¨Ù„Ø§ Ø¥Ù†ØªØ±Ù†Øª",
                            color = Color.LightGray)
                    }
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = panel),
                            shape = RoundedCornerShape(20.dp)) {
                            Column(Modifier.fillMaxWidth().padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Ø§Ù„Ù…ÙŠØ²Ø§Ù†ÙŠØ© Ø§Ù„Ø´Ù‡Ø±ÙŠØ©")
                                Text(money(budget),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold)
                                LinearProgressIndicator(
                                    progress = {
                                        if (budget > 0)
                                            (spent / budget).toFloat().coerceIn(0f, 1f)
                                        else 0f
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = green
                                )
                                Text("Ø§Ù„Ù…ØµØ§Ø±ÙŠÙ: ${money(spent)}")
                                Text("Ø§Ù„Ø¨Ø§Ù‚ÙŠ: ${money(budget - spent)}",
                                    color = if (budget < spent) Color.Red else green)
                                OutlinedButton(onClick = { budgetDialog = true }) {
                                    Text("ØªØºÙŠÙŠØ± Ø§Ù„Ù…ÙŠØ²Ø§Ù†ÙŠØ©")
                                }
                            }
                        }
                    }
                    item {
                        Text("Ù…ØµØ§Ø±ÙŠÙ Ù‡Ø°Ø§ Ø§Ù„Ø´Ù‡Ø±",
                            style = MaterialTheme.typography.titleLarge)
                    }
                    if (monthly.isEmpty()) {
                        item { Text("Ù…Ø§Ø²Ø§Ù„ Ù…Ø§ Ø³Ø¬Ù„ØªÙŠ Ø­ØªÙ‰ Ù…ØµØ±ÙˆÙ.") }
                    }
                    items(monthly.sortedByDescending { it.id }, key = { it.id }) { e ->
                        Card(colors = CardDefaults.cardColors(containerColor = panel)) {
                            Row(Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text(e.category, fontWeight = FontWeight.Bold)
                                    Text(e.note, color = Color.LightGray)
                                    Text(e.date, color = Color.Gray)
                                }
                                Text("-${money(e.amount)}", color = Color(0xFFFFA08F))
                                IconButton(onClick = {
                                    expenses.removeAll { it.id == e.id }
                                    save()
                                }) { Icon(Icons.Default.Delete, "Ø­Ø°Ù") }
                            }
                        }
                    }
                }
            }
        }

        if (addDialog) {
            var amount by remember { mutableStateOf("") }
            var category by remember { mutableStateOf("Ø§Ù„Ù…Ø§ÙƒÙ„Ø©") }
            var note by remember { mutableStateOf("") }
            val categories = listOf("Ø§Ù„Ù…Ø§ÙƒÙ„Ø©", "Ø§Ù„Ù†Ù‚Ù„", "Ø§Ù„ÙƒØ±Ø§Ø¡",
                "Ø§Ù„ÙÙˆØ§ØªÙŠØ±", "Ø§Ù„ØªØ³ÙˆÙ‚", "Ø§Ù„ØµØ­Ø©", "Ø§Ù„ØªØ±ÙÙŠÙ‡", "Ø£Ø®Ø±Ù‰")
            AlertDialog(
                onDismissRequest = { addDialog = false },
                title = { Text("Ø¥Ø¶Ø§ÙØ© Ù…ØµØ±ÙˆÙ") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(amount, { amount = it },
                            label = { Text("Ø§Ù„Ù…Ø¨Ù„Øº Ø¨Ø§Ù„Ø¯Ø±Ù‡Ù…") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal))
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(onClick = { expanded = true }) {
                                Text(category)
                            }
                            DropdownMenu(expanded, { expanded = false }) {
                                categories.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c) },
                                        onClick = { category = c; expanded = false })
                                }
                            }
                        }
                        OutlinedTextField(note, { note = it },
                            label = { Text("Ù…Ù„Ø§Ø­Ø¸Ø© Ø§Ø®ØªÙŠØ§Ø±ÙŠØ©") })
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val value = amount.replace(',', '.').toDoubleOrNull()
                        if (value != null && value > 0) {
                            expenses.add(Expense(System.currentTimeMillis(),
                                value, category, note, today))
                            if (initialized) save()
                            addDialog = false
                        }
                    }) { Text("Ø­ÙØ¸") }
                },
                dismissButton = {
                    TextButton(onClick = { addDialog = false }) {
                        Text("Ø¥Ù„ØºØ§Ø¡")
                    }
                }
            )
        }

        if (budgetDialog) {
            var value by remember { mutableStateOf(budget.toString()) }
            AlertDialog(
                onDismissRequest = { budgetDialog = false },
                title = { Text("Ø§Ù„Ù…ÙŠØ²Ø§Ù†ÙŠØ© Ø§Ù„Ø´Ù‡Ø±ÙŠØ©") },
                text = {
                    OutlinedTextField(value, { value = it },
                        label = { Text("Ø§Ù„Ù…Ø¨Ù„Øº Ø¨Ø§Ù„Ø¯Ø±Ù‡Ù…") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal))
                },
                confirmButton = {
                    TextButton(onClick = {
                        value.replace(',', '.').toDoubleOrNull()
                            ?.takeIf { it >= 0 }?.let {
                                budget = it
                                prefs.edit().putFloat("budget", it.toFloat()).apply()
                                budgetDialog = false
                            }
                    }) { Text("Ø­ÙØ¸") }
                },
                dismissButton = {
                    TextButton(onClick = { budgetDialog = false }) {
                        Text("Ø¥Ù„ØºØ§Ø¡")
                    }
                }
            )
        }
    }
}

fun money(value: Double): String =
    String.format(Locale.US, "%,.2f DH", value)