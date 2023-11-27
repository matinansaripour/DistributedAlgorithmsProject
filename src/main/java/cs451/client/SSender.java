package cs451.client;

import java.io.IOException;
import java.net.*;

public class SSender extends Thread {

    private final DatagramSocket socket;
    private InetAddress receiverAddress;
    private int receiverPort;
    private SendersManager sendersManager;
    public SSender(DatagramSocket socket, String receiverAddress, int receiverPort, SendersManager sendersManager) throws SocketException, UnknownHostException {
        this.socket = socket;
        this.receiverAddress = InetAddress.getByName(receiverAddress);
        this.receiverPort = receiverPort;
        this.sendersManager = sendersManager;
    }

    public void run() {
        byte[] buf;
        while (true) {
            if (sendersManager.senderIsAlive()){
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                buf = sendersManager.getSMessage();
            }catch (Exception e){
                continue;
            }

            DatagramPacket packet = new DatagramPacket(buf, buf.length, receiverAddress, receiverPort);
            synchronized (socket) {
                try {
                    socket.send(packet);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
