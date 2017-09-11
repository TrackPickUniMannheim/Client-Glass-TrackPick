package de.unima.ar.collector;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class Upload {

    public void upload(String fileName, String sourceFileUri) {

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "------------------------aaf9764291ba1fa4";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        File sourceFile = new File(sourceFileUri);
        if (!sourceFile.isFile()) {
            Log.e("VideoUpload", "Source File not found...");
            return;
        }

        try {
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            //URL url = new URL("https://posttestserver.com/post.php");
            URL url = new URL("http://192.168.0.129:8000/");
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("SSL_CIPHER", "ECDHE-RSA-AES256-GCM-SHA384");
            conn.setRequestProperty("Accept-Encoding", "identity");
            System.setProperty("http.keepAlive", "false");
            System.setProperty("HTTP_ACCEPT", "*/*");
            System.setProperty("HTTP_EXPECT", "100-continue");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            Log.i("HTTP-Response",Integer.toString(conn.getResponseCode()));
            fileInputStream.close();
            dos.flush();
            dos.close();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
