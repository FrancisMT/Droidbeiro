package pt.up.fe.droidbeiro.Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import pt.up.fe.droidbeiro.androidBackendAPI.Packet;

/**
 * Created by Francisco on 19/12/2014.
 */
public class PredefinedMessage {

    /** Message code table
     *
     * "Preciso de ajuda"           <-> 0
     * "Preciso afastar-me"         <-> 1
     * "Camião com problemas"       <-> 2
     * "Preciso de suporte aéreo"   <-> 3
     * "Fogo a espalhar-se"         <-> 6
     * "A retirar-me"               <-> 7
     * "Fogo perto de casa"         <-> 8
     * "Casa queimada"              <-> 9
     */

    private byte MessageType = 10;
    private byte FireFighter_ID;

    private String predefined_message = "";

    Packet predefinedmessage_packet;

    public PredefinedMessage(byte ff_id, String pred_msg){
        this.FireFighter_ID=ff_id;
        this.predefined_message=pred_msg;
    }

    public void build_predefinedmessage_packet() throws IOException {

        byte pred_msg_pkt;

        //get predefined message code
        if ((this.predefined_message).equals("Preciso de ajuda")){
            pred_msg_pkt=(byte)0;
        }else
        if ((this.predefined_message).equals("Preciso afastar-me")){
            pred_msg_pkt=(byte)1;
        }else
        if ((this.predefined_message).equals("Camião com problemas")){
            pred_msg_pkt=(byte)2;
        }else
        if ((this.predefined_message).equals("Preciso de suporte aéreo")){
            pred_msg_pkt=(byte)3;
        }else
        if ((this.predefined_message).equals("Fogo a espalhar-se")){
            pred_msg_pkt=(byte)6;
        }else
        if ((this.predefined_message).equals("A retirar-me")){
            pred_msg_pkt=(byte)7;
        }else
        if ((this.predefined_message).equals("Fogo perto de casa")){
            pred_msg_pkt=(byte)8;
        }else
        if ((this.predefined_message).equals("Casa queimada")){
            pred_msg_pkt=(byte)9;
        }else{
            pred_msg_pkt=(byte)255;
        }

        //Get Message Content
        ByteArrayOutputStream packet_content = new ByteArrayOutputStream();
        packet_content.write(pred_msg_pkt);
        byte[] message_content = packet_content.toByteArray();

        //Get Packet
        this.predefinedmessage_packet = new Packet();
        this.predefinedmessage_packet.build_packet(false, this.MessageType, this.FireFighter_ID, message_content);
    }

    public Packet getPredefinedmessage_packet() {
        return predefinedmessage_packet;
    }
}
