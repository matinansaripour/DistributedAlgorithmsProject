package cs451;

import cs451.client.Manager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    static Manager manager;
    private static void handleSignal() throws IOException {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        System.out.println("Writing output.");
        manager.clearBuffer();
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                handleSignal();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    public static void main(String[] args) throws InterruptedException, SocketException, FileNotFoundException, UnknownHostException {
        Parser parser = new Parser(args);
        parser.parse();

        initSignalHandlers();

        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID: " + pid + "\n");
        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");

        System.out.println("My ID: " + parser.myId() + "\n");
        System.out.println("List of resolved hosts is:");
        System.out.println("==========================");
        for (Host host: parser.hosts()) {
            System.out.println(host.getId());
            System.out.println("Human-readable IP: " + host.getIp());
            System.out.println("Human-readable Port: " + host.getPort());
            System.out.println();
        }
        System.out.println();

        System.out.println("Path to output:");
        System.out.println("===============");
        System.out.println(parser.output() + "\n");

        System.out.println("Path to config:");
        System.out.println("===============");
        System.out.println(parser.config() + "\n");

        System.out.println("Doing some initialization\n");
//        int port = 0;
//        if(parser.myId() == parser.config().getReceiverId()){
//            for (Host host: parser.hosts()) {
//                if (host.getId() == parser.myId()){
//                    port = host.getPort();
//                    break;
//                }
//            }
//            client = new ReceiverManager(parser.myId(), port, 7, parser.output());
//        }
//        else {
//            String receiverAddress = null;
//            int receiverPort = 0;
//            for (Host host: parser.hosts()) {
//                if (host.getId() == parser.myId()){
//                    port = host.getPort();
//                }
//                if (host.getId() == parser.config().getReceiverId()){
//                    receiverAddress = host.getIp();
//                    receiverPort = host.getPort();
//                }
//            }
//            client = new SendersManager(parser.myId(), parser.config().getM(), port, receiverAddress, receiverPort, 6, parser.output());
//        }
        ArrayList<Host> hosts = (ArrayList<Host>) parser.hosts();
        int port = 0;
        int n = hosts.size();
        HashMap<String, Integer> addressToId = new HashMap<>();
        InetAddress[] idToAddress = new InetAddress[n];
        int[] idToPort = new int[n];
        for (Host host: hosts) {
            idToAddress[host.getId() - 1] = InetAddress.getByName(host.getIp());
            addressToId.put(idToAddress[host.getId() - 1].getHostAddress() + " " + host.getPort(), host.getId());
            idToPort[host.getId() - 1] = host.getPort();
            if (host.getId() == parser.myId()){
                port = host.getPort();
            }
        }
        manager = new Manager(parser.myId(), parser.config().getM(), n, port, addressToId, idToAddress, idToPort, parser.output());

        System.out.println("Broadcasting and delivering messages...\n");
        manager.run();
    }
}
