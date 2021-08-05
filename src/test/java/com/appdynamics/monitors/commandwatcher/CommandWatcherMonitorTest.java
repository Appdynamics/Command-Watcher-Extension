/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.monitors.commandwatcher;

import com.appdynamics.extensions.commandwatcher.CommandWatcherMonitor;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by abhi.pandey on 9/4/14.
 */
public class CommandWatcherMonitorTest {

    private static final String CONFIG_ARG = "config-file";

    private CommandWatcherMonitor testClass;
    private Map<String, String> taskArgs;

    @Before
    public void init() throws Exception {
        testClass = new CommandWatcherMonitor();
        taskArgs = new HashMap();
    }

    @Test
    public void testFileWatcherExtension() throws TaskExecutionException {

        Map<String, String> taskArgs = new HashMap();
        taskArgs.put(CONFIG_ARG, "src/test/resources/conf/config.yml");
        testClass.execute(taskArgs, null);

    }
}
