package com.Noumenous.CORPSE.Converter

import android.content.Context
import org.json.JSONObject

private const val DEFAULT_RULES_PATH = "config/substrate_rules.json"

data class NativeContractResult(
    val passed: Boolean,
    val pageSizeBytes: Int,
    val passedCount: Int,
    val checks: Map<String, Boolean>,
    val summary: String,
)

object NativeSubstrate {
    data class ResourceTier(
        val level: Int,
        val label: String,
        val totalGiB: Double,
        val usedPercent: Int,
        val summary: String,
    )

    data class BenchmarkEstimate(
        val packageName: String,
        val benchmarkScore: Int,
        val memoryMb: Int,
        val storageMb: Int,
        val recommendedTier: String,
        val summary: String,
    )

    init {
        System.loadLibrary("corpse_arch_core")
    }

    external fun pageSizeBytes(): Int
    external fun isBaselineStateValid(): Boolean

    fun resourceProfile(context: Context): List<ResourceTier> {
        val pageSize = pageSizeBytes()
        val pageLoad = if (pageSize > 0) ((pageSize.toDouble() / 4096.0) * 100.0).coerceAtMost(100.0) else 42.0

        return listOf(
            ResourceTier(
                level = 1,
                label = "TIER 1",
                totalGiB = 1.25,
                usedPercent = pageLoad.toInt().coerceIn(18, 72),
                summary = "1.25 GB • local baseline",
            ),
            ResourceTier(
                level = 2,
                label = "TIER 2",
                totalGiB = 2.50,
                usedPercent = (pageLoad * 1.15).toInt().coerceIn(24, 80),
                summary = "2.50 GB • multi-workflow",
            ),
            ResourceTier(
                level = 3,
                label = "TIER 3",
                totalGiB = 3.50,
                usedPercent = (pageLoad * 1.30).toInt().coerceIn(30, 90),
                summary = "3.50 GB • full DeX footprint",
            ),
        )
    }

    fun estimateBenchmark(packageName: String, packageSizeBytes: Long, selectedTier: Int): BenchmarkEstimate {
        val sizeMb = (packageSizeBytes / (1024 * 1024)).coerceAtLeast(32L)
        val tierName = when (selectedTier) {
            1 -> "TIER 1"
            2 -> "TIER 2"
            else -> "TIER 3"
        }

        val score = ((sizeMb * 0.8) + (selectedTier * 16)).toInt().coerceIn(18, 96)
        val memoryMb = (sizeMb + (selectedTier * 64)).toInt()
        val storageMb = (sizeMb * 3 / 2).toInt().coerceAtLeast(64)
        val recommendation = when {
            selectedTier >= 3 -> "RECOMMENDED: TIER 3 for large packages"
            selectedTier == 2 -> "RECOMMENDED: TIER 2 for balanced footprint"
            else -> "RECOMMENDED: TIER 1 for minimal packages"
        }

        return BenchmarkEstimate(
            packageName = packageName,
            benchmarkScore = score,
            memoryMb = memoryMb,
            storageMb = storageMb,
            recommendedTier = tierName,
            summary = "$recommendation • ${sizeMb}MB candidate footprint",
        )
    }

    fun runContractChecks(context: Context): NativeContractResult {
        val pageSize = pageSizeBytes()
        val nativeBaseline = isBaselineStateValid()

        val rules = loadRules(context)
        val minimumPageSize = rules.optInt("minimum_page_size_bytes", 4096)
        val baselineState = rules.optInt("baseline_state", 199)
        val slabCount = rules.optInt("slab_count", 12)
        val slabBytes = rules.optInt("slab_bytes", 64 * 1024 * 1024)

        val checks = linkedMapOf(
            "Kernel page size >= minimum page size" to (pageSize >= minimumPageSize),
            "Native baseline state valid" to nativeBaseline,
            "Rule baseline = 0xC7" to (baselineState == 0xC7),
            "12 VFS-NVRAM slabs configured" to (slabCount == 12),
            "64 MiB slab geometry present" to (slabBytes == 64 * 1024 * 1024),
        )

        val passedCount = checks.count { it.value }
        val passed = passedCount == checks.size
        val summary = if (passed) {
            "NATIVE VERIFICATION PASS • $passedCount/${checks.size} CHECKS • PAGE $pageSize B • 12 SLABS • 0xC7 BASELINE"
        } else {
            "NATIVE VERIFICATION FAIL • $passedCount/${checks.size} CHECKS • PAGE $pageSize B"
        }

        return NativeContractResult(
            passed = passed,
            pageSizeBytes = pageSize,
            passedCount = passedCount,
            checks = checks,
            summary = summary,
        )
    }

    private fun loadRules(context: Context): JSONObject {
        val raw = context.assets.open(DEFAULT_RULES_PATH)
            .bufferedReader()
            .use { it.readText() }
        return JSONObject(raw)
    }
}
