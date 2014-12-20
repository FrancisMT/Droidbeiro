package pt.up.fe.droidbeiro.Messages;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Francisco on 19/12/2014.
 */
public class MD5 {

    private String Message="";
    private byte[] MD5_Byte_Array;

    public MD5(String message_to_encode){
        this.Message = message_to_encode;
    }

    public void encode_message() throws NoSuchAlgorithmException, UnsupportedEncodingException {

        byte[] bytesToEncode = this.Message.getBytes("ISO-8859-1");
        MessageDigest md5processor = MessageDigest.getInstance("MD5");
        this.MD5_Byte_Array = md5processor.digest(bytesToEncode);
    }

    public byte[] getMD5_Byte_Array() {
        return MD5_Byte_Array;
    }
}
