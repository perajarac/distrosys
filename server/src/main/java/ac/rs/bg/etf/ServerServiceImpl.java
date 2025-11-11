package ac.rs.bg.etf;

import ac.rs.bg.etf.proto.BagArg;
import ac.rs.bg.etf.proto.BagRet;
import ac.rs.bg.etf.proto.Body;
import ac.rs.bg.etf.proto.BodyRow;
import ac.rs.bg.etf.proto.TaskRet;
import io.grpc.BindableService;
import io.grpc.MethodDescriptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;

public class ServerServiceImpl implements BindableService {

    private static final String SERVICE_NAME = "distrosys.ServerService";

    private static final MethodDescriptor<BagArg, BagRet> SEND_BAG_TASK_METHOD =
            MethodDescriptor.<BagArg, BagRet>newBuilder()
                    .setType(MethodDescriptor.MethodType.UNARY)
                    .setFullMethodName(MethodDescriptor.generateFullMethodName(SERVICE_NAME,
                        "SendBagTask"))
                    .setRequestMarshaller(ProtoUtils.marshaller(BagArg.getDefaultInstance()))
                    .setResponseMarshaller(ProtoUtils.marshaller(BagRet.getDefaultInstance()))
                    .build();

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
    public ServerServiceDefinition bindService() {
        return ServerServiceDefinition.builder(SERVICE_NAME)
                .addMethod(
                        SEND_BAG_TASK_METHOD,
                        ServerCalls.asyncUnaryCall(new ServerCalls.UnaryMethod<BagArg, BagRet>() {
                            @Override
                            public void invoke(BagArg request, StreamObserver<BagRet> responseObserver) {
                                sendBagTask(request, responseObserver);
                            }
                        })
                )
                .build();
    }
}

