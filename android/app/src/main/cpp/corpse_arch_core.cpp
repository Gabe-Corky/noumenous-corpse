#include <jni.h>
#include <unistd.h>

#include <cstdint>

namespace {
constexpr std::uint32_t kBaselineApex = 0xC7U;
constexpr std::int64_t kMinimumPageSize = 4096LL;
}

extern "C" {

JNIEXPORT jint JNICALL
Java_com_Noumenous_CORPSE_Converter_NativeSubstrate_pageSizeBytes(JNIEnv* /*env*/, jclass /*clazz*/) {
    const long page_size = sysconf(_SC_PAGESIZE);
    return page_size > 0 ? static_cast<jint>(page_size) : 0;
}

JNIEXPORT jboolean JNICALL
Java_com_Noumenous_CORPSE_Converter_NativeSubstrate_isBaselineStateValid(JNIEnv* /*env*/, jclass /*clazz*/) {
    const long page_size = sysconf(_SC_PAGESIZE);
    if (page_size <= 0) {
        return JNI_FALSE;
    }

    const bool valid_page = page_size >= kMinimumPageSize;
    const std::uint32_t apex = kBaselineApex;
    const bool baseline_ok = (apex == 0xC7U);

    return (valid_page && baseline_ok) ? JNI_TRUE : JNI_FALSE;
}

}  // extern "C"
