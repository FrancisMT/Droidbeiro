

package protocolapi;


public class rotas {
        
        // id central
    public static final int CENTRAL=0;
        // id broadcast
    public static final int BROADCAST=255;
        // Número máximo de nós
    public static final int MAXNODES=256;
        // Número de minutos durante os quais uma nova rota é válida
    public static final int minutosValidade=5; 
    
    
        // Especifica o destino da rota
    int destino;
    
        // Especifica o next hop para o destino 
    int nextHop;
    
        // Especifica o hop count até o destino 
    int hopCount;
    
        // Indica se a rota é válida (true) ou não (boolean)
        // Isto deve-se ao facto de a tabela para os 255 nós ser iniciada quando
        // o protocolo inicia. Assim é necessário saber quais as rotas válidas
    boolean validade;
    
        // System.currentTimeMillis(): Especifica a altura em que a rota foi
        // validada pela última vez.
    long timeStamp; 
    
    
        // Construtor das rotas
    public rotas(int dest,int nHop,int hCount,long tStamp,boolean valid){
        destino=dest;
        nextHop=nHop;
        hopCount=hCount;
        timeStamp=tStamp;
        validade=valid;  
    }
    
        // Cria tabela com uma entrada para cada nó da rede.
    public static rotas[] criaTabela(){
        
        rotas[] tabela = new rotas[MAXNODES];
                
        for(int i=0;i<MAXNODES-1;i++){
            tabela[i] = new rotas(i,-1,-1,0,false);
        }
        
            // endereço broadcast
        tabela[MAXNODES-1] = new rotas((MAXNODES-1),(MAXNODES-1),0,0,true);
        
        return tabela;
    }
    
        // Adiciona uma entrada na tabela
    public static void adicionaEntradaTabela(rotas[] tabela,int dest,int nHop,int hCount){
        long tstamp=System.currentTimeMillis();
        tabela[dest]=new rotas(dest,nHop,hCount,tstamp,true);
    }
    
        // Remove uma entrada da tabela
    public static void removeEntradaTabela(rotas[] tabela,int dest){
        tabela[dest]=new rotas(dest,-1,-1,0,false);
    }
    
    // Imprime na consola a tabela de rotas    
    public static void imprimeTabela(rotas[] tabela,int id){
        for(int i=0;i<MAXNODES;i++){
            System.out.println("Tabela do nó: " + id);
            System.out.println("Destino: " + tabela[i].destino);
            System.out.println("NextHop: " + tabela[i].nextHop);
            System.out.println("HopCount: " + tabela[i].hopCount);
            System.out.println("Timestamp: " + tabela[i].timeStamp);
            System.out.println("Validade: " + tabela[i].validade);     
            System.out.println("\n\n");
        }
    }
    
    // Imprime na consola uma determinada entrada da tabela de rotas
    public static void imprimeEntradaDaTabela(rotas[] tabela,int dest,int id){
        System.out.println("Tabela do nó: " + id);
        System.out.println("Destino: " + tabela[dest].destino);
        System.out.println("NextHop: " + tabela[dest].nextHop);
        System.out.println("HopCount: " + tabela[dest].hopCount);
        System.out.println("Timestamp: " + tabela[dest].timeStamp);
        System.out.println("Validade: " + tabela[dest].validade);  
    }
    
    // Devolve o nextHop para um determinado destino. Caso não tenha rota válida para esse destino devolve 1 
    public static int getEntradaTabela(rotas[] tabela,int dest){
        double tempoValidade=minutosValidade*60.0*1000.0;
        if ( (tabela[dest].validade==false) || ((System.currentTimeMillis()-tabela[dest].timeStamp)>(tempoValidade)) ){
            removeEntradaTabela(tabela,dest);
            return -1;
        }
        else{
            return tabela[dest].nextHop;
        }
        
    }
    
    
}
