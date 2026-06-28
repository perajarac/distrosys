package rs.ac.bg.etf;

import rs.ac.bg.etf.proto.EventAck;
import rs.ac.bg.etf.proto.ForwardEventRequest;
import rs.ac.bg.etf.proto.SubJobRequest;
import rs.ac.bg.etf.proto.SubJobResponse;
import rs.ac.bg.etf.proto.SubJobStatus;
import rs.ac.bg.etf.proto.WorkstationServiceGrpc;
import rs.ac.bg.etf.workstation.ActiveSimulation;
import rs.ac.bg.etf.workstation.LocalSimulationTask;
import rs.ac.bg.etf.workstation.SubJobExecutor;
import rs.ac.bg.etf.workstation.SubJobResult;
import rs.ac.bg.etf.workstation.WorkstationProtoMapper;
import rs.ac.bg.etf.workstation.WorkstationState;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * gRPC service implementation for {@link WorkstationServiceGrpc}.
 *
 * <p>Executes sub-jobs locally via {@link LocalSimulationTask} and accepts forwarded
 * simulation events for distributed partitions.
 */
public class WorkstationServiceImpl extends WorkstationServiceGrpc.WorkstationServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(WorkstationServiceImpl.class);

    private final WorkstationState state;
    private final SubJobExecutor executor;

    /**
     * @param state    shared workstation runtime state
     * @param executor sub-job thread pool
     */
    public WorkstationServiceImpl(WorkstationState state, SubJobExecutor executor) {
        this.state = state;
        this.executor = executor;
    }

    @Override
    public void executeSubJob(SubJobRequest request, StreamObserver<SubJobResponse> responseObserver) {
        log.info("Executing sub-job {} for job {}", request.getSubjobId(), request.getJobId());
        state.incrementActive();
        try {
            Future<SubJobResult> future = executor.submit(new LocalSimulationTask(request, state));
            SubJobResult result = future.get();
            SubJobResponse.Builder builder = SubJobResponse.newBuilder()
                    .setJobId(request.getJobId())
                    .setSubjobId(request.getSubjobId());
            if (result.isSuccess()) {
                log.info("Sub-job {} for job {} completed", request.getSubjobId(), request.getJobId());
                builder.setStatus(SubJobStatus.SUB_JOB_DONE)
                        .addAllComponentStates(result.getComponentStates());
            } else {
                log.error("Sub-job {} for job {} failed: {}",
                        request.getSubjobId(), request.getJobId(), result.getErrorMessage());
                builder.setStatus(SubJobStatus.SUB_JOB_FAILED)
                        .setErrorMessage(result.getErrorMessage());
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Sub-job {} for job {} interrupted", request.getSubjobId(), request.getJobId(), e);
            respondFailure(request, responseObserver, "sub-job interrupted");
        } catch (ExecutionException e) {
            String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            log.error("Sub-job {} for job {} execution failed", request.getSubjobId(), request.getJobId(), e);
            respondFailure(request, responseObserver, message);
        } finally {
            state.decrementActive();
        }
    }

    @Override
    public void forwardEvent(ForwardEventRequest request, StreamObserver<EventAck> responseObserver) {
        ActiveSimulation simulation = state.getSimulation(request.getJobId());
        if (simulation == null) {
            log.warn("ForwardEvent rejected: no active simulation for job {}", request.getJobId());
            EventAck response = EventAck.newBuilder()
                    .setAccepted(false)
                    .setMessage("no active simulation for job " + request.getJobId())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        simulation.getNetwork().deliverRemoteEvent(
                WorkstationProtoMapper.toEvent(request.getEvent(), request.getPayload()));
        EventAck response = EventAck.newBuilder()
                .setAccepted(true)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private static void respondFailure(
            SubJobRequest request,
            StreamObserver<SubJobResponse> responseObserver,
            String message) {
        SubJobResponse response = SubJobResponse.newBuilder()
                .setJobId(request.getJobId())
                .setSubjobId(request.getSubjobId())
                .setStatus(SubJobStatus.SUB_JOB_FAILED)
                .setErrorMessage(message != null ? message : "sub-job failed")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
