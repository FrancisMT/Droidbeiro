package pt.up.fe.droidbeiro.Communication;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

import pt.up.fe.droidbeiro.Logic.Packet;

public class ClientSocket extends Service {

    /**
     * Designação do ficheiro de configuracao do cliente
     */
    //final static private String Client_Config = "Client_Config.txt";

    private Socket cSocket = null;
    private ObjectOutputStream out = null;
    private ObjectInputStream in = null;

    private int isSocketAlive = 0;

    private static String SERVER_IP;//= "192.168.1.65";
    private static int SERVER_PORT;// = 4200;
    private static final int SERVER_TIMEOUT = 1000;

    private boolean dataToSend = false;
    private boolean dataToRead = false;
    private String dataSend = null;
    private String dataRead = null;

    private InetAddress serverAddr;
    private boolean ServerAlive = false;

    private final IBinder myBinder = new LocalBinder();

    public boolean isServerAlive(){
        return this.ServerAlive;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        System.out.println("I am in Ibinder onBind method");
        return myBinder;
    }

    public class LocalBinder extends Binder {
        public ClientSocket getService() {
            System.out.println("I am in Localbinder ");
            return ClientSocket.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("I am in on create");
    }

    public void IsBoundable() {
        Toast.makeText(this, "I bind like butter", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle extras = intent.getExtras();

        this.SERVER_IP= (String) extras.get("IP");
        this.SERVER_PORT= Integer.parseInt((String) extras.get("PORT"));

        super.onStartCommand(intent, flags, startId);
        System.out.println("I am in on start");
        //  Toast.makeText(this,"Service created ...", Toast.LENGTH_LONG).show();
        Runnable connect = new connectSocket();
        new Thread(connect).start();
        return START_STICKY;
    }

    public void sendMessage(Packet packet_to_send) {

        try {
            out.writeObject(packet_to_send);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*
        if (out != null) {
            System.out.println("in sendMessage: " + Arrays.toString(packet_to_send.packetContent));
            out.writeObject(packet_to_send);
        }*/
    }

    class connectSocket implements Runnable {
        @Override
        public void run() {
            try {
                serverAddr = InetAddress.getByName(SERVER_IP);
                Log.e("TCP Client", "C: Connecting...");
                //create a socket to make the connection with the server
                cSocket = new Socket(serverAddr, SERVER_PORT);

                try {
                    //send the message to the server

                    out = new ObjectOutputStream(cSocket.getOutputStream());
                    Log.e("TCP Client", "C: Sent.");
                    Log.e("TCP Client", "C: Done.");
                } catch (Exception e) {
                    Log.e("TCP", "S: Error", e);
                }
            } catch (Exception e) {
                Log.e("TCP", "C: Error", e);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            cSocket.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        cSocket = null;
    }

}