package rs.ac.bg.etf.workstation;

/**
 * Handle to a running simulation registered for remote event delivery (Phase B).
 */
public final class ActiveSimulation {

    private final long jobId;
    private final SimBufferNetwork network;

    /**
     * @param jobId   parent job identifier
     * @param network network buffer routing events for this simulation
     */
    public ActiveSimulation(long jobId, SimBufferNetwork network) {
        this.jobId = jobId;
        this.network = network;
    }

    /**
     * @return parent job identifier
     */
    public long getJobId() {
        return jobId;
    }

    /**
     * @return network buffer for this simulation
     */
    public SimBufferNetwork getNetwork() {
        return network;
    }
}
