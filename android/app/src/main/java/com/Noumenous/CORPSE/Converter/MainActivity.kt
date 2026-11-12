package com.Noumenous.CORPSE.Converter

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CutCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val Void = Color(0xFF070707)
private val Panel = Color(0xFF101013)
private val PanelRaised = Color(0xFF18181D)
private val Crimson = Color(0xFFFF1828)
private val Steel = Color(0xFFE0E0E0)
private val Muted = Color(0xFF9999A2)
private val Green = Color(0xFF77E69A)

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
                ContractDiagnosticsHost(
                    onLaunch = { startActivity(Intent(this, DesktopViewportActivity::class.java)) },
                    onVerify = { NativeSubstrate.runContractChecks(this) },
                )
            }
        }
    }
}

@Composable
private fun ContractDiagnosticsHost(onLaunch: () -> Unit, onVerify: () -> NativeContractResult) {
    var result by remember { mutableStateOf<NativeContractResult?>(null) }
    var diagnosticError by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf("READY • DIAGNOSTICS ARE READ-ONLY") }

    Surface(modifier = Modifier.fillMaxSize(), color = Void) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = "PROJECT NOUMENOUS / CORPSE",
                color = Steel,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.titleLarge,
            )
            Text("READ-ONLY CONTRACT DIAGNOSTICS", color = Crimson, fontFamily = FontFamily.Monospace)
            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Panel, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text("OBSERVATION ONLY", color = Crimson, fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Verification reads the configured rules and native page-size/baseline values. It does not write registers, allocate slabs, repair state, or activate hardware.",
                            color = Muted,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Crimson.copy(alpha = 0.5f), CutCornerShape(8.dp))
                            .background(Panel, CutCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            "CONFIGURED TOKEN / REGISTER PROFILE",
                            color = Crimson,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "256 registers • 8-bit token width • 64-bit apex • baseline 0xC7",
                            color = Steel,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            "5-bit fractal / 3-bit node • 12 × 64 MiB topology (configuration only)",
                            color = Muted,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                if (result != null || diagnosticError != null) {
                    item {
                        ReadOnlyDiagnosticsPanel(result = result, error = diagnosticError)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = "[STATUS] $status",
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PanelRaised, RoundedCornerShape(5.dp))
                    .padding(9.dp),
                color = if (status.contains("PASS")) Green else if (status.contains("FAIL") || diagnosticError != null) Crimson else Muted,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.labelSmall,
            )
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                DiagnosticAction("VERIFY", Modifier.weight(1f)) {
                    try {
                        val checked = onVerify()
                        result = checked
                        diagnosticError = null
                        status = checked.summary
                    } catch (error: Exception) {
                        result = null
                        diagnosticError = error.message ?: error.javaClass.simpleName
                        status = "DIAGNOSTICS UNAVAILABLE • NO STATE CHANGED"
                    }
                }
                DiagnosticAction("LAUNCH", Modifier.weight(1f)) {
                    onLaunch()
                    status = "OPENED LOCAL VIEWPORT"
                }
                DiagnosticAction("OTHER ACTIONS", Modifier.weight(1f)) {
                    status = "IMPORT / BACKUP / MANAGE NOT IMPLEMENTED"
                }
            }
        }
    }
}

@Composable
private fun ReadOnlyDiagnosticsPanel(result: NativeContractResult?, error: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Crimson.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
            .background(PanelRaised, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "CONTRACT CHECK RESULTS • READ ONLY",
            color = Crimson,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.titleSmall,
        )
        Spacer(Modifier.height(5.dp))

        if (error != null) {
            Text("UNAVAILABLE: $error", color = Crimson, style = MaterialTheme.typography.bodySmall)
            Text("No diagnostic action was applied.", color = Muted, style = MaterialTheme.typography.labelSmall)
        } else if (result != null) {
            Text(
                text = result.summary,
                color = if (result.passed) Green else Crimson,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.labelSmall,
            )
            Spacer(Modifier.height(6.dp))
            result.checks.forEach { (name, passed) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (passed) "PASS" else "FAIL",
                        color = if (passed) Green else Crimson,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(name, color = Steel, style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(
                "Page size: ${result.pageSizeBytes} bytes • no writes or allocations performed",
                color = Muted,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun DiagnosticAction(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = CutCornerShape(7.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Panel, contentColor = Crimson),
    ) {
        Text(label, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall)
    }
}
