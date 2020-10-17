package com.takahashi.dashcam;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.hbisoft.hbrecorder.HBRecorder;
import com.hbisoft.hbrecorder.HBRecorderListener;
import com.otaliastudios.cameraview.CameraView;
import com.takahashi.dashcam.services.GpsTracker;
import com.takahashi.dashcam.utils.GeoDistance;
import com.takahashi.dashcam.utils.db.DatabaseClient;
import com.takahashi.dashcam.utils.models.VideoMetaDataModel;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements HBRecorderListener {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);

        camera = findViewById(R.id.camera);
        camera.setLifecycleOwner(this);

        hbRecorder = new HBRecorder(this, this);
        hbRecorder.isAudioEnabled(false);

        mToggleButton = (ToggleButton) findViewById(R.id.toggle);
        mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startRecordingScreen();
                    startTime = new Date();
                } else {
                    hbRecorder.stopScreenRecording();
                    endTime = new Date();
                    long diff = startTime.getTime() - endTime.getTime();

                    long diffSeconds = diff / 1000 % 60;
                    long diffMinutes = diff / (60 * 1000) % 60;
                    long diffHours = diff / (60 * 60 * 1000) % 24;

                    String fileName = hbRecorder.getFileName();
                    String filePath = hbRecorder.getFilePath();
                    String time = diffHours + ":" + diffMinutes + ":" + diffSeconds;

                    VideoMetaDataModel model = new VideoMetaDataModel(distanceCovered, fileName, time);
                    model.setFilePath(filePath);

                    saveVideoMetaData(model);
                }
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), VideoListActivity.class));
            }
        });

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        getLocation();


        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!this.isInterrupted()) {
                        Thread.sleep(10000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getLocation();
                            }
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        thread.start();
    }

    public void getLocation() {
        gpsTracker = new GpsTracker(MainActivity.this);
        if (gpsTracker.canGetLocation()) {
            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();
            webView.loadUrl("https://idrit.github.io/map-profile/?lat=" + latitude + "&lng=" + longitude);
            if (!recStarted) {
                pLat = latitude;
                pLong = longitude;
            } else {
                distanceCovered += GeoDistance.distance(pLat, pLong, latitude, longitude);
                pLat = latitude;
                pLong = longitude;
            }
        } else {
            gpsTracker.showSettingsAlert();
        }
    }

    private void startRecordingScreen() {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent permissionIntent = mediaProjectionManager != null ? mediaProjectionManager.createScreenCaptureIntent() : null;
        startActivityForResult(permissionIntent, SCREEN_RECORD_REQUEST_CODE);
    }

    private void saveVideoMetaData(final VideoMetaDataModel model) {

        @SuppressLint("StaticFieldLeak")
        class SaveTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                //adding to database
                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase()
                        .videoDataAccessObject()
                        .insert(model);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                finish();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
//                Toast.makeText(getApplicationContext(), "Saved in DB", Toast.LENGTH_LONG).show();
            }
        }

        SaveTask st = new SaveTask();
        st.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCREEN_RECORD_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //Start screen recording
                hbRecorder.startScreenRecording(data, resultCode, MainActivity.this);
            }
        }
    }

    @Override
    public void HBRecorderOnStart() { }

    @Override
    public void HBRecorderOnComplete() {
//        Toast.makeText(this, "video saved", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void HBRecorderOnError(int errorCode, String reason) {
        Log.d("HBR", "HBRecorderOnError: " + reason);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit? Dash Recording will stop!")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        hbRecorder.stopScreenRecording();
                        MainActivity.super.onBackPressed();
                    }
                }).create().show();
    }

    HBRecorder hbRecorder;

    private final int SCREEN_RECORD_REQUEST_CODE = 2460;

    GpsTracker gpsTracker;

    WebView webView;
    CameraView camera;
    ToggleButton mToggleButton;

    Date startTime;
    Date endTime;

    double distanceCovered = 0.0;
    double pLat;
    double pLong;
    boolean recStarted = false;
}