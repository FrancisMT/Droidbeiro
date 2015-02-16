package pt.up.fe.droidbeiro.Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import androidBackendAPI.Packet;

/**
 * Created by Francisco on 30/12/2014.
 */
public class ExitAlertMessage {

    private int msg_type=23;
    private byte MessageType = (byte)msg_type;
    private byte FireFighter_ID;

    Packet exitalert_packet;

    public ExitAlertMessage(byte ff_id){
        this.FireFighter_ID=ff_id;
    }

    public void build_exitalert_packet() throws IOException {
        //Get Packet
        ByteArrayOutputStream packet_content_final = new ByteArrayOutputStream();
        packet_content_final.write(this.MessageType);
        packet_content_final.write(this.FireFighter_ID);

        this.exitalert_packet = new Packet();
        this.exitalert_packet.hasProtocolHeader=true;
        this.exitalert_packet.packetContent=packet_content_final.toByteArray();
        //this.exitalert_packet.build_packet(false, this.MessageType, this.FireFighter_ID, null);
    }

    public Packet getExitalert_packet() {
        return exitalert_packet;
    }
}
