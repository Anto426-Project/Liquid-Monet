#include "liquid_wave.h"

#include <jni.h>

JNIEXPORT jboolean JNICALL
Java_com_anto426_liquidmonet_glass_internal_LiquidNativeWave_fillSineLookup(
    JNIEnv *env, jobject receiver, jfloatArray destination) {
    (void)receiver;
    if (destination == 0) {
        return JNI_FALSE;
    }
    const jsize length = (*env)->GetArrayLength(env, destination);
    if (length < 17 || length > 16385) {
        return JNI_FALSE;
    }
    jfloat *samples = (*env)->GetFloatArrayElements(env, destination, 0);
    if (samples == 0) {
        return JNI_FALSE;
    }
    const int success = liquid_wave_fill_sine(samples, (int)length - 1);
    (*env)->ReleaseFloatArrayElements(env, destination, samples, success ? 0 : JNI_ABORT);
    return success ? JNI_TRUE : JNI_FALSE;
}
