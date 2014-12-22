package pt.up.fe.droidbeiro.Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import pt.up.fe.droidbeiro.androidBackendAPI.Packet;

/**
 * Created by Francisco on 19/12/2014.
 */
public class HeartRateAlertMessage {

    private byte MessageType = 17;
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
        this.heartratealert_packet = new Packet();
        this.heartratealert_packet.build_packet(false, this.MessageType, this.FireFighter_ID, message_content);
    }

    public Packet getHeartratealert_packet() {
        return heartratealert_packet;
    }
}
