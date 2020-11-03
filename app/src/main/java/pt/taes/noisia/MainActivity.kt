package pt.taes.noisia

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.lang.Math.pow
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt


class MainActivity : AppCompatActivity() {


    private var mediaRecorder: MediaRecorder? = null
    private var isRunning: Boolean = false
    private var runningThread : Thread? = null
    private var mEMA = 0.0
    private val EMA_FILTER = 0.6


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.buttonStart).setOnClickListener {

            Thread {
                startRecording()
                runningThread = Thread {
                    while (isRunning) {
                        Thread.sleep(500)
                            runOnUiThread {
                            findViewById<TextView>(R.id.db).text = String.format("%.1f", getDecibelsFromAmplitude()).toString() + " Db";
//                            findViewById<TextView>(R.id.db).text = mediaRecorder!!.maxAmplitude.toDouble().toString();
                        }
                    }
                }

                runningThread?.start();
            }.start()
        }

        findViewById<Button>(R.id.buttonStop).setOnClickListener {
            stopRecording()
        }

    }


    private fun startRecording() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissions, 0)
        } else {

            mediaRecorder = MediaRecorder()
            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder?.setOutputFile("/dev/null");

            try {
                mediaRecorder?.prepare()
            } catch (ioe: IOException) {
                Log.e("[Monkey]", "IOException: " + Log.getStackTraceString(ioe))
            } catch (e: SecurityException) {
                Log.e("[Monkey]", "SecurityException: " + Log.getStackTraceString(e))
            }
            try {
                mediaRecorder?.start()
                isRunning = true

//                runOnUiThread {  Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show() }
            } catch (e: SecurityException) {
                Log.e("[Monkey]", "SecurityException: " + Log.getStackTraceString(e))
            } catch (e: Exception) {
                Log.e("[Monkey]", "Exception: " + Log.getStackTraceString(e))
            }
        }
    }


    private fun stopRecording() {
        if (isRunning) {
            isRunning = false
            runningThread?.join();
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
        } else {
//            runOnUiThread {  Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun getDecibelsFromAmplitude(): Double {

        val amp = getAmplitude()

        if (amp == 0.0)
            return 0.0;

        return calculateDecibels(amp);
    }

    private fun getAmplitude(): Double {

        if (mediaRecorder == null)
            return 0.0

        return mediaRecorder!!.maxAmplitude.toDouble()
    }

    private fun calculateDecibels(ampl: Double): Double {
        return 20 * log10(ampl / 2.0.pow(-0.5));
    }
}