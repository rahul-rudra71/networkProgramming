import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


//Server
public class DateServer {
    private static final int PORT = 9090;

    //array of clients currently connected
    private static ArrayList<ClientHandler> clients = new ArrayList<>();
    //max num of clients allowed
    private static ExecutorService pool = Executors.newFixedThreadPool(4);

    public static void main(String[] args) throws IOException{
        ServerSocket listener = new ServerSocket(PORT);
        System.out.println("[SERVER] Listener opened and waiting for socket...");

        //Socket socket;
        while(true) {
            System.out.println("[SERVER] Number of clients: " + clients.size());
            Socket socket = listener.accept();
            System.out.println("[SERVER] Client connected to socket");

            ClientHandler clientThread = new ClientHandler(socket);
            clients.add(clientThread);

            pool.execute(clientThread);
        }

        //need to find a way to exit out and close listener

        //System.out.println("[SERVER] Closing Socket and Listener");
        //socket.close();
        //listener.close();
    }
}