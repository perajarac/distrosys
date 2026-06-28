package rs.ac.bg.etf.job;

import rs.ac.bg.etf.proto.JobStatus;
import rs.ac.bg.etf.proto.SimulationType;
import rs.ac.bg.etf.proto.SubJobStatus;

/**
 * Converts between protobuf enums and simulation-core types used by the server.
 */
public final class JobProtoMapper {

    private JobProtoMapper() {
    }

    /**
     * Maps a protobuf simulation type to the simulation-core enum.
     *
     * @param simType protobuf simulation type
     * @return matching simulation-core type
     * @throws IllegalArgumentException if the type is unspecified or unknown
     */
    public static rs.ac.bg.etf.kdp.simulation.io.SimulationType toCore(SimulationType simType) {
        return switch (simType) {
            case OPTIMISTIC -> rs.ac.bg.etf.kdp.simulation.io.SimulationType.OPTIMISTIC;
            case SINGLETHREAD -> rs.ac.bg.etf.kdp.simulation.io.SimulationType.SINGLETHREAD;
            case MULTITHREAD -> rs.ac.bg.etf.kdp.simulation.io.SimulationType.MULTITHREAD;
            default -> throw new IllegalArgumentException("Unspecified simulation type: " + simType);
        };
    }

    /**
     * Maps a simulation-core simulation type to the protobuf enum.
     *
     * @param simType simulation-core simulation type
     * @return matching protobuf type
     */
    public static SimulationType toProto(rs.ac.bg.etf.kdp.simulation.io.SimulationType simType) {
        return switch (simType) {
            case OPTIMISTIC -> SimulationType.OPTIMISTIC;
            case SINGLETHREAD -> SimulationType.SINGLETHREAD;
            case MULTITHREAD -> SimulationType.MULTITHREAD;
        };
    }

    /**
     * @param status job status
     * @return {@code true} when the job has reached a terminal state
     */
    public static boolean isTerminal(JobStatus status) {
        return status == JobStatus.DONE
                || status == JobStatus.FAILED
                || status == JobStatus.ABORTED;
    }

    /**
     * @param status sub-job status
     * @return {@code true} when the sub-job has finished (success or failure)
     */
    public static boolean isSubJobTerminal(SubJobStatus status) {
        return status == SubJobStatus.SUB_JOB_DONE || status == SubJobStatus.SUB_JOB_FAILED;
    }
}
