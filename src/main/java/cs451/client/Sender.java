package cs451.client;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class Sender extends Thread {

    private int id;
    private int messagePerPacket = 7;
    private final DatagramSocket socket;
    private InetAddress receiverAddress;
    private int receiverPort;
    private byte[] buf = new byte[128];
    private int lastMessage = 0;
    private int m;
    private ArrayList<byte[]> sentMessages = new ArrayList<>();

    public Sender(int id, int m, DatagramSocket socket, String receiverAddress, int receiverPort) throws SocketException, UnknownHostException {
        this.id = id;
        this.m = m;
        this.socket = socket;
        this.receiverAddress = InetAddress.getByName(receiverAddress);
        this.receiverPort = receiverPort;
    }

    public void run() {
        while (true) {
            int message = Math.min(lastMessage + messagePerPacket, m);
            if(lastMessage >= message){
                break;
            }
            StringBuilder messageToSend = new StringBuilder();
            messageToSend.append(id);
            for(int i = lastMessage + 1; i <= message; i++){
                messageToSend.append(" ").append(i);
            }
            buf = messageToSend.toString().getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, receiverAddress, receiverPort);
            //todo
            synchronized (this){
                lastMessage = message;
            }
            synchronized (socket) {
                try {
                    socket.send(packet);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            synchronized (sentMessages){
                sentMessages.add(buf);
            }
        }
    }

    public int getLastMessage(){
        synchronized (this){
            return lastMessage;
        }
    }

    public int getSentMessagesSize() {
        synchronized (sentMessages){
            return sentMessages.size();
        }
    }

    public byte[] getSentMessage(int i){
        synchronized (sentMessages){
            return sentMessages.get(i);
        }
    }

}
