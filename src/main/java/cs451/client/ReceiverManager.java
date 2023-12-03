//package cs451.client;
//
//
//import java.io.*;
//import java.net.DatagramSocket;
//import java.net.SocketException;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.HashSet;
//
//
//public class ReceiverManager extends Client{
//    private final int id;
//    private final DatagramSocket socket;
//    private final int numThreads;
//    private FileOutputStream fos;
//    private BufferedOutputStream bos;
//    private OutputStreamWriter writer;
//    private final ArrayList<String> buffer = new ArrayList<>();
//    private ArrayList<Receiver> receivers = new ArrayList<>();
//    private HashSet<String> messageSet = new HashSet<>();
//
//
//    public ReceiverManager(int id, int port, int numThreads, String outputPath) throws SocketException, FileNotFoundException {
//        this.id = id;
//        socket = new DatagramSocket(port);
//        this.numThreads = numThreads;
//        fos = new FileOutputStream(outputPath);
//        bos = new BufferedOutputStream(fos);
//        writer = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
//    }
//
//    @Override
//    public void run() {
//        for(int i = 0; i < numThreads; i++){
//            Receiver receiver = new Receiver(socket, this);
//            receivers.add(receiver);
//            receiver.start();
//        }
//        while (true) {
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            synchronized (buffer){
//                if (buffer.isEmpty()){
//                    continue;
//                }
//            }
//            try {
//                processMessage();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
//
//    public void addToBuffer(String localBuffer) {
//        synchronized (buffer){
//            buffer.add(localBuffer);
//        }
//    }
//
//    private void processMessage() throws IOException {
//        StringBuilder log = new StringBuilder();
//        synchronized (buffer){
//            for(String buffered: buffer){
//                String[] strings = buffered.split(" ");
//                String tmp;
//                String senderId = "d " + strings[0];
//                for (int i = 1; i < strings.length; i++) {
//                    tmp = senderId + " " + strings[i];
//                    if(!messageSet.contains(tmp)){
//                        messageSet.add(tmp);
//                        log.append(tmp).append("\n");
//                    }
//                }
//            }
//            //todo
//            buffer.clear();
//        }
//        writer.write(log.toString());
//    }
//
//    @Override
//    public void clearBuffer() throws IOException {
//        for(Receiver receiver: receivers){
//            buffer.add(receiver.getLocalBuffer());
//        }
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
//}