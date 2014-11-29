package pt.up.fe.droidbeiro.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import pt.up.fe.droidbeiro.Protocol_API.*;


/**
 * Protocolo Specifications
 *
 *  -> Action 1 - The application receives a message and delivers it to the protocol.
 *  -> Action 2 - The protocol asks the application to send a message.
 *  -> Action 3 - The protocol unpacks a message and gives it to the application.
 *  -> Action 4 - The Android application tells the protocol that the GSM connection status has changed.
 *  -> Action 6 - The application asks the protocol to pack a message it wants
 *   to send.
 *
 *   Consult the System Design, seccion 7.2.8 for protocol message codification
 */


/**
 * Created by Francisco on 28/11/2014.
 */
public class ProtocolService extends Service {

    private Socket SocketPro;
    private Socket SocketApp;
    private ObjectInputStream AppIn;
    private ObjectInputStream ProIn;
    private ObjectOutputStream AppOut;
    private ObjectOutputStream ProOut;

    Protocol Protocol_GX;

    ServerSocket serverSocket;

    private final IBinder Protocol_Binder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        System.out.println("I am in Ibinder onBind_P method");
        return Protocol_Binder;
    }

    public class LocalBinder extends Binder {
        public ProtocolService getService() {
            System.out.println("I am in Localbinder ");
            return ProtocolService.this;
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

        super.onStartCommand(intent, flags, startId);
        System.out.println("I am in on start");
        //  Toast.makeText(this,"Service created ...", Toast.LENGTH_LONG).show();
        Runnable protocol = new startProtocol();
        new Thread(protocol).start();
        return START_STICKY;
    }

    class startProtocol implements Runnable{

        @Override
        public void run() {

            /*Create a server socket in order to establish connection with the protocol*/
            try {
                //0 means any available port
                serverSocket = new ServerSocket(0);
            } catch (IOException ex) {
                System.out.println("Could not find a free port!\n");
                return ;
            }

            try {
                Protocol_GX = new Protocol(false, serverSocket.getLocalPort(), (byte)0x00);
                Protocol_GX.execute();
            } catch (InterruptedException ex) {
                System.out.println("Thread stoped!");
            }

            /*Create socket SocketPro for the protocol's requests and the application's responses*/
            try {
                SocketPro = serverSocket.accept();
            } catch (IOException ex) {
                System.out.println("Application: Could not open SocketPro!\n");
                return;
            }

            /*Create socket SocketApp for the application's requests and the protocol's responses*/
            try {
                SocketApp = serverSocket.accept();
            } catch (IOException ex) {
                System.out.println("Application: Could not open SocketApp!\n");
                return;
            }

            /*Transform socket streams into ObjectStreams in order to send objects instead of bytes (Out's must always be created first and flushed!)*/
            try {
                AppOut = new ObjectOutputStream(SocketApp.getOutputStream());
                ProOut = new ObjectOutputStream(SocketPro.getOutputStream());
                AppOut.flush();
                ProOut.flush();
                AppOut.reset();
                ProOut.reset();
                AppIn = new ObjectInputStream(SocketApp.getInputStream());
                ProIn = new ObjectInputStream(SocketPro.getInputStream());

            } catch (IOException ex) {
                System.out.println("Could not create Object Streams!\n");
                return;
            }
        }
    }

    /**
     * Aplication receives a packet and asks the protocol to unpack it.
     * Aplication delivers message to the protocol
     *
     */
    public byte[] send_request_to_procol(byte id, byte spec, byte[] packet){

        //rqst object
        rqst request = new rqst(id, spec, packet);

        System.out.println("\nApplication asks protocol to unpack a message:");
        System.out.println("App: App requested id=" + request.id + " spec=" + request.spec + " packet=" + Arrays.toString(request.packet));

        //Send request to protocol
        try {
            AppOut.writeObject(request);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*Protocol replies (in this action the reply id is not relevant, nevertheless it must be read from the objectStream!)*/
        rspns response = null;
        try {
            response = (rspns)AppIn.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("App: protocol replied id=" + response.id);

        /*Now that the protocol has replied (with an ack) it can do what the
        application asked, which is to unpack the packet and then decide what
        to do with it.*/

        //The Application waits for the protocol's request
        try {
            request = (rqst)ProIn.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("App: Protocol requested id=" + request.id + " spec=" + request.spec + " packet=" + Arrays.toString(request.packet));

        /*
        * Deal with the request
        *
        * Packet is either sent trough GSM or RADIO
        *
        * Use flags
        * */
        byte[] protocol_response;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (request.spec==0x00){
            outputStream.write(0x00);
        }else
        if (request.spec==0x11) {
            outputStream.write(0x11);
        }
        try {
            outputStream.write(request.packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        protocol_response = outputStream.toByteArray();


        /*Application sends reply, which in this case is relevant, since
            there is a chance of something going wrong*/
        response = new rspns((byte)0x00); //Everything OK
        System.out.println("App: App replied id=" + response.id);
        try {
            ProOut.writeObject(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return protocol_response;
    }

    /**
     * Let's suposed that the operation ended (fire is out).
     * Now the application must stop the thread running the protocol
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            Protocol_GX.Stop();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }



}
