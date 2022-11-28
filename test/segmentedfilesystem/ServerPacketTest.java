package segmentedfilesystem;

import static org.junit.Assert.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.junit.Test;

/**
 * BE SURE A LOCAL SERVER IS RUNNING BEFORE RUNNING TESTS
 */
public class ServerPacketTest {

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
    public void connectsToServer() {
        try {
            assertTrue(connect().isConnected());
        } catch (Exception e) {
            fail(e.toString());
        }

    }

    @Test
    public void getsResponsesFromServer() {
        try {
            DatagramSocket socket = connect();
            DatagramPacket p = emptyPacket();
            socket.receive(p);
            assertNotNull(p);
            assertNotNull(p.getData());
            assertNotNull(p.getData()[4]);
        } catch (Exception e) {
            fail(e.toString());
        } 
    }

    @Test
    public void convertsToPackets() {
        try {
            DatagramSocket socket = connect();
            DatagramPacket p = emptyPacket();
            socket.receive(p);
            Packet clientPacket = new Packet(p);
            assertNotEquals(p.getData(), clientPacket.getData());
            assertNotEquals(clientPacket.getData().length, 1028);
            assertNotEquals(clientPacket.fileID, -1);
            assertNotEquals(clientPacket.status, -1);
        } catch (Exception e) {
            fail(e.toString());
        }
    }

}
