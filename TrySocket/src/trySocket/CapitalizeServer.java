package trySocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JOptionPane;

/**
 * A server program which accepts requests from clients to
 * capitalize strings.  When clients connect, a new thread is
 * started to handle an interactive dialog in which the client
 * sends in a string and the server thread sends back the
 * capitalized version of the string.
 *
 * The program is runs in an infinite loop, so shutdown in platform
 * dependent.  If you ran it from a console window with the "java"
 * interpreter, Ctrl+C generally will shut it down.
 */
public class CapitalizeServer {

    /**
     * Application method to run the server runs in an infinite loop
     * listening on port 9898.  When a connection is requested, it
     * spawns a new thread to do the servicing and immediately returns
     * to listening.  The server keeps a unique client number for each
     * client that connects just to show interesting logging
     * messages.  It is certainly not necessary to do this.
     * @throws IOException 
     */
	private long e,c;
	public void start() throws IOException{
		 int clientNumber = 0;
	        ServerSocket listener = new ServerSocket(9898);
	        try {
	            while (true) {
	            	 Socket socket=listener.accept();
	            	 String pubKey =  JOptionPane.showInputDialog(this 
	       			       ,"Please enter public key e,c");
	            	 e=Long.valueOf(pubKey.split(",")[0]);
	            	 c=Long.valueOf(pubKey.split(",")[1]);
	            	 new Capitalizer(socket, clientNumber++).start();
	            }
	        } finally {
	            listener.close();
	        }
	}
    public static void main(String[] args) throws Exception {
        System.out.println("The capitalization server is running.");
        CapitalizeServer server=new CapitalizeServer();
        server.start();
       
    }

    /**
     * A private thread to handle capitalization requests on a particular
     * socket.  The client terminates the dialogue by sending a single line
     * containing only a period.
     */
    private class Capitalizer extends Thread {
        private Socket socket;
        private int clientNumber;
       // private long e,c;
        public Capitalizer(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber;
            log("New connection with client# " + clientNumber
                  + " at " + socket);
        }

        /**
         * Services this thread's client by first sending the
         * client a welcome message then repeatedly reading strings
         * and sending back the capitalized version of the string.
         */
        public void run() {
            try {

                // Decorate the streams so we can send characters
                // and not just bytes.  Ensure output is flushed
                // after every newline.
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out =
                    new PrintWriter(socket.getOutputStream(), true);

                // Send a welcome message to the client.
                out.println("Hello, you are client #" + clientNumber + ".");
                out.println("Enter a line with only a period to quit\n");

                // Get messages from the client, line by line; return them
                // capitalized
                while (true) {
                    String input = in.readLine();//msg from client
                    if (input == null || input.equals(".")) {
                        break;
                    }
                    //server send back upper case encrypted numbers to client
                    String toClient="Server send back(encrypted with client's public key):";
                    for(int i=0;i<input.length();i++){
                		 toClient+=" "+(new RSA().encrypt((long)(input.toUpperCase().charAt(i)),
                		 e, c));
                		 }
                   // out.println(input.toUpperCase());
                    out.println(toClient);
                }
            } catch (IOException e) {
                log("Error handling client# " + clientNumber + ": " + e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    log("Couldn't close a socket, what's going on?");
                }
                log("Connection with client# " + clientNumber + " closed");
            }
        }

        /**
         * Logs a simple message.  In this case we just write the
         * message to the server applications standard output.
         */
        private void log(String message) {
            System.out.println(message);
        }
    }
}