package cs451.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
class Receiver extends Thread {
    private final DatagramSocket socket;
    private final ReceiverManager receiverManager;
    private String localBuffer;
    private byte[] buf = new byte[128];

    public Receiver(DatagramSocket socket, ReceiverManager receiverManager) {
        this.socket = socket;
        this.receiverManager = receiverManager;
    }

    public void run() {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        while (true) {
            //todo
            synchronized (socket) {
                try {
                    socket.receive(packet);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            localBuffer = new String(packet.getData(), 0, packet.getLength());
            receiverManager.addToBuffer(localBuffer);
        }
    }


    public String getLocalBuffer() {
        return localBuffer;
    }
}
