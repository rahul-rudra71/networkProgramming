import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

//handles clients for server, on server end
public class ClientHandler implements Runnable{
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket clientSocket) throws IOException {
        this.client = clientSocket;
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        out = new PrintWriter(client.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            while(true) {
                String clientResponse = in.readLine();
                if (clientResponse.contains("time")) {
                    String date = new Date().toString();
                    System.out.println("[Client] Response: " + clientResponse);
                    System.out.println("[SERVER] Sending date to client: " + date);
                    out.println(date);
                } else if(clientResponse.contains("quit")) {
                    System.out.println("[Client] Response: " + clientResponse);
                    break;
                } else {
                    System.out.println("[Client] Response: " + clientResponse);
                    System.out.println("[SERVER] Client made bad request");
                    out.println("I only have the time on me");
                }
            }
        } catch (IOException e){
            System.err.println("IOException: " + e.getStackTrace());
        } finally {
            out.close();
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}
