package pt.up.fe.droidbeiro.Communication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import androidBackendAPI.Packet;
import protocolapi.rqst;
import protocolapi.rspns;
import pt.up.fe.droidbeiro.R;
import pt.up.fe.droidbeiro.Service.BLE.SerialPortService;

public class ProtocolRequestProcessing extends Service {

    private static Client_Socket CS = null;

    private static ProtocolRequestProcessing instance = null;

    /**
     * Protocol Data
     */
    //[APP -> PRO] Responses
    private ObjectInputStream socketAppResp;

    //[APP -> PRO] Requests
    private ObjectOutputStream socketAppReq;

    //[PRO -> APP] Responses
    private ObjectOutputStream socketProResp;

    //[PRO -> APP] Requests
    private ObjectInputStream socketProReq;

    //Socket information
    protected ServerSocket serverSocket;
    private Socket socketApp;
    private Socket socketPro;
    public int portaSocket;

    private final IBinder myBinder = new LocalBinder_PRC();


    public ProtocolRequestProcessing() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        System.out.println("I am in Ibinder onBind method");
        return myBinder;
    }

    public class LocalBinder_PRC extends Binder {
        public ProtocolRequestProcessing getService() {
            System.out.println("I am in Localbinder ");
            return ProtocolRequestProcessing.this;
        }
    }

    public void IsBoundable() {
        Toast.makeText(this, "I bind like butter", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCreate() {

        Log.e("On Service", "ProtocolRequestProcessing Service");

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("I'm in:", "protocol ProtocolRequestProcessing service start");
        ConnectionData currentCD = ConnectionData.getInstance();


        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        portaSocket = serverSocket.getLocalPort();
        currentCD.setPortaSocket(portaSocket);

        Log.e("ProtocoloCommunication", "PortaSocket=" + portaSocket);


        Client_Socket CS = Client_Socket.getInstance();

        Runnable PC = new ProtocoloCommunication();
        new Thread(PC).start();


        CS.getInstance();

        return START_STICKY;
    }

    public class ProtocoloCommunication implements Runnable{

        @Override
        public void run() {
            Log.e("I'm in:", "protocol communication run");

            //Initialization of communication streams
            try
            {
                socketPro = serverSocket.accept();
                socketApp = serverSocket.accept();
                Log.e(" Protocol:", "socket accept");
                CS.socket_accepted=true;

                //Create the streams
                socketAppReq = new ObjectOutputStream(socketApp.getOutputStream());
                socketAppResp = new ObjectInputStream(socketApp.getInputStream());
                socketProResp = new ObjectOutputStream(socketPro.getOutputStream());
                socketProReq = new ObjectInputStream(socketPro.getInputStream());

                //Flush streams
                socketAppReq.flush();
                socketAppReq.reset();
                socketProResp.flush();
                socketProResp.reset();

                System.out.println("Connected to Socket " + portaSocket + " successfully");
            }

            catch (IOException e)
            {
                throw new Error("ERROR: Could not open sockets to the Protocol.");
            }

            //Handler
            while (true)
            {
                //Read Requests
                get_From_Protocol();
            }
        }
    }

    /**
     * Send request to the protocol
     * @param request
     */
    public void send_To_Protocol(rqst request)
    {
        try
        {
            if (request!=null){
                Log.e("Request status:" , "not null");
            }


            if (socketAppReq == null){

                Log.e("socketAppReq :" , "is null");

            }

            socketAppReq.writeObject(request);

            // Check Protocol's response
            rspns response = new rspns(socketAppResp.readObject());

            if (response.id != ProtCommConst.RSPN_ACTION_APP_OK){

                System.out.println("\n>> ERROR: Response from the protocol was not OK. Error Code: " + response.id + " " + request.id + " " + request.spec);
            }else{
                System.out.println("\n>> Response from the protocol was OK: " + response.id + " " + request.id + " " + request.spec);
            }

        }

        catch (SocketTimeoutException e)
        {
            throw new Error("ERROR: SocketTimeoutException reading from socketAppResp");
        }

        catch(IOException | ClassNotFoundException e)
        {
            throw new Error("ERROR: Writing request to the protocol instance.");
        }
    }

    /**
     * Method to retrieve requests from the protocol and write them to the request_buffer
     */
    public void get_From_Protocol()
    {
        rqst response;

        try
        {
            response = new rqst(socketProReq.readObject());

            Log.e("PROTOCOL_DEBUG::","Read request from the protocol.");

            //Reply OK to Protocol //CHECK GSM
            socketProResp.writeObject(new rspns(ProtCommConst.RSPN_ACTION_APP_OK));

            //Write to buffer
            //request_buffer.offer(request);
            /**********************************************************************************************************************/

            //response = request_buffer.poll();

            //The protocol asks the application to send a message
            if (response.id == ProtCommConst.RQST_ACTION_PRO_SEND_PACKET){

                Log.e("PROTOCOL_DEBUG::","The protocol asks the application to send a message");

                //Send to Backend through GSM
                if (response.spec == ProtCommConst.RQST_SPEC_ANDR_GSM){

                    Log.e("PROTOCOL_DEBUG::","The protocol asks the application to send a message through GSM");

                    Packet pck_to_send = new Packet();
                    pck_to_send.hasProtocolHeader=true;
                    pck_to_send.packetContent=response.packet;

                    CS.send_packet_GSM(pck_to_send);

                }

                //Send to Backend through RADIO
                else if (response.spec == ProtCommConst.RQST_SPEC_ANDR_RADIO){
                    //Creat request to Foward to BLUETOOTH


                  /*
                  Log.e("PROTOCOL_DEBUG::","The protocol asks the application to send a message through the RADIO");
                  BLESimulatorConnection BLESC = BLESimulatorConnection.getInstance();

                  //Write to HW Socket
                  try {
                      BLESC.sendDatagramThroughNetwork(response.packet);
                  } catch (IOException e) {
                      e.printStackTrace();
                  }
                  */

                    CS.broadcastUpdate(SerialPortService.BROADCAST_ACTION_WRITE, new String(response.packet));


                }

                else{
                    Log.e("Error:","request.spec is invalid");
                }
            }

            //The protocol unpacks a message and gives it to the application
            else if (response.id == ProtCommConst.RQST_ACTION_PRO_UNPACK_MSG){

                Log.e("PROTOCOL_DEBUG::","The protocol unpacks a message and gives it to the application");

                Packet pck_received = new Packet();
                pck_received.packetContent=response.packet;

                //CS.process_local_info(pck_received);


            }



            /**********************************************************************************************************************/
        }

        catch (SocketTimeoutException e){

        }

        catch (IOException | ClassNotFoundException e)
        {
            throw new Error("ERROR: Writing the response to the Protocol:" + e);
        }
    }


    /**
     * Warn if GSM is on/off
     * @param gsmOn
     */
    public void setGSM(boolean gsmOn)
    {

        rqst request;

        if (gsmOn == true)
        {
            request = new rqst(ProtCommConst.RQST_ACTION_APP_GSM_CHANGE,
                    ProtCommConst.RQST_SPEC_ANDR_GSM_GAINED);
        }
        else
        {
            request = new rqst(ProtCommConst.RQST_ACTION_APP_GSM_CHANGE,
                    ProtCommConst.RQST_SPEC_ANDR_GSM_LOST);
        }


        // Send request and check response
        try
        {
            socketAppReq.writeObject(request);

            // Check if response is OK
            rspns response = new rspns(socketAppResp.readObject());

            if (response.id != ProtCommConst.RSPN_ACTION_APP_OK)
            {
                System.out.println("\n>> ERROR: (GSM_CHANGED) Response from the protocol was not OK.");
            }else{
                System.out.println("\n>> Response from the protocol was OK: " + response.id + " " + request.id + " " + request.spec);
            }

        }
        catch (SocketTimeoutException e)
        {
        }
        catch (IOException | ClassNotFoundException e)
        {
            throw new Error("ERROR: Inserting new request (GSM_CHANGE) in the socketAppReq.");
        }
    }


    public static ProtocolRequestProcessing getInstance() {
        if(instance == null) {
            instance = new ProtocolRequestProcessing();
        }
        return instance;
    }



}
