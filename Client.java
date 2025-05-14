import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {
    private Socket client; //socket para conectar-se no server
    private BufferedReader in; //Para receber messages do server
    private PrintWriter out; // envia mensagens
    private boolean done; //flag de encerramento client

    @Override
    public void run() {
        try {
            // conecta ao servidor local na porta 9999
            client = new Socket("127.0.0.1", 9999);
            // inicializa as streams
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            // cria e inicia uma thread para ler do teclado
            InputHandler inputHandler = new InputHandler();
            Thread thread = new Thread(inputHandler);

            thread.start();

            String inMessage;
            // loop que escuta mensagens do servidor
            while(!done && (inMessage = in.readLine()) != null) {
                System.out.println(inMessage); // exibe no terminal
            }
        } catch (IOException e) {
            shutdown();  // em caso de erro, finaliza o cliente
        }
    }
    // encerra o cliente e fecha as conexõe
    public void shutdown(){
        done = true;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (client != null && !client.isClosed()) client.close();
            System.out.println("Você saiu do chat. Conexão encerrada.");
        } catch(IOException e){
            // ignore
        }
    }


    // classe interna que lida com a entrada do usuário via teclado
    class InputHandler implements Runnable {

        @Override
        public void run() {
            try {
                // leitor de entrada padrão (teclado)
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done) {
                    String input = inReader.readLine(); // lê o que o usuário digita
                    // Se digitar /quit, finaliza a conexão
                    if (input.equals("/quit")) {
                        out.println("/quit");
                        shutdown();
                        break;
                    }else{
                        out.println(input); // Envia a mensagem ao servidor
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }
    }
// Ponto de entrada principal do cliente
    public static void main(String[] args) {
    Client client = new Client();
        new Thread(new Client()).start(); // Inicia o cliente

    }
}