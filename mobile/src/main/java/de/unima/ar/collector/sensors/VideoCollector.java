package de.unima.ar.collector.sensors;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.PixelFormat;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import de.unima.ar.collector.R;
import de.unima.ar.collector.Upload;
import de.unima.ar.collector.controller.ActivityController;
import de.unima.ar.collector.controller.SQLDBController;
import de.unima.ar.collector.extended.Plotter;
import de.unima.ar.collector.shared.database.SQLTableName;
import de.unima.ar.collector.util.FileUtil;
import de.unima.ar.collector.util.UIUtils;

/**
 * @author Timo Sztyler
 */
public class VideoCollector extends CustomCollector implements SurfaceHolder.Callback, MediaScannerConnection.OnScanCompletedListener
{
    private static final int      type       = -4;
    private static final String[] valueNames = new String[]{ "attr_video", "attr_time" };

    private MediaRecorder recorder = null;
    private WindowManager windowManager;
    private SurfaceView   surfaceView;
    private long          startTime;
    private String        path;

    private static String videoURL;


    @Override
    public void onRegistered()
    {
        this.startTime = System.currentTimeMillis();
        Activity main = ActivityController.getInstance().get("MainActivity");


        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, PixelFormat.TRANSLUCENT);
        layoutParams.gravity = Gravity.START | Gravity.TOP;

        this.windowManager = (WindowManager) main.getSystemService(Context.WINDOW_SERVICE);
        this.surfaceView = new SurfaceView(main);

        this.windowManager.addView(this.surfaceView, layoutParams);
        this.surfaceView.getHolder().addCallback(this);

        Log.i("Video", "Video registered");
    }


    @Override
    public void onDeRegistered()
    {

        this.recorder.stop();
        this.recorder.reset();
        this.recorder.release();

        this.windowManager.removeView(this.surfaceView);
        File extStore = Environment.getExternalStorageDirectory();
        File root = new File("/mnt/ext_sdcard");
        if (!root.exists()) {
            root = new File(extStore.getAbsolutePath(), "SensorDataCollector");
        } else {
            root = new File(root.getAbsolutePath(), "SensorDataCollector");
        }
        File video = new File(root.getAbsolutePath(), "video_" + startTime + ".mp4");
        MediaScannerConnection.scanFile(ActivityController.getInstance().get("MainActivity"), new String[]{ video.getAbsolutePath() }, null, this);
        Log.i("Video", "Video deregistered and stored to: " + root.getAbsolutePath()+ "/video_" + startTime + ".mp4");

        this.videoURL = (root.getAbsolutePath()+ "/video_" + startTime + ".mp4");
        uploadVideo();
    }




    @Override
    public int getType()
    {
        return type;
    }

    @Override
    public void onScanCompleted(String path, Uri pat) {
        Log.i("Video", "Video scanned");
    }


    @Override
    public Plotter getPlotter(String deviceID)
    {
        return null;
    }


    public static void createDBStorage(String deviceID)
    {
        String sqlTable = "CREATE TABLE IF NOT EXISTS " + SQLTableName.PREFIX + deviceID + SQLTableName.VIDEO + " (id INTEGER PRIMARY KEY, " + valueNames[1] + " INTEGER, " + valueNames[0] + " BLOB)";
        SQLDBController.getInstance().execSQL(sqlTable);
    }


    public static void writeDBStorage(String deviceID, ContentValues newValues)
    {
        //String deviceID = DeviceID.get(SensorDataCollectorService.getInstance());
        // TODO
    }



    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder)
    {
        // init recorder
        this.recorder = new MediaRecorder();
        this.recorder.setPreviewDisplay(surfaceHolder.getSurface());
        this.recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        this.recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        this.recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        this.recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        this.recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        this.recorder.setVideoSize(1920, 1080);
        this.recorder.setMaxDuration(0); // 0 seconds = unlimit
        this.recorder.setMaxFileSize(0); // 0 = unlimit


        // set output path
        // TODO: External SD card storage & behaviour when card is full
        File extStore = Environment.getExternalStorageDirectory();
        File root = new File("/mnt/ext_sdcard");
        if (!root.exists()) {
            root = new File(extStore.getAbsolutePath(), "SensorDataCollector");
        } else {
            root = new File(root.getAbsolutePath(), "SensorDataCollector");
        }
        boolean result = root.mkdir();
        if(!result && !root.exists()) {
            Log.i("Writing_Error", "Could not write");
            return; // TODO
        }
        File output = new File(root.getAbsolutePath(), "video_" + startTime + ".mp4");
        this.recorder.setOutputFile(output.getAbsolutePath());
        this.path = output.getAbsolutePath();

        for(String s: FileUtil.getExternalMounts()) {
            Log.i("EXTERNAL STORAGE", s);
        }
        // start
        try {
            this.recorder.prepare();
        } catch(Exception e) {
            this.recorder.reset();
            e.printStackTrace();
        }

        this.recorder.start();
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        // nothing to do
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        // nothing to do
    }

    private void uploadVideo() {
        class UploadVideo extends AsyncTask<Void, Void, String> {

            @Override
            protected String doInBackground(Void... params) {
                Upload u = new Upload();
                String msg = u.upLoad2Server(VideoCollector.videoURL);
                return msg;
            }
        }
        UploadVideo uv = new UploadVideo();
        uv.execute();
    }
}