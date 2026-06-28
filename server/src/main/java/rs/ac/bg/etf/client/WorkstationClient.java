package rs.ac.bg.etf.client;

import rs.ac.bg.etf.proto.SubJobRequest;
import rs.ac.bg.etf.proto.SubJobResponse;
import rs.ac.bg.etf.proto.WorkstationServiceGrpc;
import rs.ac.bg.etf.registry.WorkstationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * gRPC client wrapper for dispatching sub-jobs to workstations.
 */
public class WorkstationClient implements SubJobDispatcher {

    private static final Logger log = LoggerFactory.getLogger(WorkstationClient.class);

    /**
     * Executes a sub-job on the given workstation via blocking gRPC.
     *
     * @param workstation target workstation metadata (includes channel)
     * @param request     sub-job request payload
     * @return workstation response with component states or failure details
     */
    public SubJobResponse executeSubJob(WorkstationInfo workstation, SubJobRequest request) {
        log.debug("Dispatching sub-job {} for job {} to workstation {}",
                request.getSubjobId(), request.getJobId(), workstation.getWorkstationId());
        WorkstationServiceGrpc.WorkstationServiceBlockingStub stub =
                WorkstationServiceGrpc.newBlockingStub(workstation.getChannel());
        SubJobResponse response = stub.executeSubJob(request);
        log.debug("Sub-job {} for job {} returned status {}",
                request.getSubjobId(), request.getJobId(), response.getStatus());
        return response;
    }
}
