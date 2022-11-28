package segmentedfilesystem;

import java.net.DatagramPacket;
import java.util.Arrays;
    /*
     * status, fileID, and packetNumber are all unsigned ints
     * packetNumber is -1 if the packet does not have a number (it is a header packet)
     */
    public class Packet {
        public int status;
        public int fileID;
        public int packetNumber = -1;
        public DatagramPacket packet;

        /*
         * Given a byte array, creates a Packet.
         * If the status's last digit is a 0 (the int is even) it's a header packet
         * If the status's last digit is a 1 (the int is odd) it's a data packet
         * Allows us to throw collections of bytes at this constructor without doing
         * a ton of book keeping.
         */
        public Packet(DatagramPacket p) {
            byte[] b = p.getData();
            status = Byte.toUnsignedInt(b[0]);
            fileID = Byte.toUnsignedInt(b[1]);
            if(status % 2 != 0) {
                packetNumber = Byte.toUnsignedInt(b[2]) * (int)Math.pow(2, 8) + Byte.toUnsignedInt(b[3]);
            }
            packet = p;
        }

        public byte[] getData() {
            byte[] b = packet.getData();
            if(packetNumber == -1) {
                return Arrays.copyOfRange(b, 2, b.length);
            } else {
                return Arrays.copyOfRange(b, 4, b.length);
            }
        }
    }
