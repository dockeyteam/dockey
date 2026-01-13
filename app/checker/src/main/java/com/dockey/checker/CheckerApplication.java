package com.dockey.comments;

import com.kumuluz.ee.common.runtime.EeRuntime;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/v1")
public class CheckerApplication extends Application {

    private static final Logger LOG = LogManager.getLogger(CheckerApplication.class.getName());

    public CheckerApplication() {
        LOG.info("Checker starting...");
    }
}
