
package protocol_g6_package;


public class tabelaValidade {
    int id=0;
	int source=0;
	public static int tamanhoTabela=500; //Tamanho da Tabela
	int nrEntradas=0;	//Conta nrº entradas na tabela
	int type=0;

    public tabelaValidade(){
    	id=0;
    	source=0;
        nrEntradas=0;
        type=0;
    }
	
    //Cria tabela de Validade
    public static tabelaValidade[] criaTabelaValidade(){
        
        tabelaValidade[] tabela = new tabelaValidade[tamanhoTabela];
        for(int i=0; i<tamanhoTabela;i++){
        	tabela[i] = new tabelaValidade();
        }
        return tabela;
    }
   
   //Adiciona novo conjunto source-id a tabela
   public static int addTabelValidade(tabelaValidade tabela[], int id, int source, int tipo){
	   tabela[tabela[0].nrEntradas].id=id;	
	   tabela[tabela[0].nrEntradas].source=source;
       tabela[tabela[0].nrEntradas].type=tipo;
	   //System.out.println("nrEntradas= "+ tabela[0].nrEntradas);
	   if(tabela[tabela[0].nrEntradas].id==id && tabela[tabela[0].nrEntradas].source==source){
		   tabela[0].nrEntradas++;
		   return 1;
   	   }   
	   else
		   return 0;	   
   }
   
   //Remove entrada da Tabela, é verificado automaticamente quando se chama a função de verificar se existe entrada
   public static void removeTabelValidade(tabelaValidade tabela[], int id, int source){
	   
	   if(tabela[0].nrEntradas>0){
		   for(int i=0; i<tabela[0].nrEntradas;i++)
		   {
			   if(tabela[i].source==source && (tabela[i].id<(id-5) || tabela[i].id>id)){ //Remove todas as entradas com o source igual ao passado nos argumentos mas
				   //com id inferior a 5 vezes o id passado nos argumentos
				   tabela[i].id=0;
				   tabela[i].source=0;
                   tabela[i].type=0;
				   
				   for(int j=i;j<tabela[0].nrEntradas;j++){ //Reordena tabela para não ficarem conjuntos vazios no meio
					   if(j==tabela[0].nrEntradas-1){
						   tabela[j].id=0;
						   tabela[j].source=0;
                           tabela[j].type=0;
					   }
					   else{
						   tabela[j].id=tabela[j+1].id;
						   tabela[j].source=tabela[j+1].source;
                           tabela[j].type=tabela[j+1].type;
					   }
						   
				   }
			   tabela[0].nrEntradas--;
			   }		   
		   }
	   }   
   }
   
   //Verifica se conjunto source-id é valido ou se já existe uma entrada na tabela
   public static int verificaValidade(tabelaValidade tabela[], int id, int source, int tipo){
	   removeTabelValidade(tabela,id,source); //Quando é pedido para verificar uma entrada chamasse o método remove para, possivelmente,
	   //eliminar conjuntos antigos
	   
	   for(int i=0; i<tabela[0].nrEntradas;i++)
	   {
		   if(tabela[i].source==source && tabela[i].id==id && tabela[i].type==tipo)
			   return 0; //Indica que existe um conjunto na tabela com mesmo id-source	   
	   }
	   
	   return 1; //Indica que não existe um conjunto na tabela com mesmo id-source	
   }
    
  //Imprime tabela 
   public static void imprimeTabelaValidade(tabelaValidade tabela[]){
	   if(tabela[0].nrEntradas>0){
		   	System.out.println("|     ID     |   Source       |");
	   		for(int i=0; i<tabela[0].nrEntradas;i++)
	   		{
	   			if(tabela[i].id>10)
	   				System.out.println("|     " + tabela[i].id + "     |     " + tabela[i].source + "         |");			   
	   			else
	   				System.out.println("|     " + tabela[i].id + "      |     " + tabela[i].source + "         |");
	   		}
	   }
	   else
		   System.out.println("Tabela de Validade está vazia!");
   }
   
}
