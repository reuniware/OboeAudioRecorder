package com.example.oboeaudiorecorder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.AudioManager.GET_DEVICES_INPUTS
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    var fullPathToFile = ""
    var recordingFrequency = 48000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Example of a call to a native method
        sample_text.text = stringFromJNI()

        buttonStartRecording.isEnabled = true
        buttonStopRecording.isEnabled = false

        editTextFreq.setText(recordingFrequency.toString())

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val devices = audioManager.getDevices(GET_DEVICES_INPUTS)

        val permissionW = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionW != PackageManager.PERMISSION_GRANTED) {
            Log.i("OboeAudioRecorder", "Permission to write denied")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_REQUEST_CODE)
        }

        val permissionR = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (permissionR != PackageManager.PERMISSION_GRANTED) {
            Log.i("OboeAudioRecorder", "Permission to read denied")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_REQUEST_CODE)
        }

        // Check if the Recorders ("/storage/emulated/0/Recorders/") directory exists, and if not then create it
        val folder = File(Environment.getExternalStorageDirectory().path.toString() + "/Recorders")
        if (folder.exists()) {
            if (folder.isDirectory) {
                // The Recorders directory exists
            } else {
                // Create the Recorders directory
                folder.mkdir()
            }
        } else {
            // Create the Recorders directory
            folder.mkdir()
        }

        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale("fr")).format(Date())

        // Full path that is going to be sent to C++ through JNI ("/storage/emulated/0/Recorders/record.wav")
        fullPathToFile = Environment.getExternalStorageDirectory().path.toString() + "/Recorders/${timeStamp}_record.wav"

        buttonStartRecording.setOnClickListener{
            val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)

            if (permission != PackageManager.PERMISSION_GRANTED) {
                Log.i("OboeAudioRecorder", "Permission to record denied")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_REQUEST_CODE)
            } else {
                processStartRecording()
            }
        }

        buttonStopRecording.setOnClickListener {
            processStopRecording()
        }
    }

    // Get the recording frequency entered by the user. If empty then default to 48000.
    fun getRecordingFreq() : Int {
        var freq = recordingFrequency
        if (!editTextFreq.text.toString().trim().isEmpty())
        {
            freq = editTextFreq.text.toString().toInt()
        }
        return freq
    }

    val RECORD_REQUEST_CODE = 1234
    val WRITE_REQUEST_CODE = 1235
    val READ_REQUEST_CODE = 1236

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("OboeAudioRecorder", "Permission has been denied by user")
                } else {
                    Log.i("OboeAudioRecorder", "Permission has been granted by user")

                    processStartRecording()
                }
            }
            WRITE_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("OboeAudioRecorder", "Permission has been denied by user")
                } else {
                    Log.i("OboeAudioRecorder", "Permission has been granted by user")
                }
            }
            READ_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("OboeAudioRecorder", "Permission has been denied by user")
                } else {
                    Log.i("OboeAudioRecorder", "Permission has been granted by user")
                }
            }
        }
    }

    var stopTimer = false
    var seconds = 0
    val maxSeconds = 10
    fun startTimer() {
        stopTimer = false
        val timer = Timer()
        val task = object : TimerTask() {
            override fun run() {
                if (!stopTimer) {
                    seconds++
                    textViewTimer.post {
                        textViewTimer.setText(seconds.toString())
                    }
                    if (seconds == maxSeconds) {
                        processStopRecording()
                    }
                } else {
                    this.cancel()
                }
            }
        }
        timer.schedule(task, 0, 1000)
    }

    fun stopTimer() {
        stopTimer = true
        seconds = 0
    }

    val enableTimer = false

    fun processStartRecording() {
        if (enableTimer) {
            startTimer()
        }
        Thread(Runnable {
            startRecording(fullPathToFile, getRecordingFreq())
        }).start()
        buttonStartRecording.isEnabled = false
        buttonStopRecording.isEnabled = true
    }

    fun processStopRecording() {
        if (enableTimer) {
            stopTimer()
        }
        Thread(Runnable { stopRecording() }).start()
        buttonStartRecording.isEnabled = true
        buttonStopRecording.isEnabled = false
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    external fun startRecording(fullPathToFile: String, recordingFrequency: Int): Boolean

    external fun stopRecording(): Boolean

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
