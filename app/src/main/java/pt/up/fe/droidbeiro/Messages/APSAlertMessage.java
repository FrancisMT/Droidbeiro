package pt.up.fe.droidbeiro.Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import androidBackendAPI.Packet;

/**
 * Created by Francisco on 19/12/2014.
 */
public class APSAlertMessage {

    private int msg_type=18;
    private byte MessageType = (byte)msg_type;
    private byte FireFighter_ID;

    Packet apsalert_packet;

    public APSAlertMessage (byte ff_id){
        this.FireFighter_ID = ff_id;
    }

    public void build_apsalert_packet() throws IOException {
        //Get Packet
        ByteArrayOutputStream packet_content_final = new ByteArrayOutputStream();
        packet_content_final.write(this.MessageType);
        packet_content_final.write(this.FireFighter_ID);

        this.apsalert_packet = new Packet();
        this.apsalert_packet.hasProtocolHeader=true;
        this.apsalert_packet.packetContent=packet_content_final.toByteArray();
        //this.apsalert_packet.build_packet(false, this.MessageType, this.FireFighter_ID, null);
    }

    public Packet getApsalert_packet() {
        return apsalert_packet;
    }
}
