

package protocol_g6_package;


import java.util.Arrays;

import static protocol_g6_package.rotas.MAXNODES;
import static protocol_g6_package.rotas.BROADCAST;
import static protocol_g6_package.rotas.CENTRAL;
import static protocol_g6_package.Protocol_G6.VERSAO_PROTOCOLO;


public class pacote {

    // Número de bits de cada campo do cabeçalho
    public static final int nBitsVersion=1;
    public static final int nBitsPacketType=2;
    public static final int nBitsPacketID=5;
    public static final int nBitsTTL=7;
    public static final int nBitsAddresses=8;
    public static final int nBitsCRCHeader=8;
    public static final int nBitsCRCData=16;
    public static final int nBitsFragmentFlag=1;
    public static final int nBitsFragmentID=4;
    public static final int nBitsTotalFragments=4;
    ///////////////////////////////////////////
    
    public static final int version=VERSAO_PROTOCOLO;
    public static final int infoPacketType=0;
    public static final int rReqPacketType=1;
    public static final int rReplyPacketType=2;
    public static final int ackPacketType=3;
    
        
        // ID dos pacotes
    public static int id=0; 
    
    ///////////////////////////////////////////////// Cálculo CRC ////////////////////////////////////////////////////
    
            //Tabela para o cálculo do CRC 16 bit
        public static int[] table_CRC16 = {
            0x0000, 0xC0C1, 0xC181, 0x0140, 0xC301, 0x03C0, 0x0280, 0xC241,
            0xC601, 0x06C0, 0x0780, 0xC741, 0x0500, 0xC5C1, 0xC481, 0x0440,
            0xCC01, 0x0CC0, 0x0D80, 0xCD41, 0x0F00, 0xCFC1, 0xCE81, 0x0E40,
            0x0A00, 0xCAC1, 0xCB81, 0x0B40, 0xC901, 0x09C0, 0x0880, 0xC841,
            0xD801, 0x18C0, 0x1980, 0xD941, 0x1B00, 0xDBC1, 0xDA81, 0x1A40,
            0x1E00, 0xDEC1, 0xDF81, 0x1F40, 0xDD01, 0x1DC0, 0x1C80, 0xDC41,
            0x1400, 0xD4C1, 0xD581, 0x1540, 0xD701, 0x17C0, 0x1680, 0xD641,
            0xD201, 0x12C0, 0x1380, 0xD341, 0x1100, 0xD1C1, 0xD081, 0x1040,
            0xF001, 0x30C0, 0x3180, 0xF141, 0x3300, 0xF3C1, 0xF281, 0x3240,
            0x3600, 0xF6C1, 0xF781, 0x3740, 0xF501, 0x35C0, 0x3480, 0xF441,
            0x3C00, 0xFCC1, 0xFD81, 0x3D40, 0xFF01, 0x3FC0, 0x3E80, 0xFE41,
            0xFA01, 0x3AC0, 0x3B80, 0xFB41, 0x3900, 0xF9C1, 0xF881, 0x3840,
            0x2800, 0xE8C1, 0xE981, 0x2940, 0xEB01, 0x2BC0, 0x2A80, 0xEA41,
            0xEE01, 0x2EC0, 0x2F80, 0xEF41, 0x2D00, 0xEDC1, 0xEC81, 0x2C40,
            0xE401, 0x24C0, 0x2580, 0xE541, 0x2700, 0xE7C1, 0xE681, 0x2640,
            0x2200, 0xE2C1, 0xE381, 0x2340, 0xE101, 0x21C0, 0x2080, 0xE041,
            0xA001, 0x60C0, 0x6180, 0xA141, 0x6300, 0xA3C1, 0xA281, 0x6240,
            0x6600, 0xA6C1, 0xA781, 0x6740, 0xA501, 0x65C0, 0x6480, 0xA441,
            0x6C00, 0xACC1, 0xAD81, 0x6D40, 0xAF01, 0x6FC0, 0x6E80, 0xAE41,
            0xAA01, 0x6AC0, 0x6B80, 0xAB41, 0x6900, 0xA9C1, 0xA881, 0x6840,
            0x7800, 0xB8C1, 0xB981, 0x7940, 0xBB01, 0x7BC0, 0x7A80, 0xBA41,
            0xBE01, 0x7EC0, 0x7F80, 0xBF41, 0x7D00, 0xBDC1, 0xBC81, 0x7C40,
            0xB401, 0x74C0, 0x7580, 0xB541, 0x7700, 0xB7C1, 0xB681, 0x7640,
            0x7200, 0xB2C1, 0xB381, 0x7340, 0xB101, 0x71C0, 0x7080, 0xB041,
            0x5000, 0x90C1, 0x9181, 0x5140, 0x9301, 0x53C0, 0x5280, 0x9241,
            0x9601, 0x56C0, 0x5780, 0x9741, 0x5500, 0x95C1, 0x9481, 0x5440,
            0x9C01, 0x5CC0, 0x5D80, 0x9D41, 0x5F00, 0x9FC1, 0x9E81, 0x5E40,
            0x5A00, 0x9AC1, 0x9B81, 0x5B40, 0x9901, 0x59C0, 0x5880, 0x9841,
            0x8801, 0x48C0, 0x4980, 0x8941, 0x4B00, 0x8BC1, 0x8A81, 0x4A40,
            0x4E00, 0x8EC1, 0x8F81, 0x4F40, 0x8D01, 0x4DC0, 0x4C80, 0x8C41,
            0x4400, 0x84C1, 0x8581, 0x4540, 0x8701, 0x47C0, 0x4680, 0x8641,
            0x8201, 0x42C0, 0x4380, 0x8341, 0x4100, 0x81C1, 0x8081, 0x4040,
        };
    
    // Aritmetica binária
    static String[] BinaryArithmetic = {
        "0+0+0=00",
        "0+0+1=01",
        "0+1+0=01", 
        "0+1+1=10",
        "1+0+0=01",
        "1+0+1=10",
        "1+1+0=10",
        "1+1+1=11",
    };
    
    public static String complement(String str,int bitsLength){
        while (str.length() < bitsLength) {    
            str = "1" + str;
        }
        return str;
    }
    
    static String lookup(char b1, char b2, char c) {
        String formula = String.format("%c+%c+%c=", b1, b2, c);
        for (String s : BinaryArithmetic) {
            if (s.startsWith(formula)) {
                return s.substring(s.indexOf("=") + 1);
            }
        }
        throw new IllegalArgumentException();
    }
    
    static String sumBinary8bit(String s1, String s2) {
        int length = Math.max(s1.length(), s2.length());      
        s1 = addNBits(s1, length);
        s2 = addNBits(s2, length);
        String result = "";
        char carry = '0';
        for (int i = length - 1; i >= 0; i--) {
            String columnResult = lookup(s1.charAt(i), s2.charAt(i), carry);
            result = columnResult.charAt(1) + result;
            carry = columnResult.charAt(0);
        }
        if (carry == '1') {
            result = carry + result; //o resultado poderá ultrapassar os 8 bits!
        }
        if(result.length() > 8) // vai ser sempre no maximo 9 bits!
        {
            // adiciona 1 zero no inicio, remove os ultimos 2 bits de forma a manter o checksum em 8 bits
            result = addNBits(result,result.length()+1);  
            result = result.substring(0,result.length()-1);  
            result = result.substring(0,result.length()-1);
        }
        return result;
    }
    
    public static String xor ( String a, String b ){
              int length = Math.min(a.length(), b.length());
              int[] result = new int[length];
              String str = "";
              
              for ( int i=0; i<length ; i++ ){
                 result[i] = ( a.charAt(i) ^ b.charAt(i) );
                 str = str + "" + result[i];
              }
              return str;
    }
    
    ///////////////////////////////////////////////// Cálculo CRC ////////////////////////////////////////////////////
    
public static byte[] binaryString2byteArray(String pacote){
        
        int value,k,startFrom;
        if (pacote.length()%8!=0){
            System.out.println("String binária tem que ter um tamanho múltiplo de 8");
            return null;
        }
        
        byte[] bytePacket=new byte[(pacote.length()/8)];

        k=0;
        for (int i=1;i<=(pacote.length()/8);i++){
            value=0;
            
            startFrom=(k*8);
            
            for (int j=7;j>=0;j--){
                if (pacote.charAt(startFrom)=='1'){
                    value=value+ ((int) Math.pow(2,j));
                }
                startFrom++;
            }
            bytePacket[i-1]=(byte) value;
            k++;
        }
        
        return bytePacket;
    }
    
    public static String byteArray2binaryString(byte[] pacote){
        
        int i=0,aux;
        String binString="",s1;
        
        for (i=0;i<pacote.length;i++){
            s1 = String.format("%8s", Integer.toBinaryString(pacote[i] & 0xFF)).replace(' ', '0');
            binString=binString+s1;
        }
        
        return binString;
    }
    
    // Adiciona zeros a uma string até a string ter o tamanho bitsLength. Útil para representar um certo número inteiro com uma
    // determinada quantidade de bits 
    public static String addNBits(String str,int bitsLength){
        while (str.length() < bitsLength) {    
            str = "0" + str;
        }
        return str;
    }
   
    // Converte os caracteres texto de uma string para uma string binária
    public static String stringToBin(String str){
        
        String binaryString = new String("");
        char caractere;
        int valorNumerico;
        for(int j=0;j<str.length();j++){
            caractere=str.charAt(j);
            valorNumerico=(int)caractere;
            binaryString=binaryString+addNBits(Integer.toBinaryString(valorNumerico), 8);
        }
        
        return binaryString;
    }
    
    public static int binaryToInteger(String binary){
        char[] numbers = binary.toCharArray();
        int result = 0;
        int count = 0;
        for(int i = numbers.length-1; i>=0 ;i--){
            if(numbers[i]=='1')result+=(int)Math.pow(2, count);
            count++;
        }
        return result;
    }
    
    public static String binaryStringToText(String str){     
        String msg = "";
        for(int i=0,j; i <= str.length() - 8; i = i+8)
        {
            j = Integer.parseInt(str.substring(i, i+8), 2);
            msg = msg + (char) j;
        }   
        return msg;
    }
    
    public static String calcularCRCHeader(String pacote){     
        // Cálculo dos CRCs
        String crcHeader;      
        String aux;
        String complement;
        
        //Cálculo Crc Header
        // Soma em Binário e guarda o resultado como String
        aux = sumBinary8bit(getHeaderVersionPacote(pacote),getHeaderTypePacote(pacote));
        aux = sumBinary8bit(aux, getHeaderIDPacote(pacote)); 
        aux = sumBinary8bit(aux, getHeaderTTLPacote(pacote));
        aux = sumBinary8bit(aux, getHeaderOrigSourcePacote(pacote));
        aux = sumBinary8bit(aux, getHeaderDestinoPacote(pacote));
        aux = sumBinary8bit(aux, getHeaderNextHopPacote(pacote));
        aux = sumBinary8bit(aux, getHeaderSourcePacote(pacote));
        aux = sumBinary8bit(aux, getHeaderFragmentFlagPacote(pacote));
        aux = sumBinary8bit(aux, getHeaderFragmentIDPacote(pacote));
        aux = sumBinary8bit(aux, getHeaderTotalFragmentsPacote(pacote));
        // Complemento para 1 
        complement = complement("", aux.length());
        crcHeader = xor(aux,complement);
        
        return crcHeader;
    }
    
    public static String calcularCRCData(String dados){  
        //Cálculo crcData (CRC16)
        int crc = 0x0000; 
        byte[] bytes = dados.getBytes();
        String crcData;

        // Cálculo do CRC com ajuda da tabela auxiliar
        // Uses irreducible polynomial:  1 + x^2 + x^15 + x^16      
        for (byte b : bytes) {
            crc = (crc >>> 8) ^ table_CRC16[(crc ^ b) & 0xff];
        }
        crcData = Integer.toBinaryString(crc);
        crcData = addNBits(crcData, nBitsCRCData);
        return crcData;
    }
    
    // Encapsula um determinado conjunto de dados num pacote de informação
    // A string dadosBin é a string que se quer enviar convertida para string binária
    public static String encapsula(int destino,int nextHop,String dadosBin,int TTL, int nodeIdentification, int FragmentFlag, int FragmentID, int TotalFragments){
        
        String packet;
        String pVersion=addNBits(Integer.toBinaryString(version),nBitsVersion);
        String pType=addNBits(Integer.toBinaryString(infoPacketType),nBitsPacketType);
        String pID=addNBits(Integer.toBinaryString(id),nBitsPacketID);
        String pTTL=addNBits(Integer.toBinaryString(TTL),nBitsTTL);
        String pOrigSource=addNBits(Integer.toBinaryString(nodeIdentification),nBitsAddresses);
        String pDest= addNBits(Integer.toBinaryString(destino),nBitsAddresses);
        String pNextHop=addNBits(Integer.toBinaryString(nextHop),nBitsAddresses);
        String pSource=addNBits(Integer.toBinaryString(nodeIdentification),nBitsAddresses);
        String pDados = "";
        // Fragmentação
        String pFragmentFlag="";
        String pFragmentID="";
        String pTotalFragments="";
        
        if(FragmentFlag==0){
            pFragmentFlag = "0";
            pFragmentID = "0000";
            pTotalFragments = "0000";
            pDados=dadosBin;
        }
        if(FragmentFlag==1){
            pFragmentFlag = addNBits(Integer.toBinaryString(FragmentFlag),nBitsFragmentFlag);
            pFragmentID = addNBits(Integer.toBinaryString(FragmentID),nBitsFragmentID);
            pTotalFragments = addNBits(Integer.toBinaryString(TotalFragments),nBitsTotalFragments);
            
            // 9 bytes(dados) * 8 = 72 bits
            if(TotalFragments==FragmentID)
            {
                 for(int i=(FragmentID*72); i < dadosBin.length(); i++)
                {        
                         pDados = pDados + dadosBin.charAt(i) + ""; 
                }
            }    
            else{
                for(int i=(FragmentID*72); i < ((FragmentID*72)+72); i++)
                {        
                         pDados = pDados + dadosBin.charAt(i) + ""; 
                }
            }        
        }
   
 
        String crcHeader;
        String crcData;       
        String aux;
        String complement;
        
        //Cálculo Crc Header
        // Soma em Binário e guarda o resultado como String
        aux = sumBinary8bit(pVersion,pType);
        aux = sumBinary8bit(aux, pID); 
        aux = sumBinary8bit(aux, pTTL);
        aux = sumBinary8bit(aux, pOrigSource);
        aux = sumBinary8bit(aux, pDest);
        aux = sumBinary8bit(aux, pNextHop);
        aux = sumBinary8bit(aux, pSource);
        aux = sumBinary8bit(aux, pFragmentFlag);
        aux = sumBinary8bit(aux, pFragmentID);
        aux = sumBinary8bit(aux, pTotalFragments);
        // Complemento para 1 
        complement = complement("", aux.length());
        crcHeader = xor(aux,complement);
        
        // Cálculo CRCData
        crcData = calcularCRCData(pDados);
        
        /* // Teste encapsula
        System.out.println("protocolog6","Source(encapsulado): " + pSource);
        System.out.println("protocolog6","crcHeader(encapsulado): " + crcHeader);
        System.out.println("protocolog6","CRC Data(encapsulado): " + crcData);
        System.out.println("protocolog6","Msg (encapsulada):     " + pDados); */
        
        packet=pVersion+pType+pID+pTTL+pOrigSource+pDest+pNextHop+pSource+pFragmentFlag+pFragmentID+pTotalFragments+crcHeader+pDados+crcData;

        id++;
        if (id==32) id=0;
        
        //System.out.println("protocolog6",packetVersion);
        //System.out.println("protocolog6",packetType);
        //System.out.println("protocolog6",packetDestino);
        return packet;
    }
    
    
    // Desencapsula um determinado pacote num conjunto de dados
    public static String desencapsula(String pacote){
        
            String dados = binaryStringToText(getDadosPacote(pacote));
            return dados;         
    }
    
    // Cria o pacote de Route Request
    public static String criaRReq(int destino, int nodeIdentification){
        String rReqPacket;
        int OrigSource=nodeIdentification;
        
        String pVersion=addNBits(Integer.toBinaryString(version),nBitsVersion);
        String pType=addNBits(Integer.toBinaryString(rReqPacketType),nBitsPacketType);
        String pID=addNBits(Integer.toBinaryString(id),nBitsPacketID);
        String pTTL=addNBits(Integer.toBinaryString(127),nBitsTTL);
        String pOrigSource=addNBits(Integer.toBinaryString(OrigSource),nBitsAddresses);
        String pDest= addNBits(Integer.toBinaryString(destino),nBitsAddresses);
        String pNextHop=addNBits(Integer.toBinaryString(BROADCAST),nBitsAddresses);
        String pSource=addNBits(Integer.toBinaryString(nodeIdentification),nBitsAddresses);
        
        String pFragmentFlag="0";
        String pFragmentID="0000";
        String pTotalFragments="0000";
        
                //Cálculo Crc Header
        String crcHeader;       
        String aux;
        String complement;
        
        // Soma em Binário e guarda o resultado como String
        aux = sumBinary8bit(pVersion,pType);
        aux = sumBinary8bit(aux, pID); 
        aux = sumBinary8bit(aux, pTTL);
        aux = sumBinary8bit(aux, pOrigSource);
        aux = sumBinary8bit(aux, pDest);
        aux = sumBinary8bit(aux, pNextHop);
        aux = sumBinary8bit(aux, pSource);
        aux = sumBinary8bit(aux, pFragmentFlag);
        aux = sumBinary8bit(aux, pFragmentID);
        aux = sumBinary8bit(aux, pTotalFragments);
        
        // Complemento para 1 
        complement = complement("", aux.length());
        crcHeader = addNBits(xor(aux,complement), nBitsCRCHeader);
        
        rReqPacket=pVersion+pType+pID+pTTL+pOrigSource+pDest+pNextHop+pSource+pFragmentFlag+pFragmentID+pTotalFragments+crcHeader;
        System.out.println("Route Request: " + Arrays.toString(binaryString2byteArray(rReqPacket)));
        id++;
        if (id==32) id=0;
        return rReqPacket;
    }
    
    // Cria o pacote de Route Reply
    public static String criaRReply(String pacote, no node){
        String rReplyPacket;
        int dest;
        dest = getSourcePacote(pacote);
        
        int nHop=rotas.getEntradaTabela(node.tabRota, dest);
        
        if (nHop==-1)
            return null;
        
        String pVersion=addNBits(Integer.toBinaryString(version),nBitsVersion);
        String pType=addNBits(Integer.toBinaryString(rReplyPacketType),nBitsPacketType);
        String pID=addNBits(Integer.toBinaryString(id),nBitsPacketID);
        String pTTL=addNBits(Integer.toBinaryString(127),nBitsTTL);
        String pOrigSource=addNBits(Integer.toBinaryString(node.nodeIdentification),nBitsAddresses);
        String pDest= addNBits(Integer.toBinaryString(dest),nBitsAddresses);
        String pNextHop=addNBits(Integer.toBinaryString(nHop),nBitsAddresses);
        String pSource=addNBits(Integer.toBinaryString(node.nodeIdentification),nBitsAddresses);
        
        //Cálculo Crc Header, falta crcData
        String crcHeader;     
        String aux;
        String complement;
        
        
        String pFragmentFlag="0";
        String pFragmentID="0000";
        String pTotalFragments="0000";
        
        // Soma em Binário e guarda o resultado como String
        aux = sumBinary8bit(pVersion,pType);
        aux = sumBinary8bit(aux, pID); 
        aux = sumBinary8bit(aux, pTTL);
        aux = sumBinary8bit(aux, pOrigSource);
        aux = sumBinary8bit(aux, pDest);
        aux = sumBinary8bit(aux, pNextHop);
        aux = sumBinary8bit(aux, pSource);
        aux = sumBinary8bit(aux, pFragmentFlag);
        aux = sumBinary8bit(aux, pFragmentID);
        aux = sumBinary8bit(aux, pTotalFragments);
        
        // Complemento para 1 
        complement = complement("", aux.length());
        crcHeader = xor(aux,complement);
        
        
        rReplyPacket=pVersion+pType+pID+pTTL+pOrigSource+pDest+pNextHop+pSource+pFragmentFlag+pFragmentID+pTotalFragments+crcHeader;
        id++;
        if (id==32) id=0;
        return rReplyPacket;
    }
    
    public static int getVersionPacote(String pacote){              
            String str="";
            // obtem o tipo em  string binária 
            for ( int i=0; i< nBitsVersion ; i++ ){

                     str = str + pacote.charAt(i) + ""; 
                  }

            // passa a string binária para inteiro
            int num = binaryToInteger(str);
            return num;
    }
    
    public static String getHeaderVersionPacote(String pacote){              
            String str="";
            // obtem o tipo em  string binária 
            for ( int i=0; i< nBitsVersion ; i++ ){

                     str = str + pacote.charAt(i) + ""; 
                  }
            
            return str;
    }

    public static int getTypePacote(String pacote){              
        String str="";
        // obtem o tipo em  string binária 
        for ( int i=1; i< (nBitsPacketType+1) ; i++ ){
                 
                 str = str + pacote.charAt(i) + ""; 
              }
        
        // passa a string binária para inteiro
        int num = binaryToInteger(str);
        
        // Testa e retorna o tipo de pacote
        if(num == infoPacketType){
            return infoPacketType;
        }    
        if(num == rReqPacketType){
            return rReqPacketType;
        }
        if(num == rReplyPacketType){
            return rReplyPacketType;
        }
        if(num == ackPacketType){
            return ackPacketType;
        }

        // em caso de não conseguir identificar o tipo retorna -1
        return -1;
    }
    
    public static String getHeaderTypePacote(String pacote){              
        String str="";
        // obtem o tipo em  string binária 
        for ( int i=1; i< (nBitsPacketType+1) ; i++ ){
                 
                 str = str + pacote.charAt(i) + ""; 
              }
        return str;
    }
    
    public static int getIDPacote(String pacote){              
            String str="";
            // obtem o tipo em  string binária 
            for ( int i=3; i< (nBitsPacketID + 3) ; i++ ){

                     str = str + pacote.charAt(i) + ""; 
                  }

            // passa a string binária para inteiro
            int num = binaryToInteger(str);
            return num;
    }       
    
    public static String getHeaderIDPacote(String pacote){              
            String str="";
            // obtem o tipo em  string binária 
            for ( int i=3; i< (nBitsPacketID + 3) ; i++ ){

                     str = str + pacote.charAt(i) + ""; 
                  }

            return str;
    }
    

    public static int getTTLPacote(String pacote){              
            String str="";
            // obtem o tipo em  string binária 
            for ( int i=8; i< (nBitsTTL + 8) ; i++ ){

                     str = str + pacote.charAt(i) + ""; 
                  }

            // passa a string binária para inteiro
            int num = binaryToInteger(str);
            return num;
    }
    
    public static String getHeaderTTLPacote(String pacote){              
            String str="";
            // obtem o tipo em  string binária 
            for ( int i=8; i< (nBitsTTL + 8) ; i++ ){

                     str = str + pacote.charAt(i) + ""; 
                  }
            return str;
    }

    public static int getOrigSourcePacote(String pacote){              
            String str ="";
            // obtem o tipo em  string binária 
            for ( int i=15; i< (nBitsAddresses + 15) ; i++ ){

                     str = str + pacote.charAt(i) + ""; 
                  }

            // passa a string binária para inteiro
            int OrigSource = binaryToInteger(str);
            return OrigSource;
    }
    
    public static String getHeaderOrigSourcePacote(String pacote){              
            String str ="";
            // obtem o tipo em  string binária 
            for ( int i=15; i< (nBitsAddresses + 15) ; i++ ){

                     str = str + pacote.charAt(i) + ""; 
                  }
            return str;
    }
    

    public static int getDestinoPacote(String pacote){              
            String str ="";
            // obtem o tipo em  string binária 
            for ( int i=23; i< (nBitsAddresses + 23) ; i++ ){

                     str = str + pacote.charAt(i) + ""; 
                  }

            // passa a string binária para inteiro
            int OrigSource = binaryToInteger(str);
            return OrigSource;
    }

      public static String getHeaderDestinoPacote(String pacote){              
            String str ="";
            // obtem o tipo em  string binária 
            for ( int i=23; i< (nBitsAddresses + 23) ; i++ ){

                     str = str + pacote.charAt(i) + ""; 
                  }
            return str;
    }

        
    public static int getNextHopPacote(String pacote){              
            String str ="";
            // obtem o tipo em  string binária 
            for ( int i=31; i< (nBitsAddresses + 31) ; i++ ){

                     str = str + pacote.charAt(i) + ""; 
                  }
            int NextHop = binaryToInteger(str);
            return NextHop;
    }
    
    public static String getHeaderNextHopPacote(String pacote){              
            String str ="";
            // obtem o tipo em  string binária 
            for ( int i=31; i< (nBitsAddresses + 31) ; i++ ){

                     str = str + pacote.charAt(i) + ""; 
                  }
            return str;
    }
    
    public static int getSourcePacote(String pacote){              
            String str ="";
            // obtem o tipo em  string binária 
            for ( int i=39; i< (nBitsAddresses + 39) ; i++ ){

                     str = str + pacote.charAt(i) + ""; 
                  }
            int Source = binaryToInteger(str);
            return Source;
    }
    
    public static String getHeaderSourcePacote(String pacote){              
            String str ="";
            // obtem o tipo em  string binária 
            for ( int i=39; i< (nBitsAddresses + 39) ; i++ ){

                     str = str + pacote.charAt(i) + ""; 
                  }
            return str;
    }
    
    public static int getFragmentFlagPacote(String pacote){              
            String str ="";
            // obtem o tipo em  string binária 
            for ( int i=47; i< (nBitsFragmentFlag + 47) ; i++ ){

                     str = str + pacote.charAt(i) + ""; 
                  }
            int Source = binaryToInteger(str);
            return Source;
    }
    
    public static String getHeaderFragmentFlagPacote(String pacote){              
            String str ="";
            // obtem o tipo em  string binária 
            for ( int i=47; i< (nBitsFragmentFlag + 47) ; i++ ){

                     str = str + pacote.charAt(i) + ""; 
                  }
            return str;
    }
    
    public static int getFragmentIDPacote(String pacote){              
            String str ="";
            // obtem o tipo em  string binária 
            for ( int i=48; i< (nBitsFragmentID + 48) ; i++ ){

                     str = str + pacote.charAt(i) + ""; 
                  }
            int Source = binaryToInteger(str);
            return Source;
    }
    
    public static String getHeaderFragmentIDPacote(String pacote){              
            String str ="";
            // obtem o tipo em  string binária 
            for ( int i=48; i< (nBitsFragmentID + 48) ; i++ ){

                     str = str + pacote.charAt(i) + ""; 
                  }
            return str;
    }
    
    public static int getTotalFragmentsPacote(String pacote){              
            String str ="";
            // obtem o tipo em  string binária 
            for ( int i=52; i< (nBitsTotalFragments + 52) ; i++ ){

                     str = str + pacote.charAt(i) + ""; 
                  }
            int Source = binaryToInteger(str);
            return Source;
    }
    
    public static String getHeaderTotalFragmentsPacote(String pacote){              
            String str ="";
            // obtem o tipo em  string binária 
            for ( int i=52; i< (nBitsTotalFragments + 52) ; i++ ){

                     str = str + pacote.charAt(i) + ""; 
                  }
            return str;
    }
    
    
    public static String getCRCHeaderPacote(String pacote){              
            String crcHeader ="";
            // obtem o tipo em  string binária 
            for ( int i=56; i< (nBitsCRCHeader + 56) ; i++ ){

                     crcHeader = crcHeader + pacote.charAt(i) + ""; 
                  }
            return crcHeader;
    }
    
    public static String getDadosPacote(String pacote){              
            String dados ="";
            int length = pacote.length()-1;
            int lengthHeader = nBitsVersion + nBitsPacketType + nBitsPacketID + nBitsTTL + (4*nBitsAddresses) + nBitsCRCHeader + nBitsFragmentFlag + nBitsFragmentID + nBitsTotalFragments;
 
            // obtem o tipo em  string binária 
            for ( int i = lengthHeader; i < length-nBitsCRCData+1 ; i++ ){

                     dados = dados + pacote.charAt(i) + ""; 
                  }
            return dados;
    }
    
   
    public static String getCRCDataPacote(String pacote){              
            String crcData ="";
            String invertido ="";
            int length = pacote.length()-1;
            // obtem o tipo em  string binária 
            for ( int i = length; i > (length-nBitsCRCData) ; i-- ){

                     invertido = invertido + pacote.charAt(i) + ""; 
                  }
            // inverte a strin para obter o crcHeader
            crcData = new StringBuffer(invertido).reverse().toString();
            return crcData;
    }
    
    public static int analisaDestino(String Pacote, int nodeIdentification){
    
        int destino = getDestinoPacote(Pacote);
        int nextHop = getNextHopPacote(Pacote);
        // Testa o destino do pacote se é broadcast ou para o nó. 
        // Testa também se o nextHop é o próprio nó
        if(destino == nodeIdentification || destino == BROADCAST){
            // Pacote válido
            return 1;    
        }    
        if (nextHop == nodeIdentification){
            // falta chamar função de encaminhamento!!
            // Retorno diferente para saber que o pacote foi reencaminhado!!
            // p. ex: public static final int nextHopNo = 2;
            return 2;
        }   
        //pacote não é válido
        return -1;
    }
    
    public static int verificaErros(String pacote){
        
        int versaoPacote = getVersionPacote(pacote);
        int tipoPacote = getTypePacote(pacote);
        if (versaoPacote != version)
            return -1;

        String crcHeader = getCRCHeaderPacote(pacote);
        String crcHeaderCalculado = calcularCRCHeader(pacote);
        
        // Se o crcHeader estiver errado, retorna -1 (existem erros)
        if(!crcHeaderCalculado.equals(crcHeader))
            return -1;

        if(tipoPacote==0)
        {
            String crcData = getCRCDataPacote(pacote);
            String crcDataCalculado = calcularCRCData(getDadosPacote(pacote));
        
            // Se o crcHeader estiver errado, retorna -1 (existem erros)
            if (!crcDataCalculado.equals(crcData))
            return -1;

        }
                
        return 0;
    }
  
    // Aqui o 'node' é o identificador do nó!
    public static String criaAck(String pacote,no node){
        int ackDest;
        String ackPacket;
        ackDest=getSourcePacote(pacote);
        
        
        int nHop=rotas.getEntradaTabela(node.tabRota, ackDest);
       
        
        if (nHop==-1)
            return null;
        
        String pVersion=addNBits(Integer.toBinaryString(version),nBitsVersion);
        String pType=addNBits(Integer.toBinaryString(ackPacketType),nBitsPacketType);
        String pID=addNBits(Integer.toBinaryString(getIDPacote(pacote)),nBitsPacketID);
        String pTTL=addNBits(Integer.toBinaryString(127),nBitsTTL);
        String pOrigSource=addNBits(Integer.toBinaryString(node.nodeIdentification),nBitsAddresses);
        String pDest= addNBits(Integer.toBinaryString(ackDest),nBitsAddresses);
        String pNextHop=addNBits(Integer.toBinaryString(nHop),nBitsAddresses);
        String pSource=addNBits(Integer.toBinaryString(node.nodeIdentification),nBitsAddresses);
        
        //Cálculo Crc Header, falta crcData
        String crcHeader ;     
        String aux;
        String complement;
        
        String pFragmentFlag = getHeaderFragmentFlagPacote(pacote);
        String pFragmentID = getHeaderFragmentIDPacote(pacote);
        String pTotalFragments = getHeaderTotalFragmentsPacote(pacote);
        
        // Soma em Binário e guarda o resultado como String
        aux = sumBinary8bit(pVersion,pType);
        aux = sumBinary8bit(aux, pID); 
        aux = sumBinary8bit(aux, pTTL);
        aux = sumBinary8bit(aux, pOrigSource);
        aux = sumBinary8bit(aux, pDest);
        aux = sumBinary8bit(aux, pNextHop);
        aux = sumBinary8bit(aux, pSource);
        aux = sumBinary8bit(aux, pFragmentFlag);
        aux = sumBinary8bit(aux, pFragmentID);
        aux = sumBinary8bit(aux, pTotalFragments);
        
        // Complemento para 1 
        complement = complement("", aux.length());
        crcHeader = xor(aux,complement);
        
        
        ackPacket=pVersion+pType+pID+pTTL+pOrigSource+pDest+pNextHop+pSource+pFragmentFlag+pFragmentID+pTotalFragments+crcHeader;
        

        
        return ackPacket;       
    }
    
    public static String criaRouteError(){
        
        return null;
    }
    
    public static String setNewHeadersPacote(String pacote,  int TTL, int OrigSource, int NextHop){
    
        String newPacket = "";
        String crcHeader=""; 
        int tipoPacote = getTypePacote(pacote);
        
        String pVersion = getHeaderVersionPacote(pacote);
        String pType = getHeaderTypePacote(pacote);
        String pID = getHeaderIDPacote(pacote); //addNBits(Integer.toBinaryString(id),nBitsPacketID);
        String pOrigSource=addNBits(Integer.toBinaryString(OrigSource),nBitsAddresses);
        String pTTL=addNBits(Integer.toBinaryString(TTL),nBitsTTL);
        String pNextHop=addNBits(Integer.toBinaryString(NextHop),nBitsAddresses);
        String pDest = getHeaderDestinoPacote(pacote);
        String pSource = getHeaderSourcePacote(pacote);
        String pCRCHeader = getCRCHeaderPacote(pacote);
        String pFragmentFlag = getHeaderFragmentFlagPacote(pacote);
        String pFragmentID = getHeaderFragmentIDPacote(pacote);
        String pTotalFragments = getHeaderTotalFragmentsPacote(pacote);
        
        if(tipoPacote==0)
        {
            String pDados = getDadosPacote(pacote);
            String crcData = getCRCDataPacote(pacote);
            newPacket=pVersion+pType+pID+pTTL+pOrigSource+pDest+pNextHop+pSource+pFragmentFlag+pFragmentID+pTotalFragments+pCRCHeader+pDados+crcData;
            crcHeader = calcularCRCHeader(newPacket);
            newPacket=pVersion+pType+pID+pTTL+pOrigSource+pDest+pNextHop+pSource+pFragmentFlag+pFragmentID+pTotalFragments+crcHeader+pDados+crcData;
            
        
            return newPacket;
        }
        else
            newPacket=pVersion+pType+pID+pTTL+pOrigSource+pDest+pNextHop+pSource+pFragmentFlag+pFragmentID+pTotalFragments+pCRCHeader;
        
        crcHeader = calcularCRCHeader(newPacket);
        
               
        newPacket=pVersion+pType+pID+pTTL+pOrigSource+pDest+pNextHop+pSource+pFragmentFlag+pFragmentID+pTotalFragments+crcHeader;
        
        
        return newPacket;
    }  
    
    public static String setNewPacketID(String pacote){
    
        String newPacket = "";
        String crcHeader=""; 
        int tipoPacote = getTypePacote(pacote);
        
        String pVersion = getHeaderVersionPacote(pacote);
        String pType = getHeaderTypePacote(pacote);
        String pID=addNBits(Integer.toBinaryString(id),nBitsPacketID);
        String pOrigSource=getHeaderOrigSourcePacote(pacote);
        String pTTL=getHeaderTTLPacote(pacote);
        String pNextHop=getHeaderNextHopPacote(pacote);
        String pDest = getHeaderDestinoPacote(pacote);
        String pSource = getHeaderSourcePacote(pacote);
        String pCRCHeader = getCRCHeaderPacote(pacote);
        String pFragmentFlag = getHeaderFragmentFlagPacote(pacote);
        String pFragmentID = getHeaderFragmentIDPacote(pacote);
        String pTotalFragments = getHeaderTotalFragmentsPacote(pacote);
        
        if(tipoPacote==0)
        {
            String pDados = getDadosPacote(pacote);
            String crcData = getCRCDataPacote(pacote);
            newPacket=pVersion+pType+pID+pTTL+pOrigSource+pDest+pNextHop+pSource+pFragmentFlag+pFragmentID+pTotalFragments+pCRCHeader+pDados+crcData;
            crcHeader = calcularCRCHeader(newPacket);
            newPacket=pVersion+pType+pID+pTTL+pOrigSource+pDest+pNextHop+pSource+pFragmentFlag+pFragmentID+pTotalFragments+crcHeader+pDados+crcData;
            
            id++;
            if (id==32) id=0;

            return newPacket;
        }
        else
            newPacket=pVersion+pType+pID+pTTL+pOrigSource+pDest+pNextHop+pSource+pFragmentFlag+pFragmentID+pTotalFragments+pCRCHeader;
        
        crcHeader = calcularCRCHeader(newPacket);
        
        
        newPacket=pVersion+pType+pID+pTTL+pOrigSource+pDest+pNextHop+pSource+pFragmentFlag+pFragmentID+pTotalFragments+crcHeader;
        
        id++;
        if (id==32) id=0;
        
        return newPacket;
    }
    
    public static void imprimePacote(String newPacket){
    
        System.out.println("Versao do pacote: " + getVersionPacote(newPacket));
        System.out.println("Tipo do pacote: " + getTypePacote(newPacket));
        System.out.println("ID do pacote: " + getIDPacote(newPacket));
        System.out.println("TTL do pacote: " + getTTLPacote(newPacket));
        System.out.println("OrigSource do pacote: " + getOrigSourcePacote(newPacket));
        System.out.println("Destino do pacote: " + getDestinoPacote(newPacket));
        System.out.println("NextHop do pacote: " + getNextHopPacote(newPacket));
        System.out.println("Source do pacote: " + getSourcePacote(newPacket));
        System.out.println("CRC Header(binario): " + getCRCHeaderPacote(newPacket));
        System.out.println("FragmentFlag: " + getFragmentFlagPacote(newPacket));
        System.out.println("Fragment ID: " + getFragmentIDPacote(newPacket)); 
        System.out.println("Total Fragments: " + getTotalFragmentsPacote(newPacket));
        
        if(getTypePacote(newPacket)==0)
        {
        System.out.println("CRC Data(binario): " + pacote.getCRCDataPacote(newPacket));
        System.out.println("Msg:" + desencapsula(newPacket));
        }
        else
           System.out.println("O pacote é do tipo " + getTypePacote(newPacket) + " logo não transporta dados. "); 
    }  

    
}
