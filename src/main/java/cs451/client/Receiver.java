package cs451.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import static cs451.Parameters.MAX_PACKET_SIZE;

class Receiver extends Thread {
    private final DatagramSocket socket;
    private final Manager manager;
    private final byte[] buf = new byte[MAX_PACKET_SIZE];

    public Receiver(DatagramSocket socket, Manager manager) {
        this.socket = socket;
        this.manager = manager;
    }

    public void run() {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        while (true) {
            try {
                socket.receive(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String localBuffer = new String(packet.getData(), 0, packet.getLength());
            manager.handleReceivedMessage(localBuffer, packet.getAddress(), packet.getPort());
        }
    }
}
