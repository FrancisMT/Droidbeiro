package pt.up.fe.droidbeiro.Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import androidBackendAPI.Packet;

/**
 * Created by Francisco on 19/12/2014.
 */
public class HeartRateAlertMessage {

    private int msg_type=0;
    private byte MessageType = (byte)msg_type;
    private byte FireFighter_ID;

    private int BPM_data = 0;

    Packet heartratealert_packet;

    public HeartRateAlertMessage(byte ff_id, int bpm){
        this.FireFighter_ID=ff_id;
        this.BPM_data=bpm;
    }

    public void build_heartrate_packet() throws IOException {

        byte bpm_pkt = (byte)BPM_data;

        //Get Message Content
        ByteArrayOutputStream packet_content = new ByteArrayOutputStream();
        packet_content.write(bpm_pkt);
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

        this.heartratealert_packet = new Packet();
        this.heartratealert_packet.hasProtocolHeader=true;
        this.heartratealert_packet.packetContent=packet_content_final.toByteArray();

        //this.heartratealert_packet.build_packet(false, this.MessageType, this.FireFighter_ID, message_content);
    }

    public Packet getHeartratealert_packet() {
        return heartratealert_packet;
    }
}
