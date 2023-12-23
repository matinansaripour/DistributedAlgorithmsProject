package cs451.client;

import java.io.IOException;
import java.net.*;
import java.util.HashSet;

public class Sender extends Thread {

    private int id;
    private int messagePerPacket = 8;
    private final DatagramSocket socket;
    private int lastMessage = 0;
    private Manager manager;
    private int m;
    private int n;

    public Sender(int id, int m, int n, DatagramSocket socket, Manager manager) {
        this.id = id;
        this.m = m;
        this.socket = socket;
        this.manager = manager;
        this.n = n;
    }

    public void run() {
        while (true) {
            if ((lastMessage / messagePerPacket) % 10000 == 0) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}
            }
//            try {
//                Thread.sleep(1);
//            } catch (InterruptedException ignored) {}
            int message = Math.min(lastMessage + messagePerPacket, m);
            if(lastMessage >= message){
                break;
            }
            StringBuilder messageToSend = new StringBuilder();
            messageToSend.append(id);
            for(int i = lastMessage + 1; i <= message; i++){
                messageToSend.append(" ").append(i);
            }
            byte[] buf = messageToSend.toString().getBytes();
            manager.logSentMessages(message);
            for(int i = 0; i < n; i++){
                int receiverId = i + 1;
                if(receiverId == id){
                    continue;
                }
                InetAddress address = manager.getAddress(i);
                int port = manager.getPort(i);
                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
                try {
                    socket.send(packet);
                } catch (IOException e) {}
            }
            manager.addSavedMessage(new SavedMessage(id, lastMessage + 1, buf));
            lastMessage = message;
        }
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
                } catch (Exception e) {}

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
