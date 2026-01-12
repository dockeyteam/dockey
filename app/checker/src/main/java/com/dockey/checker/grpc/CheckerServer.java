package com.dockey.checker.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.stub.StreamObserver;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import javax.inject.Inject;

public class CheckerServer {
    private static final Logger logger = LogManager.getLogger(CheckerServer.class);
    private static final int PORT = 50051;
    private Server server;

    @Inject
    private CheckingService checkingService;

    public void start() throws Exception {
        server = ServerBuilder.forPort(PORT)
                .addService(new CheckerService())
                .build()
                .start();
        logger.info("Checker gRPC server started on port {}", PORT);
    }

    public void stop() throws Exception {
        if (server != null) {
            server.shutdown();
            logger.info("DocChecker gRPC server stopped");
        }
    }

    public static void main(String[] args) throws Exception {
        CheckerServer server = new CheckerServer();
        server.start();
        server.awaitTermination();
    }

    private void awaitTermination() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    private class CheckerService extends CheckerGrpc.CheckerImplBase {

        public void checkComment(Comm request, io.grpc.stub.StreamObserver<Check> responseObserver) {
            // Implement the comment checking logic here
            Check response = Check.newBuilder()
                    //.setCheck(checkingService.checkComment("lalalalala")) // Placeholder response
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        public void checkDocument(Doc request, io.grpc.stub.StreamObserver<Check> responseObserver) {
            // Implement the document checking logic here
            Check response = Check.newBuilder()
                    //.setCheck(checkingService.checkDocument("lalala")) // Placeholder response
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }
    }
}