package cs451.client;

import java.util.ArrayList;
import java.util.HashSet;

public class Proposal {
    private HashSet<Integer> proposedValues;
    private int proposalId;
    private int activeProposalNumber;
    private boolean active;
    private HashSet<Integer> ackCount;
    private HashSet<Integer> nackCount;
    private HashSet<Integer> delivered;
    private StringBuilder message;
    private StringBuilder messageToSend;


    public Proposal(int proposalId, ArrayList<Integer> proposedValues) {
        this.proposalId = proposalId;
        this.proposedValues = new HashSet<>();
        this.proposedValues.addAll(proposedValues);
        this.activeProposalNumber = 0;
        this.active = true;
        this.ackCount = new HashSet<>();
        this.nackCount = new HashSet<>();
        this.message = new StringBuilder();
        this.messageToSend = new StringBuilder();
        for (int value : proposedValues) {
            message.append(value).append(" ");
        }
        message.deleteCharAt(message.length() - 1);
        messageToSend.append(proposalId).append(" ").append(activeProposalNumber).append(" ").append(message);
    }

    public int getProposalId() {
        return proposalId;
    }

    public int getActiveProposalNumber() {
        return activeProposalNumber;
    }

    public boolean isActive() {
        return active;
    }

    public int getAckCount() {
        return ackCount.size();
    }

    public int getNackCount() {
        return nackCount.size();
    }

    public void addAckCount(int id) {
        ackCount.add(id);
    }

    public void addNackCount(int id) {
        nackCount.add(id);
    }

    public void clear() {
        ackCount.clear();
        nackCount.clear();
        ++this.activeProposalNumber;
        messageToSend.setLength(0);
        messageToSend.append(proposalId).append(" ").append(activeProposalNumber).append(" ").append(message);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getMessageToSend() {
        synchronized (this){
            if (!isActive()){
                return "3 " + proposalId;
            }
            return messageToSend.toString();
        }
    }

    public StringBuilder getMessage() {
        return message;
    }

    public void addProposedValue(ArrayList<Integer> proposedValues) {
        for (int value : proposedValues) {
            if (!this.proposedValues.contains(value)) {
                this.proposedValues.add(value);
                message.append(" ").append(value);
            }
        }
    }

    public void addDelivered(int id) {
        delivered.add(id);
    }

    public boolean isDelivered(int id) {
        return delivered.contains(id);
    }

    public boolean isAcked(int id) {
        return (ackCount.contains(id) || nackCount.contains(id));
    }
}
