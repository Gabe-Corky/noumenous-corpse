package com.Noumenous.CORPSE.Converter

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.platform.LocalConfiguration
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
                ConverterHostScreen(
                    onLaunch = { startActivity(Intent(this, DesktopViewportActivity::class.java)) },
                )
            }
        }
    }
}

@Composable
private fun ConverterHostScreen(onLaunch: () -> Unit) {
    var selectedAction by remember { mutableStateOf("READY") }
    val pageSize = remember { NativeSubstrate.pageSizeBytes() }
    val wideLayout = LocalConfiguration.current.screenWidthDp >= 720

    Surface(modifier = Modifier.fillMaxSize(), color = Void) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            HeaderPanel(wideLayout)
            Spacer(Modifier.height(10.dp))

            if (wideLayout) {
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(0.9f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        RepositoryPanel(
                            modifier = Modifier.weight(1.1f),
                            onAction = { selectedAction = "Repository import is not implemented yet; see the SAF import milestone." },
                        )
                        ArchitecturePanel(modifier = Modifier.weight(1f), pageSize = pageSize)
                    }
                    Column(modifier = Modifier.weight(1.8f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ProgramsPanel(
                            modifier = Modifier.weight(1.15f),
                            onLaunch = onLaunch,
                            onPackage = { selectedAction = "Workspace packaging is not implemented yet." },
                        )
                        MemoryPanel(
                            modifier = Modifier.weight(0.85f),
                            onAction = { selectedAction = "No fixed slab allocation is performed by this starter." },
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item { RepositoryPanel(onAction = { selectedAction = "Repository import is not implemented yet; see the SAF import milestone." }) }
                    item { ProgramsPanel(onLaunch = onLaunch, onPackage = { selectedAction = "Workspace packaging is not implemented yet." }) }
                    item { ArchitecturePanel(pageSize = pageSize) }
                    item { MemoryPanel(onAction = { selectedAction = "No fixed slab allocation is performed by this starter." }) }
                }
            }

            Spacer(Modifier.height(10.dp))
            StatusPanel(selectedAction)
            Spacer(Modifier.height(8.dp))
            ActionBar(onSelect = { selectedAction = it })
        }
    }
}

@Composable
private fun HeaderPanel(wide: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .border(1.dp, Crimson.copy(alpha = 0.75f), CutCornerShape(12.dp))
            .background(Panel, CutCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = if (wide) 12.dp else 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("///", color = Crimson, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
        Spacer(Modifier.width(10.dp))
        Column {
            Text("PROJECT NOUMENOUS", color = Muted, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall)
            Text("CORPSE", color = Steel, fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineSmall, letterSpacing = 5.dp)
            Text("MOBILE DESKTOP WORKSTATION CONVERTER", color = Muted, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.weight(1f))
        Text(if (wide) "DeX / LANDSCAPE" else "ANDROID / PORTRAIT", color = Crimson, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun RepositoryPanel(
    modifier: Modifier = Modifier,
    onAction: () -> Unit,
) {
    SectionPanel("REPOSITORY", modifier) {
        Text("Mount a local static app bundle", color = Muted, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(10.dp))
        OutlineAction("MOUNT LOCAL (.ZIP / DIR)", onAction)
        Spacer(Modifier.height(7.dp))
        OutlineAction("CLONE GIT REPOSITORY", onAction)
        Text("Imports are not enabled in this starter.", color = Muted, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun ProgramsPanel(
    modifier: Modifier = Modifier,
    onLaunch: () -> Unit,
    onPackage: () -> Unit,
) {
    SectionPanel("PROGRAMS", modifier) {
        ProgramCard(
            name = "CORPSE DESKTOP VIEWPORT",
            detail = "Packaged local HTML placeholder",
            status = "READY",
            onLaunch = onLaunch,
            onPackage = onPackage,
        )
        Spacer(Modifier.height(8.dp))
        ProgramCard(
            name = "FUTURE WORKSPACE",
            detail = "No imported workspace mounted",
            status = "STANDBY",
            onLaunch = null,
            onPackage = null,
        )
        Spacer(Modifier.height(8.dp))
        OutlineAction("[+] IMPORT REPOSITORY DIRECTORY", onPackage)
    }
}

@Composable
private fun ProgramCard(
    name: String,
    detail: String,
    status: String,
    onLaunch: (() -> Unit)?,
    onPackage: (() -> Unit)?,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .border(1.dp, Crimson.copy(alpha = if (status == "READY") 0.75f else 0.3f), RoundedCornerShape(8.dp))
            .background(PanelRaised, RoundedCornerShape(8.dp))
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = Steel, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(detail, color = Muted, style = MaterialTheme.typography.bodySmall)
            }
            Text("● $status", color = if (status == "READY") Green else Muted, style = MaterialTheme.typography.labelSmall)
        }
        if (onLaunch != null || onPackage != null) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onLaunch != null) SmallAction("LAUNCH", onLaunch)
                if (onPackage != null) SmallAction("PACKAGE", onPackage)
            }
        }
    }
}

@Composable
private fun ArchitecturePanel(modifier: Modifier = Modifier, pageSize: Int) {
    SectionPanel("ARCHITECTURE", modifier) {
        ArchitectureRow("DESKTOP UI", "Packaged WebView assets")
        ArchitectureRow("LOCAL BRIDGE", "Not started")
        ArchitectureRow("CORPSE SUBSTRATE", "Native library scaffold")
        Spacer(Modifier.height(8.dp))
        Text("OS PAGE SIZE", color = Muted, style = MaterialTheme.typography.labelSmall)
        Text(if (pageSize > 0) "$pageSize bytes" else "Unavailable", color = Green, fontFamily = FontFamily.Monospace)
        Text("No daemon, hardware dispatch, or slab reservation is active.", color = Muted, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun ArchitectureRow(title: String, detail: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("◇", color = Crimson, modifier = Modifier.width(25.dp))
        Column {
            Text(title, color = Steel, style = MaterialTheme.typography.labelMedium)
            Text(detail, color = Muted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun MemoryPanel(modifier: Modifier = Modifier, onAction: () -> Unit) {
    SectionPanel("MEMORY TOPOLOGY", modifier) {
        Text("Planned topology profiles", color = Muted, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(5.dp))
        MemoryTier("TIER 1", "1.25 GB", "12 × 64 MB lanes + 512 MB cache")
        MemoryTier("TIER 2", "2.50 GB", "12 × 64 MB lanes + 1,792 MB cache")
        MemoryTier("TIER 3", "3.50 GB", "12 × 64 MB lanes + 2,816 MB cache")
        Spacer(Modifier.height(5.dp))
        Text("Illustrative targets only; this app does not allocate these footprints.", color = Muted, style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(8.dp))
        OutlineAction("INSPECT SUBSTRATE STATUS", onAction)
    }
}

@Composable
private fun MemoryTier(tier: String, capacity: String, detail: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            .border(1.dp, Color.DarkGray, RoundedCornerShape(5.dp)).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(tier, color = Crimson, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall)
            Text(detail, color = Muted, style = MaterialTheme.typography.labelSmall)
        }
        Text(capacity, color = Steel, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun SectionPanel(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth()
            .border(1.dp, Crimson.copy(alpha = 0.55f), CutCornerShape(10.dp))
            .background(Panel, CutCornerShape(10.dp))
            .padding(12.dp),
        content = {
            Text("/ $title ///", color = Crimson, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            content()
        },
    )
}

@Composable
private fun OutlineAction(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(42.dp),
        shape = CutCornerShape(7.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PanelRaised, contentColor = Steel),
    ) {
        Text(label, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun SmallAction(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = CutCornerShape(5.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Crimson.copy(alpha = 0.88f)),
    ) {
        Text(label, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun StatusPanel(message: String) {
    Text(
        text = "[STATUS] $message",
        modifier = Modifier.fillMaxWidth().background(PanelRaised, RoundedCornerShape(5.dp)).padding(9.dp),
        color = if (message == "READY") Green else Muted,
        fontFamily = FontFamily.Monospace,
        style = MaterialTheme.typography.labelSmall,
    )
}

@Composable
private fun ActionBar(onSelect: (String) -> Unit) {
    val actions = listOf("LOAD REPO", "VERIFY", "LAUNCH", "BACKUP", "MANAGE")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        actions.forEach { action ->
            Button(
                onClick = { onSelect(action) },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = CutCornerShape(7.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Panel, contentColor = Crimson),
            ) {
                Text(action, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
