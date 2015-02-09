package pt.up.fe.droidbeiro.Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import androidBackendAPI.Packet;

/**
 * Created by Francisco on 19/12/2014.
 */
public class DenyRequestMessage {

    private int msg_type=20;
    private byte MessageType = (byte)msg_type;
    private byte FireFighter_ID;

    Packet denyrequest_packet;

    public DenyRequestMessage(byte ff_id){
        this.FireFighter_ID=ff_id;
    }

    public void build_denyrequest_packet() throws IOException {
        //Get Packet
        ByteArrayOutputStream packet_content_final = new ByteArrayOutputStream();
        packet_content_final.write(this.MessageType);
        packet_content_final.write(this.FireFighter_ID);

        this.denyrequest_packet = new Packet();
        this.denyrequest_packet.hasProtocolHeader=true;
        this.denyrequest_packet.packetContent=packet_content_final.toByteArray();
        //this.denyrequest_packet.build_packet(false, this.MessageType, this.FireFighter_ID, null);
    }

    public Packet getDenyrequest_packet() {
        return denyrequest_packet;
    }
}
