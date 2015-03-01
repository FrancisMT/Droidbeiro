
package protocolapi;

import java.util.LinkedList;
import java.util.Queue;


public class filaEspera {
    
    String dados;
    int destino;
    
        // Construtor para os elementos da fila de espera 
    public filaEspera(String info,int dest){
        dados=info;
        destino=dest;
    }
    
        // Cria fila de espera 
    public static Queue<filaEspera> criarFila(){        
        Queue<filaEspera> queue = new LinkedList<>();
        return queue;
    }
    
        // Adiciona um elemento à fila de espera 
    public static Queue<filaEspera> adicionarElementoFila(Queue<filaEspera> queue,String dados,int dest){
        filaEspera packet=new filaEspera(dados,dest);
        queue.offer(packet);
        return queue;
    }
    
        // Remove um elemento da fila de espera 
    public static Queue<filaEspera> removerElementoFila(Queue<filaEspera> queue){
        queue.remove();
        return queue;
    }
    
        // Devolve o primeiro elemento da fila de espera
    public static filaEspera verElementoCabeçaFila(Queue<filaEspera> queue){
        filaEspera packet;
        packet=queue.peek();
        if (packet!=null)
            return packet;
        else return null;
    }
    
        // Imprime o primeiro elemento da fila de espera
    public static void imprimirCabeçaFilaEspera(Queue<filaEspera> queue){
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
        
        // Devolve o destino do primeiro elemento da fila de espera
    public static int getDest(Queue<filaEspera> queue){
        filaEspera pckt;
        pckt = queue.peek();
        return pckt.destino;
    }
    
        // Devolve os dados do primeiro elemento da fila de espera
    public static String getDados(Queue<filaEspera> queue){
        filaEspera pckt;
        pckt = queue.peek();
        return pckt.dados;
    }
    
}
