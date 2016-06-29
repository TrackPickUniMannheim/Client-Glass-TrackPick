package de.unima.ar.collector.sensors;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.hardware.Camera;
import android.util.Log;

import java.io.File;
import java.util.Timer;

import de.unima.ar.collector.SensorDataCollectorService;
import de.unima.ar.collector.controller.SQLDBController;
import de.unima.ar.collector.extended.Plotter;
import de.unima.ar.collector.shared.database.SQLTableName;
import de.unima.ar.collector.shared.util.DeviceID;

/**
 * Created by Alexander Diete on 03.06.16.
 */
public class VideoCollector extends CustomCollector {

    private static final int      type        = -4;
    private static final String[] valueNames  = new String[]{ "attr_db", "attr_time" };
    private static final long     videoLength = 5000;

    private Timer timer;
    private Context context;
    private Camera camera;
    private MediaRecorder recorder;
    private long currentStamp;
    private String path;

    public VideoCollector(Context context) {
        super();
        this.context = context;
    }

    @Override
    public void onRegistered() {
        currentStamp = System.currentTimeMillis();
        File f = new File(context.getFilesDir(), Long.toString(currentStamp));
        path = f.getAbsolutePath();

        Intent i = new Intent(context, CameraRecordingService.class);
        i.putExtra("target_video_file", f);

        context.startService(i);
    }

    @Override
    public void onDeRegistered() {
        context.stopService(new Intent(context, CameraRecordingService.class));
        ContentValues newValues = new ContentValues();
        newValues.put(valueNames[0], currentStamp);
        newValues.put(valueNames[1], path);
        String deviceID = DeviceID.get(SensorDataCollectorService.getInstance());
        writeDBStorage(deviceID, newValues);

        for (File f: context.getFilesDir().listFiles()) {
            Log.i("FILE_INFO", f.getAbsolutePath());
        }
    }

    public long getVideoLength() {
        return videoLength;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public Plotter getPlotter(String deviceID) {
        return null;
    }

    public static void createDBStorage(String deviceID)
    {
        String sqlTable = "CREATE TABLE IF NOT EXISTS " + SQLTableName.PREFIX + deviceID + SQLTableName.VIDEO + " (id INTEGER PRIMARY KEY, " + valueNames[1] + " INTEGER, " + valueNames[0] + " STRING)";
        SQLDBController.getInstance().execSQL(sqlTable);
    }


    public static void writeDBStorage(String deviceID, ContentValues newValues)
    {
        SQLDBController.getInstance().insert(SQLTableName.PREFIX + deviceID + SQLTableName.VIDEO, null, newValues);
    }
}
