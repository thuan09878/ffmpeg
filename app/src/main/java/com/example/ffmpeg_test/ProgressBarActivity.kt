package com.example.ffmpeg_test

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_progress_bar.*

class ProgressBarActivity : AppCompatActivity(), FFMpegService.Callbacks {

    var duration: Int = 0
    var path: String? = null
    var command: Array<String>? = null

    lateinit var serviceConnection: ServiceConnection
    lateinit var ffMpegService: FFMpegService
    var res: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress_bar)

        progressBar.max = 100

        intent?.let {
            duration = it.getIntExtra("duration", 0)
            command = it.getStringArrayExtra("command")
            path = it.getStringExtra("destination")

            val intent = Intent(this, FFMpegService::class.java)
            intent.putExtra("duration", duration.toString())
            intent.putExtra("command", command)
            intent.putExtra("destination", path)
            startService(intent)

            serviceConnection = object: ServiceConnection {
                override fun onServiceDisconnected(name: ComponentName?) {

                }

                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder = service as FFMpegService.LocalBinder
                    ffMpegService = binder.getServiceInstance()
                    ffMpegService.registerClient(this@ProgressBarActivity)

                    val observer = Observer<Int> { percentage ->
                        progressBar.progress = percentage
                        if (percentage == 100) {
                            stopService(intent)
                            Toast.makeText(this@ProgressBarActivity, "Trim done!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    ffMpegService.getPercentage().observe(this@ProgressBarActivity, observer)
                }
            }

            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun updateClient(data: Float) {

    }
}