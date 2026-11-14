package com.example.sessioninspector

import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { InspectorScreen() } }
    }
}

@Composable
private fun InspectorScreen() {
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("Paste a JSON object or JWT that you are authorized to inspect.") }
    var error by remember { mutableStateOf(false) }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Session Inspector", style = MaterialTheme.typography.headlineMedium)
            Text("Local JSON / JWT viewer", style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary)
            Text("This tool analyzes text you paste. It does not connect to Instagram, Swiggy, cloned apps, or extract credentials from other apps.")
            OutlinedTextField(
                value = input, onValueChange = { input = it; error = false },
                modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp),
                label = { Text("JSON or JWT") },
                placeholder = { Text("{ user_id: demo, authenticated: true }") },
                minLines = 6
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = {
                    try { result = inspectInput(input); error = false }
                    catch (_: Exception) {
                        result = "Could not parse input. Check that it is valid JSON or a three-part JWT."
                        error = true
                    }
                }) { Text("Inspect locally") }
                OutlinedButton(onClick = { input = ""; result = "Cleared."; error = false }) { Text("Clear") }
            }
            HorizontalDivider()
            Text("Results", style = MaterialTheme.typography.titleLarge)
            Card {
                Text(result, Modifier.padding(14.dp), fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
            }
            Text("Security: never paste a real password, OTP, access token, session cookie, or live session JSON into an untrusted app. Use dummy data for testing.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

private fun inspectInput(raw: String): String {
    val value = raw.trim()
    require(value.isNotEmpty())
    if (value.count { it == '.' } == 2 && !value.startsWith("{")) {
        val parts = value.split(".")
        val header = JSONObject(decodeJwtPart(parts[0]))
        val payload = JSONObject(decodeJwtPart(parts[1]))
        return buildString {
            appendLine("FORMAT: JWT")
            appendLine()
            appendLine("HEADER")
            appendLine(pretty(header))
            appendLine()
            appendLine("PAYLOAD (sensitive values masked)")
            appendLine(pretty(maskObject(payload)))
            appendLine()
            appendLine("NOTE: Signature is not verified. Decoding a JWT does not prove it is valid.")
        }
    }
    val obj = JSONObject(value)
    return buildString {
        appendLine("FORMAT: JSON")
        appendLine()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val v = obj.opt(key)
            val display = if (isSensitiveKey(key)) maskValue(v?.toString() ?: "") else v.toString()
            appendLine("$key: $display")
        }
        appendLine()
        appendLine("Parsing happened locally on this device.")
    }
}

private fun decodeJwtPart(part: String): String {
    val bytes = Base64.decode(part, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    return String(bytes, StandardCharsets.UTF_8)
}

private fun pretty(obj: JSONObject): String {
    val out = StringBuilder()
    val keys = obj.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        val v = obj.opt(key)
        val display = if (isSensitiveKey(key)) maskValue(v?.toString() ?: "") else v.toString()
        out.append(key).append(": ").append(display).append('\n')
    }
    return out.toString().trimEnd()
}

private fun maskObject(obj: JSONObject): JSONObject {
    val masked = JSONObject()
    val keys = obj.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        val v = obj.opt(key)
        masked.put(key, if (isSensitiveKey(key)) maskValue(v?.toString() ?: "") else v)
    }
    return masked
}

private fun isSensitiveKey(key: String): Boolean {
    val k = key.lowercase()
    return listOf("token", "session", "cookie", "authorization", "secret", "password", "otp", "tid", "sid")
        .any { k.contains(it) }
}

private fun maskValue(value: String): String {
    if (value.length <= 8) return "••••"
    return value.take(4) + "••••••" + value.takeLast(4)
}
