package com.Noumenous.CORPSE.Converter

import android.content.Context
import org.json.JSONObject

data class NativeContractResult(
    val passed: Boolean,
    val pageSizeBytes: Int,
    val passedCount: Int,
    val checks: Map<String, Boolean>,
    val summary: String
)

object NativeSubstrate {
    init {
        System.loadLibrary("corpse_arch_core")
    }

    external fun pageSizeBytes(): Int
    external fun isBaselineStateValid(): Boolean

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
            "64 MiB slab geometry present" to (slabBytes == 64 * 1024 * 1024)
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
            summary = summary
        )
    }

    private fun loadRules(context: Context): JSONObject {
        val raw = context.assets.open("config/substrate_rules.json")
            .bufferedReader()
            .use { it.readText() }

        return JSONObject(raw)
    }
}
