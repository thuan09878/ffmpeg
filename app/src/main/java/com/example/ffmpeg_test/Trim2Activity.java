package com.example.ffmpeg_test;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.io.File;

public class Trim2Activity extends AppCompatActivity implements View.OnClickListener {

    Uri uri;
    int duration;
    String filePrefix;
    String[] command;
    File dest;
    String originalPath;
    boolean isPlaying = false;

    VideoView videoView;
    ImageView btnControl;
    TextView tvLeft, tvRight;
    RangeSeekBar seekBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trim);

        seekBar = (RangeSeekBar) findViewById(R.id.seekbar);
        videoView = (VideoView) findViewById(R.id.videoView);
        tvLeft = (TextView) findViewById(R.id.tvLeft);
        tvRight = (TextView) findViewById(R.id.tvRight);
        btnControl = (ImageView) findViewById(R.id.btnControl);

        Intent intent = getIntent();
        if (intent != null) {
            String it = intent.getStringExtra("uri");
            uri = Uri.parse(it);
            isPlaying = true;
            videoView.setVideoURI(uri);
            videoView.start();
        }
        setOnclickEvents();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer it) {
                videoView.start();
                duration = it.getDuration() / 1000;
                tvLeft.setText("00:00:00");
                tvRight.setText(getTime(duration));
                it.setLooping(true);
                seekBar.setRangeValues(0, duration);
                seekBar.setSelectedMaxValue(duration);
                seekBar.setSelectedMinValue(0);
                seekBar.setEnabled(true);
                seekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
                    @Override
                    public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                        videoView.seekTo((int) minValue * 1000);

                        tvLeft.setText(getTime((int) bar.getSelectedMinValue()));
                        tvRight.setText(getTime((int) bar.getSelectedMaxValue()));
                    }
                });

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if (videoView.getCurrentPosition() > seekBar.getSelectedMaxValue().intValue() * 1000) {
                        videoView.seekTo(seekBar.getSelectedMinValue().intValue() * 1000);
                    }
                }, 1000);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.trim) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(Trim2Activity.this);
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(50, 0, 50, 100);
            final EditText input = new EditText(this);
            input.setLayoutParams(params);
            input.setGravity(Gravity.TOP | Gravity.START);
            input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            linearLayout.addView(input, params);
            alert.setView(linearLayout)
                    .setMessage("Set video name")
                    .setTitle("Change video name")
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            filePrefix = input.getText().toString();
                            trimVideo(seekBar.getSelectedMinValue().intValue() * 1000,
                                    seekBar.getSelectedMaxValue().intValue() * 1000, filePrefix);

                            Intent intent = new Intent(Trim2Activity.this, ProgressBarActivity.class);
                            intent.putExtra("duration", duration);
                            intent.putExtra("destination", dest.getAbsolutePath());
                            intent.putExtra("command", command);
                            startActivity(intent);
                            finish();
                            dialog.dismiss();
                        }
                    });
            alert.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void trimVideo(int start, int end, String filePrefix) {
        File folder = new File(Environment.getExternalStorageDirectory() + "/TrimVideos");
        if (!folder.exists()) {
            folder.mkdir();
        }
        String fileExt = ".mp4";
        dest = new File(folder, filePrefix + fileExt);
        originalPath = getRealPathFromUri(getApplicationContext(), uri);

        duration = (end - start) / 1000;
        command = new String[]{"-ss", "" + start / 1000, "-y", "-i", originalPath, "-t", "" + duration, "-vcodec", "mpeg4",
                "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", dest.getAbsolutePath()};
    }

    private String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(columnIndex);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    private void setOnclickEvents() {
        btnControl.setOnClickListener(this);
        videoView.setOnClickListener(this);
    }

    private String getTime(int duration) {
        int hr = duration / 3600;
        int rem = duration % 3600;
        int mn = rem / 60;
        int sec = rem % 60;
        return String.format("%02d", hr) + ":" + String.format("%02d", mn) + ":" + String.format("%02d", sec);
    }

    private void setupControl() {
        if (isPlaying) {
            btnControl.setImageResource(R.drawable.ic_play);
            videoView.pause();
            isPlaying = false;
        } else {
            btnControl.setImageResource(R.drawable.ic_pause);
            videoView.start();
            isPlaying = true;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnControl:
            case R.id.videoView:
                setupControl();
                break;
        }
    }
}
