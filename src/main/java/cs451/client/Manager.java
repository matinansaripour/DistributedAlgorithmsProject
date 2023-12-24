package cs451.client;

import cs451.ConfigParser;

import java.io.FileNotFoundException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;

import static cs451.Parameters.MAX_COMPRESSION;
import static cs451.Parameters.PROPOSAL_BATCH;

public class Manager extends Client{

    public static final LinkedList<ArrayList<Integer>> originals = new LinkedList<>();

    private int id;
    private int finished = 0;
    private int messagePerPacket = 8;
    private final DatagramSocket socket;
    private final HashMap<String, Integer> addressToId;
    private final InetAddress[] idToAddress;
    private final int[] idToPort;
    private int last;
    private int lastSSent = 0;
    private int lastCleaned = 0;
    private int n;
    private Log log;
    private final HashMap<Integer, HashSet<Integer>> acceptorSet;
    private final HashMap<Integer, StringBuilder> acceptorBuffer;
    private final HashMap<Integer, Proposal> proposals;
    private final HashMap<Integer, Integer>[] processResponses;
    private final HashMap<Integer, HashSet<Integer>> numberDecided = new HashMap<>();
    private final TreeSet<Integer> isHere;
    private int lastIndex;
    private  Iterator<Integer> iterator;

    public Manager(int id, int n, int port, HashMap<String, Integer> addressToId, InetAddress[] idToAddress, int[] idToPort, String outputPath) throws SocketException, FileNotFoundException {
        this.id = id;
        this.n = n;
        this.lastIndex = 0;
        this.socket = new DatagramSocket(port);
        this.addressToId = addressToId;
        this.idToAddress = idToAddress;
        this.idToPort = idToPort;
        this.last = 0;
        this.acceptorBuffer = new HashMap<>();
        this.acceptorSet = new HashMap<>();
        this.proposals = new HashMap<>();
        this.isHere = new TreeSet<>();
        iterator = isHere.iterator();
        log = new Log(outputPath);
        processResponses = new HashMap[n];

        loadNext();
    }

    @Override
    public void run() {
        for (int i = 0; i < 3; i++){
            SSender sSender = new SSender(id, n, socket, this);
            sSender.start();
        }
        for (int i = 0; i < 4; i++){
            Receiver receiver = new Receiver(socket, this);
            receiver.start();
        }
        while (true){
            try {
                Thread.sleep(300);
            } catch (InterruptedException ignored) {}
            handleDecidedMessages();
            if (finished >= Math.min(MAX_COMPRESSION, PROPOSAL_BATCH)) {
                if (ConfigParser.readProposals()) {
                    loadNext();
                }
                finished = 0;
            }
            while (lastCleaned < last && numberDecided.getOrDefault(lastCleaned, new HashSet<>()).size() == n){
                synchronized (this){
                    isHere.remove(lastCleaned);
                    acceptorSet.remove(lastCleaned);
                    acceptorBuffer.remove(lastCleaned);
                    proposals.remove(lastCleaned);
                    for (int i = 0; i < n; i++){
                        processResponses[i].remove(lastCleaned);
                    }
                    numberDecided.remove(lastCleaned);
                }

                lastCleaned++;
            }
        }
    }


    public InetAddress getAddress(int id) {
        return idToAddress[id];
    }

    public int getPort(int id) {
        return idToPort[id];
    }

    public void checkDecide(Proposal proposal){
        if (!proposal.isActive()){
            return;
        }
        int ackCount = proposal.getAckCount();
        int nackCount = proposal.getNackCount();
        if(ackCount >= n / 2){
            proposal.setActive(false);
            ++finished;
        }
        else if (nackCount + ackCount >= n / 2){
            proposal.clear();
        }
    }

    public void handleReceivedMessage(String localBuffer, InetAddress address, int port) {
        String[] strings = localBuffer.split(",");
        int senderId = addressToId.get(address.getHostAddress() + " " + port);
        StringBuilder ackNaks = new StringBuilder();

        for (int i = 0; i < strings.length; i++){
            String[] request = strings[i].split(" ");
            int type = Integer.parseInt(request[0]);
            int proposalId = Integer.parseInt(request[1]);
            if (type == 3){
                HashSet<Integer> set;
                synchronized (numberDecided){
                    set = numberDecided.getOrDefault(proposalId, null);
                    if (set == null){
                        set = new HashSet<>();
                        numberDecided.put(proposalId, set);
                    }
                }
                synchronized (set){
                    set.add(senderId);
                }
                ackNaks.append("-3 ").append(proposalId).append(",");
                continue;
            }
            if (type == -3){
                Proposal proposal = proposals.getOrDefault(proposalId, null);
                if (proposal == null){
                    continue;
                }
                synchronized (proposal){
                    proposal.addDelivered(senderId);
                }
            }
            int proposalNumber = Integer.parseInt(request[2]);
            if (type == 0){
                //propose
                ArrayList<Integer> set = new ArrayList<>();
                for (int j = 3; j < request.length; j++){
                    set.add(Integer.parseInt(request[j]));
                }
                try {
                    if (acceptorCheck(set, proposalId)){
                        //send ack
                        ackNaks.append("1 ").append(proposalId).append(" ").append(proposalNumber).append(",");
                    }
                    else {
                        //send nack
                        StringBuilder acBuffer = acceptorBuffer.get(proposalId);
                        synchronized (acBuffer){
                            ackNaks.append("2 ").append(proposalId).append(" ").append(proposalNumber).append(" ").append(acBuffer).append(",");
                        }
                    }
                } catch (Exception e) {}
            }
            else{
                Proposal proposal = proposals.getOrDefault(proposalId, null);
                if (proposal == null){
                    continue;
                }
                synchronized (proposal){
                    if (!proposal.isActive() || proposal.getActiveProposalNumber() != proposalNumber){
                        continue;
                    }
                    if (type == 1){
                        //ack
                        proposal.addAckCount(senderId);
                    }
                    else if (type == 2){
                        //nack
                        ArrayList<Integer> set = new ArrayList<>();
                        for (int j = 3; j < request.length; j++){
                            set.add(Integer.parseInt(request[j]));
                        }
                        proposal.addProposedValue(set);
                        proposal.addNackCount(senderId);
                    }
                    processResponses[senderId - 1].put(proposalId, proposalNumber);
                    checkDecide(proposal);
                }
            }
        }
        if (ackNaks.length() > 0){
            ackNaks.deleteCharAt(ackNaks.length() - 1);
            sendAckNack(ackNaks.toString().getBytes(), senderId);
        }
    }

    private void sendAckNack(byte[] buf, int senderId) {
        InetAddress address = idToAddress[senderId - 1];
        int port = idToPort[senderId - 1];
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        try {
            socket.send(packet);
        } catch (Exception ignored) {}
    }

    public void handleDecidedMessages() {
        Proposal proposal = proposals.getOrDefault(last, null);
        if (proposal == null){
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        while (!proposal.isActive()){
            stringBuilder.append(proposal.getMessage().append('\n'));
            last++;
        }
        log.add(stringBuilder);
    }

    public boolean acceptorCheck(ArrayList<Integer> set, int proposalId) throws Exception {
        Set<Integer> acSet = acceptorSet.getOrDefault(proposalId, null);
        StringBuilder acBuffer = acceptorBuffer.getOrDefault(proposalId, null);
        if (acSet == null || acBuffer == null){
            throw new Exception("Acceptor not found");
        }
        synchronized (acSet){
            boolean ack = true;
            for (int value : set) {
                if (!acSet.contains(value)) {
                    acSet.add(value);
                    synchronized (acBuffer){
                        acBuffer.append(" ").append(value);
                    }
                    ack = false;
                }
            }
            return ack;
        }
    }

    public synchronized Proposal getProposal(){
        synchronized (this){
            if (iterator.hasNext()) {
                // Get the next element
                int element = iterator.next();

                // Do something with the element
                return proposals.get(element);
            }
            else {
                if (isHere.isEmpty()){
                    return null;
                }
                iterator = isHere.iterator();
            }
            return proposals.get(iterator.next());
        }
    }

    private void loadNext() {
        Iterator<ArrayList<Integer>> iterator = originals.listIterator();
        while (iterator.hasNext()) {
            ArrayList<Integer> arrayList = iterator.next();
            iterator.remove();
            synchronized (this){
                acceptorBuffer.put(lastIndex, new StringBuilder());
                acceptorSet.put(lastIndex, new HashSet<>());
                try{
                    acceptorCheck(arrayList, lastIndex);
                } catch (Exception e) {}
                proposals.put(lastIndex, new Proposal(lastIndex, arrayList));
                isHere.add(lastIndex);
            }
            lastIndex++;
        }
    }

    @Override
    public void clearBuffer() {
        handleDecidedMessages();
        log.close();
        socket.close();
    }
}
