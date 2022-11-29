package segmentedfilesystem;

import java.util.ArrayList;


public class PacketManager {
    public int numFiles;
    private byte[][] fileNames;
    private int[] numBytes;
    private ArrayList<Packet> packets;
    private boolean[] hasName;
    public PacketManager(int expectedNumOfFiles) {
        //Keeps track of the packets
        packets = new ArrayList<Packet>();
        numFiles = expectedNumOfFiles;
        //Keeps track of if we know what the name is
        hasName = new boolean[expectedNumOfFiles];
        //Keeps track of the names
        fileNames = new byte[expectedNumOfFiles][];
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
     * Checks if packet is data/header packet. If header packet stores filename and fileID
     */
    public boolean inputPacket(Packet p) {
        if(p.packetNumber == -1) {
            fileNames[p.fileID] = p.getData();
            hasName[p.fileID] = true;
            //Checks to see if the header packet was the last one.
            return isComplete(p.fileID);
        } else {
            return inputDataPacket(p);
        }

    }

    /*
     * Add DataPacket to list, returns true if the relevant file has been completed.
     */
    private boolean inputDataPacket(Packet p) {
        packets.add(p);

        if(p.packetNumber == -1) {
            System.out.println("Attempted to enter header packet into inputDataPacket");
            return false;
        } else {

        //If packet is final packet, assign the number of bytes.
        if(p.status % 4 == 3) {
            if(numBytes[p.fileID] == -1) {
                numBytes[p.fileID] = p.packetNumber+1;
            } else {
                System.out.println("Got two final packets for the same file");
            }
        }
        return isComplete(p.fileID);
    }}

    private boolean isComplete(int fileNum) {
        // Checks if the relevant file has been completed.
        if(numBytes[fileNum] >= 1) {
            //If it ever exits the loop with found = false it did so because it did not find a specific packet
            boolean found = false;
            for(int i=0; i<numBytes[fileNum]; i++) {
                //For each packet in file
                found = false;
                for(int j=0; j<packets.size(); j++) {
                    //Search through packets                    
                    if(packets.get(j).fileID == fileNum) {
                    //Find packets with the correct fileID
                        if(packets.get(j).packetNumber == i) {
                        //Verify packet with that number has been stored.
                            found = true;
                            j = packets.size();
                        }
                    }
                }
                if(!found) {
                    i = numBytes[fileNum];
                }
            }
            //Checks to make sure we also have the header packet.
            return found && hasName[fileNum]; 
        }
        return false;
    }

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
     * Assumes filenames do not contain NUL
     */
    public String getFileName(int fileNum) {
        String result = "";
        for(byte b: fileNames[fileNum]) {
            if(b != 0)
                result += (char)b;
        }
        return result;
    }
}
