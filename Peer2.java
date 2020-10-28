import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

public class Peer2 {

    //this is just for testing we will need to change this later to project specifications
    private static final int myID = 1001;   //The peer will have this ID
    public static String bitfield = "0000000000";
    public static HashMap<Integer, String> peer_bits = new HashMap<Integer, String>();
    public static List<Integer> peer_interest = new ArrayList<Integer>();
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
        if(peerList.size() != 0) {
            for (String[] s : peerList) {
                new peerHandler(s[1], Integer.parseInt(s[2]), Integer.parseInt(s[0])).start();
            }
        } else {
            System.out.println("I am Top");
        }

        //Here we begin handling our peers who connect to us
		System.out.println("The Peer"+ myID +" is running."); 
        ServerSocket listener = new ServerSocket(sPort);
		//int clientNum = 1;
        try {
        	while(true) {
            	new Handler(listener.accept()).start();
	    		System.out.println("Someone is attempting to connect!");
		    	//clientNum++;
    		}
        } finally {
    		listener.close();
    	} 
    }

    /* A peerhandler thread class. peerhandlers are spawned everytime you want to connect to a peer lower than yourself
    This is the client perspective of the project*/
    private static class peerHandler extends Thread {
        private String peerdest;               //peer's ip
        private int peerPort;                  //peer's port number
        private int peerID;                    //peer's ID 
        private ObjectOutputStream out;        //stream write to the socket
 	    private ObjectInputStream in;          //stream read from the socket
	    private String inMessage;              //messages coming in
        private String outMessage;             //messages going out     
        Socket requestSocket;                  //the socket we are requesting

        public peerHandler(String peerdest, int peerPort, int peerID) {
            this.peerdest = peerdest;
            this.peerPort = peerPort;
            this.peerID = peerID;
        }

        public void run() {
            try {
                //create a socket to connect to the server
                requestSocket = new Socket(peerdest, peerPort);
                System.out.println("Attempting to connect to "+ peerdest +" in "+ Integer.toString(peerPort) +" [Peer "+ peerID +"]");

                //initialize input/output streams
                out = new ObjectOutputStream(requestSocket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(requestSocket.getInputStream());
                boolean shake = false;
                int length = 0;
                String msg_type = "";
                String contents = "";
                String zero_pad = "";
                int length_bytes = 0;
                boolean sentInterest = false;

                //send handshake message to peer I want to connect to
                outMessage = "P2PFILESHARINGPROJ0000000000" + Integer.toString(myID);
                sendMessage(outMessage);
        
                while(true) {
                    //receive the message sent from the client
                    inMessage = (String)in.readObject();
                    //show the message to the user
                    System.out.println("Receive message: " + inMessage + " from Peer " + peerID);

                    //if statement determines if first message is handshake
                    //if handshake was successful enter if
                    if(shake) {
                        // As of right now this if is never entered. handshakes are completed and then no messages are sent
                        // program will be stuck waiting for the next message [message = (String)in.readObject()]
                        // this is where the remaining logic will belong I believe

                        //this block is just for reference not useful for acctual project
                        /*if(message.equals("quit")) {
                            break;
                        } else {
                            //Capitalize all letters in the message
                            MESSAGE = message.toUpperCase();
                            //send MESSAGE back to the client
                            sendMessage(MESSAGE);
                        }*/

                        // Delimit the message length and type from the received byte-string
                        length = Integer.parseInt(inMessage.substring(0,4));
                        msg_type = inMessage.substring(4,5);
                        contents = inMessage.substring(5);

                        // Choke --> 0
                        if(msg_type.equals("0")) {}
                            
                        // Unchoke --> 1
                        if(msg_type.equals("1")) {}

                        // Interested --> 2
                        if(msg_type.equals("2")) {
                            //add peerID --> interest pair to map
                            peer_interest.add(peerID);
                        }
                        // Not Interested --> 3
                        if(msg_type.equals("3")) {
                            //remove peer from list of interested peers
                            if(peer_interest.contains(peerID)){
                                peer_interest.remove(peerID);
                            }
                        }

                        // Have --> 4
                        if(msg_type.equals("4")) {}

                        // Bitfield --> 5
                        if(msg_type.equals("5")) {
                            //add peerID --> bitfield pair to map
                            peer_bits.put(peerID, contents);

                            //determine if server peer has pieces that client peer does not, then send
                            //appropriate interest message
                            if(peer_bits.get(peerID).equals(bitfield)){
                                //bifields are equivalent, not interested
                                outMessage = "00013";
                                sendMessage(outMessage);
                            }else{
                                for(int i = 0; i < contents.length(); i++){
                                    if(Integer.parseInt(contents.substring(i, i + 1)) > Integer.parseInt(bitfield.substring(i, i + 1))){
                                        //peers bitfield has pieces that this bitfield lacks
                                        outMessage = "00012";
                                        sendMessage(outMessage);
                                        sentInterest = true;
                                        break;
                                    }
                                }
                                //this bitfield has pieces that the peers bitfield lacks
                                if(!sentInterest){
                                    outMessage = "00013";
                                    sendMessage(outMessage);
                                }
                            }
                        }
                        // Request --> 6
                        if(msg_type.equals("6")) {}

                        // Piece --> 7
                        if(msg_type.equals("7")) {}

                    } else {
                        //if first message is P2P, handshake is successful 
                        if((inMessage.substring(0, 18)).equals("P2PFILESHARINGPROJ")) {
                            System.out.println("Handshake with peer " + Integer.toString(peerID) + " successful");
                            shake = true;
                            //send bitfield message to new peer connection to be used in establishing piece interest
                            
                            //zero pad length field if needed
                            zero_pad = Integer.toString(bitfield.length() + 1);
                            if(zero_pad.length() < 4){
                                length_bytes = zero_pad.length();
                                for(int i = 0; i < (4 - length_bytes); i++){
                                    zero_pad = "0" + zero_pad;
                                }
                            }
                            
                            outMessage = zero_pad + "5" + bitfield;
                            sendMessage(outMessage);

                        //otherwise bad handshake and disconnect
                        } else {
                            System.out.println("bad handshake");
                            outMessage= "bad handshake";
                            sendMessage(outMessage);
                            break;
                        }
                    }
                }
            } catch(ConnectException e) {
                System.err.println("Connection refused. You need to initiate a server first.");
            } catch(ClassNotFoundException e) {
                System.err.println("Class not found");
            } catch (UnknownHostException unknownHost) {
                System.err.println("You are trying to connect to an unknown host!");
            } catch(IOException ioException) {
                ioException.printStackTrace();
            } finally {
                //Close connections
                try {
                    in.close();
                    out.close();
                    requestSocket.close();
                    System.out.println("Disconnected with Server");
                } catch(IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }

        //method to send message to peer
        public void sendMessage(String msg) {
            try {
                out.writeObject(msg);
                out.flush();
                System.out.println("Send message: " + msg + " to Peer " + Integer.toString(peerID));
            } catch(IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    //code in Handler and peerHandler is largely the same with similar logic. difference in loop structure due to handshake

    /* A handler thread class.  Handlers are spawned everytime a peer tries to connect with you
    This is the server perspective of the project*/
    private static class Handler extends Thread {
        private Socket connection;             //connection to socket
        private String inMessage;              //messages coming in
		private String outMessage;             //messages going out
        private ObjectInputStream in;	       //stream read from the socket
        private ObjectOutputStream out;        //stream write to the socket
        private int peerID;                    //peer's ID

        public Handler(Socket connection) {
        	this.connection = connection;
        }

        public void run() {
            try {
                //initialize Input and Output streams
                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();
                in = new ObjectInputStream(connection.getInputStream());
                boolean shake = false;
                String msg_type = "";
                int length = 0;
                String contents = "";
                String zero_pad = "";
                int length_bytes = 0;
                boolean sentInterest = false;

                try{
                    while(true) {
                        //receive the message sent from the client
                        inMessage = (String)in.readObject();

                        //if statement determines if first message is handshake
                        //if handshake was successful enter if
                        if(shake) {
                            //show the message to the user
                            System.out.println("Receive message: " + inMessage + " from Peer " + peerID);
                            // add parsing logic from peerHandler class here
                            // Delimit the message length and type from the received byte-string
                            length = Integer.parseInt(inMessage.substring(0,4));
                            msg_type = inMessage.substring(4,5);
                            contents = inMessage.substring(5);

                            // Choke --> 0
                            if(msg_type.equals("0")) {}
                                
                            // Unchoke --> 1
                            if(msg_type.equals("1")) {}

                            // Interested --> 2
                            if(msg_type.equals("2")) {
                                //add peerID --> interest pair to map
                                peer_interest.add(peerID);
                            }
                            // Not Interested --> 3
                            if(msg_type.equals("3")) {
                                //remove peer from list of interested peers
                                if(peer_interest.contains(peerID)){
                                    peer_interest.remove(peerID);
                                }
                            }
                            // Have --> 4
                            if(msg_type.equals("4")) {}

                            // Bitfield --> 5
                            if(msg_type.equals("5")) {
                                //add peerID --> bitfield pair to map
                                //this new bitfield will be considered in the unchoking decision block, which runs on an input timer
                                peer_bits.put(peerID, contents);

                                //determine if server peer has pieces that client peer does not, then send
                                //appropriate interest message
                                if(peer_bits.get(peerID).equals(bitfield)){
                                    //bifields are equivalent, not interested
                                    outMessage = "00013";
                                    sendMessage(outMessage);
                                }else{
                                    for(int i = 0; i < contents.length(); i++){
                                        if(Integer.parseInt(contents.substring(i, i + 1)) > Integer.parseInt(bitfield.substring(i, i + 1))){
                                            //peers bitfield has pieces that this bitfield lacks
                                            outMessage = "00012";
                                            sendMessage(outMessage);
                                            sentInterest = true;
                                            break;
                                        }
                                    }
                                    //this bitfield has pieces that the peers bitfield lacks
                                    if(!sentInterest){
                                        outMessage = "00013";
                                        sendMessage(outMessage);
                                    }
                                }
                                //send own bitfield back to client peer

                                //zero pad message length field if needed
                                zero_pad = Integer.toString(bitfield.length() + 1);
                                if(zero_pad.length() < 4){
                                    length_bytes = zero_pad.length();
                                    for(int i = 0; i < (4 - length_bytes); i++){
                                        zero_pad = "0" + zero_pad;
                                    }
                                }

                                outMessage = zero_pad + "5" + bitfield;
                                sendMessage(outMessage);
                            }
                            // Request --> 6
                            if(msg_type.equals("6")) {}

                            // Piece --> 7
                            if(msg_type.equals("7")) {}
                            
                        } else {
                            //if first message is correct, handshake is successful 
                            if((inMessage.substring(0, 18)).equals("P2PFILESHARINGPROJ")) {
                                peerID = Integer.parseInt(inMessage.substring(inMessage.length() - 4));

                                //show the message to the user
                                System.out.println("Receive message: " + inMessage + " from Peer " +peerID);
                                System.out.println("Handshake with peer " + Integer.toString(peerID) + " successful");

                                //return handshake
                                outMessage = "P2PFILESHARINGPROJ0000000000" + Integer.toString(myID);
                                sendMessage(outMessage);
                                shake = true;
                            //otherwise bad handshake and disconnect
                            } else {
                                System.out.println("Received message: " + inMessage);
                                System.out.println("bad handshake");
                                outMessage = "bad handshake";
                                out.writeObject(outMessage);
                                out.flush();
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
                    System.out.println("Disconnect with Client");
                } catch(IOException ioException) {
                    System.out.println("Disconnect with Client");
                }
            }
        }

        //send a message to the output stream
        public void sendMessage(String msg) {
            try {
                out.writeObject(msg);
                out.flush();
                System.out.println("Send message: " + msg + " to Peer "+ peerID);
            } catch(IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}