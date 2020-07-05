package com.example.oboeaudiorecorder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.AudioManager.GET_DEVICES_INPUTS
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Example of a call to a native method
        sample_text.text = stringFromJNI()

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

        // Full path that is going to be sent to C++ through JNI ("/storage/emulated/0/Recorders/record.wav")
        val fullPathToFile = Environment.getExternalStorageDirectory().path.toString() + "/Recorders/record.wav";

        buttonStartRecording.setOnClickListener{
            val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)

            if (permission != PackageManager.PERMISSION_GRANTED) {
                Log.i("OboeAudioRecorder", "Permission to record denied")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_REQUEST_CODE)
            } else {
                Thread(Runnable { startRecording(fullPathToFile) }).start()
            }
        }

        buttonStopRecording.setOnClickListener {
            Thread(Runnable { stopRecording() }).start()
        }
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

                    Thread(Runnable{
                        val fullPathToFile = Environment.getExternalStorageDirectory().path.toString() + "/Music/record.wav";
                        startRecording(fullPathToFile)
                    }).start()
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

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    external fun startRecording(fullPathToFile: String): Boolean

    external fun stopRecording(): Boolean

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
