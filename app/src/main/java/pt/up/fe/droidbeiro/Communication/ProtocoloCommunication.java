package pt.up.fe.droidbeiro.Communication;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentLinkedQueue;

import protocolapi.rqst;
import protocolapi.rspns;

public class ProtocoloCommunication extends Service {

    //[APP -> PRO] Responses
    private ObjectInputStream socketAppResp;

    //[APP -> PRO] Requests
    private ObjectOutputStream socketAppReq;

    //[PRO -> APP] Responses
    private ObjectOutputStream socketProResp;

    //[PRO -> APP] Requests
    private ObjectInputStream socketProReq;

    protected ServerSocket serverSocket;
    private Socket socketApp;
    private Socket socketPro;
    public int portaSocket;

    /**
     * Protocol Requests Buffer
     */
    public ConcurrentLinkedQueue<rqst> request_buffer = new ConcurrentLinkedQueue<>();

    public ProtocoloCommunication() {
    }

    @Override
    public void onCreate() {

        Log.e("On Service", "Protocol Communication");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("I'm in:", "protocol communication start");

        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        portaSocket=serverSocket.getLocalPort();
        Log.e("ProtocoloCommunication", "PortaSocket="+portaSocket);

        Runnable run_prot_comm = new run_protocol_communication();
        new Thread(run_prot_comm).start();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class run_protocol_communication implements Runnable{

        @Override
        public void run() {


            Log.e("I'm in:", "protocol communication run");

            //Initialization of communication streams
            try
            {
                socketPro = serverSocket.accept();
                socketApp = serverSocket.accept();
                Log.e(" Protocol:", "socket accept");


                socketPro.setSoTimeout(500); //In milliseconds

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


            socketAppReq.writeObject(request);

            // Check Protocol's response
            rspns response = new rspns(socketAppResp.readObject());

            if (response.id != ProtCommConst.RSPN_ACTION_APP_OK){

                System.out.println("\n>> ERROR: Response from the protocol was not OK. Error Code: " + response.id + " " + request.id + " " + request.spec);
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
     * Method to retrieve requests from the protocol instance and write them to the medium's buffer.
     */
    public void get_From_Protocol()
    {
        rqst request;

        try
        {
            request = new rqst(socketProReq.readObject());

            //Write to buffer
            request_buffer.offer(request);

            //Reply OK to Protocol
            socketProResp.writeObject(new rspns(ProtCommConst.RSPN_ACTION_APP_OK));
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

}
