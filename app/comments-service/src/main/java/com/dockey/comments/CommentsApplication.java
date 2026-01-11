package com.dockey.comments;

import com.kumuluz.ee.common.runtime.EeRuntime;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/api")
public class CommentsApplication extends Application {

    private static final Logger LOG = LogManager.getLogger(CommentsApplication.class.getName());

    public CommentsApplication() {
        LOG.info("Comments Service starting...");
    }
}
