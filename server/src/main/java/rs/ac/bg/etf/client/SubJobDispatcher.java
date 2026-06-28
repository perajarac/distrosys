package rs.ac.bg.etf.client;

import rs.ac.bg.etf.proto.SubJobRequest;
import rs.ac.bg.etf.proto.SubJobResponse;
import rs.ac.bg.etf.registry.WorkstationInfo;

/** Dispatches sub-jobs to a workstation (blocking gRPC or test double). */
@FunctionalInterface
public interface SubJobDispatcher {

  /**
   * Executes a sub-job on the given workstation.
   *
   * @param workstation target workstation metadata
   * @param request sub-job request payload
   * @return workstation response
   */
  SubJobResponse executeSubJob(WorkstationInfo workstation, SubJobRequest request);
}
