#include "corpse_primitives.h"

#include <jni.h>
#include <unistd.h>

namespace {
constexpr jint kBaselineState = 0xC7;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_Noumenous_CORPSE_Converter_NativeSubstrate_pageSizeBytes(JNIEnv*, jobject) {
    const long page_size = sysconf(_SC_PAGESIZE);
    return page_size > 0 ? static_cast<jint>(page_size) : 0;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_Noumenous_CORPSE_Converter_NativeSubstrate_isBaselineStateValid(JNIEnv*, jobject) {
    // This verifies the starter's fixed state marker only. It is not a storage
    // integrity check, checksum, hardware audit, or self-healing mechanism.
    return corpse::encode_octet(24U, 7U) == kBaselineState ? JNI_TRUE : JNI_FALSE;
}
