package com.dockey.docs.grpc;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import com.dockey.docs.grpc.CheckerGrpc.CheckerBlockingStub;
import com.dockey.docs.entities.Document;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class CheckerClient {
  
    
  
    private static final Logger logger = LogManager.getLogger(CheckerClient.class.getName());
    private static final String CHECKER_HOST = System.getenv().getOrDefault("CHECKER_HOST", "dockey-checker");
    private static final int CHECKER_PORT = Integer.parseInt(System.getenv().getOrDefault("CHECKER_PORT", "50051"));
    private static final int TIMEOUT_SECONDS = 5;

    private ManagedChannel channel;
    private CheckerBlockingStub blockingStub;
    private boolean checkerAvailable = false;

    public CheckerClient() {
        // Default constructor for CDI
    }

    @PostConstruct
    public void init() {
        try {
            String target = CHECKER_HOST + ":" + CHECKER_PORT;
            logger.info("Initializing CheckerClient with target: {}", target);
            channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create()).build();
            blockingStub = CheckerGrpc.newBlockingStub(channel).withDeadlineAfter(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            checkerAvailable = true;
            logger.info("CheckerClient initialized successfully");
        } catch (Exception e) {
            logger.warn("Failed to initialize CheckerClient, content checking will be disabled: {}", e.getMessage());
            checkerAvailable = false;
        }
    }

    /**
     * Check comment text for inappropriate content.
     * This method is fault-tolerant - if the checker service is unavailable,
     * it will allow the comment through (fail-open policy) to ensure the
     * comments service remains functional.
     *
     * @param comment The comment to check
     * @return true if the comment is acceptable, false if it contains inappropriate content
     */
    public boolean checkText(Comment comment) {
        if (!checkerAvailable || blockingStub == null) {
            logger.info("Checker service not available, allowing comment through (fail-open)");
            return true;
        }

        try {
            logger.info("Checking document content for docId: {}", documnt.getId());

            Text txtreq = Text.newBuilder().setContents(document.getContent()).build();
            
            // Create a new stub with deadline for this specific call
            CheckerBlockingStub stubWithDeadline = blockingStub.withDeadlineAfter(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Check response = stubWithDeadline.checkText(txtreq);
            
            boolean isClean = !("flagged".equals(response.getResult()));
            logger.info("Content check result: {}", isClean ? "clean" : "flagged");
            return isClean;
        } catch (StatusRuntimeException e) {
            // Service unavailable, timeout, or other gRPC error - fail open
            logger.warn("Checker service call failed ({}), allowing comment through: {}", 
                e.getStatus().getCode(), e.getMessage());
            return true;
        } catch (Exception e) {
            // Unexpected error - fail open
            logger.error("Unexpected error during content check, allowing comment through: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Check if the checker service is available.
     * @return true if the service is configured and reachable
     */
    public boolean isAvailable() {
        return checkerAvailable && channel != null && !channel.isShutdown();
    }
}