package com.example.ffmpeg_test

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_trim.*
import java.io.File

class TrimActivity : AppCompatActivity() {

    var uri: Uri? = null
    var duration: Int = 0
    var prefix: String = ""
    var dest: File? = null
    var originalPath: String? = null
    var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trim)

        if (intent != null) {
            intent.getStringExtra("uri")?.let {
                uri = Uri.parse(it)
                isPlaying = true
                videoView.setVideoURI(uri)
                videoView.start()
            }
        }

        setOnclickEvents()

        videoView.setOnPreparedListener {
            videoView.start()
            duration = it.duration / 1000
            tvLeft.text = "00:00:00"
            tvRight.text = getTime(duration)
            it.isLooping = true
//            seekbar.setRangeValues(0, duration)
        }
    }

    private fun setOnclickEvents() {
        btnControl.setOnClickListener {
            setupControl()
        }
        videoView.setOnClickListener {
            setupControl()
        }
    }

    private fun getTime(duration: Int):String {
        return ""
    }

    private fun setupControl() {
        isPlaying = if (isPlaying) {
            btnControl.setImageResource(R.drawable.ic_play)
            videoView.pause()
            false
        } else {
            btnControl.setImageResource(R.drawable.ic_pause)
            videoView.start()
            true
        }
    }
}