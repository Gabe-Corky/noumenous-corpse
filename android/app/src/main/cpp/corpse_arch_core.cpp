#include "corpse_primitives.h"

#include <jni.h>
#include <unistd.h>

#include <cstdint>

namespace {
constexpr jint kSupportedRulesVersion = 1;
constexpr std::uint8_t kExpectedBaseline = 0xC7U;
constexpr std::uint8_t kExpectedFractalBits = 5U;
constexpr std::uint8_t kExpectedNodeBits = 3U;
constexpr jint kExpectedSlabCount = 12;
constexpr jint kExpectedSlabBytes = 64 * 1024 * 1024;
constexpr std::uint32_t kExpectedCrc32c = 0xE3069283U;

jstring result(JNIEnv* env, bool passed, long page_size) {
    if (passed) {
        return env->NewStringUTF(
            ("{\"passed\":true,\"checks\":6,\"state\":\"0xC7\",\"page_size_bytes\":" +
             std::to_string(page_size) + "}").c_str());
    }
    return env->NewStringUTF(
        ("{\"passed\":false,\"checks\":6,\"page_size_bytes\":" +
         std::to_string(page_size) + "}").c_str());
}
} // namespace

extern "C" JNIEXPORT jint JNICALL
Java_com_Noumenous_CORPSE_Converter_NativeSubstrate_pageSizeBytes(JNIEnv*, jobject) {
    const long page_size = sysconf(_SC_PAGESIZE);
    return page_size > 0 ? static_cast<jint>(page_size) : 0;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_Noumenous_CORPSE_Converter_NativeSubstrate_isBaselineStateValid(JNIEnv*, jobject) {
    return corpse::encode_octet(24U, 7U) == kExpectedBaseline ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_Noumenous_CORPSE_Converter_NativeSubstrate_runContractChecks(
    JNIEnv* env,
    jobject,
    jint rules_version,
    jint baseline_state,
    jint fractal_bits,
    jint node_bits,
    jint slab_count,
    jint slab_bytes,
    jint minimum_page_size,
    jlong expected_crc32c) {
    const long page_size = sysconf(_SC_PAGESIZE);

    const std::uint8_t sample[] = {'1','2','3','4','5','6','7','8','9'};
    const std::uint64_t total_slab_bytes =
        static_cast<std::uint64_t>(slab_count) * static_cast<std::uint64_t>(slab_bytes);
    std::uint64_t aligned_address = 0;

    const bool valid_rules =
        rules_version == kSupportedRulesVersion &&
        baseline_state == kExpectedBaseline &&
        fractal_bits == kExpectedFractalBits &&
        node_bits == kExpectedNodeBits &&
        slab_count == kExpectedSlabCount &&
        slab_bytes == kExpectedSlabBytes &&
        minimum_page_size > 0 &&
        static_cast<std::uint32_t>(expected_crc32c) == kExpectedCrc32c;

    const bool octet_fields =
        corpse::encode_octet(24U, 7U) == kExpectedBaseline &&
        corpse::extract_fractal(kExpectedBaseline) == 24U &&
        corpse::extract_node(kExpectedBaseline) == 7U;
    const bool byte_rotation = corpse::rotate_left(kExpectedBaseline, 1U) == 0x8FU &&
                               corpse::rotate_right(kExpectedBaseline, 1U) == 0xE3U;
    const bool page_geometry = page_size > 0 && minimum_page_size > 0 &&
        (page_size % minimum_page_size) == 0 &&
        corpse::align_down(0x12345U, static_cast<std::uint64_t>(page_size), aligned_address) &&
        (aligned_address % static_cast<std::uint64_t>(page_size)) == 0U;
    const bool slab_geometry = total_slab_bytes == 768ULL * 1024ULL * 1024ULL;
    const bool crc_vector = corpse::crc32c(sample, sizeof(sample)) == kExpectedCrc32c;
    const bool passed = valid_rules && octet_fields && byte_rotation && page_geometry &&
                        slab_geometry && crc_vector;

    return result(env, passed, page_size);
}
