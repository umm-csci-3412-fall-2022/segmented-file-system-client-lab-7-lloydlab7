package segmentedfilesystem;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

import org.junit.Test;

/*
 * Tests specific to the packet manager, that do not need the server involved.
 */
public class PacketManagerTest {

    private static final int port = 6014;
    private static final String server = "localhost";

    public static DatagramPacket emptyPacket() {
        byte[] b = new byte[1028];
        return new DatagramPacket(b, b.length);
    }

    @Test
    public void inputsOrderedPackets() {
        try {
            PacketManager manager = new PacketManager(3);
            //Serves as the header packet, 0 is the status, 0 is the fileID, NUL is the name.
            manager.inputPacket(new Packet(emptyPacket()));
            byte[] b = new byte[1028];
            //Serves as the final packet, 3 is the status, 0 is the fileID, there is no data to the file.
            b[0] = (byte)3;
            assertTrue(manager.inputPacket(new Packet(new DatagramPacket(b, 1028))));
            //Verifies that this can run without Exception.
            manager.completeFile(0);
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
            //Converts the file names to bytes, then puts it into the buffer of b
            char[] charArr = s.toCharArray();
            for(int i=0; i<charArr.length; i++) {
                b[i+2] = (byte)charArr[i];
            }
            //inputs b, status 0, fileID 0.
            manager.inputPacket(new Packet(new DatagramPacket(b, 1028)));
            //verifies that the manager grabbed the fileName.
            assertEquals(manager.getFileName(0), s);
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void handlesMultiplePackets() {
        try {
            PacketManager manager = new PacketManager(3);
            String s = "abcdef";
            byte[] b = new byte[1028];
            //Serves as the header packet, 0 is the status, 0 is the fileID, NUL is the name.
            manager.inputPacket(new Packet(emptyPacket()));
            //Sets them as data packets
            b[0] = (byte)1;
            //loops through 5 data packets, each one being another letter in the string abcdef
            for(int i=0; i<5; i++) {
                b[3] = (byte)i;
                b[4] = (byte)s.charAt(i);
                manager.inputPacket(new Packet(new DatagramPacket(b, 6)));
            }
            //Then sends end packet, this is in order.
            b[0] = (byte)3;
            b[3] = (byte)5;
            b[4] = (byte)s.charAt(5);
            assertTrue(manager.inputPacket(new Packet(new DatagramPacket(b, 6))));
            Packet[] p = manager.completeFile(0);
            String result = "";
            for(int i=0; i<6; i++) {
                result += (char)p[i].getData()[0];
            }
            assertEquals(s, result);
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void handlesMultipleFiles() {
        try {
            PacketManager manager = new PacketManager(3);
            for(int i=0; i<3; i++) {
                //Creates the header packet, status 0, fileID of i, NUL is the name
                byte[] b = new byte[1028];
                b[0] = (byte)0;
                b[1] = (byte)i;
                manager.inputPacket(new Packet(new DatagramPacket(b, 1028)));
                //Creates the end packet, status 3, fileID of i, no other data within
                b = new byte[1028];
                b[0] = (byte)3;
                b[1] = (byte)i;
                assertTrue(manager.inputPacket(new Packet(new DatagramPacket(b, 1028))));
            }
            //Runs in reverse order to prove it can handle all of the packets at once.
            for(int i=2; i>=0; i--) {
                manager.completeFile(i);
            }
            assertTrue(manager.done());
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void handlesComplexRandomOrderedFiles() {
        int numFiles = 10;
        PacketManager manager = new PacketManager(numFiles);
        String s = "abcdefghjklmnopqrstuvwxyz";
        ArrayList<Packet> packets = new ArrayList<Packet>();
        //For each file store packets in a seperate arraylist
        for(int i=0; i<numFiles; i++) {
            //Create a header packet
            byte[] b = new byte[1028];
            b[0] = (byte)0;
            b[1] = (byte)i;
            packets.add(new Packet(new DatagramPacket(b, 5)));
            b[0] = (byte)1;
            //loop through 10 data packets, each one being another letter in the string
            for(int j=0; j<10; j++) {
                b[3] = (byte)j;
                b[4] = (byte)s.charAt(j);
                packets.add(new Packet(new DatagramPacket(b, 5)));
            }
            //Create final packet
            b[0] = (byte)3;
            b[3] = (byte)10;
            b[4] = (byte)s.charAt(10);
            packets.add(new Packet(new DatagramPacket(b, 5)));
        }
        //Loop through this arraylist inputting elements at random.
        for(int i=numFiles * 12; i>0; i--) {
            int randInt = (int)(i * Math.random());
            manager.inputPacket(packets.remove(randInt));
        }
        //Complete the files after all elements have been inputted.
        for(int i=0; i<numFiles; i++) {
            manager.completeFile(i);
        }
        assertTrue(manager.done());    
    }
}
