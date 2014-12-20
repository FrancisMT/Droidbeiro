package pt.up.fe.droidbeiro.Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import pt.up.fe.droidbeiro.androidBackendAPI.Packet;

/**
 * Created by Francisco on 19/12/2014.
 */
public class GPSMessage {

    //Message Lenght
    //8 bytes
    //First 4 bytes: Latitude
    //Second 4 bytes: Longitude

    private byte MessageType = 0;
    private byte FireFighter_ID;

    private String latitude="";
    private String longitude="";

    Packet gps_packet;

    public GPSMessage(byte ff_id, String latit, String longt){
        this.FireFighter_ID=ff_id;
        this.latitude=latit;
        this.longitude=longt;
    }

    public void build_gps_packet() throws IOException {

        byte[] latitude_pkt = new byte[4];
        latitude_pkt = latitude.getBytes("ISO-8859-1");

        byte[] longitude_pkt = new byte[4];
        longitude_pkt = longitude.getBytes("ISO-8859-1");

        //Get Message Content
        ByteArrayOutputStream packet_content = new ByteArrayOutputStream();
        packet_content.write(latitude_pkt);
        packet_content.write(longitude_pkt);
        byte[] message_content = packet_content.toByteArray();

        //Get Packet
        this.gps_packet = new Packet();
        this.gps_packet.build_packet(false, this.MessageType, this.FireFighter_ID, message_content);
    }

    public Packet getGps_packet() {
        return gps_packet;
    }
}
