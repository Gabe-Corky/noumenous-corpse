#include "corpse_primitives.h"

#include <jni.h>
#include <unistd.h>

#include <array>
#include <cstdint>

namespace {
constexpr jint kSupportedRulesVersion = 1;
constexpr std::uint8_t kBaselineState = 0xC7U;
constexpr std::uint8_t kActiveState = 0xFFU;
constexpr jint kRegisterCount = 256;
constexpr jint kRegisterWidthBits = 8;
constexpr jint kApexRegisterBits = 64;
constexpr jint kFractalBits = 5;
constexpr jint kNodeBits = 3;
constexpr jint kSlabCount = 12;
constexpr jint kSlabBytes = 64 * 1024 * 1024;
constexpr jint kDiaTetraRouteMask = 0xFF;
constexpr std::uint32_t kCrc32cVector = 0xE3069283U;

// Stable bit assignments returned to Kotlin as a compact native result.
enum Check : std::uint64_t {
    RulesVersion = 1ULL << 0U,
    RegisterGeometry = 1ULL << 1U,
    BaselineRegister = 1ULL << 2U,
    ActiveRegister = 1ULL << 3U,
    OctetFields = 1ULL << 4U,
    Rotations = 1ULL << 5U,
    PageSize = 1ULL << 6U,
    PageAlignment = 1ULL << 7U,
    SlabTopology = 1ULL << 8U,
    DiaTetraRoutes = 1ULL << 9U,
    Crc32cReference = 1ULL << 10U,
};

constexpr std::uint64_t kAllChecks = (1ULL << 11U) - 1ULL;

jlongArray make_result(JNIEnv* env, std::uint64_t passed_mask, long page_size) {
    const jlong values[2] = {
        static_cast<jlong>(passed_mask),
        static_cast<jlong>(page_size > 0 ? page_size : 0),
    };
    jlongArray result = env->NewLongArray(2);
    if (result != nullptr) {
        env->SetLongArrayRegion(result, 0, 2, values);
    }
    return result;
}
} // namespace

extern "C" JNIEXPORT jint JNICALL
Java_com_Noumenous_CORPSE_Converter_NativeSubstrate_pageSizeBytes(JNIEnv*, jobject) {
    const long page_size = sysconf(_SC_PAGESIZE);
    return page_size > 0 ? static_cast<jint>(page_size) : 0;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_Noumenous_CORPSE_Converter_NativeSubstrate_isBaselineStateValid(JNIEnv*, jobject) {
    return corpse::encode_octet(24U, 7U) == kBaselineState ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jlongArray JNICALL
Java_com_Noumenous_CORPSE_Converter_NativeSubstrate_runNativeContractChecks(
    JNIEnv* env,
    jobject,
    jint rules_version,
    jint register_count,
    jint register_width_bits,
    jint apex_register_bits,
    jint baseline_state,
    jint active_state,
    jint fractal_bits,
    jint node_bits,
    jint slab_count,
    jint slab_bytes,
    jint minimum_page_size,
    jint diatetra_route_mask,
    jlong expected_crc32c) {
    const long page_size = sysconf(_SC_PAGESIZE);
    const std::uint8_t crc_sample[] = {'1','2','3','4','5','6','7','8','9'};
    std::uint64_t aligned_address = 0;
    std::uint64_t passed = 0;

    if (rules_version == kSupportedRulesVersion) passed |= RulesVersion;
    if (register_count == kRegisterCount && register_width_bits == kRegisterWidthBits &&
        apex_register_bits == kApexRegisterBits) passed |= RegisterGeometry;
    if (baseline_state == kBaselineState && corpse::encode_octet(24U, 7U) == kBaselineState)
        passed |= BaselineRegister;
    if (active_state == kActiveState) passed |= ActiveRegister;
    if (fractal_bits == kFractalBits && node_bits == kNodeBits &&
        corpse::extract_fractal(kBaselineState) == 24U && corpse::extract_node(kBaselineState) == 7U)
        passed |= OctetFields;
    if (corpse::rotate_left(kBaselineState, 1U) == 0x8FU &&
        corpse::rotate_right(kBaselineState, 1U) == 0xE3U) passed |= Rotations;
    if (page_size > 0 && minimum_page_size > 0 && page_size >= minimum_page_size &&
        (page_size % minimum_page_size) == 0) passed |= PageSize;
    if (page_size > 0 && corpse::align_down(0x12345U, static_cast<std::uint64_t>(page_size), aligned_address) &&
        (aligned_address % static_cast<std::uint64_t>(page_size)) == 0U &&
        !corpse::align_down(0x12345U, 0U, aligned_address)) passed |= PageAlignment;
    if (slab_count == kSlabCount && slab_bytes == kSlabBytes &&
        static_cast<std::uint64_t>(slab_count) * static_cast<std::uint64_t>(slab_bytes) ==
            768ULL * 1024ULL * 1024ULL) passed |= SlabTopology;
    if (diატetra_route_mask == kDiaTetraRouteMask) passed |= DiaTetraRoutes;
    if (expected_crc32c >= 0 && static_cast<std::uint64_t>(expected_crc32c) <= 0xFFFFFFFFULL &&
        static_cast<std::uint32_t>(expected_crc32c) == kCrc32cVector &&
        corpse::crc32c(crc_sample, sizeof(crc_sample)) == kCrc32cVector) passed |= Crc32cReference;

    (void)kAllChecks; // Kotlin owns display names; kept here as the native mask contract.
    return make_result(env, passed, page_size);
}
