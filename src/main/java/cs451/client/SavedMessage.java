package cs451.client;

import java.net.InetAddress;

public class SavedMessage {
    private byte[] message;
    private int senderId;
    private int sequenceNumber;

    public SavedMessage(int senderId, int sequenceNumber, byte[] message) {
        this.message = message;
        this.senderId = senderId;
        this.sequenceNumber = sequenceNumber;
    }

    public byte[] getMessage() {
        return message;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

}
