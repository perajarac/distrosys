package rs.ac.bg.etf;

import rs.ac.bg.etf.job.Job;
import rs.ac.bg.etf.job.JobStore;
import rs.ac.bg.etf.job.JobValidator;
import rs.ac.bg.etf.job.ValidationResult;
import rs.ac.bg.etf.proto.BagArg;
import rs.ac.bg.etf.proto.BagRet;
import rs.ac.bg.etf.proto.Body;
import rs.ac.bg.etf.proto.BodyRow;
import rs.ac.bg.etf.proto.CancelJobRequest;
import rs.ac.bg.etf.proto.CancelJobResponse;
import rs.ac.bg.etf.proto.FileChunk;
import rs.ac.bg.etf.proto.HeartbeatRequest;
import rs.ac.bg.etf.proto.HeartbeatResponse;
import rs.ac.bg.etf.proto.JobFilePaths;
import rs.ac.bg.etf.proto.JobId;
import rs.ac.bg.etf.proto.JobResultResponse;
import rs.ac.bg.etf.proto.JobStatus;
import rs.ac.bg.etf.proto.JobStatusResponse;
import rs.ac.bg.etf.proto.RegisterWorkstationRequest;
import rs.ac.bg.etf.proto.RegisterWorkstationResponse;
import rs.ac.bg.etf.proto.ServerServiceGrpc;
import rs.ac.bg.etf.proto.SubmitJobRequest;
import rs.ac.bg.etf.proto.SubmitJobResponse;
import rs.ac.bg.etf.proto.TaskRet;
import rs.ac.bg.etf.proto.UploadJobFilesResponse;
import rs.ac.bg.etf.registry.WorkstationRegistry;
import rs.ac.bg.etf.scheduling.JobScheduler;
import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * gRPC service implementation for {@link ServerServiceGrpc}.
 *
 * <p>Delegates job lifecycle operations to {@link JobStore}, {@link JobValidator},
 * {@link JobScheduler}, and {@link WorkstationRegistry}.
 */
public class ServerServiceImpl extends ServerServiceGrpc.ServerServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(ServerServiceImpl.class);

    private final JobStore jobStore;
    private final JobValidator jobValidator;
    private final JobScheduler jobScheduler;
    private final WorkstationRegistry workstationRegistry;

    /**
     * @param jobStore            persisted job registry
     * @param jobValidator        input file validator
     * @param jobScheduler        async job scheduler
     * @param workstationRegistry workstation registry
     */
    public ServerServiceImpl(
            JobStore jobStore,
            JobValidator jobValidator,
            JobScheduler jobScheduler,
            WorkstationRegistry workstationRegistry) {
        this.jobStore = jobStore;
        this.jobValidator = jobValidator;
        this.jobScheduler = jobScheduler;
        this.workstationRegistry = workstationRegistry;
    }

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
        if (request.getInputCase() != SubmitJobRequest.InputCase.PATHS) {
            responseObserver.onError(Status.UNIMPLEMENTED
                    .withDescription("upload_ref jobs require UploadJobFiles (not implemented)")
                    .asRuntimeException());
            return;
        }

        JobFilePaths paths = request.getPaths();
        ValidationResult validation = jobValidator.validate(
                paths.getComponentsPath(), paths.getConnectionsPath());
        if (!validation.isValid()) {
            log.warn("Job validation failed: {}", validation.getMessage());
            try {
                Job failedJob = new Job();
                failedJob.setSimType(request.getSimType());
                failedJob.setEndTime(request.getEndTime());
                failedJob.setComponentsPath(paths.getComponentsPath());
                failedJob.setConnectionsPath(paths.getConnectionsPath());
                failedJob.setClientOutputName(request.getClientOutputName());
                failedJob.setStatus(JobStatus.FAILED);
                failedJob.setErrorMessage(validation.getMessage());
                jobStore.create(failedJob);
                SubmitJobResponse response = SubmitJobResponse.newBuilder()
                        .setJobId(failedJob.getId())
                        .setStatus(JobStatus.FAILED)
                        .setMessage(validation.getMessage())
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } catch (IOException e) {
                responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
            }
            return;
        }

        try {
            Job job = new Job();
            job.setSimType(request.getSimType());
            job.setEndTime(request.getEndTime());
            job.setComponentsPath(paths.getComponentsPath());
            job.setConnectionsPath(paths.getConnectionsPath());
            job.setClientOutputName(request.getClientOutputName());
            job.setStatus(JobStatus.READY);
            jobStore.create(job);
            jobScheduler.schedule(job);

            JobStatus status = jobStore.get(job.getId()).map(Job::getStatus).orElse(JobStatus.SCHEDULED);
            String message = status == JobStatus.FAILED
                    ? jobStore.get(job.getId()).map(Job::getErrorMessage).orElse("scheduling failed")
                    : "job scheduled";

            log.info("Submitted job {} with status {}", job.getId(), status);

            SubmitJobResponse response = SubmitJobResponse.newBuilder()
                    .setJobId(job.getId())
                    .setStatus(status == JobStatus.FAILED ? JobStatus.FAILED : JobStatus.SCHEDULED)
                    .setMessage(message)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IOException e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getJobStatus(JobId request, StreamObserver<JobStatusResponse> responseObserver) {
        jobStore.get(request.getJobId()).ifPresentOrElse(job -> {
            JobStatusResponse response = JobStatusResponse.newBuilder()
                    .setJobId(job.getId())
                    .setStatus(job.getStatus())
                    .setMessage(statusMessage(job))
                    .setCompletedSubjobs(job.countCompletedSubJobs())
                    .setTotalSubjobs(job.getSubJobs().size())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }, () -> responseObserver.onError(Status.NOT_FOUND
                .withDescription("Unknown job id: " + request.getJobId())
                .asRuntimeException()));
    }

    @Override
    public void getJobResult(JobId request, StreamObserver<JobResultResponse> responseObserver) {
        jobStore.get(request.getJobId()).ifPresentOrElse(job -> {
            JobResultResponse.Builder builder = JobResultResponse.newBuilder()
                    .setJobId(job.getId())
                    .setStatus(job.getStatus());
            if (job.getStatus() == JobStatus.DONE && job.getResultContent() != null) {
                builder.setResultContent(ByteString.copyFrom(job.getResultContent()));
            } else if (job.getErrorMessage() != null && !job.getErrorMessage().isBlank()) {
                builder.setErrorMessage(job.getErrorMessage());
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        }, () -> responseObserver.onError(Status.NOT_FOUND
                .withDescription("Unknown job id: " + request.getJobId())
                .asRuntimeException()));
    }

    @Override
    public void cancelJob(CancelJobRequest request, StreamObserver<CancelJobResponse> responseObserver) {
        jobStore.get(request.getJobId()).ifPresentOrElse(job -> {
            try {
                if (job.getStatus() == JobStatus.RUNNING || job.getStatus() == JobStatus.SCHEDULED) {
                    jobStore.updateStatus(request.getJobId(), JobStatus.ABORTED, "cancelled by client");
                    log.info("Cancelled job {}", request.getJobId());
                    responseObserver.onNext(CancelJobResponse.newBuilder()
                            .setSuccess(true)
                            .setMessage("job aborted")
                            .build());
                } else {
                    log.warn("Cancel rejected for job {} in status {}", request.getJobId(), job.getStatus());
                    responseObserver.onNext(CancelJobResponse.newBuilder()
                            .setSuccess(false)
                            .setMessage("job is not cancellable in status " + job.getStatus())
                            .build());
                }
                responseObserver.onCompleted();
            } catch (IOException e) {
                responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
            }
        }, () -> responseObserver.onError(Status.NOT_FOUND
                .withDescription("Unknown job id: " + request.getJobId())
                .asRuntimeException()));
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
        try {
            String workstationId = workstationRegistry.register(request);
            log.info("Registered workstation {} at {}:{} (parallelism={})",
                    workstationId, request.getGrpcHost(), request.getGrpcPort(), request.getParallelism());
            RegisterWorkstationResponse response = RegisterWorkstationResponse.newBuilder()
                    .setWorkstationId(workstationId)
                    .setAccepted(true)
                    .setMessage("registered")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalStateException e) {
            log.warn("Workstation registration rejected: {}", e.getMessage());
            RegisterWorkstationResponse response = RegisterWorkstationResponse.newBuilder()
                    .setAccepted(false)
                    .setMessage(e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void heartbeat(HeartbeatRequest request, StreamObserver<HeartbeatResponse> responseObserver) {
        boolean acknowledged = workstationRegistry.heartbeat(request);
        if (!acknowledged) {
            log.debug("Heartbeat from unknown workstation {}", request.getWorkstationId());
        }
        HeartbeatResponse response = HeartbeatResponse.newBuilder()
                .setAcknowledged(acknowledged)
                .setMessage(acknowledged ? "ok" : "unknown workstation")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private static String statusMessage(Job job) {
        if (job.getErrorMessage() != null && !job.getErrorMessage().isBlank()) {
            return job.getErrorMessage();
        }
        return job.getStatus().name().toLowerCase();
    }
}
