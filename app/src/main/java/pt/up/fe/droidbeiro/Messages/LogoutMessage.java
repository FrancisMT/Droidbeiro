package pt.up.fe.droidbeiro.Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import androidBackendAPI.Packet;

/**
 * Created by Francisco on 19/12/2014.
 */
public class LogoutMessage {

    private int msg_type=8;
    private byte MessageType = (byte)msg_type;
    private byte FireFighter_ID;

    Packet logout_packet;

    public LogoutMessage(byte ff_id){
        this.FireFighter_ID = ff_id;
    }

    public void build_logout_packet() throws IOException {
        //Get Packet
        ByteArrayOutputStream packet_content_final = new ByteArrayOutputStream();
        packet_content_final.write(this.MessageType);
        packet_content_final.write(this.FireFighter_ID);

        this.logout_packet = new Packet();
        this.logout_packet.hasProtocolHeader=true;
        this.logout_packet.packetContent=packet_content_final.toByteArray();
        //this.logout_packet.build_packet(false, this.MessageType, this.FireFighter_ID, null);
    }

    public Packet getLogout_packet() {
        return logout_packet;
    }
}
