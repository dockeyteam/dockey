package com.dockey.comments.grpc;

import javax.enterprise.context.ApplicationScoped;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import com.dockey.comments.grpc.CheckerGrpc.CheckerBlockingStub;
import com.dockey.comments.grpc.CheckerGrpc.CheckerStub;
import com.google.protobuf.Message;
import com.dockey.comments.entities.Comment;
import io.grpc.stub.StreamObserver;
import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.Status;

@ApplicationScoped
public class CheckerClient {
  
    private static final Logger logger = LogManager.getLogger(CheckerClient.class.getName());

    private final CheckerBlockingStub blockingStub;

    ManagedChannel channel = Grpc.newChannelBuilder("dockey-checker:50051", InsecureChannelCredentials.create()).build();
    
    public CheckerClient(Channel channel) {
        blockingStub = CheckerGrpc.newBlockingStub(channel);
    }

    public boolean checkText(Comment comment) {
        logger.info("Checking comment with id: {}", comment.getId());

        Text txtreq = Text.newBuilder().setContents(comment.getContent()).build();

        return (blockingStub.checkText(txtreq).getResult() == "success");
    }
}