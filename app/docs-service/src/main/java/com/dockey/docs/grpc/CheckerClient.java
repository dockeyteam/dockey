package com.dockey.docs.grpc;

import javax.enterprise.context.ApplicationScoped;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import com.dockey.docs.grpc.CheckerGrpc.CheckerBlockingStub;
import com.dockey.docs.grpc.CheckerGrpc.CheckerStub;
import com.google.protobuf.Message;
import com.dockey.docs.entities.Document;
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

    private final ManagedChannel channel;
    
    public CheckerClient() {
        channel = Grpc.newChannelBuilder("dockey-checker:50051", InsecureChannelCredentials.create()).build();
        blockingStub = CheckerGrpc.newBlockingStub(channel);
    }

    public boolean checkText(Document document) {
        logger.info("Checking document with id: {}", document.getId());

        Text txtreq = Text.newBuilder().setContents(document.getContent()).build();

        return (blockingStub.checkText(txtreq).getResult() == "success");
    }
}