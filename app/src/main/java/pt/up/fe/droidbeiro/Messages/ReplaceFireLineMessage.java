package pt.up.fe.droidbeiro.Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import androidBackendAPI.Packet;

/**
 * Created by Francisco on 30/12/2014.
 */
public class ReplaceFireLineMessage {

    private int msg_type=24;
    private byte MessageType = (byte)msg_type;
    private byte FireFighter_ID;

    private String latitude="";
    private String longitude="";

    Packet replacefireline_packet;

    public ReplaceFireLineMessage(byte ff_id, String latit, String longt){
        this.FireFighter_ID=ff_id;
        this.latitude=latit;
        this.longitude=longt;
    }

    public void build_replacefireline_packet() throws IOException {

        Float lat = Float.parseFloat(latitude);
        Float lon = Float.parseFloat(longitude);

        ByteBuffer bb_lat = ByteBuffer.allocate(4);
        bb_lat.order(ByteOrder.LITTLE_ENDIAN);
        bb_lat.putFloat(lat.floatValue ());
        byte [] latitude_pkt = bb_lat.array();

        ByteBuffer bb_lon = ByteBuffer.allocate(4);
        bb_lon.order(ByteOrder.LITTLE_ENDIAN);
        bb_lon.putFloat(lon.floatValue ());
        byte [] longitude_pkt = bb_lon.array();


        //Get Message Content
        ByteArrayOutputStream packet_content = new ByteArrayOutputStream();
        packet_content.write(latitude_pkt);
        packet_content.write(longitude_pkt);
        byte[] message_content = packet_content.toByteArray();

        //Get Packet
        ByteArrayOutputStream packet_content_final = new ByteArrayOutputStream();
        packet_content_final.write(this.MessageType);
        packet_content_final.write(this.FireFighter_ID);
        if (message_content!=null) {
            try {
                packet_content_final.write(message_content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.replacefireline_packet = new Packet();
        this.replacefireline_packet.hasProtocolHeader=true;
        this.replacefireline_packet.packetContent=packet_content_final.toByteArray();

        //this.replacefireline_packet.build_packet(false, this.MessageType, this.FireFighter_ID, message_content);
    }

    public Packet getReplacefireline_packet() {
        return replacefireline_packet;
    }
}
