package pt.up.fe.droidbeiro.Communication;

/**
 * Created by francisco on 20/02/15.
 */
public class ConnectionData {

    private static ConnectionData instance = null;

    /**
     * Backend
     */
    public static String SERVER_IP;
    public static int SERVER_PORT;
    public static boolean PROTOCOLG5;
    public static boolean PROTOCOLG6;

    /**
     * Protocol
     */
    public static byte system_id;
    public static int portaSocket;

    /**
     * GPS Data
     */
    public static float new_LAT;
    public static float new_LON;



    public ConnectionData(){

    }

    public static void setPROTOCOLG5(boolean PROTOCOLG5) {
        ConnectionData.PROTOCOLG5 = PROTOCOLG5;
    }

    public static void setPROTOCOLG6(boolean PROTOCOLG6) {
        ConnectionData.PROTOCOLG6 = PROTOCOLG6;
    }

    public static void setSERVER_IP(String SERVER_IP) {
        ConnectionData.SERVER_IP = SERVER_IP;
    }

    public static void setSERVER_PORT(int SERVER_PORT) {
        ConnectionData.SERVER_PORT = SERVER_PORT;
    }

    public static void setSystem_id(byte system_id) {
        ConnectionData.system_id = system_id;
    }

    public static void setPortaSocket(int portaSocket) {
        ConnectionData.portaSocket = portaSocket;
    }

    public static void setNew_LAT(float new_LAT) {
        ConnectionData.new_LAT = new_LAT;
    }

    public static void setNew_LON(float new_LON) {
        ConnectionData.new_LON = new_LON;
    }

    public static int getSERVER_PORT() {
        return SERVER_PORT;
    }

    public static String getSERVER_IP() {
        return SERVER_IP;
    }

    public static boolean isPROTOCOLG5() {
        return PROTOCOLG5;
    }

    public static boolean isPROTOCOLG6() {
        return PROTOCOLG6;
    }

    public static byte getSystem_id() {
        return system_id;
    }

    public static int getPortaSocket() {
        return portaSocket;
    }

    public static float getNew_LAT() {
        return new_LAT;
    }

    public static float getNew_LON() {
        return new_LON;
    }

    public static ConnectionData getInstance() {
        if(instance == null) {
            instance = new ConnectionData();
        }
        return instance;
    }



}
