package pt.up.fe.droidbeiro.Communication;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import pt.up.fe.droidbeiro.Logic.Packet;

public class Client_Socket extends Service {

    /**
     * Designação do ficheiro de configuracao do cliente
     */
    //final static private String Client_Config = "Client_Config.txt";

    private Socket cSocket = null;
    //private PrintWriter out = null;
    //private BufferedReader in = null;
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
        public Client_Socket getService() {
            System.out.println("I am in Localbinder ");
            return Client_Socket.this;
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

   /* public void sendMessage(String message) {
        if (out != null && !out.checkError()) {
            System.out.println("in sendMessage" + message);
            out.println(message);
            out.flush();
        }
    }*/

   /* public void sendMessage(Packet packet_to_send) throws IOException {

        out.writeObject(packet_to_send);
        out.flush();
    }*/

    /**
     * Justo to test writeObjet
     * @param message_to_send
     * @throws IOException
     */
    public void sendMessage(String message_to_send) throws IOException {

        out.writeObject(message_to_send);
        out.flush();
    }

    public void getMessage() throws IOException, ClassNotFoundException {

        String message = (String) in.readObject();
        System.out.println("Message: " + message);
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
                    
                   // out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(cSocket.getOutputStream())), true);
                    out = new ObjectOutputStream(cSocket.getOutputStream());
                    //out.writeObject("Hi");
                    in = new ObjectInputStream(cSocket.getInputStream());
                    //String message = (String) in.readObject();
                    //System.out.println("Message: " + message);

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