package cs451.client;

import java.io.FileNotFoundException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;

public class Manager extends Client{

    private int id;
    private int messagePerPacket = 8;
    private final DatagramSocket socket;
    private final HashMap<String, Integer> addressToId;
    private final InetAddress[] idToAddress;
    private final int[] idToPort;
    private final int[] lastMessage;
    private final int[] lastLog;
    private int lastSentLog = 0;
    private int m;
    private int n;
    private ArrayList<SavedMessage> messageQueue = new ArrayList<>();
    private ArrayList<TreeSet<Integer>> ackMessages = new ArrayList<>();
    private Log log;
    private HashMap<String, HashSet<Integer>> ackCount = new HashMap<>();
    private int[] tmpLog;
    private int lastSSent = -1;

    public Manager(int id, int m, int n, int port, HashMap<String, Integer> addressToId, InetAddress[] idToAddress, int[] idToPort, String outputPath) throws SocketException, FileNotFoundException {
        this.id = id;
        this.m = m;
        this.n = n;
        this.socket = new DatagramSocket(port);
        this.addressToId = addressToId;
        this.idToAddress = idToAddress;
        lastMessage = new int[n];
        lastLog = new int[n];
        this.idToPort = idToPort;
        for(int i = 0; i < n; i++){
            ackMessages.add(new TreeSet<>());
        }
        log = new Log(outputPath);
        tmpLog = new int[n];
    }

    @Override
    public void run() {
        Sender sender = new Sender(id, m, n, socket, this);
        sender.start();
        for (int i = 0; i < 2; i++){
            SSender sSender = new SSender(id, n, socket, this);
            sSender.start();
        }
        for (int i = 0; i < 3; i++){
            Receiver receiver = new Receiver(socket, this);
            receiver.start();
        }
        new Thread(() -> {
            while (true){
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ignored) {}
                processLog();
            }
        }).start();
        while (true){
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {}
            handleAckMessages();
        }
    }


    public InetAddress getAddress(int id) {
        return idToAddress[id];
    }

    public int getPort(int id) {
        return idToPort[id];
    }

    public void addSavedMessage(SavedMessage savedMessage) {
        synchronized (messageQueue){
            messageQueue.add(savedMessage);
        }
    }

    public SavedMessage getMessage() throws Exception {
        int tmp;
        synchronized (this){
            if (lastSSent <= 0){
                lastSSent = messageQueue.size() - 1;
            }
            else {
                lastSSent = lastSSent - 1;
            }
            tmp = lastSSent;
        }
        if(tmp == -1){
            throw new Exception();
        }
        return messageQueue.get(tmp);
    }

    private void processLog() {
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < n; i++){
            if (lastLog[i] >= lastMessage[i]){
                continue;
            }
            String senderId = "d " + (i + 1) + " ";
            tmpLog[i] = lastMessage[i];
            for(int j = lastLog[i] + 1; j <= tmpLog[i]; j++){
                stringBuilder.append(senderId).append(j).append("\n");
            }
        }
        for (int i = 0; i < n; i++){
            lastLog[i] = tmpLog[i];
        }
        if (stringBuilder.length() == 0){
            return;
        }
        log.add(stringBuilder, false);
    }

    public void handleReceivedMessage(String localBuffer, InetAddress address, int port) {
        String[] strings = localBuffer.split(" ");
        int senderId = addressToId.get(address.getHostAddress() + " " + port);
        int first = Integer.parseInt(strings[1]);
        int last = Integer.parseInt(strings[strings.length - 1]);
        int messageId = Integer.parseInt(strings[0]);
        SavedMessage savedMessage = null;
        int num;
        synchronized (ackCount){
            HashSet<Integer> set;
            if(ackCount.containsKey(messageId + " " + strings[1])){
                set = ackCount.get(messageId + " " + strings[1]);
            }
            else {
                set = new HashSet<>();
                ackCount.put(messageId + " " + strings[1], set);
                savedMessage = new SavedMessage(senderId, first, localBuffer.getBytes());
            }
            set.add(senderId);
            num = set.size();
        }
        if(num >= n / 2){
            synchronized (idToAddress[senderId - 1]){
                if(lastMessage[senderId - 1] < last){
                    ackMessages.get(senderId - 1).add(last);
                }
            }
        }
        if(savedMessage != null){
            addSavedMessage(savedMessage);
        }
    }

    public void handleAckMessages() {
        for(int i = 0; i < n; i++){
            synchronized (idToAddress[i]){
                TreeSet<Integer> acks = ackMessages.get(i);
                if(acks.isEmpty()){
                    continue;
                }
                while (true){
                    int smallest = acks.first();
                    if(smallest - lastMessage[i] > messagePerPacket){
                        break;
                    }
                    acks.pollFirst();
                    lastMessage[i] = smallest;
                }
            }
        }
    }

    public void logSentMessages(int lastSent) {
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = lastSentLog + 1; i <= lastSent; i++){
            stringBuilder.append("b ").append(i).append("\n");
        }
        log.add(stringBuilder, true);
        lastSentLog = lastSent;
    }

    public HashSet<Integer> getAckCount(String key){
        try {
            return ackCount.get(key);
        } catch (Exception e){
            try{
                return ackCount.get(key);
            } catch (Exception e1){
                System.out.println("Error in getAckCount");
                return new HashSet<>();
            }
        }
    }

    @Override
    public void clearBuffer() {
        handleAckMessages();
        processLog();
        log.close();
        socket.close();
    }
}