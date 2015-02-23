
package protocol_g6_package;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


public class filaEspera {
    
    String dados;
    int destino;
    int nRetransmissoes;
    long time;
    byte socketID;
    
        // Construtor para os elementos da fila de espera 
    public filaEspera(String info,int dest){
        dados=info;
        destino=dest;
        nRetransmissoes=0;
        time=0;
        socketID=0;
    }
    
        // Cria fila de espera 
    public static ConcurrentLinkedQueue<filaEspera> criarFila(){        
        ConcurrentLinkedQueue<filaEspera> queue = new ConcurrentLinkedQueue<>();
        return queue;
    }
    
        // Adiciona um elemento à fila de espera 
    public static ConcurrentLinkedQueue<filaEspera> adicionarElementoFila(ConcurrentLinkedQueue<filaEspera> queue,String dados,int dest){
        filaEspera packet = new filaEspera(dados,dest);
        queue.offer(packet);
        
        return queue;
    }
    
        // Remove um elemento da fila de espera 
    public static ConcurrentLinkedQueue<filaEspera> removerElementoFila(ConcurrentLinkedQueue<filaEspera> queue){
        
        if(!queue.isEmpty()){
            queue.remove();
            return queue;
        }
        else
            return null;
    }
    
        // Devolve o primeiro elemento da fila de espera
    public static filaEspera verElementoCabeçaFila(ConcurrentLinkedQueue<filaEspera> queue){
        if(!queue.isEmpty()){
        
            filaEspera packet;
            packet=queue.peek();
            if (packet!=null)
                return packet;
            return null;
        }
        else 
            return null;
    }
    
        // Imprime o primeiro elemento da fila de espera
    public static void imprimirCabeçaFilaEspera(ConcurrentLinkedQueue<filaEspera> queue){
        if(!queue.isEmpty()){
            filaEspera packet;
            packet=queue.peek();
            if (packet!=null){
                System.out.println("Mensagem: "+packet.dados);
                System.out.println("Destino: "+Integer.toString(packet.destino));
            }
            else{
                 System.out.println("Fila vazia");
            }
        }
    }
        
        // Devolve o destino do primeiro elemento da fila de espera
    public static int getDest(ConcurrentLinkedQueue<filaEspera> queue){
       if(!queue.isEmpty()){
        filaEspera pckt;
        pckt = queue.peek();
        if(queue.size()!=0 && pckt != null)
            return pckt.destino;
        return -1;
       }
        else
            return -1;
    }
    
        // Devolve os dados do primeiro elemento da fila de espera
    public static String getDados(ConcurrentLinkedQueue<filaEspera> queue){
        if(!queue.isEmpty()){
            filaEspera pckt;
            pckt = queue.peek();
            return pckt.dados;
        }
        return null;
    }
    
        // Devolve nTransmissoes do primeiro elemento da fila de espera
    public static int getNumTransmissoes(ConcurrentLinkedQueue<filaEspera> queue){
        filaEspera pckt;
        pckt = queue.peek();
        if(!queue.isEmpty())
            return pckt.nRetransmissoes;
        else
            return -1;
    }
    
        // Incrementa nRetransmissoes
    public static int incrementaNumTransmissoes(ConcurrentLinkedQueue<filaEspera> queue){
       if(!queue.isEmpty()){
        filaEspera packet;
        packet=queue.peek();
        if (packet!=null){
            packet.nRetransmissoes++;
            return 1;
        }
        return 0;
       }
        else return 0;
    }
    // Devolve tempo inicio do primeiro elemento da fila de espera
    // Usado quando enviamos um pacote e estamos a espera do ack ou rrply
    // Se não chegar dentro de um determinado tempo enviamos de novo
    public static long getTime(ConcurrentLinkedQueue<filaEspera> queue){
        filaEspera pckt;
        pckt = queue.peek();
        if(queue.size()!=0)
            return pckt.time;
        else
            return -1;
    }
    
    // Atualiza tempo
    public static int setTime(ConcurrentLinkedQueue<filaEspera> queue){
        filaEspera packet;
        packet=queue.peek();
        
        if (packet!=null){
            packet.time=System.currentTimeMillis();
            return 1;
        }
        else return 0;
    }
    
     // Para Backend -> Atualiza socketID
    public static int setSocketID(ConcurrentLinkedQueue<filaEspera> queue, byte ID){
        
        if(!queue.isEmpty()){
        filaEspera packet;
        packet=queue.peek();
        
        if (packet!=null){
            packet.socketID=ID;
            return 1;
        }
        return 0;
        }
        else return 0;
    }
    
    public static long getSocketID(ConcurrentLinkedQueue<filaEspera> queue){
        if(!queue.isEmpty()){
        filaEspera pckt;
        pckt = queue.peek();
        return pckt.socketID;
        }
        return -1;
    }
}
