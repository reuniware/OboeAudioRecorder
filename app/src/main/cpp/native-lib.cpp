#include <jni.h>
#include <string>
#include <android/log.h>
#include <oboe/Oboe.h>
#include <thread>
#include "OboeAudioRecorder.h"
#include "OboeAudioRecorder.cpp"

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
    static auto a = OboeAudioRecorder::get();
    a->StartAudioRecorder();
    return true;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_oboeaudiorecorder_MainActivity_stopRecording(
        JNIEnv * env,
        jobject MainActivity
        ) {
    static auto a = OboeAudioRecorder::get();
    a->StopAudioRecorder();
    return true;
}

