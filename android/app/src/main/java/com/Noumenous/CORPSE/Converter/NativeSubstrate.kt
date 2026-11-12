package com.Noumenous.CORPSE.Converter

import android.content.Context
import org.json.JSONObject

object NativeSubstrate {
    init {
        System.loadLibrary("corpse_arch_core")
    }

    /** Returns the kernel page size in bytes, or 0 if the query fails. */
    external fun pageSizeBytes(): Int

    /** Checks the fixed demonstration marker only; not a file integrity audit. */
    external fun isBaselineStateValid(): Boolean

    /** Loads explicit JSON contract values and asks native code to validate them. */
    fun runContractChecks(context: Context): String {
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

        return runNativeContractChecks(
            requiredInt("schema_version"),
            requiredInt("baseline_state"),
            requiredInt("fractal_bits"),
            requiredInt("node_bits"),
            requiredInt("slab_count"),
            requiredInt("slab_bytes"),
            requiredInt("minimum_page_size_bytes"),
            requiredLong("crc32c_test_vector"),
        )
    }

    private external fun runNativeContractChecks(
        rulesVersion: Int,
        baselineState: Int,
        fractalBits: Int,
        nodeBits: Int,
        slabCount: Int,
        slabBytes: Int,
        minimumPageSize: Int,
        expectedCrc32c: Long,
    ): String
}
