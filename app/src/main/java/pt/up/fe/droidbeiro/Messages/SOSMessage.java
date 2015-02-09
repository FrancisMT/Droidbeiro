package pt.up.fe.droidbeiro.Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import androidBackendAPI.Packet;

/**
 * Created by Francisco on 19/12/2014.
 */
public class SOSMessage {

    private int msg_type=12;
    private byte MessageType = (byte)msg_type;
    private byte FireFighter_ID;

    Packet sos_packet;

    public SOSMessage(byte ff_id){
        this.FireFighter_ID=ff_id;
    }

    public void build_sos_packet() throws IOException {
        //Get Packet
        ByteArrayOutputStream packet_content_final = new ByteArrayOutputStream();
        packet_content_final.write(this.MessageType);
        packet_content_final.write(this.FireFighter_ID);

        this.sos_packet = new Packet();
        this.sos_packet.hasProtocolHeader=true;
        this.sos_packet.packetContent=packet_content_final.toByteArray();

        //this.sos_packet.build_packet(false, this.MessageType, this.FireFighter_ID, null);
    }

    public Packet getSos_packet() {
        return sos_packet;
    }
}
