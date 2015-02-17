package pt.up.fe.droidbeiro.Messages;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import androidBackendAPI.Packet;

/**
 * Created by Francisco on 19/12/2014.
 */
public class PredefinedMessage {

    /** Message code table
     *
     * "Afirmativo"             <-> 0
     * "Aguarde"                <-> 1
     * "Assim Farei"            <-> 2
     * "Correto"                <-> 3
     * "Errado"                 <-> 4
     * "Informe"                <-> 5
     * "Negativo"               <-> 6
     * "A Caminho"              <-> 7
     * "No local"               <-> 8
     * "No Hospital"            <-> 9
     * "Disponível"             <-> 10
     * "De Regresso"            <-> 11
     * "INOP"                   <-> 12
     * "No Quartel"             <-> 13
     * "Necessito de Reforços"  <-> 14
     * "Casa em Perigo"         <-> 15
     * "Preciso de Descansar"   <-> 16
     * "Carro em Perigo"        <-> 17
     * "Descanse"               <-> 18
     * "Fogo a Alastrar"        <-> 19
     */

    private int msg_type=10;
    private byte MessageType = (byte)msg_type;
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
        if ((this.predefined_message).equals("Afirmativo")){
            pred_msg_pkt=(byte)0;
        }else
        if ((this.predefined_message).equals("Aguarde")){
            pred_msg_pkt=(byte)1;
        }else
        if ((this.predefined_message).equals("Assim Farei")){
            pred_msg_pkt=(byte)2;
        }else
        if ((this.predefined_message).equals("Correto")){
            pred_msg_pkt=(byte)3;
        }else
        if ((this.predefined_message).equals("Errado")){
            pred_msg_pkt=(byte)4;
        }else
        if ((this.predefined_message).equals("Informe")){
            pred_msg_pkt=(byte)5;
        }else
        if ((this.predefined_message).equals("Negativo")){
            pred_msg_pkt=(byte)6;
        }else
        if ((this.predefined_message).equals("A Caminho")){
            pred_msg_pkt=(byte)7;
        }else
        if ((this.predefined_message).equals("No local")){
            pred_msg_pkt=(byte)8;
        }else
        if ((this.predefined_message).equals("No Hospital")){
            pred_msg_pkt=(byte)9;
        }else
        if ((this.predefined_message).equals("Disponível")){
            pred_msg_pkt=(byte)10;
        }else
        if ((this.predefined_message).equals("De Regresso")){
            pred_msg_pkt=(byte)11;
        }else
        if ((this.predefined_message).equals("INOP")){
            pred_msg_pkt=(byte)12;
        }else
        if ((this.predefined_message).equals("No Quartel")){
            pred_msg_pkt=(byte)13;
        }else
        if ((this.predefined_message).equals("Necessito de Reforços")){
            pred_msg_pkt=(byte)14;
        }else
        if ((this.predefined_message).equals("Casa em Perigo")){
            pred_msg_pkt=(byte)15;
        }else
        if ((this.predefined_message).equals("Preciso de Descansar")){
            pred_msg_pkt=(byte)16;
        }else
        if ((this.predefined_message).equals("Carro em Perigo")){
            pred_msg_pkt=(byte)17;
        }else
        if ((this.predefined_message).equals("Descanse")){
            pred_msg_pkt=(byte)18;
        }else
        if ((this.predefined_message).equals("Fogo a Alastrar")){
            pred_msg_pkt=(byte)19;
        }else{
            pred_msg_pkt=(byte)255;
        }

        //Get Message Content
        ByteArrayOutputStream packet_content = new ByteArrayOutputStream();
        packet_content.write(pred_msg_pkt);
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

        this.predefinedmessage_packet = new Packet();
        this.predefinedmessage_packet.hasProtocolHeader=true;
        this.predefinedmessage_packet.packetContent=packet_content_final.toByteArray();
        //this.predefinedmessage_packet.build_packet(false, this.MessageType, this.FireFighter_ID, message_content);
    }

    /*public byte[] getMessageContent(){
        return predefinedmessage_packet.getMessage();
    }*/

    public Packet getPredefinedmessage_packet() {
        return predefinedmessage_packet;
    }
}
