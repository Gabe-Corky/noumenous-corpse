package com.Noumenous.CORPSE.Converter

import android.content.Context
import org.json.JSONObject

/** A bounded native contract-check result. The passed mask uses stable bit positions. */
data class NativeContractResult(
    val passedMask: Long,
    val pageSizeBytes: Long,
) {
    val checks: List<Pair<String, Boolean>>
        get() = CHECK_NAMES.mapIndexed { bit, name -> name to (passedMask and (1L shl bit) != 0L) }

    val passed: Boolean
        get() = checks.all { it.second }

    val passedCount: Int
        get() = checks.count { it.second }

    val summary: String
        get() = "${passedCount}/${CHECK_NAMES.size} checks • page ${pageSizeBytes} B"

    companion object {
        private val CHECK_NAMES = listOf(
            "Rules version",
            "Register geometry",
            "Baseline register 0xC7",
            "Active register 0xFF",
            "5-bit / 3-bit octet fields",
            "Byte rotations",
            "Runtime page size",
            "Page alignment",
            "12 × 64 MiB topology",
            "DiaTetra route mask",
            "CRC-32C reference vector",
        )
    }
}

object NativeSubstrate {
    init {
        System.loadLibrary("corpse_arch_core")
    }

    /** Returns the kernel page size in bytes, or 0 if the query fails. */
    external fun pageSizeBytes(): Int

    /** Checks the fixed demonstration marker only; not a file integrity audit. */
    external fun isBaselineStateValid(): Boolean

    /** Loads explicit JSON contract values and asks native code to validate them. */
    fun runContractChecks(context: Context): NativeContractResult {
        val json = context.assets.open("config/substrate_rules.json").bufferedReader().use { it.readText() }
        val rules = JSONObject(json)
        fun requiredInt(key: String): Int {
            require(rules.has(key) && !rules.isNull(key)) { "Missing rule: $key" }
            return rules.getInt(key)
        }
        fun requiredLong(key: String): Long {
            require(rules.has(key) && !rules.isNull(key)) { "Missing rule: $key" }
            return rules.getLong(key)
        }
        val result = runNativeContractChecks(
            requiredInt("schema_version"),
            requiredInt("register_count"),
            requiredInt("register_width_bits"),
            requiredInt("apex_register_bits"),
            requiredInt("baseline_state"),
            requiredInt("active_state"),
            requiredInt("fractal_bits"),
            requiredInt("node_bits"),
            requiredInt("slab_count"),
            requiredInt("slab_bytes"),
            requiredInt("minimum_page_size_bytes"),
            requiredInt("diatetra_route_mask"),
            requiredLong("crc32c_test_vector"),
        )
        require(result.size == 2) { "Native contract result has invalid length" }
        return NativeContractResult(result[0], result[1])
    }

    private external fun runNativeContractChecks(
        rulesVersion: Int,
        registerCount: Int,
        registerWidthBits: Int,
        apexRegisterBits: Int,
        baselineState: Int,
        activeState: Int,
        fractalBits: Int,
        nodeBits: Int,
        slabCount: Int,
        slabBytes: Int,
        minimumPageSize: Int,
        diatetraRouteMask: Int,
        expectedCrc32c: Long,
    ): LongArray
}
