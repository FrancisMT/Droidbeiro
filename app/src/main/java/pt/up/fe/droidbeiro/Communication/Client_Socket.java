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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client_Socket extends Service {

    /**
     * Designação do ficheiro de configuracao do cliente
     */
    //final static private String Client_Config = "Client_Config.txt";

    private Socket cSocket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;

    private int isSocketAlive = 0;

    private static String SERVER_IP;//= "192.168.1.65";
    private static int SERVER_PORT;// = 4200;
    private static final int SERVER_TIMEOUT = 1000;

    private boolean dataToSend = false;
    private boolean dataToRead = false;
    private String dataSend = null;
    private String dataRead = null;

    private InetAddress serverAddr;

    private final IBinder myBinder = new LocalBinder();

    public void setSERVER_config(String IP, int PORT){
        this.SERVER_IP=IP;
        this.SERVER_PORT=PORT;
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
        /*try {
            read_client_config_data();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

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

    public void sendMessage(String message) {
        if (out != null && !out.checkError()) {
            System.out.println("in sendMessage" + message);
            out.println(message);
            out.flush();
        }
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
                    
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(cSocket.getOutputStream())), true);
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

    /**
     * Obtenção da informação necessária à configuração do cliente:
     * o nome do Servidor;
     * o número da porta a ser utilizada.
     *
     * @throws java.io.FileNotFoundException
     */
    public void read_client_config_data() throws IOException {

        String r = null;
        String[] res = null;

        //Find the directory for the SD Card using the API
        File sdCard = Environment.getExternalStorageDirectory();
        //Get the text file
        File file = new File(sdCard, "Droidbeiro_config/Client_Config.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));

        try {
            r=br.readLine();
            res=r.split(">");
        }catch (IOException ex){
            System.err.println("Server: erro a ler linha do ficheiro de configuração: " + ex);
        }

        SERVER_IP=res[0];
        SERVER_PORT= Integer.parseInt(res[1]);
    }
}