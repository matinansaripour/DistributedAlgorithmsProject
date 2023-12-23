package cs451.client;

import java.io.IOException;
import java.net.*;
import java.util.HashSet;

public class SSender extends Thread {

    private final DatagramSocket socket;
    private Manager manager;
    private int id;
    private int n;
    public SSender(int id, int n, DatagramSocket socket, Manager manager){
        this.socket = socket;
        this.manager = manager;
        this.id = id;
        this.n = n;
    }

    public void run() {
        SavedMessage message;
        while (true) {
            try {
                message = manager.getMessage();
            }catch (Exception e){
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException ignored) {}
                continue;
            }
            HashSet<Integer> set = manager.getAckSendCount(message.getSenderId() + " " + message.getSequenceNumber());
            for (int i = 0; i < n; i++) {
                if (i + 1 == id) {
                    continue;
                }
                try{
                    if(set != null && set.contains(i + 1)) {
                        continue;
                    }
                } catch (Exception ignored) {}
                InetAddress address = manager.getAddress(i);
                int port = manager.getPort(i);
                DatagramPacket packet = new DatagramPacket(message.getMessage(), message.getMessage().length, address, port);
                try {
                    socket.send(packet);
                } catch (IOException ignored) {}
            }
        }
    }

}
