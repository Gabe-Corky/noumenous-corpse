package com.Noumenous.CORPSE.Converter

object NativeSubstrate {
    init {
        System.loadLibrary("corpse_arch_core")
    }

    /** Returns the kernel page size in bytes, or 0 if the query fails. */
    external fun pageSizeBytes(): Int

    /** Checks the demo's fixed 0xC7 marker; this is not an integrity audit. */
    external fun isBaselineStateValid(): Boolean
}
