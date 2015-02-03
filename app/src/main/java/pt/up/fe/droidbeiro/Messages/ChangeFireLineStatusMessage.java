package pt.up.fe.droidbeiro.Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import androidBackendAPI.Packet;

/**
 * Created by Francisco on 19/12/2014.
 */
public class ChangeFireLineStatusMessage {

    private byte MessageType = 14;
    private byte FireFighter_ID;

    private int status = 0;

    Packet changefirelinestatus_packet;

    public ChangeFireLineStatusMessage(byte ff_id, int stat){
        this.FireFighter_ID=ff_id;
        this.status=stat;
    }

    public void build_changefirelinestatus_packet()throws IOException {

        byte status_pkt = (byte)status;

        //Get Message Content
        ByteArrayOutputStream packet_content = new ByteArrayOutputStream();
        packet_content.write(status_pkt);
        byte[] message_content = packet_content.toByteArray();

        //Get Packet
        this.changefirelinestatus_packet = new Packet();
        //this.changefirelinestatus_packet.build_packet(false, this.MessageType, this.FireFighter_ID, message_content);
    }

    public Packet getChangefirelinestatus_packet() {
        return changefirelinestatus_packet;
    }
}
