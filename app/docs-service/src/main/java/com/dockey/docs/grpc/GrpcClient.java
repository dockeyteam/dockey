package com.dockey.docs.grpc;

import javax.enterprise.context.ApplicationScoped;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import com.dockey.checker.CheckerGrpc.CheckerBlockingStub;
import com.dockey.checker.CheckerGrpc.CheckerStub;
import com.google.protobuf.Message;
import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.Status;

@ApplicationScoped
public class CheckerClient {
  
    private static final Logger logger = Logger.getLogger(RouteGuideClient.class.getName());

    private final CheckerBlockingStub blockingStub;

    public CheckerClient(Channel channel) {
        blockingStub = CheckerGrpc.newBlockingStub(channel);
    }

    ManagedChannel channel = Grpc.newChannelBuilder("localhost:50051", InsecureChannelCredentials.create()).build();
    CheckerClient checkerClient = new CheckerClient(channel);

    StreamObserver<RouteNote> requestObserver =
        asyncStub.routeChat(new StreamObserver<RouteNote>() {
          @Override
          public void onNext(RouteNote note) {
            info("Got message \"{}\"", note.getMessage());
          }

          @Override
          public void onError(Throwable t) {
            warning("Checker Failed: {0}", Status.fromThrowable(t));
          }

          @Override
          public void onCompleted() {
            info("Finished RouteChat");
          }
        });

    public Check checkDocument(Document document) {
        logger.info("Checking document with id: {}", document.getId());

        Doc docreq = Doc.newBuilder().setTitle(document.getTitle()).setContent(document.getContent()).build();

        return blockingStub.checkDocument(docreq, requestObserver);
    }
}