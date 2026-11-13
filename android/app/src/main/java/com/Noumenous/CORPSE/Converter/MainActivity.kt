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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
private val PanelSoft = Color(0xFF12141B)
private val Crimson = Color(0xFFFF1728)
private val CrimsonSoft = Color(0xFFB30C16)
private val Steel = Color(0xFFD9DEE8)
private val Muted = Color(0xFF8E949E)
private val Green = Color(0xFF7BE5A0)
private val DarkLine = Color(0xFF2A2C33)

private enum class Destination(val label: String, val icon: String) {
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
                            // ignore non-persistable providers
                        }
                    }
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
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var verification by remember { mutableStateOf<NativeContractResult?>(null) }
    var verificationError by remember { mutableStateOf<String?>(null) }
    val tiers = remember(context) { NativeSubstrate.resourceProfile(context) }
    var tierIndex by remember { mutableIntStateOf(1) }
    var benchmark by remember { mutableStateOf<NativeSubstrate.BenchmarkEstimate?>(null) }

    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            onPersistFolderPermission(uri)
            selectedFolder = uri
            statusMessage = "FOLDER SELECTED"
        }
    }

    BackHandler(enabled = destination != Destination.HOME) {
        destination = Destination.HOME
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Void) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Void)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            if (destination == Destination.HOME) {
                BrandHeader()
                Spacer(Modifier.height(20.dp))
                TileGrid {
                    listOf(
                        Destination.REPOSITORY,
                        Destination.VERIFY,
                        Destination.WORKSPACE,
                        Destination.BACKUP,
                        Destination.SUBSTRATE,
                    ).forEach { action ->
                        ActionTile(
                            icon = action.icon,
                            title = action.label,
                            onClick = {
                                destination = action
                                statusMessage = null
                            }
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "INFINITE POTENTIAL. LOCAL CONTROL. NOUMENOUS.",
                    modifier = Modifier.fillMaxWidth(),
                    color = Muted,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                )
            } else {
                PanelHeader(destination = destination) {
                    destination = Destination.HOME
                }
                Spacer(Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .border(1.dp, Crimson.copy(alpha = 0.7f), CutCornerShape(18.dp))
                        .background(PanelSoft, CutCornerShape(18.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    when (destination) {
                        Destination.REPOSITORY -> {
                            RepositoryPanel(
                                selectedFolder = selectedFolder,
                                onChooseFolder = { folderPicker.launch(null) },
                                onClearFolder = {
                                    selectedFolder = null
                                    statusMessage = "FOLDER CLEARED"
                                }
                            )
                        }
                        Destination.VERIFY -> {
                            val selectedTier = tiers.firstOrNull { it.level == tierIndex } ?: tiers.first()
                            VerifyPanel(
                                selectedTier = selectedTier,
                                tiers = tiers,
                                onSelectTier = { tierIndex = it },
                                result = verification,
                                error = verificationError,
                                onVerify = {
                                    try {
                                        verification = NativeSubstrate.runContractChecks(context)
                                        verificationError = null
                                        statusMessage = "VERIFICATION COMPLETE"
                                    } catch (error: Exception) {
                                        verification = null
                                        verificationError = error.message ?: "verification failed"
                                        statusMessage = "VERIFICATION ERROR"
                                    }
                                },
                                onBenchmark = {
                                    benchmark = NativeSubstrate.estimateBenchmark(
                                        packageName = "CANDIDATE_PACKAGE",
                                        packageSizeBytes = 128L * 1024 * 1024,
                                        selectedTier = selectedTier.level,
                                    )
                                    statusMessage = "BENCHMARK READY"
                                },
                                benchmark = benchmark,
                            )
                        }
                        Destination.WORKSPACE -> {
                            WorkspacePanel(selectedFolder = selectedFolder)
                        }
                        Destination.BACKUP -> {
                            BackupPanel()
                        }
                        Destination.SUBSTRATE -> {
                            val selectedTier = tiers.firstOrNull { it.level == tierIndex } ?: tiers.first()
                            SubstratePanel(
                                selectedTier = selectedTier,
                                tiers = tiers,
                                onSelectTier = { tierIndex = it },
                                onBenchmark = {
                                    benchmark = NativeSubstrate.estimateBenchmark(
                                        packageName = "CURRENT_PROGRAM",
                                        packageSizeBytes = 64L * 1024 * 1024,
                                        selectedTier = selectedTier.level,
                                    )
                                    statusMessage = "SUBSTRATE BENCHMARK READY"
                                },
                                benchmark = benchmark,
                            )
                        }
                        Destination.HOME -> Unit
                    }
                }
                if (statusMessage != null) {
                    Spacer(Modifier.height(10.dp))
                    StatusBox(statusMessage ?: "READY")
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
            .background(PanelSoft, CutCornerShape(18.dp))
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "/// PROJECT NOUMENOUS",
            color = Muted,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 3.dp,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "CORPSE",
            color = Steel,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.displaySmall,
            letterSpacing = 7.dp,
        )
        Text(
            text = "MOBILE DESKTOP WORKSTATION CONVERTER",
            color = Muted,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.dp,
        )
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color.DarkGray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.35f)
                    .height(2.dp)
                    .background(Crimson)
            )
        }
    }
}

@Composable
private fun TileGrid(content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        content()
    }
}

@Composable
private fun ActionTile(icon: String, title: String, onClick: () -> Unit) {
    val frame = CutCornerShape(18.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .border(1.dp, Crimson.copy(alpha = 0.75f), frame)
            .background(Panel, frame)
            .clickable { onClick() }
            .padding(top = 12.dp, bottom = 14.dp, start = 10.dp, end = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier.size(42.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = icon,
                color = Steel,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
        }

        Text(
            text = title,
            color = Crimson,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.titleMedium.lineHeight,
        )

        Text(
            text = ">>>",
            color = Crimson,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun PanelHeader(destination: Destination, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Crimson.copy(alpha = 0.75f), CutCornerShape(14.dp))
            .background(PanelSoft, CutCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onBack,
            shape = CutCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Steel),
        ) { Text("‹") }
        Spacer(Modifier.width(12.dp))
        Text(
            text = destination.icon,
            color = Steel,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = destination.label,
            color = Crimson,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun RepositoryPanel(
    selectedFolder: Uri?,
    onChooseFolder: () -> Unit,
    onClearFolder: () -> Unit,
) {
    Text("WORKSPACE SOURCE", color = Muted, fontFamily = FontFamily.Monospace,
        style = MaterialTheme.typography.labelSmall)

    Text(
        text = selectedFolder?.lastPathSegment ?: "No folder selected",
        color = if (selectedFolder == null) Steel else Green,
        fontFamily = FontFamily.Monospace,
        style = MaterialTheme.typography.titleMedium,
    )

    ActionButton(
        label = if (selectedFolder == null) "MOUNT LOCAL (.ZIP / DIR)" else "CHANGE FOLDER",
        onClick = onChooseFolder,
    )

    if (selectedFolder != null) {
        ActionButton(label = "CLEAR SELECTION", onClick = onClearFolder, subdued = true)
    }
}

@Composable
private fun VerifyPanel(
    selectedTier: NativeSubstrate.ResourceTier,
    tiers: List<NativeSubstrate.ResourceTier>,
    onSelectTier: (Int) -> Unit,
    result: NativeContractResult?,
    error: String?,
    onVerify: () -> Unit,
    onBenchmark: () -> Unit,
    benchmark: NativeSubstrate.BenchmarkEstimate?,
) {
    Text("CURRENT RESOURCE USE", color = Muted, fontFamily = FontFamily.Monospace,
        style = MaterialTheme.typography.labelSmall)
    TierSelector(tiers = tiers, selectedTier = selectedTier.level, onSelectTier = onSelectTier)
    ResourceBar(label = "SUBSTRATE", usedPercent = selectedTier.usedPercent, detail = selectedTier.summary)

    ActionButton(label = "RUN VERIFICATION", onClick = onVerify)
    ActionButton(label = "BENCHMARK PACKAGE", onClick = onBenchmark, subdued = true)

    if (error != null) {
        Text(text = "UNAVAILABLE • $error", color = Crimson, style = MaterialTheme.typography.bodySmall)
    }

    result?.let { checked ->
        Text(
            text = checked.summary,
            color = if (checked.passed) Green else Crimson,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.labelMedium,
        )
        checked.checks.forEach { (name, passed) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "●", color = if (passed) Green else Crimson)
                Spacer(Modifier.width(8.dp))
                Text(text = name, color = Steel, style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    benchmark?.let {
        BenchmarkCard(benchmark = it)
    }
}

@Composable
private fun WorkspacePanel(selectedFolder: Uri?) {
    Text("WORKSPACE STATE", color = Muted, fontFamily = FontFamily.Monospace,
        style = MaterialTheme.typography.labelSmall)

    val message = if (selectedFolder == null) {
        "No workspace selected." 
    } else {
        "Workspace ready for launch."
    }

    Text(text = message, color = Steel, style = MaterialTheme.typography.bodyMedium)

    if (selectedFolder != null) {
        Text(
            text = selectedFolder.lastPathSegment ?: selectedFolder.toString(),
            color = Green,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun BackupPanel() {
    Text("PACKAGE", color = Muted, fontFamily = FontFamily.Monospace,
        style = MaterialTheme.typography.labelSmall)
    Text(
        text = "Only run export and packaging actions when a build is active. Otherwise the panel stays minimal.",
        color = Steel,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun SubstratePanel(
    selectedTier: NativeSubstrate.ResourceTier,
    tiers: List<NativeSubstrate.ResourceTier>,
    onSelectTier: (Int) -> Unit,
    onBenchmark: () -> Unit,
    benchmark: NativeSubstrate.BenchmarkEstimate?,
) {
    Text("PROGRAM RESOURCE LOAD", color = Muted, fontFamily = FontFamily.Monospace,
        style = MaterialTheme.typography.labelSmall)
    TierSelector(tiers = tiers, selectedTier = selectedTier.level, onSelectTier = onSelectTier)
    ResourceBar(label = "LOAD", usedPercent = selectedTier.usedPercent, detail = selectedTier.summary)

    ActionButton(label = "BENCHMARK PROGRAMS", onClick = onBenchmark, subdued = true)

    benchmark?.let { BenchmarkCard(benchmark = it) }
}

@Composable
private fun TierSelector(
    tiers: List<NativeSubstrate.ResourceTier>,
    selectedTier: Int,
    onSelectTier: (Int) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        tiers.forEach { tier ->
            val active = tier.level == selectedTier
            Button(
                onClick = { onSelectTier(tier.level) },
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .border(
                        1.dp,
                        if (active) Crimson else Muted.copy(alpha = 0.5f),
                        CutCornerShape(8.dp),
                    ),
                shape = CutCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (active) Crimson.copy(alpha = 0.12f) else PanelSoft,
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
private fun ResourceBar(label: String, usedPercent: Int, detail: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Crimson.copy(alpha = 0.35f), CutCornerShape(10.dp))
            .background(PanelSoft, CutCornerShape(10.dp))
            .padding(10.dp),
    ) {
        Text(text = label, color = Muted, fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(DarkLine, CutCornerShape(6.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((usedPercent / 100f).coerceIn(0f, 1f))
                    .height(10.dp)
                    .background(Crimson, CutCornerShape(6.dp))
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "$usedPercent% used • $detail",
            color = Steel,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun BenchmarkCard(benchmark: NativeSubstrate.BenchmarkEstimate) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Crimson.copy(alpha = 0.4f), CutCornerShape(12.dp))
            .background(PanelSoft, CutCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Text(
            text = "BENCHMARK",
            color = Crimson,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.labelSmall,
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = "${benchmark.packageName} • score ${benchmark.benchmarkScore} / 100 • ${benchmark.recommendedTier}",
            color = Steel,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = "memory ${benchmark.memoryMb} MB • storage ${benchmark.storageMb} MB",
            color = Muted,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = benchmark.summary,
            color = Green,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun ActionButton(label: String, onClick: () -> Unit, subdued: Boolean = false) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .border(1.dp, Crimson.copy(alpha = 0.7f), CutCornerShape(10.dp)),
        shape = CutCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (subdued) PanelSoft else Crimson.copy(alpha = 0.12f),
            contentColor = if (subdued) Muted else Steel,
        ),
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge,
            letterSpacing = 0.8.dp,
        )
    }
}

@Composable
private fun StatusBox(message: String) {
    Text(
        text = message,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Green.copy(alpha = 0.45f), CutCornerShape(8.dp))
            .background(PanelSoft, CutCornerShape(8.dp))
            .padding(10.dp),
        color = Green,
        fontFamily = FontFamily.Monospace,
        style = MaterialTheme.typography.labelSmall,
    )
}
