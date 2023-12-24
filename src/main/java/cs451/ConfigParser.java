package cs451;


import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static cs451.client.Manager.originals;
import static cs451.Parameters.MAX_COMPRESSION;
import static cs451.Parameters.PROPOSAL_BATCH;

/**
 * The ConfigParser reads the .config file,
 * check if it's properly formatted and then
 * create a list of all the proposals.
 */
public class ConfigParser {

    /** BufferedReader to read from config files the proposals. */
    private static BufferedReader br;

    /** Current last proposal read. */
    private static int currentProposal = 0;

    /** Total number of proposals. */
    public static int totalProposal = 0;

    /** Maximum number of distinct elements in a proposal's shot. */
    public static int maxProposalLength;

    /** Maximum number of distinct elements in total. */
    public static int maxDistinctValues;

    /**
     * Read the config file and populate the proposals list.
     * @param value config filename.
     * @return true if correctly parsed, false otherwise.
     */
    public static boolean populate(String value, int hostNum) throws IOException {
        Parameters.NUM_HOSTS = hostNum;
        File file = new File(value);
        try {
            br = new BufferedReader(new FileReader(file.getPath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        };
        // Read the header -> p vs ds
        String[] header = br.readLine().split("\\s");
        try {
            totalProposal = Integer.parseInt(header[0]);
            maxProposalLength = Integer.parseInt(header[1]);
            maxDistinctValues = Integer.parseInt(header[2]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
        Parameters.setParams();
        // Load first batch of proposals
        try {
            for (String line;
                currentProposal < PROPOSAL_BATCH && (line = br.readLine()) != null; ) {
                readLine(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        if (currentProposal == totalProposal) {
            br.close();
        }
        return true;
    }

    /**
     * Read a "packet" of proposal.
     * @return true if read at least one, false otherwise.
     */
    public static boolean readProposals() {
        // If already read all proposals, then already closed buffer
        if (currentProposal == totalProposal) {
            return false;
        }
        try {
            int p = 0;
            // Load min between MAX_COMPRESSION (8) and the current batch (needed when batch is less than 8)
            for (String line; p < Math.min(MAX_COMPRESSION, PROPOSAL_BATCH) && (line = br.readLine()) != null; ++p) {
                readLine(line);
                if (currentProposal == totalProposal) {
                    br.close();
                    break;
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
            return !originals.isEmpty();
        }
        return true;
    }

    /**
     * Read and parse a line of the config file.
     * @param line string with proposal's set.
     */
    private static void readLine(String line) {
        try {
            String[] splits = line.split("\\s");
            ArrayList<Integer> values = new ArrayList<>(maxProposalLength);
            for (String split : splits) {
                values.add(Integer.parseInt(split));
            }
            originals.add(values);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close the file reader if still open.
     */
    public static void closeFile() {
        // If already closed
        if (currentProposal == totalProposal) {
            return;
        }
        try {
            br.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

}
