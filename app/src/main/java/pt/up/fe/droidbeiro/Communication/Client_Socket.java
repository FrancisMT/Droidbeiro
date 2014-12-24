package pt.up.fe.droidbeiro.Communication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import pt.up.fe.droidbeiro.R;
import pt.up.fe.droidbeiro.androidBackendAPI.Packet;

public class Client_Socket extends Service{

    private final static byte prelogin_msg_type = (byte)0x84;
    private final static byte cc_predefined_msg_type = (byte)0x128;
    private final static byte cc_personalised_msg_type = (byte)0x129;
    private final static byte cc_requests_fl_update_msg_type = (byte)0x130;
    private final static byte cc_sends_team_info_msg_type = (byte)0x131;
    private final static byte cc_sends_ff_id_msg_type = (byte)0x132;
    private final static byte cc_denies_login_msg_type = (byte)0x133;
    private final static byte cc_accepts_login_msg_type = (byte)0x134;
    private final static byte cc_requests_movetogps_msg_type = (byte)0x135;

    public static byte Firefighter_ID;

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
    private boolean running = false;

    final static String ACTION = "NotifyServiceAction";
    final static String STOP_SERVICE = "";
    final static int RQS_STOP_SERVICE = 1;

    private static final int MY_NOTIFICATION_ID=1;
    private NotificationManager notificationManager;
    private Notification myNotification;

    private final IBinder myBinder = new LocalBinder();

    /**
     * Just for initial tests
     */
    private String msgToServer = null;
    public String response = "";

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

    /**
     * Justo to test writeObjet
     * @param message_to_send
     * @throws IOException
     */
    public void sendMessage(String message_to_send) throws IOException {
        out.writeObject(message_to_send);
        //out.println(message_to_send);
        out.flush();
    }

    public void send_packet(Packet pck_to_send) throws IOException {
        out.writeObject(pck_to_send);
        //out.println(message_to_send);
        out.flush();
    }

    public String getMessage(){
        return this.response;
    }

    public  byte getFirefighter_ID(){
        return this.Firefighter_ID;
    }

    public class connectSocket implements Runnable {
        @Override
        public void run() {
            try {
                serverAddr = InetAddress.getByName(SERVER_IP);
                Log.e("TCP Client", "C: Connecting...");
                //create a socket to make the connection with the server
                cSocket = new Socket(serverAddr, SERVER_PORT);
                running = true;

                if (cSocket.isConnected()) {
                    Log.e("TCP Client", "C: Connected!");

                    //out = new PrintWriter(cSocket.getOutputStream(), true);
                    //in = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));
                    out = new ObjectOutputStream(cSocket.getOutputStream());
                    in = new ObjectInputStream(cSocket.getInputStream());

                    while(running){
                        //response = (String) in.readLine();
                        /*response = (String) in.readObject();
                        Log.d("response", response);*/
                        Packet pck_received = (Packet) in.readObject();

                        response = pck_received.getMessage().toString();

                        switch(pck_received.getMessageType()){

                            case prelogin_msg_type:
                                Firefighter_ID=pck_received.getFirefighterID();
                                break;

                            default:
                                break;
                        }

                        if(response!=null) {
                            Log.d("response", response);

                            // Send Notification
                            notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                            myNotification = new Notification(R.drawable.droidbeiro_app_icon,"Nova Mensagem",System.currentTimeMillis());
                            String notificationTitle = "Nova mensagem";
                            String notificationText = response;
                            Intent myIntent = new Intent(Intent.ACTION_VIEW);

                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0, new Intent(),PendingIntent.FLAG_UPDATE_CURRENT);
                            myNotification.defaults |= Notification.DEFAULT_SOUND;
                            myNotification.flags |= Notification.FLAG_AUTO_CANCEL;
                            myNotification.setLatestEventInfo(getApplicationContext(),notificationTitle,notificationText,pendingIntent);
                            notificationManager.notify(MY_NOTIFICATION_ID, myNotification);
                        }
                    }
                } else {

                    Log.w("CLientSocket", "SOCKET IS NOT CONNECTED::::" + isSocketAlive);
                    throw new UnknownHostException();
                }

            } catch (StreamCorruptedException e1) {
                e1.printStackTrace();
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnect() throws IOException {

        this.running=false;

        try {
            cSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
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
        try {
            cSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cSocket = null;
    }
}