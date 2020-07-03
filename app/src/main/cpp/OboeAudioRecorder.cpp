//
// Created by DidierV on 29/06/2020.
//
#include <jni.h>
#include <string>
#include <oboe/Oboe.h>
#include "OboeAudioRecorder.h"
#include "oboe/samples/debug-utils/logging_macros.h"

class OboeAudioRecorder: public oboe::AudioStreamCallback {

private:
    oboe::ManagedStream outStream;
    oboe::AudioStream *stream{};

    oboe::DataCallbackResult
    onAudioReady(oboe::AudioStream *oboeStream, void *audioData, int32_t numFrames) override {
        LOGE("onAudioReady");
    }

    static OboeAudioRecorder *singleton;
    explicit OboeAudioRecorder() = default;

public:
    static OboeAudioRecorder *get() {
        if (!singleton)
            singleton = new OboeAudioRecorder();
        return singleton;
    }

    bool isRecording = true;

    void StartAudioRecorder() {
        oboe::AudioStreamBuilder builder;
        builder.setDirection(oboe::Direction::Input);
        builder.setPerformanceMode(oboe::PerformanceMode::LowLatency);
        //builder.setFormat(oboe::AudioFormat::Float);
        builder.setFormat(oboe::AudioFormat::I16);
        builder.setChannelCount(oboe::ChannelCount::Mono);
        builder.setInputPreset(oboe::InputPreset::Unprocessed);
        builder.setSharingMode(oboe::SharingMode::Shared);
        builder.setSampleRate(48000);
        builder.setAudioApi(oboe::AudioApi::OpenSLES);
        //builder.setCallback(this);

        oboe::Result r = builder.openStream(&stream);
        if (r != oboe::Result::OK) {
            return;
        }

        r = stream->requestStart();
        if (r != oboe::Result::OK) {
            return;
        }

        auto a = stream->getState();
        if (a == oboe::StreamState::Started) {

            constexpr int kMillisecondsToRecord = 2;
            auto requestedFrames = (int32_t) (kMillisecondsToRecord *
                                                       (stream->getSampleRate() /
                                                        oboe::kMillisPerSecond));
            __android_log_print(ANDROID_LOG_INFO, "OboeAudioRecorder", "requestedFrames = %d",
                                requestedFrames);
            //const int32_t requestedFrames = 65536;
            //int16_t mybuffer[requestedFrames];
            int16_t mybuffer[requestedFrames];
            constexpr int64_t kTimeoutValue = 3 * oboe::kNanosPerMillisecond;

            int framesRead = 0;
            do {
                auto result = stream->read(mybuffer, requestedFrames, 0);
                if (result != oboe::Result::OK) {
                    break;
                }
                framesRead = result.value();
                __android_log_print(ANDROID_LOG_INFO, "OboeAudioRecorder", "framesRead = %d",
                                    framesRead);
                if (framesRead > 0) {
                    break;
                }
            } while (framesRead != 0);

            while (isRecording) {
                auto result = stream->read(mybuffer, requestedFrames, kTimeoutValue * 1000);
                if (result == oboe::Result::OK) {
                    auto nbFramesRead = result.value();
                    __android_log_print(ANDROID_LOG_INFO, "OboeAudioRecorder", "nbFramesRead = %d",
                                        nbFramesRead);
                    for (int i = 0; i < nbFramesRead; i++) {
                        __android_log_print(ANDROID_LOG_INFO, "OboeAudioRecorder",
                                            "nbFramesRead[%d] = %d", i, mybuffer[i]);
                    }
                } else {
                    auto error = convertToText(result.error());
                    __android_log_print(ANDROID_LOG_INFO, "OboeAudioRecorder", "error = %s", error);
                }
            }

            stream->requestStop();
            stream->close();
        }
    }


};

OboeAudioRecorder *OboeAudioRecorder::singleton = nullptr;

