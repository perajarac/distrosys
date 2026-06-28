package ac.rs.bg.etf;

import ac.rs.bg.etf.proto.EventAck;
import ac.rs.bg.etf.proto.ForwardEventRequest;
import ac.rs.bg.etf.proto.SubJobRequest;
import ac.rs.bg.etf.proto.SubJobResponse;
import ac.rs.bg.etf.proto.SubJobStatus;
import ac.rs.bg.etf.proto.WorkstationServiceGrpc;
import io.grpc.stub.StreamObserver;

/**
 * gRPC service implementation for {@link WorkstationServiceGrpc}.
 *
 * <p>Sub-job execution and distributed event forwarding are stubbed until the
 * workstation module is fully implemented.
 */
public class WorkstationServiceImpl extends WorkstationServiceGrpc.WorkstationServiceImplBase {

    @Override
    public void executeSubJob(SubJobRequest request, StreamObserver<SubJobResponse> responseObserver) {
        SubJobResponse response = SubJobResponse.newBuilder()
                .setJobId(request.getJobId())
                .setSubjobId(request.getSubjobId())
                .setStatus(SubJobStatus.SUB_JOB_STATUS_UNSPECIFIED)
                .setErrorMessage("not implemented")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void forwardEvent(ForwardEventRequest request, StreamObserver<EventAck> responseObserver) {
        EventAck response = EventAck.newBuilder()
                .setAccepted(false)
                .setMessage("not implemented")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
