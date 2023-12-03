//package cs451.client;
//
//
//import java.io.*;
//import java.net.DatagramSocket;
//import java.net.SocketException;
//import java.net.UnknownHostException;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//
//public class SendersManager extends Client{
//    private int receiverPort;
//    private final int id;
//    private final int numThreads;
//    private FileOutputStream fos;
//    private BufferedOutputStream bos;
//    private OutputStreamWriter writer;
//    private String receiverAddress;
//    private int lastWrite = 0;
//    private int lastSSent = -1;
//    private Sender sender;
//    private ArrayList<SSender> sSenders = new ArrayList<>();
//    private DatagramSocket socket;
//    public SendersManager(int id, int m, int port, String receiverAddress, int receiverPort, int numThreads, String outputPath) throws FileNotFoundException, SocketException, UnknownHostException {
//        this.id = id;
//        this.numThreads = numThreads;
//        this.receiverAddress = receiverAddress;
//        this.receiverPort = receiverPort;
//        fos = new FileOutputStream(outputPath);
//        bos = new BufferedOutputStream(fos);
//        writer = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
//        socket = new DatagramSocket(port);
//        sender = new Sender(id, m, socket, receiverAddress, receiverPort);
//    }
//
//    @Override
//    public void run() {
//        sender.start();
//        for (int i = 0; i < numThreads; i++){
//            SSender sSender;
//            try {
//                sSender = new SSender(socket, receiverAddress, receiverPort, this);
//            } catch (SocketException | UnknownHostException e) {
//                throw new RuntimeException(e);
//            }
//            sSenders.add(sSender);
//            sSender.start();
//        }
//        while (true){
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            if (sender.getLastMessage() > lastWrite){
//                try {
//                    processMessage();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }
//    }
//
//    public synchronized byte[] getSMessage() throws Exception {
//
//        if (lastSSent <= 0){
//            lastSSent = sender.getSentMessagesSize() - 1;
//        }
//        else {
//            lastSSent = lastSSent - 1;
//        }
//        if(lastSSent == -1){
//            throw new Exception();
//        }
//        return sender.getSentMessage(lastSSent);
//    }
//
//    private void processMessage() throws IOException {
//        StringBuilder log = new StringBuilder();
//        int lastSent = sender.getLastMessage();
//        for(int i = lastWrite + 1; i < lastSent; i++){
//            log.append("b ").append(i).append("\n");
//        }
//        if(lastSent <= lastWrite){
//            return;
//        }
//        else {
//            log.append("b ").append(lastSent).append("\n");
//        }
//        //todo
//        writer.write(log.toString());
//        lastWrite = lastSent;
//    }
//
//    @Override
//    public void clearBuffer() throws IOException {
//        processMessage();
//        writer.flush();
//        bos.flush();
//        fos.flush();
//        writer.close();
//        bos.close();
//        fos.close();
//        socket.close();
//    }
//
//    public boolean senderIsAlive() {
//        return sender.isAlive();
//    }
//}
