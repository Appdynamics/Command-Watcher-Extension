/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.commandwatcher.config;

import java.util.List;

/**
 * Created by abhi.pandey on 4/24/15.
 */
public class Configuration {

    private List<CommandToProcess> commandToProcess;
    private String metricPrefix;

    public List<CommandToProcess> getCommandToProcess() {
        return commandToProcess;
    }

    public void setCommandToProcess(List<CommandToProcess> commandToProcess) {
        this.commandToProcess = commandToProcess;
    }

    public String getMetricPrefix() {
        return metricPrefix;
    }

    public void setMetricPrefix(String metricPrefix) {
        this.metricPrefix = metricPrefix;
    }
}
