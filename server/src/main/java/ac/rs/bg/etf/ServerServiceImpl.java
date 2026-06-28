package ac.rs.bg.etf;

import ac.rs.bg.etf.proto.BagArg;
import ac.rs.bg.etf.proto.BagRet;
import ac.rs.bg.etf.proto.Body;
import ac.rs.bg.etf.proto.BodyRow;
import ac.rs.bg.etf.proto.CancelJobRequest;
import ac.rs.bg.etf.proto.CancelJobResponse;
import ac.rs.bg.etf.proto.FileChunk;
import ac.rs.bg.etf.proto.HeartbeatRequest;
import ac.rs.bg.etf.proto.HeartbeatResponse;
import ac.rs.bg.etf.proto.JobId;
import ac.rs.bg.etf.proto.JobResultResponse;
import ac.rs.bg.etf.proto.JobStatus;
import ac.rs.bg.etf.proto.JobStatusResponse;
import ac.rs.bg.etf.proto.RegisterWorkstationRequest;
import ac.rs.bg.etf.proto.RegisterWorkstationResponse;
import ac.rs.bg.etf.proto.ServerServiceGrpc;
import ac.rs.bg.etf.proto.SubmitJobRequest;
import ac.rs.bg.etf.proto.SubmitJobResponse;
import ac.rs.bg.etf.proto.TaskRet;
import ac.rs.bg.etf.proto.UploadJobFilesResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

/**
 * gRPC service implementation for {@link ServerServiceGrpc}.
 *
 * <p>Job scheduling, workstation registry, and file upload handling are stubbed
 * until the server module is fully implemented.
 */
public class ServerServiceImpl extends ServerServiceGrpc.ServerServiceImplBase {

    @Override
    public void sendBagTask(BagArg request, StreamObserver<BagRet> responseObserver) {
        Body sampleBody = Body.newBuilder()
                .setId(1)
                .setM(10)
                .setX(0.0)
                .setY(0.0)
                .setZ(0.0)
                .setVx(1.0)
                .setVy(0.0)
                .setVz(0.0)
                .build();

        BagRet response = BagRet.newBuilder()
                .addResult(BodyRow.newBuilder().addBody(sampleBody).build())
                .addTaskRet(
                        TaskRet.newBuilder()
                                .setStatus(TaskRet.Status.OK)
                                .setId(request.hasT() ? request.getT().getTaskId() : 0L)
                                .build()
                )
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void submitJob(SubmitJobRequest request, StreamObserver<SubmitJobResponse> responseObserver) {
        SubmitJobResponse response = SubmitJobResponse.newBuilder()
                .setJobId(0L)
                .setStatus(JobStatus.JOB_STATUS_UNSPECIFIED)
                .setMessage("not implemented")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getJobStatus(JobId request, StreamObserver<JobStatusResponse> responseObserver) {
        JobStatusResponse response = JobStatusResponse.newBuilder()
                .setJobId(request.getJobId())
                .setStatus(JobStatus.JOB_STATUS_UNSPECIFIED)
                .setMessage("not implemented")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getJobResult(JobId request, StreamObserver<JobResultResponse> responseObserver) {
        JobResultResponse response = JobResultResponse.newBuilder()
                .setJobId(request.getJobId())
                .setStatus(JobStatus.JOB_STATUS_UNSPECIFIED)
                .setErrorMessage("not implemented")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void cancelJob(CancelJobRequest request, StreamObserver<CancelJobResponse> responseObserver) {
        CancelJobResponse response = CancelJobResponse.newBuilder()
                .setSuccess(false)
                .setMessage("not implemented")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<FileChunk> uploadJobFiles(StreamObserver<UploadJobFilesResponse> responseObserver) {
        responseObserver.onError(Status.UNIMPLEMENTED.withDescription("not implemented").asRuntimeException());
        return new StreamObserver<FileChunk>() {
            @Override
            public void onNext(FileChunk value) {
                // discard until UNIMPLEMENTED is propagated
            }

            @Override
            public void onError(Throwable t) {
                // no-op
            }

            @Override
            public void onCompleted() {
                // no-op
            }
        };
    }

    @Override
    public void registerWorkstation(
            RegisterWorkstationRequest request,
            StreamObserver<RegisterWorkstationResponse> responseObserver) {
        RegisterWorkstationResponse response = RegisterWorkstationResponse.newBuilder()
                .setAccepted(false)
                .setMessage("not implemented")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void heartbeat(HeartbeatRequest request, StreamObserver<HeartbeatResponse> responseObserver) {
        HeartbeatResponse response = HeartbeatResponse.newBuilder()
                .setAcknowledged(false)
                .setMessage("not implemented")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
