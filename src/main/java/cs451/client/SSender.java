package cs451.client;

import java.io.IOException;
import java.net.*;

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
        while (true) {
            Proposal proposal = manager.getProposal();
            if(proposal == null){
                continue;
            }
            String messageToSend = proposal.getMessageToSend();
            for(int i = 0; i < n; i++){
                if(i == id - 1){
                    continue;
                }
                if(!proposal.isActive() && proposal.isDelivered(i)){
                    continue;
                }
                if(proposal.isAcked(i) && proposal.isActive()){
                    continue;
                }
                InetAddress address = manager.getAddress(i);
                int port = manager.getPort(i);
                byte[] message = messageToSend.getBytes();
                DatagramPacket packet = new DatagramPacket(message, message.length, address, port);
                try {
                    socket.send(packet);
                } catch (IOException ignored) {}

            }
        }
    }

}
