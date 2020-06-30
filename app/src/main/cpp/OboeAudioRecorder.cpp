//
// Created by DidierV on 29/06/2020.
//
#include <jni.h>
#include <string>
#include <oboe/Oboe.h>
#include "OboeAudioRecorder.h"
#include "oboe/samples/debug-utils/logging_macros.h"

class OboeAudioRecorder: public oboe::AudioStreamCallback {
public:

    explicit OboeAudioRecorder() {
        oboe::AudioStreamBuilder builder;
        builder.setDirection(oboe::Direction::Input);
        builder.setPerformanceMode(oboe::PerformanceMode::LowLatency);
        builder.setDeviceId(0);
        builder.setChannelCount(oboe::ChannelCount::Mono);
        builder.setInputPreset(oboe::InputPreset::Unprocessed);
        builder.setFormat(oboe::AudioFormat::Float);
        builder.setSharingMode(oboe::SharingMode::Shared);
        builder.setSampleRate(48000);
        builder.setCallback(this);

        oboe::AudioStream *stream;
        oboe::Result r = builder.openStream(&stream);
        if (r != oboe::Result::OK) {
            return;
        }

        r = stream->requestStart();
        if (r != oboe::Result::OK) {
            return;
        }

        constexpr int kMillisecondsToRecord = 2;
        const int32_t requestedFrames = (int32_t) (kMillisecondsToRecord *
                                                   (stream->getSampleRate() /
                                                    oboe::kMillisPerSecond));
        int16_t mybuffer[requestedFrames];

        constexpr int64_t kTimeoutValue = 3 * oboe::kNanosPerMillisecond;

        int framesRead = 0;
        do {
            auto result = stream->read(mybuffer, stream->getBufferSizeInFrames(), 0);
            if (result != oboe::Result::OK) break;
            framesRead = result.value();
            if (framesRead > 0) {
                break;
            }
        } while (framesRead != 0);

        /*bool isRecording = true;
        while (isRecording) {
            auto result = stream->read(mybuffer, requestedFrames, kTimeoutValue);
            if (result == oboe::Result::OK) {
                auto nbFramesRead = result.value();
            } else {
                auto error = convertToText(result.error());
            }
        }*/

        stream->close();
    }

    oboe::DataCallbackResult onAudioReady(oboe::AudioStream *oboeStream, void *audioData, int32_t numFrames) override {
        LOGE("onAudioReady");

        try {
            oboeStream->close();
        } catch(int e) {
            LOGE("onAudioReady exception");
        }
        //LOGE("onAudioReady end");
    }

private:
    oboe::ManagedStream outStream;
    // Stream params
    //static int constexpr kChannelCount = 2;
    //static int constexpr kSampleRate = 48000;
    // Wave params, these could be instance variables in order to modify at runtime
    //static float constexpr kAmplitude = 0.5f;
    //static float constexpr kFrequency = 432;
    //static float constexpr kPI = M_PI;
    //static float constexpr kTwoPi = kPI * 2;
    //static double constexpr mPhaseIncrement = kFrequency * kTwoPi / (double) kSampleRate;
    // Keeps track of where the wave is
    //float mPhase = 0.0;
};
