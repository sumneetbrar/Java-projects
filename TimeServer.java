import java.util.Random;
import rf.RF;

/**
 * A simple test class showing the creation of an RF instance and
 * the sending of three bytes of data.
 * 
 * The TimeServer class must process commnad-line arguments, start the threads,
 * and waits for them to complete. 
 * 
 * @author Sumneet Brar
 * @version 10/2/23
 */
public class TimeServer
{
    private static short macAddress;
    // private static int macAddress
    private static Random r = new Random();
    //private static RF theRf;
    public static void main(String[] args)
    {
        // process the arguments
        if(args.length < 1) {
            // make a random macAddress
            // macAddress = r.nextInt(0,65536);
            macAddress = (short) r.nextInt(Short.MAX_VALUE);
            System.out.println("Using a random MAC address: " + macAddress);
        }
        else {
            // this reads the command line argument to a short
            // macAddress = Integer.parseInt(args[0]);
            macAddress = Short.parseShort(args[0]);
            if(macAddress < 0 || macAddress > 65535) {
                System.out.println("The macAddress is out of range");
                System.exit(0);
            }
        }
        // Create an instance of the RF layer. See documentation for
        // info on parameters, but they're null here since we don't need
        // to override any of its default settings.
        RF theRF = new RF(null, null);  
        
        // create the objects
        SendPackets a = new SendPackets(theRF, macAddress);
        IncomingPackets b = new IncomingPackets(theRF);

        // the threads
        Thread senderThread = new Thread(a);
        Thread listenerThread = new Thread(b);

        // run the threads
        senderThread.start();
        listenerThread.start();

        // infinite loop until the program is killed or 
        // until 20 minutes
        while(true) {} 
        
        //System.exit(0); - Make sure all threads die
    }
}

