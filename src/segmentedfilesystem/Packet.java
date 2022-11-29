package segmentedfilesystem;

import java.net.DatagramPacket;
import java.util.Arrays;
    /*
     * status, fileID, and packetNumber are all unsigned ints
     * packetNumber is -1 if the packet does not have a number (it is a header packet)
     * bytes contains all of the actual information being put into the relevant file.
     */
    public class Packet {
        public int status = -1;
        public int fileID = -1;
        public int packetNumber = -1;
        private byte[] bytes;

        /*
         * Given a byte array, creates a Packet.
         * If the status's last digit is a 0 (the int is even) it's a header packet
         * If the status's last digit is a 1 (the int is odd) it's a data packet
         * Does all of the book keeping as soon as we recieve a packet.
         */
        public Packet(DatagramPacket p) {
            byte[] b = Arrays.copyOfRange(p.getData(), p.getOffset(), p.getLength());
            status = Byte.toUnsignedInt(b[0]);
            fileID = Byte.toUnsignedInt(b[1]);
            if(status % 2 != 0) {
                bytes = Arrays.copyOfRange(b, 4, b.length);
                packetNumber = (Byte.toUnsignedInt(b[2]) * (int)Math.pow(2, 8)) + Byte.toUnsignedInt(b[3]);
            } else {
                bytes = Arrays.copyOfRange(b, 2, b.length);
            }
            
        }

        //Assures that the data inside a packet cannot be changed.
        public byte[] getData() {
            return bytes;
        }
    }
