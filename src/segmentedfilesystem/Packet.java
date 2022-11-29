package segmentedfilesystem;

import java.net.DatagramPacket;
import java.util.Arrays;
    /*
     * status, fileID, and packetNumber are all unsigned ints
     * packetNumber is -1 if the packet does not have a number (it is a header packet)
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
         * Allows us to throw collections of bytes at this constructor without doing
         * a ton of book keeping.
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

        /*
         * Only grabs the portion that references actual data, rather than the book keeping bytes.
         */
        public byte[] getData() {
            return bytes;
        }
    }
