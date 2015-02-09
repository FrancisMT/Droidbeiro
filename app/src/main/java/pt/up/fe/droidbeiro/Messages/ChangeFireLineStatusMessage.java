package pt.up.fe.droidbeiro.Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import androidBackendAPI.Packet;

/**
 * Created by Francisco on 19/12/2014.
 */
public class ChangeFireLineStatusMessage {

    private int msg_type=14;
    private byte MessageType = (byte)msg_type;
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

        this.changefirelinestatus_packet = new Packet();
        this.changefirelinestatus_packet.hasProtocolHeader=true;
        this.changefirelinestatus_packet.packetContent=packet_content_final.toByteArray();
        //this.changefirelinestatus_packet.build_packet(false, this.MessageType, this.FireFighter_ID, message_content);
    }

    public Packet getChangefirelinestatus_packet() {
        return changefirelinestatus_packet;
    }
}
