package de.unima.ar.collector.database;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.unima.ar.collector.MainActivity;
import de.unima.ar.collector.R;
import de.unima.ar.collector.SensorDataCollectorService;
import de.unima.ar.collector.controller.SQLDBController;
import de.unima.ar.collector.sensors.AccelerometerSensorCollector;
import de.unima.ar.collector.shared.database.SQLTableName;
import de.unima.ar.collector.shared.util.DeviceID;
import de.unima.ar.collector.util.UIUtils;

public class DatabaseDelete extends AsyncTask<String, Void, Boolean>
{
    private Context context;
    private boolean isGlass = false;


    public DatabaseDelete(Context context) {
        this.context = context;
        this.isGlass = false;
    }

    public DatabaseDelete(Context context, boolean isGlass) {
        this.context = context;
        this.isGlass = isGlass;
    }


    @Override
    protected void onPreExecute()
    {
        showProgressBar();
    }


    @Override
    protected Boolean doInBackground(String... params)
    {
        String deviceID = DeviceID.get(SensorDataCollectorService.getInstance());
        String sqlTable = "SELECT * FROM " + SQLTableName.PREFIX + deviceID + SQLTableName.VIDEO + ";";
        List<String[]> resultSQL = SQLDBController.getInstance().query(sqlTable, null, false);

        boolean deleteVideo = true;

        for (String[] row: resultSQL) {
            Log.i("VIDEO_DATABASE_DELETE", Arrays.toString(row));
            File video = new File(row[1]);
            deleteVideo = deleteVideo && video.delete();
        }

        return SQLDBController.getInstance().deleteDatabase() && deleteVideo;
    }


    @Override
    protected void onPostExecute(final Boolean success)
    {
        if(success) {
            UIUtils.makeToast((Activity) context, R.string.option_export_delete_success, Toast.LENGTH_SHORT);
        } else {
            UIUtils.makeToast((Activity) context, R.string.option_export_delete_failed1, Toast.LENGTH_LONG);
        }
        if (this.isGlass) {
            MediaPlayer player = MediaPlayer.create(this.context, R.raw.doorbell);
            player.start();
        }
        hideProgressBar();
    }


    private void showProgressBar()
    {
        if(!isGlass) {
            ((Activity) context).runOnUiThread(new Runnable()
            {
                public void run()
                {
                    ((Activity) context).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    FrameLayout progressBarHolder = (FrameLayout) ((Activity) context).findViewById(R.id.progressBarHolder);
                    progressBarHolder.setVisibility(View.VISIBLE);
               }
            });
        }
    }


    private void hideProgressBar()
    {
        if(!isGlass) {
            ((Activity) context).runOnUiThread(new Runnable()
            {
                public void run()
                {
                    ((Activity) context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    FrameLayout progressBarHolder = (FrameLayout) ((Activity) context).findViewById(R.id.progressBarHolder);
                    progressBarHolder.setVisibility(View.GONE);
                }
            });
        }
    }
}