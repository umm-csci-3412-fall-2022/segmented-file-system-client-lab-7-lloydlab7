package segmentedfilesystem;

import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.crypto.Data;

public class PacketManager {
    public int numFiles;
    private int[] numBytes;
    private ArrayList<Packet> packets;
    public PacketManager(int expectedNumOfFiles) {
        numFiles = expectedNumOfFiles;
        //Keeps track of number of bytes in each file. -1 means we don't know. -2 means the file is completed.
        numBytes = new int[numFiles];
        // Set each numBytes to -1 to show we don't know how many are in this file.
        for(int i=0; i<numFiles; i++) {
            numBytes[i] = -1;
        }
    }

    // -2 in numBytes means the file has been completed.
    public boolean done() {
        for(int i=0; i<numFiles; i++) {
            if(numBytes[i] != -2) {
                return false;
            }
        }
        return true;
    }

    /*
     * Add DataPacket to list, returns true if the relevant file has been completed.
     */
    public boolean inputDataPacket(Packet p) {
        packets.add(p);

        if(p.packetNumber == -1) {
            System.out.println("Attempted to enter header packet into inputDataPacket");
            return false;
        } else {

        //If packet is final packet, assign the number of bytes.
        if(p.status % 4 == 3) {
            if(numBytes[p.fileID] == -1) {
                numBytes[p.fileID] = p.packetNumber;
            } else {
                System.out.println("Got two final packets for the same file");
            }
        }

        // Checks if the relevant file has been completed.
        if(numBytes[p.fileID] >= 0) {
            //If it ever exits the loop with found = false it did so because it did not find a specific packet
            boolean found = false;
            for(int i=0; i<numBytes[p.fileID]; i++) {
                found = false;
                for(int j=0; j<packets.size(); j++) {                    
                    if(packets.get(i).fileID == p.fileID) {
                        if(packets.get(i).packetNumber == i) {
                            found = true;
                            j = packets.size();
                        }
                    }
                }
                if(!found) {
                    i = numBytes[p.fileID];
                }
            }
            return found; 
        }
        return false;
    }}

    /*
     * When run, assumes that a certain file has been completed,
     * and therefore all the necessary data packets are contained within our ArrayList of packets
     * 
     * Returns the array containing all of the packets, in order.
     */
    public Packet[] completeFile(int fileNum) {
        Packet[] result = new Packet[numBytes[fileNum]];
        Packet[] packetsInFile = new Packet[numBytes[fileNum]];

        //Grabs all the relevant packets for this specific file
        int count = 0;
        for(int i=0; i<packets.size(); i++) {
            Packet p = packets.get(i);
            if(p.fileID == fileNum) {
                if(p.packetNumber == -1) {
                    System.out.println("Header packet in our Data Packets");
                } else {
                    packetsInFile[count] = p;
                    count++;
                    packets.remove(p);
                    i--;
                }
            }
        }

        // Counting sort
        for(int i=0; i<packetsInFile.length; i++) {
            for(int j=0; j<packetsInFile.length; j++) {
                if(packetsInFile[j].packetNumber == i) {
                    result[i] = packetsInFile[j];
                    j = packetsInFile.length;
                }
            }
        }
        numBytes[fileNum] = -2;
        return result;
    }


    /*
     * Defines the structure of packets
     * status, fileID, and packetNumber are all unsigned ints
     * packetNumber is -1 if the packet does not have a number (it is a header packet)
     */
    public class Packet {
        public int status;
        public int fileID;
        public int packetNumber = -1;
        public Byte[] bytes;

        /*
         * Given a byte array, creates a Packet.
         * If the status's last digit is a 0 (the int is even) it's a header packet
         * If the status's last digit is a 1 (the int is odd) it's a data packet
         */
        public Packet(Byte[] b) {
            status = Byte.toUnsignedInt(b[0]);
            fileID = Byte.toUnsignedInt(b[1]);
            if(status % 2 == 0) {
                bytes = Arrays.copyOfRange(b, 2, b.length);
            } else {
                packetNumber = Byte.toUnsignedInt(b[2]) * (int)Math.pow(2, 8) + Byte.toUnsignedInt(b[3]);
                bytes = Arrays.copyOfRange(b, 4, fileID);
            }
        }
    }
}
