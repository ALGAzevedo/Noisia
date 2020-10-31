package pt.taes.noisia

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import kotlin.math.log10


class MainActivity : AppCompatActivity() {


    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private var mEMA = 0.0
    private val EMA_FILTER = 0.6


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.buttonStart).setOnClickListener {

            Thread {
                startRecording()
            }.start()

            Thread {
                while (mediaRecorder != null) {
                    Thread.sleep(1000)
                    runOnUiThread {
                        findViewById<TextView>(R.id.db).text = getAmplitudeEMA().toString()
                    }
                }
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
                state = true

                Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show()
            } catch (e: SecurityException) {
                Log.e("[Monkey]", "SecurityException: " + Log.getStackTraceString(e))
            }catch (e: Exception){
                Log.e("[Monkey]", "Exception: " + Log.getStackTraceString(e))
            }
        }
    }


    private fun stopRecording(){
        if(state){
            mediaRecorder?.stop()
            mediaRecorder?.release()
            state = false
        }else{
            Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show()
        }
    }

    fun soundDb(ampl: Double): Double {
        return 20 * log10(getAmplitudeEMA() / ampl)
    }

    private fun getAmplitude(): Double {
        if(mediaRecorder != null){
            return mediaRecorder!!.maxAmplitude.toDouble()
        }
        return 0.0
    }

    fun getAmplitudeEMA(): Double {
        val amp = getAmplitude()
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA
        return mEMA
    }


}