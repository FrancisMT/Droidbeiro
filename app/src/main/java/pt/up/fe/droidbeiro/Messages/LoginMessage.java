package pt.up.fe.droidbeiro.Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import pt.up.fe.droidbeiro.androidBackendAPI.Packet;

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

    private String username = "";
    private String password = "";

    Packet login_packet;

    public LoginMessage(byte ff_id, String user, String pass){
        this.FireFighter_ID=ff_id;
        this.username=user;
        this.password=pass;
    }

    public void build_login_packet() throws IOException {

        byte[] username_pkt = new byte[2];
        username_pkt = username.getBytes("ISO-8859-1");

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
        this.login_packet = new Packet();
        this.login_packet.build_packet(false, this.MessageType, this.FireFighter_ID, message_content);
    }

    public Packet getLogin_packet() {
        return login_packet;
    }
}
