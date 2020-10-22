import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
//import javax.swing.JOptionPane;


public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 9090;

    public static void main(String[] args)  throws IOException{
        Socket socket = new Socket(SERVER_IP, SERVER_PORT);
        System.out.println("[Client] Connected to Server");
        InetAddress myIP=InetAddress.getLocalHost();

        //Buffer to take in what the Server has sent (date) required for taking in data
        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //Buffer to output what has been typed by user (not sure if this is required for sending out data but O don't think so)
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        while (true) {
            System.out.print("Enter Input: ");
            String userIn = keyboard.readLine();
            out.println(myIP.getHostAddress() + ": " + userIn);

            if (userIn.equals("quit"))
                break;
            
            String serverResponse = input.readLine();
            System.out.println("[Server] response: " + serverResponse);
            //JOptionPane.showMessageDialog(null, serverResponse);
        }

        System.out.println("[Client] Closing Client socket");
        socket.close();
        System.exit(0);
    }
}