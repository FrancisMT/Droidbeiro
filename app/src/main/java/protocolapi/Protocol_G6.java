package protocolapi;



import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class Protocol_G6 {
    
        // Versão do protocolo 
    public static final int VERSAO_PROTOCOLO=0;
        // True se a aplicação é do backend e false se a aplicação é do android
    boolean application; 
        // Identificação da porta do socket
    final int socketPort;
        // Identificação do aparelho
    int deviceID;  
        // No em que o protocolo está a ser exectuado
    no node;   
        //Just to know when to stop
    boolean running;
    /* socket for the protocol's requests and the application's responses*/
    private Socket SocketPro;
    /* socket for the protocol's responses and the application's requests*/
    private Socket SocketApp;
    
    private ObjectInputStream AppIn;
    private ObjectInputStream ProIn;
    private ObjectOutputStream AppOut;
    private ObjectOutputStream ProOut;

    
        // Inicializa o protocolo e o nó
    public Protocol_G6(boolean machine, int port, byte system_id){
        
        application=machine;
        socketPort=port;
        deviceID= (int) system_id;
        running = true;
        
            // Inicia nó
        rotas[] tabelaRota = rotas.criaTabela();
        tabelaValidade[] tabValidade = tabelaValidade.criaTabelaValidade();
        Queue<filaEspera> filaout= filaEspera.criarFila();
        Queue<filaEspera> filain= filaEspera.criarFila();
        node = new no(filaout,filain,tabelaRota,tabValidade,((int)system_id));
        rotas.adicionaEntradaTabela(node.tabRota,((int)system_id),-1,0);
            //
    }

        // Executa um objecto da classe Protocolo_G6
    @SuppressWarnings("empty-statement")
    public void execute() throws IOException, ClassNotFoundException, InterruptedException{
        byte id;
        byte spec;
        byte[] packet;
        
        rqst request;
        rspns response;
        /*Open the sockets
        127.0.0.1 is the current machine's address*/        
        try {      
            SocketPro = new Socket("127.0.0.1", socketPort);
        } catch (IOException ex) {
            System.out.println("Protocol: Could not open SocketPro!\n");
            return;
        }
        
        try {      
            SocketApp = new Socket("127.0.0.1", socketPort);
        } catch (IOException ex) {
            System.out.println("Protocol: Could not open SocketApp!\n");
            return;
        }
        
        /*Transform socket streams into ObjectStreams in order to send objects
        instead of bytes (Out's must always be created first and flushed!)*/
        try {
            AppOut = new ObjectOutputStream(SocketApp.getOutputStream());
            ProOut = new ObjectOutputStream(SocketPro.getOutputStream());
            AppOut.flush();
            ProOut.flush();
            AppOut.reset();
            ProOut.reset();
            AppIn = new ObjectInputStream(SocketApp.getInputStream());
            ProIn = new ObjectInputStream(SocketPro.getInputStream());
           
        } catch (IOException ex) {
            System.out.println("Could not create Object Streams!\n");
            return;
        }
        ///////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////
        
        /*Exchange random messages just for demostration purposes
        On the real project these messages would be either writen by the firefighter
        or received by bluetooth*/
        try {
            
            //Protocol waits for an application's request
            request = (rqst)AppIn.readObject();
            System.out.println("Protocol: App requested id=" + request.id + " spec=" + request.spec + 
                    " packet=" + Arrays.toString(request.packet));
            
            
            /*Protocol replies (in this action the reply id is not relevant,
            nevertheless it must be written to the objectStream)*/
            response = new rspns((byte)0x00);
            System.out.println("Protocol: Protocol replied id=" + response.id);
            AppOut.writeObject(response);
            
            
            /*Now that the protocol has replied (with an ack) it can
            do what the application asked, which is to unpack the packet and then 
            decide what to do with it.
            Let's supose the protocol found that the packet's destination is not
            the current machine. Therefore, the protocol processes the packet and 
            asks the application to send it to the next node.
            Since this action is a new one and it is a request from the protocol,
            it must be sent through the SocketPro (ProIn and ProOut objectStreams)
            */
            
            //Simulate processing time
            TimeUnit.MILLISECONDS.sleep(10);
            
            //Protocol creates a new rqst
            
            //Id of this action
            id = (byte)0x11;
            
            //Spec of the radio output
            spec = (byte)0x11;
            
            //Processed packet
            packet = new byte[] {(byte)0x04, (byte)0x03, (byte)0x02, (byte)0x01};
            
            request = new rqst(id, spec, packet);
            
            //Protocol sends request
            System.out.println("\nProtocol asks application to send a message:");
            System.out.println("Protocol: Protocol requested id=" + request.id + " spec=" + request.spec 
                    + " packet=" + Arrays.toString(request.packet));
            ProOut.writeObject(request);
            
            /*Protocol waits for response, which in this case is relevant, since
            there is a chance of something going wrong*/
            response = (rspns)ProIn.readObject();
            System.out.println("Protocol: App replied id=" + response.id );
            
            /*And the procces repeats itself, without a known end. Therefore for
            demonstration purposes a loop is here to keep the protocol
            running until the application kills it.*/
            while(running);
            
        } catch (IOException ex) {
            System.out.println("Protocol: Could not send rqst or receive rspns!\n");
        } catch (ClassNotFoundException ex) {
            System.out.println("Protocol: No rspns object found in the socket!\n");
        }
        /*Thread t = new Thread(new MyRunnable(Protocol_G6.this));
        t.start();*/
    }
    
    // O método main simula o lado do Android
    public static void main(String[] args) throws IOException, ClassNotFoundException {

    }
    
    public void Stop(){
        running = false;
    }
    /*
    public class MyRunnable implements Runnable {
        
        private Protocol_G6 threadProtocol;
        
        public MyRunnable(Protocol_G6 protocol){
            this.threadProtocol = protocol;
        }
        
        public void run() {
            String packet="";
            int nid=this.threadProtocol.node.nodeIdentification;
            //rotas.imprimeTabela(this.threadProtocol.node.tabRota,nid);
            
            //Quando recebe um pacote chama a função recebePacote
            // getPacoteSocket
            //this.threadProtocol.node.recebePacote(packet);
        }
    }*/
}
    

  

