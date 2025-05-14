import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
Autores: Filipe Ferreira e Otávio Nardini
Objetivo: Implementar um servidor de chat multiusuário usando sockets TCP.

Comandos no Chat:

 `/nick novoNome`: Altera o apelido do usuário.
 `/quit`: Sai do chat.

--- */

/*/
Objetivo: Cliente que se conecta ao servidor e envia/recebe mensagens.

Principais Componentes:

`Socket client = new Socket("127.0.0.1", 9999);`: Conecta ao servidor local.
`PrintWriter out`: Envia mensagens ao servidor.
`BufferedReader in`: Recebe mensagens do servidor.
`InputHandler`: (classe interna esperada) lê do teclado e envia mensagens para o servidor.
`run()`: Inicia a conexão e escuta mensagens do servidor.

Fluxo Geral:

1. Cliente conecta ao servidor.
2. Envia nickname.
3. Lê e envia mensagens com ajuda da `InputHandler`.
4. Mostra mensagens recebidas do servidor no terminal.

Conceito de Redes Aplicado: Comunicação confiável com múltiplos clientes via TCP.
*/

public class Server implements Runnable {
    private ArrayList<ConnectionHandler> connections; // Lista de conexões
    private ServerSocket server; // Socket do servidor
    private boolean done; // Flag para falar ao servidor se deve parar
    private ExecutorService pool; //Gerenciador de threads para multiplos clients

    public Server() {
        connections = new ArrayList<>(); // Inicializa a lista de conexões
        done = false; // servidor ativo logo de cara
    }

    @Override
    public void run() {
        try {
            // cria o socket do servidor na porta 9999 ouvindo em todas interfaces (0.0.0.0)
            server = new ServerSocket(9999, 50, InetAddress.getByName("0.0.0.0"));
            // executor para lidar com múltiplos clientes simultaneamente
            pool = Executors.newCachedThreadPool();
            while (!done) {
                // aguarda a conexão de um cliente
                Socket client = server.accept();
                // cria o handler para o novo cliente
                ConnectionHandler handler = new ConnectionHandler(client);
                // adiciona à lista de conexões
                connections.add(handler);
                // executa o handler em uma thread separada
                pool.execute(handler);
            }

        } catch (IOException e) {
            shutdown(); // Em caso de erro, finaliza o servidor
        }
    }
    // envia uma mensagem para todos os clientes conectados
    public void broadcast(String message) {
        for (ConnectionHandler connectionHandler : connections) {
            if(connectionHandler != null) {
                connectionHandler.sendMessage(message);
            }
        }
    }
    // finaliza o servidor e todas as conexões
    public void shutdown() {
        try {
            done = true;
            if (!server.isClosed()) {
                server.close(); // Fecha o socket do servidor
            }
            for (ConnectionHandler connectionHandler : connections) {
                connectionHandler.shutdown(); // Fecha cada conexão
            }
        } catch (IOException e) {
            //ignore
        }
    }
    // classe interna responsável por lidar com um cliente específico
    class ConnectionHandler implements Runnable {
        private Socket client;
        private BufferedReader in; // para ler do cliente
        private PrintWriter out;  // para enviar ao cliente
        private String nickname; // nick do cliente
        private String clientIP; // ip do client
        private boolean done;

        public ConnectionHandler(Socket client) {
            this.done = false;
            this.client = client; //inicializa o nome
            this.clientIP = client.getInetAddress().getHostAddress(); // pega IP
        }

        @Override
        public void run() {
            try {
                // inicializa as streams de entrada e saída
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                //solicita nickname
                out.println("Please enter a nickname: ");
                nickname = in.readLine();
                //loga no server
                System.out.println(nickname +" ["+ clientIP + "]" +  " connected!");
                broadcast(nickname + " joined the server! ");
                String message;
                //comando para trocar de nick
                while(!done && (message = in.readLine()) != null) {
                    if(message.startsWith("/nick")){
                        String[] messageSplit = message.split(" ", 2);
                        if(messageSplit.length == 2) {
                            broadcast(nickname + " renamed themselves to " + messageSplit[1]);
                            System.out.println(nickname + " ["+ clientIP +"]" + " renamed themselves to " + messageSplit[1]);
                            nickname = messageSplit[1];
                            out.println(nickname+" Succesfully changed nickname to "+ nickname);
                        } else{
                            out.println("No nickname provided! ");
                        }
                    }else if(message.startsWith("/quit")){
                        broadcast(nickname + " left the server! ");
                        shutdown(); // fecha conexão
                        break;

                    }else{
                        // mensagem comum que envia para todos em broadcast
                  String fullMessage = nickname +  " ["+ clientIP +"]" + ": " + message;
                    System.out.println("[Broadcast] " + fullMessage);
                    broadcast(fullMessage);
                    }
                }
            }catch (Exception e) {
             shutdown();
            }
        }
        //metodo de enviar mensagem
        public void sendMessage(String message) {
            out.println(message);
            out.flush();  // garante que a mensagem seja enviada na hora
        }

        //fecha a conexão
        public void shutdown() {
            done = true;
            connections.remove(this);
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (!client.isClosed()) client.close();
            } catch (IOException e) {
                //ignore
            }
            System.out.println((nickname != null ? nickname : "Unknown") + " [" + clientIP + "] disconnected.");
        }
    }
}
