package segmentedfilesystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class FileRetriever {
	public static final int NUM_FILES = 3;
	String svr;
	PacketManager manager;
	Socket socket;
	public FileRetriever(String server, int port) {
		try {
			svr = server;
			manager = new PacketManager(NUM_FILES);
			socket = new Socket(svr, port);
		} catch(IOException ioe) {
			System.out.println("Caught IOException: ");
			System.out.println(ioe);
		}

	}

	public void downloadFiles() {
		try {
			InputStream input = socket.getInputStream();
			OutputStream output = socket.getOutputStream();
			output.write((byte)0);
			byte[] b = new byte[1028];
			while(!manager.done()) {
				int i = input.read(b);
				//This assumes that the packets themselves get here in one piece.
				if(manager.inputPacket(new Packet(b))) {
					manager.completeFile(b[1]);
				}
			}
		} catch(IOException ioe) {
			System.out.println("Caught IOException: ");
			System.out.println(ioe);
		}

        // Do all the heavy lifting here.
        // This should
        //   * Connect to the server
        //   * Download packets in some sort of loop
        //   * Handle the packets as they come in by, e.g.,
        //     handing them to some PacketManager class
        // Your loop will need to be able to ask someone
        // if you've received all the packets, and can thus
        // terminate. You might have a method like
        // PacketManager.allPacketsReceived() that you could
        // call for that, but there are a bunch of possible
        // ways.
	}
}
