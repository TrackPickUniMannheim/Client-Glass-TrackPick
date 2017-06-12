package de.unima.ar.collector.database;


import android.app.Activity;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import de.unima.ar.collector.R;
import de.unima.ar.collector.SensorDataCollectorService;
import de.unima.ar.collector.controller.SQLDBController;
import de.unima.ar.collector.shared.database.SQLTableName;
import de.unima.ar.collector.shared.util.DeviceID;
import de.unima.ar.collector.util.UIUtils;

public class DatabaseExportSQL extends AsyncTask<String, Void, Boolean> implements MediaScannerConnection.OnScanCompletedListener
{
    private Context context;
    private AsyncTask<String, Void, Boolean> successfullExecute = null;


    public DatabaseExportSQL(Context context)
    {
        this.context = context;
    }

    public DatabaseExportSQL(Context context, AsyncTask<String, Void, Boolean> task) {
        this.context = context;
        this.successfullExecute = task;

    }

    @Override
    protected void onPreExecute()
    {
        showProgressBar();
    }


    @Override
    protected Boolean doInBackground(String... params)
    {
        // Make dirs on sd card
        File extStore = Environment.getExternalStorageDirectory();

        // sdcard does not exist
        if(extStore == null) {
            UIUtils.makeToast((Activity) context, R.string.option_export_nosdcardfound, Toast.LENGTH_LONG);
            return false;
        }

        // Unseren eigenen Ordner auf der SD Karte erstellen falls nicht vorhanden
        File root = new File("/mnt/ext_sdcard");
        if (!root.exists()) {
            root = new File(extStore.getAbsolutePath(), "SensorDataCollector");
        } else {
            root = new File(root.getAbsolutePath(), "SensorDataCollector");
        }
        boolean result = root.mkdir();
        if(!result && !root.exists()) {
            Log.i("Writing_Error", "Could not write");
            return false; // TODO
        }
        /*File root = new File(extStore.getAbsolutePath(), "SensorDataCollector");
        boolean result = root.mkdir();
        if(!result && !root.exists()) {
            UIUtils.makeToast((Activity) context, R.string.option_export_couldnotcreatefile, Toast.LENGTH_LONG);
            return false;
        } */

        // write data
        File source = new File(SQLDBController.getInstance().getPath());
        File target = new File(root, "db" + System.currentTimeMillis() + ".sqlite");
        boolean success = writeDataToDisk(source, target);

        String deviceID = DeviceID.get(SensorDataCollectorService.getInstance());
        String sqlTable = "SELECT * FROM " + SQLTableName.PREFIX + deviceID + SQLTableName.VIDEO + ";";
        List<String[]> resultSQL = SQLDBController.getInstance().query(sqlTable, null, false);

        for (String[] row: resultSQL) {
            Log.i("VIDEO_DATABASE", Arrays.toString(row));
            File video = new File(root, row[2] + ".mp4");
            writeDataToDisk(new File(row[1]), video);
            MediaScannerConnection.scanFile(context, new String[]{ video.getAbsolutePath() }, null, this);
        }

        if(!success) {
            return false;
        }

        // make new file discoverable
        MediaScannerConnection.scanFile(context, new String[]{ target.getAbsolutePath() }, null, this);

        return true;
    }


    @Override
    protected void onPostExecute(final Boolean success)
    {
        if(!success) {  // if success see method onScanCompleted
            hideProgressBar();
        }
    }


    @Override
    public void onScanCompleted(String path, Uri uri)
    {
        hideProgressBar();
        UIUtils.makeToast((Activity) context, R.string.option_export_copysuccessful, Toast.LENGTH_SHORT);
        if (this.successfullExecute != null) {
            this.successfullExecute.execute();
        }
    }


    private boolean writeDataToDisk(File source, File target)
    {
        Log.i("FILE_WRITE_SOURCE", source.getAbsolutePath());
        Log.i("FILE_WRITE_TARGET", target.getAbsolutePath());
        try {
            boolean success = target.createNewFile();

            /*if(!success) {
                UIUtils.makeToast((Activity) context, R.string.option_export_fileexists, Toast.LENGTH_LONG);
                return false;
            } */
        } catch(IOException e1) {
            UIUtils.makeToast((Activity) context, R.string.option_export_couldnotcreatefile, Toast.LENGTH_LONG);
            Log.w("FILE_WRITE", e1.toString());
            return false;
        }

        if(source == null || !source.exists()) {
            UIUtils.makeToast((Activity) context, R.string.option_export_filedoesnotexist, Toast.LENGTH_LONG);
            return false;
        }

        try {
            InputStream instream = new FileInputStream(source);
            OutputStream outstream = new FileOutputStream(target);

            byte[] buf = new byte[1024];
            int len;

            while((len = instream.read(buf)) > 0) {
                outstream.write(buf, 0, len);
            }

            instream.close();
            outstream.close();
        } catch(IOException e) {
            UIUtils.makeToast((Activity) context, R.string.option_export_errorwritingfile, Toast.LENGTH_LONG);
            return false;
        }

        return true;
    }


    private void showProgressBar()
    {
        if (this.successfullExecute == null) {
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
        if (this.successfullExecute == null) {
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