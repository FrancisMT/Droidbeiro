package pt.up.fe.droidbeiro.Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import androidBackendAPI.Packet;

/**
 * Created by Francisco on 19/12/2014.
 */
public class DenyIDMessage {

    private int msg_type=22;
    private byte MessageType = (byte)msg_type;
    private byte FireFighter_ID;

    private byte newID;

    Packet denyid_packet;

    public DenyIDMessage(byte ff_id, byte new_id){
        this.FireFighter_ID=ff_id;
        this.newID=new_id;
    }

    public void build_denyid_packet() throws IOException {

        byte newID_pkt = (byte)newID;

        //Get Message Content
        ByteArrayOutputStream packet_content = new ByteArrayOutputStream();
        packet_content.write(newID_pkt);
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

        this.denyid_packet = new Packet();
        this.denyid_packet.hasProtocolHeader=true;
        this.denyid_packet.packetContent=packet_content_final.toByteArray();
        //this.denyid_packet.build_packet(false, this.MessageType, this.FireFighter_ID, message_content);
    }

    public Packet getDenyid_packet() {
        return denyid_packet;
    }
}
