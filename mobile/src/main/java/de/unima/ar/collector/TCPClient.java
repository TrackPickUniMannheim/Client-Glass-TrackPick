package de.unima.ar.collector; /**
 * Created by Nancy Kunath on 03.10.2017.
 */
import android.util.Log;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

import de.unima.ar.collector.shared.Settings;

public class TCPClient {

    public static String SERVER_IP = "192.168.192.49";
    public static int SERVER_PORT = 9999;
    private String mServerMessage;
    private boolean mRun = false;
    private PrintWriter mBufferOut;
    private BufferedReader mBufferIn;
    //private static TCPClient obj = new TCPClient();
    private static Integer counter = 0;

    public TCPClient() {

    }

    /*public static TCPClient getInstance(){
        return obj;
    }

    public void register(){
        counter++;
        if(counter == 1){
            run();
        }
    }

    public void deregister(){
        counter--;
        if(counter == 0){
            stopClient();
        }
    }*/

    public boolean getMRun(){
        return mRun;
    }

    public void sendMessage(String message) {
        if (mBufferOut != null && !mBufferOut.checkError()) {
            mBufferOut.println(message);
            mBufferOut.flush();
        }
    }

    public void stopClient() {
        TCPClient.counter--;
        if(counter == 0){
            sendMessage("Disconnect");
        }

        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }

    public void run() {
        //SERVER_IP = Settings.SERVER_IP;
        //SERVER_PORT = Settings.SERVER_PORT;

        mRun = true;

        try {
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
            Log.i("TCP Client", "C: Connecting..." + serverAddr.toString());

            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, SERVER_PORT);

            Log.i("TCP Client",socket.toString());

            try {

                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                if(TCPClient.counter == 0){
                    sendMessage("Connect");
                }

                TCPClient.counter++;

                //listens for messages from server
                while (mRun) {

                    mServerMessage = mBufferIn.readLine();

                    if (mServerMessage != null ) {
                        //do nothing
                    }
                }

            } catch (Exception e) {

                Log.i("TCP","Socket closed! (2)");

            } finally {
                socket.close();
            }

        } catch (Exception e) {

            Log.i("TCP","Socket closed! (1)");

        }

    }
}