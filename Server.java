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
Objetivo: Implementa um servidor de chat multiusuário usando sockets TCP.

Principais Componentes:

 `ServerSocket server = new ServerSocket(9999);`: Cria o servidor na porta 9999.
`Socket client = server.accept();`: Aguarda conexões de clientes.
 `ConnectionHandler`: Classe interna que lida com cada cliente conectado.
 `broadcast()`: Envia mensagens para todos os clientes conectados.
 `ExecutorService`: Permite lidar com múltiplos clientes simultaneamente (multithread).

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
    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    public Server() {
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(9999, 50, InetAddress.getByName("0.0.0.0"));
            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }

        } catch (IOException e) {
            shutdown();
        }
    }
    public void broadcast(String message) {
        for (ConnectionHandler connectionHandler : connections) {
            if(connectionHandler != null) {
                connectionHandler.sendMessage(message);
            }
        }
    }

    public void shutdown() {
        try {
            done = true;
            if (!server.isClosed()) {
                server.close();
            }
            for (ConnectionHandler connectionHandler : connections) {
                connectionHandler.shutdown();
            }
        } catch (IOException e) {
            //ignore
        }
    }
    class ConnectionHandler implements Runnable {
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;
        private String clientIP;

        public ConnectionHandler(Socket client) {
            this.client = client;
            this.clientIP = client.getInetAddress().getHostAddress();
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Please enter a nickname: ");
                nickname = in.readLine();

                System.out.println(nickname +" ["+ clientIP + "]" +  " connected!");
                broadcast(nickname + " joined the server! ");
                String message;
                while((message = in.readLine()) != null) {
                    if(message.startsWith("/nick ")){
                        String[] messageSplit = message.split(" ", 2);
                        if(messageSplit.length == 2) {
                            broadcast(nickname + " renamed themselves to " + messageSplit[1]);
                            System.out.println(nickname + " renamed themselves to " + messageSplit[1]);
                            nickname = messageSplit[1];
                            out.println(clientIP+" Succesfully changed nickname to "+ nickname);
                        } else{
                            out.println("No nickname provided! ");
                        }
                    }else if(message.startsWith("/quit")){
                        broadcast(nickname + " left the server! ");
                        shutdown();
                    }else{
                        broadcast(nickname + ": " + message);
                    }
                }
            }catch (Exception e) {
             shutdown();
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }


        public void shutdown() {
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                //ignore
            }
        }
    }
}
