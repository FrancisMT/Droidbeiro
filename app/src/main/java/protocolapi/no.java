
package protocolapi;

import java.util.Queue;
import static protocolapi.Protocol_G6.VERSAO_PROTOCOLO;

public class no {
     
    int nodeIdentification;
    rotas[] tabRota;
    tabelaValidade[] tabValidade;
    Queue<filaEspera> filain;
    Queue<filaEspera> filaout;
    boolean GSM;
    
    public no(Queue<filaEspera> queue,Queue<filaEspera> queue2, rotas[] table1,tabelaValidade[] table2,int nodeID){
        
        nodeIdentification = nodeID;
        tabRota = table1;
        tabValidade=table2;
        filaout = queue;
        filain = queue2;
        GSM = false;
    }
    
    public int reencaminhaPacote(String pckt){
    
        int dest=pacote.getDestinoPacote(pckt);
        int ttl=pacote.getTTLPacote(pckt)-1;
        int nHop=rotas.getEntradaTabela(tabRota,dest); 
        String dados=pacote.getDadosPacote(pckt);
        String newPacket=pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification);
        //enviaPacote(newPacket,dest);
        return 1;
    }
    
    public int recebePacote(String packet){
               
        int versao,id,source;
        
        versao=pacote.getVersionPacote(packet);
            
        if (versao!=VERSAO_PROTOCOLO)
            return -1;
        if (pacote.verificaErros(packet)==-1)
            return -2;
            
        id=pacote.getIDPacote(packet);
        source=pacote.getSourcePacote(packet);
                   
        if (tabelaValidade.verificaValidade(this.tabValidade, id, source)==1);
            return -3;
        
        
        //return 0;
    }
}
