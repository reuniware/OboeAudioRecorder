#include <jni.h>
#include <string>
#include "OboeAudioRecorder.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_oboeaudiorecorder_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_oboeaudiorecorder_MainActivity_recordAudio(
        JNIEnv * env,
        jobject MainActivity
) {

    auto a = new OboeAudioRecorder();

    return true;
}
