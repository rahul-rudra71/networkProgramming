import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Peer {

    //this is just for testing we will need to change this later to project specifications
    private static final int myID = 1001;   //The peer will have this ID
	public static void main(String[] args) throws Exception {
        //This block of code reads in the peer info file, splits each line into a list of string arrays, determines peers with lower ids
        int sPort = 0;
        List<String[]> peerList = new ArrayList<String[]>();
        try {
            File myObj = new File("PeerInfo.cfg");
            Scanner myReader = new Scanner(myObj);

            //parsing through file
            while(myReader.hasNextLine()) {
                String[] peerData = (myReader.nextLine()).split(" ");
                int peerID = Integer.parseInt(peerData[0]);

                if (peerID == myID) {
                    sPort = Integer.parseInt(peerData[2]);
                }
                //if peer id is lower than ours add it to arraylist
                //each line is put into array split by spaces
                if(peerID < myID)
                    peerList.add(peerData);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        //connect to peers with lower id (peers we want to connect to)
        //create two threads to run simulatneous
        if(peerList.size() != 0) {
            for (String[] s : peerList) {
                Socket requestSocket = new Socket(s[1], Integer.parseInt(s[2]));
                System.out.println("Connected to "+ s[1] +" in " + s[2]);
            }
        } else {
            System.out.println("I am Top");
        }
        

        //Here we begin handling our peers who connect to us
		System.out.println("The Peer"+ myID +" is running."); 
        ServerSocket listener = new ServerSocket(sPort);
		int clientNum = 0;
        try {
        	while(true) {
            	new Handler(listener.accept(), clientNum).start();
	    		System.out.println("Client " + clientNum + " is connected!");
		    	clientNum++;
    		}
        } finally {
    		listener.close();
    	} 
    }

	    /* A handler thread class.  Handlers are spawned everytime a peer tries to connect with you*/
    private static class Handler extends Thread {
        private String message;    //message received from the client
		private String MESSAGE;    //uppercase message send to the client
		private Socket connection;
        private ObjectInputStream in;	//stream read from the socket
        private ObjectOutputStream out;    //stream write to the socket
		private int no;		//The index number of the client

        public Handler(Socket connection, int no) {
        	this.connection = connection;
	    	this.no = no;
        }

        public void run() {
            try {
                //initialize Input and Output streams
                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();
                in = new ObjectInputStream(connection.getInputStream());
                boolean shake = false;
                try{
                    while(true) {
                        //receive the message sent from the client
                        message = (String)in.readObject();
                        //show the message to the user
                        System.out.println("Receive message: " + message + " from client " + no);

                        //if statement determines if first message is handshake
                        //if handshake was successful enter if
                        if(shake) {
                            if(message.equals("quit")) {
                                break;
                            } else {
                                //Capitalize all letters in the message
                                MESSAGE = message.toUpperCase();
                                //send MESSAGE back to the client
                                sendMessage(MESSAGE);
                            }
                        } else {
                            //if first message is P2P, handshake is successful 
                            if(message.equals("P2P")) {
                                System.out.println("handshake");
                                MESSAGE = "handshake";
                                sendMessage(MESSAGE);
                                shake = true;
                            //otherwise bad handshake and disconnect
                            } else {
                                System.out.println("bad handshake");
                                MESSAGE = "bad handshake";
                                sendMessage(MESSAGE);
                                break;
                            }
                        }
                    }
                } catch(ClassNotFoundException classnot){
                    System.err.println("Data received in unknown format");
                }
            } catch(IOException ioException){
                System.out.println("Interupted by User Input");
            } finally {
                //Close connections
                try {
                    in.close();
                    out.close();
                    connection.close();
                    System.out.println("Disconnect with Client " + no);
                } catch(IOException ioException) {
                    System.out.println("Disconnect with Client " + no);
                }
            }
        }

        //send a message to the output stream
        public void sendMessage(String msg) {
            try {
                out.writeObject(msg);
                out.flush();
                System.out.println("Send message: " + msg + " to Client " + no);
            } catch(IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}