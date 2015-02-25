package protocol_g6_package;

import java.util.List;
import java.util.Queue;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import static protocol_g6_package.Protocol_G6.VERSAO_PROTOCOLO;
import static protocol_g6_package.rotas.ENDEREÇO_NULO;
import static protocol_g6_package.rotas.CENTRAL;
import static protocol_g6_package.rotas.BROADCAST;
import static protocol_g6_package.rotas.MAXNODES;
//import static protocol_g6_package.simuladorv2.DEBUG;
//import static protocol_g6_package.simuladorv2.DEBUG_DETAILED;
//import static protocol_g6_package.simuladorv2.DEBUG_FINAL;
import static protocol_g6_package.Protocol_G6.DEBUG;
import static protocol_g6_package.Protocol_G6.DEBUG_DETAILED;
import static protocol_g6_package.Protocol_G6.DEBUG_FINAL;
import protocolapi.rqst;
import protocolapi.rspns;

public class no {

    volatile int nodeIdentification;
    volatile rotas[] tabRota;
    volatile tabelaValidade[] tabValidade;
    ConcurrentLinkedQueue<filaEspera> filain;
    ConcurrentLinkedQueue<filaEspera> fila_dados_in;
    ConcurrentLinkedQueue<filaEspera> filaout;
    // List<waitingPackets> fila_espera_ACK_RRply;
    ConcurrentLinkedQueue<filaEspera> fila_dados_out;
    ConcurrentLinkedQueue<rqst> filaFragmentacao;
    ConcurrentLinkedQueue<Byte> socketQueue;

    volatile int Flag_app_Back; //se =0 é App Se =1 é BackEnd
    volatile boolean GSM;
    volatile int espera_rrply; //Quando está à espera de um rrply não envia mais nenhum
    String[][] fragmentacao = new String[255][10];
    boolean rreq = false;
    volatile int flag_frag;
    ConcurrentHashMap<Integer, waitingPackets> Hashmap = new ConcurrentHashMap<Integer, waitingPackets>();

    public no(ConcurrentLinkedQueue<filaEspera> queue, ConcurrentLinkedQueue<filaEspera> queue2, ConcurrentLinkedQueue<filaEspera> queue3, ConcurrentLinkedQueue<filaEspera> queue5, ConcurrentLinkedQueue<rqst> queue6, rotas[] table1, tabelaValidade[] table2, int nodeID, ConcurrentLinkedQueue<Byte> sckQueue, ConcurrentHashMap<Integer, waitingPackets> map) {

        nodeIdentification = nodeID;
        tabRota = table1;
        tabValidade = table2;
        filaout = queue;
        filain = queue2;
        fila_dados_in = queue3;
        fila_dados_out = queue5;
        //fila_espera_ACK_RRply = queue4;
        filaFragmentacao = queue6;
        GSM = false; // Inicialmente é falso
        espera_rrply = 0;
        if (this.nodeIdentification == 1) {
            this.Flag_app_Back = 1;
        } else {
            this.Flag_app_Back = 0;
        }
        socketQueue = sckQueue;
        flag_frag = 1;
        Hashmap = map;
        //if(this.Flag_app_Back==1)
        //Crias tabela sockets do backend
    }

    public void reencaminhaPacote(String packet, int dest, int nHop) {

        int ttl = pacote.getTTLPacote(packet) - 1;
        //System.out.println("NO: " + this.nodeIdentification);
        //System.out.println("Pacote para reencaminhar:::::");
        //pacote.imprimePacote(packet);
        String Npcket = pacote.setNewHeadersPacote(packet, ttl, this.nodeIdentification, nHop);
        //System.out.println("Pacote reencaminha:::::");
        //pacote.imprimePacote(Npcket);
        filaEspera.adicionarElementoFila(this.filaout, Npcket, dest);
        /*System.out.println("\n\nFILA OUT");
         System.out.println("tamanho filaout: "+filaout.size());
         filaEspera.imprimirCabeçaFilaEspera(this.filaout);
         System.out.println("\n\n");
         pacote.imprimePacote(filaEspera.getDados(filaout));
         System.out.println("\n\n");*/
    }

    public int recebePacote() {

        //synchronized(this.fila_espera_ACK_RRply){ 
        int versao, id, source;

        if (filaEspera.verElementoCabeçaFila(this.filain) != null) { //verifica se tem elementos na fila

            /*
             if (nodeIdentification==3){
             System.out.println("\nXXXXXX");
             pacote.imprimePacote(filaEspera.verElementoCabeçaFila(this.filain).dados);
             }*/
            /*if(this.nodeIdentification==1){  
             System.out.println("PACOTE RECEBIDO PELA CENTRAL");
        
             pacote.imprimePacote(filaEspera.getDados(this.filain));
             }*/
            String packet = filaEspera.getDados(this.filain);
            versao = pacote.getVersionPacote(packet);

            if (versao != VERSAO_PROTOCOLO) { //se n for o nosso protocolo descarta
                if (DEBUG_FINAL) {
                    if (this.nodeIdentification != 1) {
                        System.out.println("Pacote recebido pelo nó " + this.nodeIdentification + " não corresponde à nossa versão");
                    } else {
                        System.out.println("Pacote recebido pelo Backend não corresponde à nossa versão");
                    }
                }
                filaEspera.removerElementoFila(this.filain);
                return -1;
            }
            if (pacote.verificaErros(packet) == -1) { //se tiver erros descarta
                if (DEBUG_FINAL) {
                    if (this.nodeIdentification != 1) {
                        System.out.println("Pacote recebido pelo nó " + this.nodeIdentification + " contém erros");
                    } else {
                        System.out.println("Pacote recebido pelo Backend contém erros");
                    }

                }
                filaEspera.removerElementoFila(this.filain);
                return -2;
            }

            id = pacote.getIDPacote(packet);
            source = pacote.getSourcePacote(packet);
            int type = pacote.getTypePacote(packet);
            //f (pacote.getTypePacote(packet) == 3 && pacote.getDestinoPacote(packet)==this.nodeIdentification)

            //System.out.println("É aqui");
            //pacote.imprimePacote(packet);
            if ((tabelaValidade.verificaValidade(this.tabValidade, id, source, type) == 0
                    && source != this.nodeIdentification && type != 3) || source == this.nodeIdentification) { //sejativer visto o pacote descarta
                if (false) {
                    if (this.nodeIdentification != 1) {
                        System.out.println("Pacote recebido pelo nó " + this.nodeIdentification
                                + " é um pacote repetido do tipo " + pacote.getTypePacote(packet)
                                + " proveniente do nó " + source + " e enviada pelo nó " + pacote.getOrigSourcePacote(packet));
                    } else {
                        System.out.println("Pacote recebido pelo Backend é um pacote repetido do tipo " + pacote.getTypePacote(packet)
                                + " proveniente do nó " + source + " e enviada pelo nó " + pacote.getOrigSourcePacote(packet));
                    }

                    // pacote.imprimePacote(packet);
                    //tabelaValidade.imprimeTabelaValidade(this.tabValidade);
                }
                filaEspera.removerElementoFila(this.filain);
                return -3;
            }

            if (pacote.getNextHopPacote(packet) != this.nodeIdentification
                    && pacote.getNextHopPacote(packet) != BROADCAST) { //se nao for o next hop descarta
                if (false) {
                    if (this.nodeIdentification != 1) {
                        System.out.println("Pacote recebido pelo nó " + this.nodeIdentification + " não era para si destinado");
                    } else {
                        System.out.println("Pacote recebido pelo Backend não era para si destinado");
                    }

                }
                filaEspera.removerElementoFila(this.filain);
                return -4;
            }

            ///
            /*
             System.out.println("Dono tabela -> "+this.nodeIdentification);
             System.out.println("id= "+id);
             System.out.println("source= "+source);
             ///*/
            tabelaValidade.addTabelValidade(this.tabValidade, id, source, type);

            ///
            /*
             System.out.println("Tabela de Validade do no "+ this.nodeIdentification);
             tabelaValidade.imprimeTabelaValidade(this.tabValidade);
             ///*/
            int dest = pacote.getDestinoPacote(packet);

            //int num=0;
            //if(num==0){
            //rotas.adicionaEntradaTabela(this.tabRota, dest, 1,0);
            rotas.adicionaEntradaTabela(this.tabRota, source, pacote.getOrigSourcePacote(packet), 0);
            //num++;
            //}

            int nHop = rotas.getEntradaTabela(this.tabRota, dest);
            if ((pacote.getNextHopPacote(packet) == this.nodeIdentification)
                    && dest != this.nodeIdentification) { //Se for so o next hop

                if (type == 0 || type == 3) { //se for info ou ack reencaminha
                    if (nHop != -1) {
                        reencaminhaPacote(packet, dest, nHop);
                        if (DEBUG) {
                            System.out.println("Pacote recebido pelo nó " + this.nodeIdentification + " é reencaminhado (O nó é apenas o next hop do pacote)");
                        }
                    } else {

                        synchronized (this.Hashmap) {

                            Set<Integer> keySet = Hashmap.keySet();
                            Iterator<Integer> keySetIterator = keySet.iterator();
                            int key = 0;
                            if (!Hashmap.isEmpty()) {

                                while (keySetIterator.hasNext()) {

                                    key = keySetIterator.next();

                                    //System.out.println("Size: " + this.Hashmap.size());
                                    String compPacket1 = this.Hashmap.get(key).waitPacket;
                                    if (pacote.getTypePacote(compPacket1) == 1 && (pacote.getDestinoPacote(packet) == pacote.getDestinoPacote(compPacket1))) {
                                        this.rreq = true;
                                    }
                                }
                            }
                            waitingPackets pac = waitingPackets.criarElemento(packet);
                            this.Hashmap.put(key + 1, pac);
                            //waitingPackets.adicionarElementoLista(this.fila_espera_ACK_RRply, packet);
                            if (this.rreq == false) {

                                String RReq = pacote.criaRReq(dest, this.nodeIdentification);
                                waitingPackets RReq2 = waitingPackets.criarElemento(packet);

                                Set<Integer> keySet2 = Hashmap.keySet();
                                Iterator<Integer> keySetIterator2 = keySet2.iterator();
                                int key2 = 0;

                                while (keySetIterator2.hasNext()) {

                                    key2 = keySetIterator2.next();
                                }
                                this.Hashmap.put(key2 + 1, RReq2);

                                espera_rrply = 1;
                            }

                            if (false) {
                                System.out.println("Pacote recebido pelo nó " + this.nodeIdentification + " era para ser reencaminhado mas devido à inexistência de rota cria-se RRequest ");
                            }
                            this.rreq = false;
                        }
                    }

                } else if (type == 2) {
                    nHop = rotas.getEntradaTabela(this.tabRota, dest);
                    if (nHop == -1) {
                        System.out.println("Pacote de RReply recebido pelo nó " + this.nodeIdentification + ", no entanto n existe rota para retransmitir!!");
                    } else {
                        reencaminhaPacote(packet, dest, nHop);
                        if (DEBUG_DETAILED) {
                            System.out.println("Pacote de RReply recebido pelo nó " + this.nodeIdentification + " é reencaminhado (nó não é o destino do pacote)");
                        }
                    }
                }
            } else if ((pacote.getNextHopPacote(packet) == BROADCAST) && dest != this.nodeIdentification) {
                if (type == 0) { //se for BROADCAST reencaminha
                    reencaminhaPacote(packet, dest, BROADCAST);
                    if (DEBUG) {
                        System.out.println("Pacote de info (broadcast) recebido pelo nó " + this.nodeIdentification + " é retransmitido em broadcast");
                    }
                } else if (type == 1) { //se for rreq reencaminha e actualiza tabela -> actualizar

                    reencaminhaPacote(packet, dest, BROADCAST);
                    //rotas.imprimeTabela(this.tabRota,this.nodeIdentification);
                    if (DEBUG_DETAILED) {
                        System.out.println("Pacote de RRequest recebido pelo nó " + this.nodeIdentification + " é enviado em broadcast (nó não é o destino do pacote)");
                    }
                }

            } else if (dest == this.nodeIdentification) {

                if (type == 0) { //se for o destino juntar os pacotes e mandar dados p android

                    if (pacote.getFragmentFlagPacote(packet) == 1) {
                        int fragid = pacote.getFragmentIDPacote(packet);

                        fragmentacao[source][fragid] = pacote.getDadosPacote(packet);

                        //filaEspera.removerElementoFila(this.filain);
                        String Ack = pacote.criaAck(packet, this);
                        //System.out.println("ACK: "+ Ack);
                        //pacote.imprimePacote(Ack);
                        filaEspera.adicionarElementoFila(filaout, Ack, source);

                        for (int i = 0; i < (pacote.getTotalFragmentsPacote(packet) + 1); i++) {
                            if (fragmentacao[source][i] == null) {
                                filaEspera.removerElementoFila(this.filain);
                                return 1;
                            }
                        }
                        String dados = "";
                        for (int i = 0; i < (pacote.getTotalFragmentsPacote(packet) + 1); i++) {
                            dados += fragmentacao[source][i];

                        }
                        //System.out.println("dados juntos "+ pacote.binaryStringToText(dados));

                        filaEspera.adicionarElementoFila(this.fila_dados_out, dados, this.nodeIdentification);
                        for (int j = 0; j < (pacote.getTotalFragmentsPacote(packet) + 1); j++) {
                            fragmentacao[source][j] = null;
                        }
                        //System.out.println("Je " + this.nodeIdentification +" Recebi pacote de info do no "+source);
                        if (DEBUG_FINAL) {
                            if (this.nodeIdentification != 1) {
                                System.out.println("Nó " + this.nodeIdentification + " recebe Info (com fragmentação)");
                            } else {
                                System.out.println("Backend recebe Info (com fragmentação)");
                            }

                        }
                    } else {

                        String dados = pacote.getDadosPacote(packet);
                        // System.out.println("Je " + this.nodeIdentification +" Recebi pacote de info do no "+source);
                        //System.out.println("\n*****************************");
                        //pacote.imprimePacote(packet);
                        //System.out.println("*****************************\n");
                        //System.out.println("*****************************this.nodeIdentification: "+this.nodeIdentification);
                        filaEspera.adicionarElementoFila(this.fila_dados_out, dados, this.nodeIdentification);
                        String Ack = pacote.criaAck(packet, this);
                        filaEspera.adicionarElementoFila(this.filaout, Ack, source);
                        //pacote.imprimePacote(Ack);
                        if (DEBUG_FINAL) {
                            if (this.nodeIdentification != 1) {
                                System.out.println("Nó " + this.nodeIdentification + " recebe Info (sem fragmentação)");
                            } else {
                                System.out.println("Backend recebe Info (sem fragmentação)");
                            }

                        }
                    }

                } else if (type == 1) {
                    String RRep = pacote.criaRReply(packet, this);
                    filaEspera.adicionarElementoFila(filaout, RRep, source);
                    //rotas.imprimeTabela(this.tabRota,this.nodeIdentification);
                    if (DEBUG_FINAL) {
                        if (this.nodeIdentification != 1) {
                            System.out.println("Nó " + this.nodeIdentification + " recebe RRequest");
                        } else {
                            System.out.println("Backend recebe RRequest");
                        }

                    }
                } else if (type == 2) {
                    //System.out.println("**************Size"+this.fila_dados_in.size());
                    if (DEBUG_FINAL) {
                        if (this.nodeIdentification != 1) {
                            System.out.println("Nó " + this.nodeIdentification + " recebe RReply");
                        } else {
                            System.out.println("Backend recebe RReply");
                        }

                    }

                    synchronized (this.Hashmap) {

                    //waitingPackets.imprimeFila(this.fila_espera_ACK_RRply);
                        // System.out.println("**************Size"+this.fila_espera_ACK_RRply.size());
                        Set<Integer> keySet = Hashmap.keySet();
                        Iterator<Integer> keySetIterator = keySet.iterator();

                        if (!Hashmap.isEmpty()) {

                            while (keySetIterator.hasNext()) {

                                int key = keySetIterator.next();

                                if (Hashmap.get(key) != null) {

                                    String compPacket = this.Hashmap.get(key).waitPacket;//waitingPackets.verElementoLista(this.fila_espera_ACK_RRply, i).waitPacket;

                                    if (pacote.getTypePacote(compPacket) == 1 && pacote.getDestinoPacote(compPacket) == source) {
                                //System.out.println("**************Pacote a remover\n");
                                        //pacote.imprimePacote(compPacket);

                                        this.Hashmap.remove(key);
                                        //waitingPackets.removerElementoLista(this.fila_espera_ACK_RRply, i);
                                        espera_rrply = 0;
                                        if (!Hashmap.isEmpty()) {
                                            Set<Integer> keySet2 = Hashmap.keySet();
                                            Iterator<Integer> keySetIterator2 = keySet2.iterator();

                                            while (keySetIterator2.hasNext()) {

                                                int key2 = keySetIterator2.next();

                                                String compPacket1 = this.Hashmap.get(key2).waitPacket;//waitingPackets.verElementoLista(this.fila_espera_ACK_RRply, j).waitPacket;

                                                if (pacote.getTypePacote(compPacket1) == 0 && pacote.getDestinoPacote(compPacket1) == source) {

                                            //System.out.println("**************Enviei fragmento" + pacote.getFragmentIDPacote(compPacket1));
                                                    //pacote.imprimePacote(compPacket1);
                                                    //compPacket1 = pacote.setNewPacketID(compPacket1);
                                                    //System.out.println("Pacote depois :::::");
                                                    //pacote.imprimePacote(compPacket1);
                                                    reencaminhaPacote(compPacket1, pacote.getDestinoPacote(compPacket1), rotas.getEntradaTabela(tabRota, pacote.getDestinoPacote(compPacket1)));
                                                    this.Hashmap.remove(key2);
                                            //waitingPackets.removerElementoLista(this.fila_espera_ACK_RRply, j);

                                                }
                                            }
                                        }
                                        if (DEBUG) {
                                            System.out.println("Pacote RReply recebido pelo nó " + this.nodeIdentification + ". Actualiza tabelas");
                                        }

                                    }
                                }
                            }
                        }
                    }
                } else if (type == 3) {
                    //System.out.println("Pacote Ack recebido pelo nó "+this.nodeIdentification);
                    //pacote.imprimePacote(packet);
                    //System.out.println("Eu "+this.nodeIdentification+ " Recebeu ack do no "+ source);
                    if (DEBUG_FINAL) {
                        if (this.nodeIdentification != 1) {
                            System.out.println("Nó " + this.nodeIdentification + " recebe Ack");
                        } else {
                            System.out.println("Backend recebe Ack");
                        }
                    }

                    synchronized (this.Hashmap) {

                        if (!Hashmap.isEmpty()) {
                            Set<Integer> keySet3 = Hashmap.keySet();
                            Iterator<Integer> keySetIterator3 = keySet3.iterator();

                            while (keySetIterator3.hasNext()) {

                                int key3 = keySetIterator3.next();
                                //System.out.println("Size: " + this.Hashmap.size());
                                String compPacket = this.Hashmap.get(key3).waitPacket;//waitingPackets.verElementoLista(this.fila_espera_ACK_RRply, i).waitPacket;

                                if (pacote.getTypePacote(compPacket) == 0) {
                                    if (pacote.getDestinoPacote(compPacket) == source && (pacote.getIDPacote(compPacket) == id)) {
                                        if (pacote.getFragmentFlagPacote(packet) == 1) {
                                            if (pacote.getFragmentIDPacote(packet) == pacote.getFragmentIDPacote(compPacket)) {
                                                this.Hashmap.remove(key3);
                                                //waitingPackets.removerElementoLista(this.fila_espera_ACK_RRply, i);

                                            }
                                        } else {
                                            this.Hashmap.remove(key3);
                                            //waitingPackets.removerElementoLista(this.fila_espera_ACK_RRply, i);

                                        }
                                    }
                                }
                            }
                        }
                        if (!Hashmap.isEmpty()) {

                            Set<Integer> keySet4 = Hashmap.keySet();
                            Iterator<Integer> keySetIterator4 = keySet4.iterator();

                            while (keySetIterator4.hasNext()) {

                                int key4 = keySetIterator4.next();
                                String compPacket2 = this.Hashmap.get(key4).waitPacket;
                                if (pacote.getTypePacote(compPacket2) == 0 && (pacote.getFragmentFlagPacote(compPacket2) == 1)) {//waitingPackets.verElementoLista(this.fila_espera_ACK_RRply, i).waitPacket) == 0 && (pacote.getFragmentFlagPacote(waitingPackets.verElementoLista(this.fila_espera_ACK_RRply, i).waitPacket) == 1)) {
                                    this.flag_frag = 0;
                                    break;
                                } else {
                                    this.flag_frag = 1;
                                }
                            }
                        } else {
                            this.flag_frag = 1;
                        }

                        String pac = null;
                        if (this.flag_frag == 1) {
                            if (!(this.filaFragmentacao.isEmpty())) {
                                pac = pacote.byteArray2binaryString(this.filaFragmentacao.peek().packet);
                                //System.out.println("pac: " + pacote.binaryStringToText(pac));
                                filaEspera.adicionarElementoFila(this.fila_dados_in, pac, this.filaFragmentacao.peek().spec);
                                this.filaFragmentacao.remove();
                                preparaPacote();
                            }
                        }
                    }
                }
            } else if (dest == BROADCAST) {

                if (pacote.getFragmentFlagPacote(packet) == 1) {

                }

                String dados = pacote.getDadosPacote(packet);

                filaEspera.adicionarElementoFila(this.fila_dados_out, dados, this.nodeIdentification);

            }

        }

        filaEspera.removerElementoFila(this.filain);
        return 0;
    }

    public int preparaPacote() {

        if (filaEspera.verElementoCabeçaFila(this.fila_dados_in) != null) {
            int numFrag = calculaNumFragmentos(filaEspera.getDados(this.fila_dados_in));
            if (this.flag_frag == 1 || numFrag == 0) {

                if (this.Flag_app_Back == 0) {
                    //Prepara Pacote para dados recebidos do Android
                    if (filaEspera.verElementoCabeçaFila(this.fila_dados_in) != null) {
                        //dest e sempre central
                        int dest = CENTRAL;
                        int ttl = 127;
                        String dados = filaEspera.getDados(this.fila_dados_in);

                        //rotas.adicionaEntradaTabela(this.tabRota, CENTRAL, 2, 0);
                        if (rotas.getEntradaTabela(this.tabRota, dest) != -1) {

                            if (DEBUG_DETAILED) {
                                System.out.println("Nó " + this.nodeIdentification + " quer enviar informação e tem rota.");
                            }
                            int nHop = rotas.getEntradaTabela(this.tabRota, dest);
                            int TotalFragments = this.calculaNumFragmentos(dados);
                            int FragmentFlag;
                            if (TotalFragments == 0) {
                                FragmentFlag = 0;
                                filaEspera.adicionarElementoFila(filaout, pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, 0, TotalFragments), dest);

                            } else {
                                FragmentFlag = 1;
                                for (int i = 0; i < TotalFragments; i++) {
                                    filaEspera.adicionarElementoFila(filaout, pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, i, TotalFragments - 1), dest);
                                }
                                this.flag_frag = 0;
                            }
                            filaEspera.removerElementoFila(this.fila_dados_in);

                            espera_rrply = 0;

                        } else if (espera_rrply == 0) {

                            if (DEBUG_DETAILED) {
                                System.out.println("Nó " + this.nodeIdentification + " quer enviar informação mas não tem rota. Envia RRequest e espera RReply ");
                            }
                            int nHop = 0;
                            int TotalFragments = this.calculaNumFragmentos(dados);
                            int FragmentFlag;
                            String RReq = pacote.criaRReq(dest, this.nodeIdentification);
                            filaEspera.adicionarElementoFila(filaout, RReq, dest);
                            if (TotalFragments == 0) {
                                FragmentFlag = 0;
                                waitingPackets packet = new waitingPackets(pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, 0, TotalFragments));

                                synchronized (this.Hashmap) {

                                    Set<Integer> keySet = Hashmap.keySet();
                                    Iterator<Integer> keySetIterator = keySet.iterator();
                                    int key = 0;
                                    while (keySetIterator.hasNext()) {

                                        key = keySetIterator.next();
                                    }

                                    this.Hashmap.put(key + 1, packet);
                                }
                                //waitingPackets.adicionarElementoLista(this.fila_espera_ACK_RRply, pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, 0, TotalFragments));
                            } else {

                                FragmentFlag = 1;
                                for (int i = 0; i < TotalFragments; i++) {

                                    waitingPackets packet = new waitingPackets(pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, i, TotalFragments - 1));

                                    synchronized (this.Hashmap) {

                                        Set<Integer> keySet = Hashmap.keySet();
                                        Iterator<Integer> keySetIterator = keySet.iterator();
                                        int key = 0;
                                        while (keySetIterator.hasNext()) {

                                            key = keySetIterator.next();
                                        }

                                        this.Hashmap.put(key + 1, packet);
                                    }
                                    //waitingPackets.adicionarElementoLista(this.fila_espera_ACK_RRply, pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, i, TotalFragments - 1));
                                }
                                this.flag_frag = 0;
                            }
                            //System.out.println("FILAA\n");
                            //waitingPackets.imprimeFila(this.fila_espera_ACK_RRply);
                            filaEspera.removerElementoFila(this.fila_dados_in);

                            //Envia para Android->Adiciona filaout e passa para Protocol_G6
                            espera_rrply = 1;
                        } else {

                            int nHop = 0;
                            int TotalFragments = this.calculaNumFragmentos(dados);
                            int FragmentFlag;
                            if (TotalFragments == 0) {
                                FragmentFlag = 0;
                                waitingPackets packet = new waitingPackets(pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, 0, TotalFragments));

                                synchronized (this.Hashmap) {

                                    Set<Integer> keySet = Hashmap.keySet();
                                    Iterator<Integer> keySetIterator = keySet.iterator();
                                    int key = 0;
                                    while (keySetIterator.hasNext()) {

                                        key = keySetIterator.next();
                                    }

                                    this.Hashmap.put(key + 1, packet);
                                }
                                //waitingPackets.adicionarElementoLista(this.fila_espera_ACK_RRply, pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, 0, TotalFragments));
                            } else {
                                FragmentFlag = 1;
                                for (int i = 0; i < TotalFragments; i++) {
                                    waitingPackets packet = new waitingPackets(pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, i, TotalFragments - 1));

                                    synchronized (this.Hashmap) {

                                        Set<Integer> keySet = Hashmap.keySet();
                                        Iterator<Integer> keySetIterator = keySet.iterator();
                                        int key = 0;
                                        while (keySetIterator.hasNext()) {

                                            key = keySetIterator.next();
                                        }

                                        this.Hashmap.put(key + 1, packet);
                                    }
                                    //waitingPackets.adicionarElementoLista(this.fila_espera_ACK_RRply, pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, i, TotalFragments - 1));
                                }
                                this.flag_frag = 0;
                            }
                            //System.out.println("FILAA\n");
                            //waitingPackets.imprimeFila(this.fila_espera_ACK_RRply);
                            filaEspera.removerElementoFila(this.fila_dados_in);

                        }
                    }

                    return 1;
                } else { //Central
                    //Prepara Pacote para dados recebidos do Backhend
                    if (filaEspera.verElementoCabeçaFila(this.fila_dados_in) != null) {

                        int dest = filaEspera.getDest(this.fila_dados_in);
                        //dest e sempre central
                        int ttl = 127;
                        String dados = filaEspera.getDados(this.fila_dados_in);

                        // MODIFICAÇÃO: Para a central põe-se directamente na filaout a central não tem tabela de rotas
                        int nHop = 0;
                        int TotalFragments = this.calculaNumFragmentos(dados);
                        int FragmentFlag;
                        if (TotalFragments == 0) {
                            FragmentFlag = 0;
                            filaEspera.adicionarElementoFila(filaout, pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, 0, TotalFragments), dest);

                        } else {
                            FragmentFlag = 1;
                            for (int i = 0; i < TotalFragments; i++) {
                                filaEspera.adicionarElementoFila(filaout, pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, i, TotalFragments - 1), dest);
                            }
                            this.flag_frag = 0;
                        }
                        filaEspera.removerElementoFila(this.fila_dados_in);
                        espera_rrply = 0;

                        /*
                         //rotas.adicionaEntradaTabela(this.tabRota, CENTRAL, 2, 0);
                         if (rotas.getEntradaTabela(this.tabRota, dest) != -1) {
                         if (DEBUG) {
                         System.out.println("Nó " + this.nodeIdentification + " quer enviar informação e tem rota.");
                         }
                         int nHop = rotas.getEntradaTabela(this.tabRota, dest);
                         int TotalFragments = this.calculaNumFragmentos(dados);
                         int FragmentFlag;
                         if (TotalFragments == 0) {
                         FragmentFlag = 0;
                         filaEspera.adicionarElementoFila(filaout, pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, 0, TotalFragments), dest);

                         } else {
                         FragmentFlag = 1;
                         for (int i = 0; i < TotalFragments; i++) {
                         filaEspera.adicionarElementoFila(filaout, pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, i, TotalFragments - 1), dest);
                         }
                         this.flag_frag = 0;
                         }
                         filaEspera.removerElementoFila(this.fila_dados_in);
                         espera_rrply = 0;

                         } else if (espera_rrply == 0) {
                         if (DEBUG_DETAILED) {
                         System.out.println("Nó " + this.nodeIdentification + " quer enviar informação mas não tem rota. Envia RRequest e espera RReply ");
                         }

                         int nHop = 0;
                         int TotalFragments = this.calculaNumFragmentos(dados);
                         int FragmentFlag;
                         String RReq = pacote.criaRReq(dest, this.nodeIdentification);
                         filaEspera.adicionarElementoFila(filaout, RReq, dest);
                         if (TotalFragments == 0) {
                         FragmentFlag = 0;
                         waitingPackets.adicionarElementoLista(this.fila_espera_ACK_RRply, pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, 0, TotalFragments));
                         } else {
                         FragmentFlag = 1;
                         for (int i = 0; i < TotalFragments; i++) {
                         waitingPackets.adicionarElementoLista(this.fila_espera_ACK_RRply, pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, i, TotalFragments - 1));
                         }
                         }

                         filaEspera.removerElementoFila(this.fila_dados_in);
                         espera_rrply = 1;
                         } */
                    }
                    return 1;
                }
            }
        }
        return 1;
    }

    //Calcula Nº Fragmentos para uns determinados dados
    public int calculaNumFragmentos(String dados) {

        int numFragmentos;
        double tamanhoTotalDadosPacote = 10;
        String dados2 = pacote.binaryStringToText(dados);

        if (dados2.length() <= tamanhoTotalDadosPacote) {
            return 0;
        } else {
            numFragmentos = (int) Math.ceil(((double) dados2.length() / (double) tamanhoTotalDadosPacote));
            //System.out.println("num fragmentos: " + numFragmentos );
            return numFragmentos;
        }
    }
}
