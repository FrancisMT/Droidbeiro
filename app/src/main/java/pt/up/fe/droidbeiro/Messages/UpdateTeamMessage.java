package pt.up.fe.droidbeiro.Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import androidBackendAPI.Packet;

/**
 * Created by Francisco on 19/12/2014.
 */
public class UpdateTeamMessage {

    private byte MessageType = 15;
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
        this.updateteam_packet = new Packet();
        //this.updateteam_packet.build_packet(false, this.MessageType, this.FireFighter_ID, message_content);
    }

    public Packet getUpdateteam_packet() {
        return updateteam_packet;
    }
}
