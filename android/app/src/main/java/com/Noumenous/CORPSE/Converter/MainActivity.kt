package com.Noumenous.CORPSE.Converter

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val Void = Color(0xFF070707)
private val Panel = Color(0xFF121212)
private val Crimson = Color(0xFFFF1122)
private val Steel = Color(0xFFE0E0E0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                background = Void,
                surface = Panel,
                primary = Crimson,
                onPrimary = Color.White,
                onBackground = Steel,
                onSurface = Steel,
            )) {
                ConverterHostScreen(
                    onLaunch = { startActivity(Intent(this, DesktopViewportActivity::class.java)) },
                )
            }
        }
    }
}

@Composable
private fun ConverterHostScreen(onLaunch: () -> Unit) {
    var auditMessage by remember { mutableStateOf("Not checked") }
    val pageSize = remember { NativeSubstrate.pageSizeBytes() }

    Surface(modifier = Modifier.fillMaxSize(), color = Void) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Text(
                    text = "// PROJECT NOUMENOUS",
                    color = Crimson,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge,
                )
                Text("CORPSE Converter", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                Text("Android host foundation • local assets only", color = Color.LightGray)
            }
            item {
                StatusCard("NATIVE PAGE SIZE", if (pageSize > 0) "$pageSize bytes" else "Unavailable")
                Spacer(Modifier.height(8.dp))
                StatusCard("BASELINE MARKER", auditMessage)
            }
            item {
                Text("Workspace", style = MaterialTheme.typography.titleLarge)
                Text(
                    "The embedded desktop frontend is a placeholder until a reviewed static build is added under app/src/main/assets/desktop_repo/.",
                    color = Color.LightGray,
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = {
                        auditMessage = if (NativeSubstrate.isBaselineStateValid()) {
                            "0xC7 marker present (demo only)"
                        } else {
                            "Baseline marker check failed"
                        }
                    }) {
                        Text("CHECK MARKER")
                    }
                    Button(
                        onClick = onLaunch,
                        colors = ButtonDefaults.buttonColors(containerColor = Crimson),
                    ) {
                        Text("OPEN VIEWPORT")
                    }
                }
            }
            item {
                Text(
                    "No daemon, VFS slabs, hardware integrity audit, repository importer, or archive builder is implemented yet. See android/README.md and docs/ARCHITECTURE_REVIEW.md.",
                    color = Color(0xFFFFCC80),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun StatusCard(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(8.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(label, color = Crimson, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelMedium)
        Text(value, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodyLarge)
    }
}
