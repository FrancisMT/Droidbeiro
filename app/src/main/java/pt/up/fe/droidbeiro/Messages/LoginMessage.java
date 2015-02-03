package pt.up.fe.droidbeiro.Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import pt.up.fe.droidbeiro.Communication.Client_Socket;
import androidBackendAPI.Packet;

/**
 * Created by Francisco on 16/11/2014.
 */
public class LoginMessage {

    /**
     * Message Lenght
     * 3 bytes -> User Name
     * 16 bytes -> MD5 Password
     */

    private byte MessageType = 7;
    private byte FireFighter_ID;

    private int username;
    private String password = "";

    Packet login_packet;

    Client_Socket CS = null;

    public LoginMessage(byte ff_id, int user, String pass){
        this.FireFighter_ID=ff_id;
        this.username=user;
        this.password=pass;
    }

    public static final byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }

    public void build_login_packet() throws IOException {

        byte[] username_pkt = intToByteArray(username);

        //Get MD5 password
        MD5 newMD5 = new MD5(this.password);
        try {
            newMD5.encode_message();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] password_MD5 = new byte[15];
        password_MD5 = newMD5.getMD5_Byte_Array();

        //Get Message Content
        ByteArrayOutputStream packet_content = new ByteArrayOutputStream();
        packet_content.write(username_pkt);
        packet_content.write(password_MD5);
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



        this.login_packet = new Packet();
        this.login_packet.hasProtocolHeader=true;
        this.login_packet.packetContent=packet_content_final.toByteArray();
        //this.login_packet.build_packet(false, this.MessageType, this.FireFighter_ID, message_content);


    }

    public Packet getLogin_packet() {
        return login_packet;
    }

}
