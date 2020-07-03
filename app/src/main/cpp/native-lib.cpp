#include <jni.h>
#include <string>
#include <android/log.h>
#include <oboe/Oboe.h>
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
    a->isRecording = true;
    a->StartAudioRecorder();

    //auto a = OboeAudioRecorder();
    //auto b = a.getInstance();
    //b->StartAudioRecorder();

    return true;
}
