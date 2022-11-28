package segmentedfilesystem;

import static org.junit.Assert.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.junit.Test;

/*
 * Tests specific to the packet manager, that do not need the server involved.
 */
public class PacketManagerTest {

    private static final int port = 6014;
    private static final String server = "localhost";


    public static DatagramSocket connect() throws Exception {
        DatagramSocket socket = new DatagramSocket();
        socket.connect(InetAddress.getByName(server), port);
        DatagramPacket p = emptyPacket();
        socket.send(p);
        return socket;
    }

    public static DatagramPacket emptyPacket() {
        byte[] b = new byte[1028];
        return new DatagramPacket(b, b.length);
    }

    @Test
    public void inputsOrderedPackets() {
        try {
            PacketManager manager = new PacketManager(3);
            byte[] b = new byte[1028];
            b[0] = (byte)0;
            b[1] = (byte)0;
            manager.inputPacket(new Packet(new DatagramPacket(b, 1028)));
            b = new byte[1028];
            b[0] = (byte)3;
            b[1] = (byte)0;
            b[2] = (byte)0;
            b[3] = (byte)0;
            b[4] = (byte)0;
            assertTrue(manager.inputPacket(new Packet(new DatagramPacket(b, 1028))));
            manager.completeFile(0);
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void handlesMultipleFiles() {
        try {
            PacketManager manager = new PacketManager(3);
            for(int i=0; i<3; i++) {
                byte[] b = new byte[1028];
                b[0] = (byte)0;
                b[1] = (byte)i;
                manager.inputPacket(new Packet(new DatagramPacket(b, 1028)));
                b = new byte[1028];
                b[0] = (byte)3;
                b[1] = (byte)i;
                b[2] = (byte)0;
                b[3] = (byte)0;
                b[4] = (byte)0;
                assertTrue(manager.inputPacket(new Packet(new DatagramPacket(b, 1028))));
                manager.completeFile(i);
            }
            assertTrue(manager.done());
        } catch (Exception e) {
            fail(e.toString());
        }
    }



    @Test
    public void storesFileNames() {
        try {
            PacketManager manager = new PacketManager(1);
            byte[] b = new byte[1028];
            String s = "asdfghjklasdfghjkldfsajkfldhsakgdhasjgdsa";
            char[] charArr = s.toCharArray();
            for(int i=0; i<charArr.length; i++) {
                b[i+2] = (byte)charArr[i];
            }
            manager.inputPacket(new Packet(new DatagramPacket(b, 1028)));
            assertEquals(manager.getFileName(0).substring(0,s.length()), s);
        } catch (Exception e) {
            fail(e.toString());
        }
    }
}
