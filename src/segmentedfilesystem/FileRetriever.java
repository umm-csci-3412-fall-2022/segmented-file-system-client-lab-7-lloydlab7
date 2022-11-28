package segmentedfilesystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;

public class FileRetriever {
	public static final int NUM_FILES = 3;
	String svr;
	PacketManager manager;
	DatagramSocket socket;
	public FileRetriever(String server, int port) {
		try {
			svr = server;
			manager = new PacketManager(NUM_FILES);
			socket = new DatagramSocket();
			socket.connect(InetAddress.getByName(server), port);
		} catch(IOException ioe) {
			System.out.println("Caught IOException: ");
			System.out.println(ioe);
		}

	}

	public void downloadFiles() {
		try {
			byte[] b = new byte[1028];
			DatagramPacket p = new DatagramPacket(b, b.length);
			socket.send(p);
			while(!manager.done()) {
				socket.receive(p);
				//This assumes that the packets themselves get here in one piece.
				if(manager.inputPacket(new Packet(p))) {
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
