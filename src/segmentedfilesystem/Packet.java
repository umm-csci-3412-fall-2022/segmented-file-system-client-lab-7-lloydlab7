package segmentedfilesystem;

import java.util.Arrays;

    /*
     * status, fileID, and packetNumber are all unsigned ints
     * packetNumber is -1 if the packet does not have a number (it is a header packet)
     */
    public class Packet {
        public int status;
        public int fileID;
        public int packetNumber = -1;
        public byte[] bytes;

        /*
         * Given a byte array, creates a Packet.
         * If the status's last digit is a 0 (the int is even) it's a header packet
         * If the status's last digit is a 1 (the int is odd) it's a data packet
         */
        public Packet(byte[] b) {
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
