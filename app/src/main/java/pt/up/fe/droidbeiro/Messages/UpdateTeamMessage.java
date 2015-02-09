package pt.up.fe.droidbeiro.Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import androidBackendAPI.Packet;

/**
 * Created by Francisco on 19/12/2014.
 */
public class UpdateTeamMessage {

    private int msg_type=15;
    private byte MessageType = (byte)msg_type;
    private byte FireFighter_ID;

    private int team = 0;

    Packet updateteam_packet;

    public UpdateTeamMessage(byte ff_id, int newTeam){
        this.FireFighter_ID=ff_id;
        this.team=newTeam;
    }

    public void build_updateteam_packet() throws IOException {

        byte team_pkt = (byte)team;

        //Get Message Content
        ByteArrayOutputStream packet_content = new ByteArrayOutputStream();
        packet_content.write(team_pkt);
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

        this.updateteam_packet = new Packet();
        this.updateteam_packet.hasProtocolHeader=true;
        this.updateteam_packet.packetContent=packet_content_final.toByteArray();
        //this.updateteam_packet.build_packet(false, this.MessageType, this.FireFighter_ID, message_content);
    }

    public Packet getUpdateteam_packet() {
        return updateteam_packet;
    }
}
