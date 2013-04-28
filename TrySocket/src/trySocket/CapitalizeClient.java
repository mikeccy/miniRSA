package trySocket;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * A simple Swing-based client for the capitalization server. It has a main
 * frame window with a text field for entering strings and a textarea to see the
 * results of capitalizing them.
 */
public class CapitalizeClient {

	private BufferedReader in;
	private PrintWriter out;
	private JFrame frame = new JFrame("Capitalize Client");
	private JTextField dataField = new JTextField(40);
	private JTextArea messageArea = new JTextArea(8, 60);
	//private JPanel panel = new JPanel();
	private JButton button = new JButton("crack");
	//private long e;
	@SuppressWarnings("unused")
	private long e, d, c;

	/**
	 * Constructs the client by laying out the GUI and registering a listener
	 * with the textfield so that pressing Enter in the listener sends the
	 * textfield contents to the server.
	 */
	public CapitalizeClient() {

		// Layout GUI
		messageArea.setEditable(false);
		frame.setLayout(new GridLayout(3, 1));
		frame.add(dataField, "North");
		frame.add(new JScrollPane(messageArea), "Center");
		frame.add(button);
		// Add Listeners
		dataField.addActionListener(new ActionListener() {
			/**
			 * Responds to pressing the enter key in the textfield by sending
			 * the contents of the text field to the server and displaying the
			 * response from the server in the text area. If the response is "."
			 * we exit the whole application, which closes all sockets, streams
			 * and windows.
			 */
			public void actionPerformed(ActionEvent e) {
				out.println(dataField.getText());
				// get what server has sent
				String response = "";
				try {
					// response += in.readLine();
					// while(in.readLine()!=null)
					response += in.readLine();

					if (response == null || response.equals("")) {
						System.exit(0);
					}
				} catch (IOException ex) {
					response = "Error: " + ex;
				}
				messageArea.append(response + "\n");
				dataField.selectAll();
				String second = response.split(":")[1];
				// System.out.println(second);
				String[] num = second.split(" ");
				String clientDec = "After client decrypt with private key:";
				for (int i = 1; i < num.length; i++) {
					clientDec += (char) RSA.decrypt(Long.valueOf(num[i]), d, c);
				}
				messageArea.append(clientDec + "\n");
			}
		});
		button.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				String crackPubKey = JOptionPane.showInputDialog(frame,
						"Please enter public key",
						"Welcome to the Capitalization Program",
						JOptionPane.QUESTION_MESSAGE);
				final long[] pubLong=new long[2];
				pubLong[0] = Integer.valueOf(crackPubKey.split(",")[0]);
				pubLong[1] = Integer.valueOf(crackPubKey.split(",")[1]);
				final long dCrack=RSA.bruteFind(pubLong);
				messageArea.append("Cracked!public key:(" + pubLong[0] + ", " + pubLong[1] + "), private key:("
								+ dCrack + ", " + pubLong[1] + ")\n");
				
				JFrame frameCrack = new JFrame("Capitalize Client");
				final JTextField dataCrack = new JTextField(40);
				final JTextArea messageCrack = new JTextArea(8, 60);
				messageCrack.setEditable(false);
				frameCrack.add(dataCrack, "North");
				frameCrack.add(new JScrollPane(messageCrack), "Center");
				frameCrack.setSize(500, 500);
				frameCrack.setVisible(true);
				dataCrack.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO Auto-generated method stub
						String encrypted=dataCrack.getText();
						String[] num = encrypted.split(" ");
						String clientDec = "After client decrypt with private key:";
						for (int i = 0; i < num.length; i++) {
							clientDec += (char) RSA.decrypt(Long.valueOf(num[i]), dCrack, pubLong[1]);
						}
						messageCrack.append(clientDec + "\n");
					}});
			}});
		
	}

	
	public void connectToServer() throws IOException {

		String serverAddress = "127.0.0.1";

		// Make connection and initialize streams
		Socket socket = new Socket(serverAddress, 9898);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);

		// Consume the initial welcoming messages from the server
		for (int i = 0; i < 3; i++) {
			messageArea.append(in.readLine() + "\n");
		}
	}

	public void askForKeys() {
		// ArrayList<Long> result = new ArrayList<Long>();
		String nums = JOptionPane.showInputDialog(frame,
				"Please enter two numbers NUM1,NUM2",
				"Welcome to the Capitalization Program",
				JOptionPane.QUESTION_MESSAGE);
		// should handle illegal user input!!!
		int n1 = Integer.valueOf(nums.split(",")[0]);
		int n2 = Integer.valueOf(nums.split(",")[1]);

		int p1 = RSA.generateNthPrime(n1);
		int p2 = RSA.generateNthPrime(n2);
		// in sequence e, d, c
		long c = p1 * p2;
		long m = (p1 - 1) * (p2 - 1);
		long e = RSA.coprime(m);
		long d = RSA.mod_inverse(e, m);
		messageArea.append("public key:(" + e + ", " + c + "), private key:("
				+ d + ", " + c + ")\n");
		this.e = e;
		this.c = c;
		this.d = d;
	}

	public static void main(String[] args) throws Exception {
		CapitalizeClient client = new CapitalizeClient();
		client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.frame.pack();
		client.frame.setVisible(true);
		client.askForKeys();
		client.connectToServer();
	}
}