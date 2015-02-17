package pt.up.fe.droidbeiro.Communication;

/**
 * Created by francisco on 17/02/15.
 */
public class ProtCommConst {

    //Requests
    //[APP] Packet received by the application.
    public static final byte RQST_ACTION_APP_PACKET_RECEIVED = (byte) 0x00;

    //[PRO] Send packet to output interface.
    public static final byte RQST_ACTION_PRO_SEND_PACKET = (byte) 0x11;

    //[PRO] Unpacks a message and gives it to the APP
    public static final byte RQST_ACTION_PRO_UNPACK_MSG = (byte) 0x22;

    //[APP] GMS status changed */
    public static final byte RQST_ACTION_APP_GSM_CHANGE = (byte) 0x33;

    //[APP] Socket connection is lost
    public static final byte RQST_ACTION_APP_SOCKET_LOST = (byte) 0x44;

    //[APP] Gives the protocol the message to Pack
    public static final byte RQST_ACTION_APP_PACK_MSG = (byte) 0x55;



    //Other Requests
    //Send message through GSM
    public static final byte RQST_SPEC_ANDR_GSM = (byte) 0x00;

    //Send message through RADIO
    public static final byte RQST_SPEC_ANDR_RADIO = (byte) 0x11;

    //GSM is available
    public static final byte RQST_SPEC_ANDR_GSM_GAINED = (byte) 0x00;

    //GSM isn't available
    public static final byte RQST_SPEC_ANDR_GSM_LOST = (byte) 0x11;



    //GSM connection lost
    public static final byte RSPN_SPEC_GSM_LOST = (byte) 0xFF;

    //Socket connection lost
    public static final byte RSPN_SPEC_SOCKET_LOST = (byte) 0xEE;

    //Generic error
    public static final byte RSPN_GEN_ERROR = (byte) 0xDD;


    //Response OK
    public static final byte RSPN_ACTION_APP_OK = (byte) 0x00;


    //Request and response are null
    public static final byte RQST_NULL = (byte) 0x00;

}
