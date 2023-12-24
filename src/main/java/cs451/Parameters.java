package cs451;

import static cs451.ConfigParser.maxDistinctValues;

/**
 * Class containing some important parameters, tuned
 * heuristically depending on the number of hosts in the network.
 */
public class Parameters {

    public static int MAX_COMPRESSION = 8;

    /** Starting timeout for each host. */
    public static int TIMEOUT = 1024;

    /** Threshold for the maximum timeout for a host. */
    public static int MAX_TIMEOUT = 16384;

    /** Threshold to be arbitrary added to hosts' timeout to prevent collisions. */
    public static int THRESHOLD = 10;

    /** Maximum number of packets to resend at a given time in the system. */
    public static int LINK_BATCH = 1024;

    /** Window size for the proposal to be processed. */
    public static int PROPOSAL_BATCH = 16;

    public static int GC_BATCH;

    public static int MAX_PACKET_SIZE;

    public static int NUM_HOSTS;



    /**
     * Tune the hyper-params depending on the number of hosts.
     */
    public static void setParams() {
        // Since the ack/nack are individual for process, the link batch is intended as for the whole network
        LINK_BATCH /= NUM_HOSTS;
        // Set global maximum packet size
        MAX_PACKET_SIZE = MAX_COMPRESSION * (maxDistinctValues + 32) * Integer.BYTES;
        switch (NUM_HOSTS / 10) {
            // From 0 to 39 hosts
            case 0: case 1: case 2: case 3:
                PROPOSAL_BATCH = 32;
                GC_BATCH = 256;
                break;
            // From 40 to 79 hosts
            case 4: case 5: case 6: case 7:
                GC_BATCH = 128;
                break;
            // From 80 to 128 hosts
            default:
                GC_BATCH = 64;
        }
        // Set the batch accordingly with ds
        if (maxDistinctValues > 150 && maxDistinctValues < 300) {
            PROPOSAL_BATCH >>= 1;
            GC_BATCH >>= 1;
        } else if (maxDistinctValues >= 300 && maxDistinctValues < 500) {
            PROPOSAL_BATCH >>= 2;
            GC_BATCH >>= 2;
        } else if (maxDistinctValues >= 500 && maxDistinctValues < 800) {
            PROPOSAL_BATCH >>= 3;
            LINK_BATCH >>= 1;
            GC_BATCH >>= 3;
        } else if (maxDistinctValues >= 800) {
            PROPOSAL_BATCH >>= 3;
            LINK_BATCH >>= 2;
            GC_BATCH >>= 4;
        }
    }

}
