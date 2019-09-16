/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.commandwatcher;

import com.appdynamics.extensions.PathResolver;
import com.appdynamics.extensions.commandwatcher.config.CommandToProcess;
import com.appdynamics.extensions.commandwatcher.config.Configuration;
import com.appdynamics.extensions.yml.YmlReader;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by abhi.pandey on 4/24/15.
 * Refactored by atom!
 */
public class CommandWatcherMonitor extends AManagedMonitor {

    public static final Logger logger = LoggerFactory.getLogger(CommandWatcherMonitor.class);
    private String metricPrefix;
    private static final String CONFIG_ARG = "config-file";
    private static final String METRIC_SEPARATOR = "|";
    private static final long DEFAULT_TIMEOUT = 10;
    private ProcessExecutor processExecutor;

    public CommandWatcherMonitor() {
        logger.info("Using Monitor Version [" + getImplementationVersion() + "]");
        processExecutor = new ProcessExecutor();
    }

    /**
     * This is the entry point to the monitor called by the Machine Agent
     *
     * @param taskArguments
     * @param taskContext
     * @return
     * @throws com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException
     */


    public TaskOutput execute(Map<String, String> taskArguments, TaskExecutionContext taskContext) throws TaskExecutionException {
        logger.debug("The task Arguments are {}", taskArguments);
        if (taskArguments != null && !taskArguments.isEmpty()) {
            try {
                String configPath = taskArguments.get(CONFIG_ARG);
                File file = PathResolver.getFile(configPath, AManagedMonitor.class);
                if (file != null && file.exists()) {
                    //read the config.
                    Configuration config = YmlReader.readFromFile(file, Configuration.class);
                    // no point continuing if we don't have this
                    if (!config.getCommandToProcess().isEmpty()) {
                        processMetricPrefix(config.getMetricPrefix());
                        executeCommands(config);
                    } else {
                        logger.warn("There are no commands configured in the config.yml");
                    }
                } else {
                    logger.error("The config file path [{}] is resolved to [{}]", configPath, file != null ? file.getAbsolutePath() : null);
                }

            } catch (Exception e) {
                logger.error("Error while running the CommandWatcherMonitor", e);
            }
        } else {
            logger.error("Skipping CommandWatcherMonitor since the task arguments in monitor.xml is not set");
        }
        return null;
    }

    private void executeCommands(Configuration config) {
        List<CommandToProcess> commands = config.getCommandToProcess();
        logger.debug("The commands are {}", commands);
        for (CommandToProcess commandToProcess : commands) {
            try {
                String command = commandToProcess.getCommand();
                //setting default timeout of 10 seconds
                long timeOut = commandToProcess.getTimeOut() > 0 ? commandToProcess.getTimeOut() : DEFAULT_TIMEOUT;
                String displayName = commandToProcess.getDisplayName();
                if (!StringUtils.isEmpty(command) && !StringUtils.isEmpty(displayName)) {
                    ProcessExecutor.Response response;
                    if (Boolean.TRUE.equals(commandToProcess.getIsScript())) {
                        File file = PathResolver.getFile(command.trim(), AManagedMonitor.class);
                        if (file != null && file.exists()) {
                            response = processExecutor.execute(timeOut, file);
                        } else {
                            String err = String.format("The file [%s] was resolved to [%s]", command, file != null ? file.getAbsolutePath() : null);
                            response = new ProcessExecutor.Response(null,err);
                        }
                    } else {
                        response = processExecutor.execute(timeOut, "bash", "-c", command);
                    }
                    logger.debug("The response of the command [{}] is {}", command, response);
                    if (response != null) {
                        processResponse(command, displayName, response);
                    }
                } else {
                    logger.error("The command and display name shouldn't be null {}", commandToProcess);
                }
            } catch (Exception e) {
                logger.error("Error while running the command " + commandToProcess, e);
            }
        }
    }

    private void processResponse(String command, String displayName, ProcessExecutor.Response response) {
        String out = response.getOut();
        if (out != null) {
            out = out.trim();
            if (NumberUtils.isNumber(out.trim())) {
                BigDecimal bigDecimal = new BigDecimal(out);
                StringBuffer metricPath = new StringBuffer();
                metricPath.append(metricPrefix);
                String valueStr = bigDecimal.setScale(0, RoundingMode.HALF_UP).toString();
                printCollectiveObservedAverage(metricPath.toString() + displayName, valueStr);
            } else {
                logger.error("The output of the command [{}] is not a number [{}]", out);
            }
        }
        if (!StringUtils.isEmpty(response.getError())) {
            logger.error("There was an error while running the command [{}]. The output is [{}]. The error is [{}]",
                    command, out, response.getError());
        }
    }

    public void printMetric(String metricPath, String metricValue, String aggType, String timeRollupType, String clusterRollupType) {
        MetricWriter metricWriter = getMetricWriter(metricPath,
                aggType,
                timeRollupType,
                clusterRollupType
        );

        if (logger.isDebugEnabled()) {
            logger.debug("Sending [" + aggType + METRIC_SEPARATOR + timeRollupType + METRIC_SEPARATOR + clusterRollupType
                    + "] metric = " + metricPath + " = " + metricValue);
        }

        metricWriter.printMetric(metricValue);
    }


    private void printCollectiveObservedAverage(String metricPath, String metricValue) {
        printMetric(metricPath, metricValue,
                MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL
        );
    }

    private void processMetricPrefix(String metricPrefix) {

        if (!metricPrefix.endsWith("|")) {
            metricPrefix = metricPrefix + "|";
        }

        this.metricPrefix = metricPrefix;
    }

    private static String getImplementationVersion() {
        return CommandWatcherMonitor.class.getPackage().getImplementationTitle();
    }

    public static void main(String[] args) throws TaskExecutionException {

        Map<String, String> taskArgs = new HashMap<String, String>();
        taskArgs.put(CONFIG_ARG, "src/main/resources/conf/config.yml");
//        taskArgs.put(METRIC_ARG, "src/main/resources/conf/metrics.xml");

        CommandWatcherMonitor muleesbMonitor = new CommandWatcherMonitor();
        muleesbMonitor.execute(taskArgs, null);
    }
}
