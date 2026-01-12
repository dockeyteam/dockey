package com.dockey.checker.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;

public class DocCheckerServer {
    private static final Logger logger = LogManager.getLogger(DocCheckerServer.class);
    private static final int PORT = 50051;
    private Server server;

    @Inject
    private CheckingService checkingService;

    public void start() throws Exception {
        server = ServerBuilder.forPort(PORT)
                .addService(new DocCheckerServiceImpl())
                .build()
                .start();
        logger.info("DocChecker gRPC server started on port {}", PORT);
    }

    public void stop() throws Exception {
        if (server != null) {
            server.shutdown();
            logger.info("DocChecker gRPC server stopped");
        }
    }

    public static void main(String[] args) throws Exception {
        DocCheckerServer server = new DocCheckerServer();
        server.start();
        server.awaitTermination();
    }

    private static class CheckerService extends CheckerGrpc.CheckerImplBase {

        @Override
        public void checkComment(Comm request, io.grpc.stub.StreamObserver<CheckResponse> responseObserver) {
            // Implement the comment checking logic here
            Check response = CheckResponse.newBuilder()
                    .setCheck(checkingService.checkComment("yeahhhh boyyyyyyyyyy")) // Placeholder response
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void checkDocument(Doc request, io.grpc.stub.StreamObserver<CheckResponse> responseObserver) {
            // Implement the document checking logic here
            Check response = CheckResponse.newBuilder()
                    .setCheck(checkingService.checkDocument("yeahhhh boyyyyy")) // Placeholder response
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}