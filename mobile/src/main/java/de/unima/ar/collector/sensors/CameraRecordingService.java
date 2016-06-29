package de.unima.ar.collector.sensors;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;

import de.unima.ar.collector.controller.ActivityController;

/**
 * Created by Alexander Diete on 08.06.16.
 *
 * https://github.com/columbia/helios_android/blob/master/src/com/Helios/BackgroundVideoRecorder.java
 */
public class CameraRecordingService extends Service implements SurfaceHolder.Callback {

    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private static Camera camera;
    private File targetFile;
    private String parent;
    private WindowManager manager;
    private MediaRecorder recorder;
    private SurfaceTexture texture = new SurfaceTexture(10);

    @Override
    public void onCreate() {
        camera = Camera.open();
        manager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        surfaceView = new SurfaceView(this);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(1,1,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT | Gravity.TOP;
        manager.addView(surfaceView, params);
        surfaceView.getHolder().addCallback(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        recorder = new MediaRecorder();
        targetFile = (File) intent.getExtras().get("target_video_file");
        return 0;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        try {
            stopRecording();
        } catch (IOException e) {
            Log.w("VIDEO_RECORDING_ERROR", e.toString());
            e.printStackTrace();
        }
        super.onDestroy();
    }

    public void startRecording(File target) throws IOException {
        long timestamp = System.currentTimeMillis();
        camera.unlock();

        this.recorder.setCamera(camera);
        this.recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        this.recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        this.recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        this.recorder.setOutputFile(target.getAbsolutePath());
        this.recorder.setVideoSize(640,480);
        this.recorder.setPreviewDisplay(this.holder.getSurface());
        this.recorder.prepare();
        this.recorder.start();
    }

    public void stopRecording() throws IOException {
        this.recorder.stop();
        this.recorder.release();
        camera.lock();
        camera.release();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.holder = holder;
        try {
            startRecording(targetFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
