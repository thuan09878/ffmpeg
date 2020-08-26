package com.example.ffmpeg_test

import android.app.Activity
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException

class FFMpegService: Service() {

    var fFmpeg: FFmpeg? = null
    var duration: Int? = 0
    var command: Array<String>? = null

    private val percentage = MutableLiveData<Int>()
    private val myBinder = LocalBinder()
    lateinit var callBack: Callbacks

    override fun onBind(intent: Intent?): IBinder? {
        return myBinder
    }

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            duration = intent.getStringExtra("duration")?.toInt()
            command = intent.getStringArrayExtra("command")
            try {
                loadFFMpegBinary()
                exeFFmpegCommand()
            } catch (e: FFmpegNotSupportedException) {
                e.printStackTrace()
            } catch (e: FFmpegCommandAlreadyRunningException) {
                e.printStackTrace()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        try {
            loadFFMpegBinary()

        } catch (e: FFmpegNotSupportedException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fFmpeg?.killRunningProcesses()
    }

    @Throws(FFmpegCommandAlreadyRunningException::class)
    private fun exeFFmpegCommand() {
        fFmpeg?.execute(command, object : ExecuteBinaryResponseHandler() {
            override fun onFailure(message: String?) {
                super.onFailure(message)
            }

            override fun onSuccess(message: String?) {
                super.onSuccess(message)
            }

            override fun onProgress(message: String?) {
                var arr = listOf<String>()
                if (message?.contains("time=") == true) {
                    arr = message.split("time=")
                    val yalo = arr[1]
                    val abikamha = yalo.split(":")
                    val yaenda = abikamha[2].split(" ")
                    val seconds = yaenda[0]

                    var hours = abikamha[0].toInt()
                    hours *= 3600
                    var min = abikamha[1].toInt()
                    min *= 60
                    val sec = seconds.toFloat()

                    val timeInSec = hours + min + sec
                    percentage.value = (timeInSec/(duration ?: 1) * 100).toInt()
                }
            }

            override fun onStart() {
                super.onStart()
            }

            override fun onFinish() {
                percentage.value = 100
            }
        })
    }

    @Throws(FFmpegNotSupportedException::class)
    private fun loadFFMpegBinary() {
        if (fFmpeg == null) {
            fFmpeg = FFmpeg.getInstance(this)
        }
        fFmpeg?.loadBinary(object : LoadBinaryResponseHandler() {
            override fun onFailure() {
                super.onFailure()
            }

            override fun onSuccess() {
                super.onSuccess()
            }
        })
    }

    class LocalBinder: Binder() {
        fun getServiceInstance(): FFMpegService {
            return FFMpegService()
        }
    }

    fun registerClient(activity: Activity) {
        this.callBack = activity as Callbacks
    }

    fun getPercentage(): MutableLiveData<Int> {
        return percentage
    }

    interface Callbacks {
        fun updateClient(data: Float)
    }
}