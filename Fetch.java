import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * This class fetches files from HTTPs servers. 
 * 
 * @author Sumneet
 */

public class Fetch {

    // The size of the buffer (array) we use to hold incoming data. It's
    // sized at the moment to be large enough to contain the entire response.
    private static final int BUFFER_SIZE = 600;
    // Create a socket so we can connect to the network
    private static Socket theSocket = null; 
    private static OutputStream outgoing = null;
    private static DataInputStream incoming = null;
    // The default server that is hosting our default page
    private static String host;
    // The default page that we are accessing
    private static String page;
    // Our default output file
    private static File myFile;

    /**
     * This method creates a socket that is connected to the user's requested server at
     * port 80 (HTTP) and associates output and input streams with the socket.
     * 
     * @throws UnknownHostException
     * @throws IOException
     */ 
    public static void createSocket() throws IOException {
        try {
            theSocket = new Socket(host, 80);   // Port 80 is HTTP
            outgoing = theSocket.getOutputStream();
            incoming = new DataInputStream(theSocket.getInputStream());
            if (outgoing != null && incoming != null) {}
            else {
                throw new IOException("Either the input or output stream was null", null);
            }
        }
        catch (UnknownHostException e) {
            System.out.println("The host could not be found! Please check any possible errors and try again.");
            System.exit(1);
        }
    }

    /**
     * This method constructs the outgoing packet and then writes
     * it to the OutputStream.
     * 
     * @param host
     * @param page
     * @throws IOException
     */
    public static void sendPacket(String host, String page) {
        try {
            String packet = "GET " + page +  " HTTP/1.1\r\n"
                        + "Host: " + host + "\r\n"
                        + "Connection: close\r\n"
                        + "\r\n";
            System.out.println("Grabbing  "+ page + " from " + host);
            outgoing.write(packet.getBytes());  // write the packet to the output stream
        }
        catch (IOException e) {
            System.out.println("Something went wrong while writing the packet to the outgoing data stream."
            + " The program will exit now and you can try again.");
            System.exit(1);
        }
    }
    
    /**
     * This method reads the incoming data, parses the header from the content, and writes
     * the content to the user's given file. 
     * 
     * @param incoming The DataInputStream that holds the incoming data from the server
     * @throws NullPointerException
     * @throws IOException
     */
    public static void writeOutput(DataInputStream incoming) {
        try {
            int numBytes;
            byte[] buf = new byte[BUFFER_SIZE];
            FileOutputStream myWriter = new FileOutputStream(myFile); 
            boolean emptyLineFound = false;  // becomes true when we have reached the end of the header

            while((numBytes = incoming.read(buf)) != -1) { 
                if(!emptyLineFound) {
                    for(int i = 0; i < numBytes; i++) {
                        // if we find the header
                        if (buf[i] == '\r' && buf[i + 1] == '\n' && buf[i + 2] == '\r' && buf[i + 3] == '\n') { 
                            emptyLineFound = true;
                            myWriter.write(buf, i + 4, numBytes - (i + 4)); // write content after header to file
                            break; // no need to continue the loop
                        }
                    }
                }
                else {
                    myWriter.write(buf, 0, numBytes); // write the rest of the data
                }
                
            }
            myWriter.close(); //close the output stream
            System.out.println("Writing data to " + myFile.toString());
        }
        catch (NullPointerException file) {
            System.out.println("Something went wrong while accessing the file you listed." 
            + " The program will exit now and you can try again.");
            System.exit(1);
        }
        catch (IOException e) {
            System.out.println("Something went wrong while writing to the data to the output file." 
            + " The program will exit now and you can try again.");
            System.exit(1);
        }
    }


    /**
     * The main function takes in the user's input and sets up the appropriate variables
     * according the server and page that the user is trying to access. It also initializes
     * the file we will be writing to. Then, it calls the createSocket() method to set up our 
     * connection with the server and page. Next, it calls sendPacket() and writeOutput() to 
     * send our request packet and interpret the resulting data. 
     * 
     * @param args  An array of command-line arguments
     * @throws Exception
     */
    public static void main(String[] args) {
        try {
            // if there are no arguments given, do not run
            if (args.length < 1) {
                System.out.println("Usage: <hostname> <resource> <filename>");
                System.exit(1);
            }

            if (args.length > 0) {
                host = args[0];
            }

            if(args.length > 1) {
                page = args[1];
            }

            if(args.length > 2) {
                myFile = new File(args[2]);
            }
            
            createSocket();
            sendPacket(host, page);
            writeOutput(incoming);
            theSocket.close();  // close the socket connection 
        }
        catch (IOException e) {
            System.out.println("Something went wrong while closing the socket.");
            System.exit(1);
        }
    }
}