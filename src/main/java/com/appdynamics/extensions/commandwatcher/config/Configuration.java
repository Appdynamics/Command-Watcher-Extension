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
