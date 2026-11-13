package com.Noumenous.CORPSE.Converter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val Void = Color(0xFF050507)
private val Panel = Color(0xFF0D0E12)
private val Crimson = Color(0xFFFF1728)
private val Steel = Color(0xFFD8DEE9)
private val Muted = Color(0xFF89909C)
private val Green = Color(0xFF77E69A)

private enum class Destination(val title: String, val icon: String) {
    HOME("CORPSE", ""),
    REPOSITORY("REPOSITORY", "▤"),
    VERIFY("VERIFY INTEGRITY", "◇"),
    WORKSPACE("LAUNCH WORKSPACE", "▷"),
    BACKUP("BACKUP & PACKAGE", "⟳"),
    SUBSTRATE("MANAGE SUBSTRATE", "⚙"),
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Void,
                    surface = Panel,
                    primary = Crimson,
                    onPrimary = Color.White,
                    onBackground = Steel,
                    onSurface = Steel,
                )
            ) {
                CorpseApp(
                    onPersistFolderPermission = { uri ->
                        try {
                            contentResolver.takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                            )
                        } catch (_: SecurityException) {
                            // Some providers do not allow persistable access.
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun CorpseApp(onPersistFolderPermission: (Uri) -> Unit) {
    val context = LocalContext.current
    var destination by remember { mutableStateOf(Destination.HOME) }
    var selectedFolder by remember { mutableStateOf<Uri?>(null) }
    var status by remember { mutableStateOf<String?>(null) }
    val tierProfile = remember(context) { NativeSubstrate.resourceProfile(context) }
    var selectedTier by remember { mutableIntStateOf(1) }
    var verification by remember { mutableStateOf<NativeContractResult?>(null) }
    var verificationError by remember { mutableStateOf<String?>(null) }
    var benchmarkResult by remember { mutableStateOf<NativeSubstrate.BenchmarkEstimate?>(null) }

    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            onPersistFolderPermission(uri)
            selectedFolder = uri
            status = "FOLDER SELECTED"
        }
    }

    BackHandler(enabled = destination != Destination.HOME) {
        destination = Destination.HOME
        status = null
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Void) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Void)
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            if (destination == Destination.HOME) {
                BrandHeader()
                Spacer(Modifier.height(22.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                    listOf(
                        Destination.REPOSITORY,
                        Destination.VERIFY,
                        Destination.WORKSPACE,
                        Destination.BACKUP,
                        Destination.SUBSTRATE,
                    ).forEach { item ->
                        HomeAction(item, Modifier.weight(1f)) {
                            destination = item
                            status = null
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    "INFINITE POTENTIAL. LOCAL CONTROL. NOUMENOUS.",
                    modifier = Modifier.fillMaxWidth(),
                    color = Muted,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                )
            } else {
                PanelHeader(destination) { destination = Destination.HOME }
                Spacer(Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .border(1.dp, Crimson.copy(alpha = 0.65f), CutCornerShape(18.dp))
                        .background(Panel, CutCornerShape(18.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    when (destination) {
                        Destination.REPOSITORY -> RepositoryPanel(
                            selectedFolder = selectedFolder,
                            onChooseFolder = { folderPicker.launch(null) },
                            onClearFolder = {
                                selectedFolder = null
                                status = "FOLDER CLEARED"
                            },
                        )
                        Destination.VERIFY -> VerifyPanel(
                            tiers = tierProfile,
                            selectedTier = selectedTier,
                            onSelectTier = { selectedTier = it },
                            onVerify = {
                                try {
                                    verification = NativeSubstrate.runContractChecks(context)
                                    verificationError = null
                                    status = "VERIFICATION COMPLETE"
                                } catch (error: Exception) {
                                    verification = null
                                    verificationError = error.message ?: error.javaClass.simpleName
                                    status = "VERIFICATION UNAVAILABLE"
                                }
                            },
                            result = verification,
                            error = verificationError,
                            onBenchmark = {
                                benchmarkResult = NativeSubstrate.estimateBenchmark(
                                    packageName = "PROJECT_PACKAGE",
                                    packageSizeBytes = 96L * 1024 * 1024,
                                    selectedTier = selectedTier,
                                )
                                status = "BENCHMARK RUN"
                            },
                            benchmark = benchmarkResult,
                        )
                        Destination.WORKSPACE -> WorkspacePanel(selectedFolder)
                        Destination.BACKUP -> BackupPanel()
                        Destination.SUBSTRATE -> SubstratePanel(
                            tiers = tierProfile,
                            selectedTier = selectedTier,
                            onSelectTier = { selectedTier = it },
                            onBenchmark = {
                                benchmarkResult = NativeSubstrate.estimateBenchmark(
                                    packageName = "CORE_WORKSPACE",
                                    packageSizeBytes = 64L * 1024 * 1024,
                                    selectedTier = selectedTier,
                                )
                                status = "SUBSTRATE BENCHMARK READY"
                            },
                            benchmark = benchmarkResult,
                        )
                        Destination.HOME -> Unit
                    }
                }
                status?.let {
                    Spacer(Modifier.height(10.dp))
                    StatusIndicator(it)
                }
            }
        }
    }
}

@Composable
private fun BrandHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Crimson.copy(alpha = 0.8f), CutCornerShape(18.dp))
            .background(Panel, CutCornerShape(18.dp))
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "/// PROJECT NOUMENOUS",
            color = Muted,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 3.dp,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "CORPSE",
            color = Steel,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.displaySmall,
            letterSpacing = 7.dp,
        )
        Text(
            "MOBILE DESKTOP WORKSTATION CONVERTER",
            color = Muted,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.dp,
        )
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color.DarkGray)) {
            Spacer(Modifier.fillMaxWidth(0.32f).height(2.dp).background(Crimson))
        }
    }
}

@Composable
private fun HomeAction(destination: Destination, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Crimson.copy(alpha = 0.72f), CutCornerShape(16.dp)),
        shape = CutCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Panel, contentColor = Steel),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                destination.icon,
                color = Steel,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.width(18.dp))
            Text(
                destination.title,
                color = Crimson,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.titleMedium,
                letterSpacing = 1.dp,
            )
            Spacer(Modifier.weight(1f))
            Text("›››", color = Crimson, fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun PanelHeader(destination: Destination, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Crimson.copy(alpha = 0.75f), CutCornerShape(14.dp))
            .background(Panel, CutCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onBack,
            shape = CutCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Steel),
        ) { Text("‹", style = MaterialTheme.typography.headlineSmall) }
        Text(destination.icon, color = Steel, fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.width(12.dp))
        Text(destination.title, color = Crimson, fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun RepositoryPanel(selectedFolder: Uri?, onChooseFolder: () -> Unit, onClearFolder: () -> Unit) {
    Text("WORKSPACE SOURCE", color = Muted, fontFamily = FontFamily.Monospace,
        style = MaterialTheme.typography.labelSmall)
    Text(
        selectedFolder?.lastPathSegment ?: "No folder selected",
        color = if (selectedFolder == null) Steel else Green,
        fontFamily = FontFamily.Monospace,
        style = MaterialTheme.typography.titleMedium,
    )
    ActionButton(if (selectedFolder == null) "SELECT FOLDER" else "CHANGE FOLDER", onChooseFolder)
    if (selectedFolder != null) ActionButton("CLEAR SELECTION", onClearFolder, subdued = true)
}

@Composable
private fun VerifyPanel(
    tiers: List<NativeSubstrate.ResourceTier>,
    selectedTier: Int,
    onSelectTier: (Int) -> Unit,
    onVerify: () -> Unit,
    result: NativeContractResult?,
    error: String?,
    onBenchmark: () -> Unit,
    benchmark: NativeSubstrate.BenchmarkEstimate?,
) {
    Text("READ-ONLY CHECK", color = Muted, fontFamily = FontFamily.Monospace,
        style = MaterialTheme.typography.labelSmall)
    Text("Current substrate usage and validation can be checked without writing state.",
        color = Steel, style = MaterialTheme.typography.bodyMedium)

    TierSelector(tiers = tiers, selectedTier = selectedTier, onSelectTier = onSelectTier)

    val selected = tiers.firstOrNull { it.level == selectedTier } ?: tiers.first()
    ResourceBar(label = "SUBSTRATE USAGE", usedPercent = selected.usedPercent, availableText = selected.summary)

    ActionButton("RUN VERIFICATION", onVerify)
    ActionButton("BENCHMARK PACKAGE", onBenchmark, subdued = true)

    if (error != null) Text("UNAVAILABLE • $error", color = Crimson, style = MaterialTheme.typography.bodySmall)

    result?.let { checked ->
        Text(checked.summary, color = if (checked.passed) Green else Crimson,
            fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelMedium)
        checked.checks.forEach { (name, passed) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("●", color = if (passed) Green else Crimson)
                Spacer(Modifier.width(8.dp))
                Text(name, color = Steel, style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    benchmark?.let {
        Text("BENCHMARK", color = Crimson, fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.labelSmall)
        Text("${it.packageName} • ${it.benchmarkScore} / 100 • ${it.recommendedTier}",
            color = Steel, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
        Text("memory ${it.memoryMb} MB • storage ${it.storageMb} MB • ${it.summary}",
            color = Muted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun WorkspacePanel(selectedFolder: Uri?) {
    Text("WORKSPACE", color = Muted, fontFamily = FontFamily.Monospace,
        style = MaterialTheme.typography.labelSmall)
    Text(
        if (selectedFolder == null) "Select a repository folder first." else "Folder selected. Workspace launching is available when ready.",
        color = Steel,
        style = MaterialTheme.typography.bodyMedium,
    )
    if (selectedFolder != null) Text(selectedFolder.lastPathSegment ?: selectedFolder.toString(),
        color = Green, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun BackupPanel() {
    Text("BACKUP & PACKAGE", color = Muted, fontFamily = FontFamily.Monospace,
        style = MaterialTheme.typography.labelSmall)
    Text("Package or export actions are intentionally minimal and only become active when an export begins.",
        color = Steel, style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun SubstratePanel(
    tiers: List<NativeSubstrate.ResourceTier>,
    selectedTier: Int,
    onSelectTier: (Int) -> Unit,
    onBenchmark: () -> Unit,
    benchmark: NativeSubstrate.BenchmarkEstimate?,
) {
    Text("MANAGE SUBSTRATE", color = Muted, fontFamily = FontFamily.Monospace,
        style = MaterialTheme.typography.labelSmall)
    TierSelector(tiers = tiers, selectedTier = selectedTier, onSelectTier = onSelectTier)
    val selected = tiers.firstOrNull { it.level == selectedTier } ?: tiers.first()
    ResourceBar(label = "CURRENT LOAD", usedPercent = selected.usedPercent, availableText = selected.summary)
    ActionButton("BENCHMARK PROGRAMS", onBenchmark, subdued = true)
    benchmark?.let {
        Text("RESULT", color = Crimson, fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.labelSmall)
        Text("${it.packageName} • ${it.benchmarkScore} / 100 • ${it.recommendedTier}",
            color = Steel, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
        Text("${it.summary}", color = Muted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun TierSelector(
    tiers: List<NativeSubstrate.ResourceTier>,
    selectedTier: Int,
    onSelectTier: (Int) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        tiers.forEach { tier ->
            val selected = tier.level == selectedTier
            Button(
                onClick = { onSelectTier(tier.level) },
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .border(1.dp, if (selected) Crimson else Muted.copy(alpha = 0.5f), CutCornerShape(8.dp)),
                shape = CutCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected) Crimson.copy(alpha = 0.16f) else Panel,
                    contentColor = Steel,
                ),
            ) {
                Text("T${tier.level}", fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun ResourceBar(label: String, usedPercent: Int, availableText: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Crimson.copy(alpha = 0.4f), CutCornerShape(10.dp))
            .background(Panel, CutCornerShape(10.dp))
            .padding(10.dp),
    ) {
        Text(label, color = Muted, fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(10.dp).background(Color.DarkGray, CutCornerShape(6.dp)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(
                Modifier
                    .fillMaxWidth(usedPercent / 100f)
                    .height(10.dp)
                    .background(Crimson, CutCornerShape(6.dp)),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text("$usedPercent% used • $availableText", color = Steel,
            fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun ActionButton(label: String, onClick: () -> Unit, subdued: Boolean = false) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(54.dp).border(1.dp, Crimson.copy(alpha = 0.75f), CutCornerShape(10.dp)),
        shape = CutCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (subdued) Panel else Crimson.copy(alpha = 0.14f),
            contentColor = if (subdued) Muted else Steel,
        ),
    ) {
        Text(label, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge, letterSpacing = 1.dp)
    }
}

@Composable
private fun StatusIndicator(message: String) {
    Text(
        message,
        modifier = Modifier.fillMaxWidth().border(1.dp, Green.copy(alpha = 0.55f), CutCornerShape(8.dp)).padding(10.dp),
        color = Green,
        fontFamily = FontFamily.Monospace,
        style = MaterialTheme.typography.labelSmall,
    )
}
