package segmentedfilesystem;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;

public class FileRetriever {
	public static final int NUM_FILES = 3;
	String svr;
	PacketManager manager;
	DatagramSocket socket;
	/*
	 * Initializes the socket and connects to the server/port.
	 */
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

	/*
	 * Downloads files gotten from server
	 */
	public void downloadFiles() {
		try {
			//Creates empty packet
			byte[] b = new byte[1028];
			DatagramPacket p = new DatagramPacket(b, b.length);
			socket.send(p);
			//Recieves packets until there are none left to be processed.
			while(!manager.done()) {
				socket.receive(p);
				//This assumes that the packets themselves get here in one piece.
				Packet clientPacket = new Packet(p);
				//If the file has been completed
				if(manager.inputPacket(clientPacket)) {
					//Create a new file to write to
					File f = new File(manager.getFileName(clientPacket.fileID));
					//If that file doesn't already exist
					if(f.createNewFile()) {
						//Start writing the bytes stored in those packets.
						FileOutputStream output = new FileOutputStream(f);
						Packet[] arr = manager.completeFile(b[1]);
						for(Packet pack: arr) {
							output.write(pack.getData());
						}
						output.flush();
						output.close();
					} else {
						System.out.println("File " + f.getName() + " already exists.");
					}
				}
			}
			socket.close();
		} catch(IOException ioe) {
			System.out.println("Caught IOException: ");
			System.out.println(ioe);
		}
	}
}
