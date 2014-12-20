package pt.up.fe.droidbeiro.Messages;

import java.io.IOException;

import pt.up.fe.droidbeiro.androidBackendAPI.Packet;

/**
 * Created by Francisco on 19/12/2014.
 */
public class PersonalizedMessage {

    private byte MessageType = 11;
    private byte FireFighter_ID;

    private String personalized_message = "";

    Packet personalizedmessage_packet;

    public PersonalizedMessage(){

    }

    public void build_personalizedmessage_packet() throws IOException {

    }

    public Packet getPersonalizedmessage_packet() {
        return personalizedmessage_packet;
    }
}
