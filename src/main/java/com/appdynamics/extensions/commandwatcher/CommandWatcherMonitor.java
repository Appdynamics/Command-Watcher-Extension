package com.appdynamics.extensions.commandwatcher;

import com.appdynamics.extensions.PathResolver;
import com.appdynamics.extensions.commandwatcher.config.CommandToProcess;
import com.appdynamics.extensions.commandwatcher.config.Configuration;
import com.appdynamics.extensions.yml.YmlReader;
import com.google.common.base.Strings;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Created by abhi.pandey on 4/24/15.
 */
public class CommandWatcherMonitor extends AManagedMonitor {

    protected final Logger logger = Logger.getLogger(CommandWatcherMonitor.class.getName());
    private String metricPrefix;
    private static final String CONFIG_ARG = "config-file";
    private static final String LOG_PREFIX = "log-prefix";
    private static String logPrefix;
    private static final String METRIC_SEPARATOR = "|";

    /**
     * This is the entry point to the monitor called by the Machine Agent
     *
     * @param taskArguments
     * @param taskContext
     * @return
     * @throws com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException
     */
    public TaskOutput execute(Map<String, String> taskArguments, TaskExecutionContext taskContext) throws TaskExecutionException {
        if (taskArguments != null && !taskArguments.isEmpty()) {
            setLogPrefix(taskArguments.get(LOG_PREFIX));
            logger.info("Using Monitor Version [" + getImplementationVersion() + "]");
            logger.info(getLogPrefix() + "Starting the CommandWatcher Monitoring task.");
            if (logger.isDebugEnabled()) {
                logger.debug(getLogPrefix() + "Task Arguments Passed ::" + taskArguments);
            }
            String status = "Success";

            String configFilename = getConfigFilename(taskArguments.get(CONFIG_ARG));

            try {
                //read the config.
                Configuration config = YmlReader.readFromFile(configFilename, Configuration.class);

                // no point continuing if we don't have this
                if (config.getCommandToProcess().isEmpty()) {
                    return new TaskOutput("Failure");
                }
                processMetricPrefix(config.getMetricPrefix());
                status = getStatus(config, status);
            } catch (Exception e) {
                logger.error("Exception", e);
            }

            return new TaskOutput(status);
        }
        throw new TaskExecutionException(getLogPrefix() + "CommandWatcher monitoring task completed with failures.");
    }

    private String getStatus(Configuration config, String status) {
        try {
            for (CommandToProcess commandToProcess : config.getCommandToProcess()) {
                String command = commandToProcess.getCommand();
                String displayName = commandToProcess.getDisplayName();
                if (commandToProcess.getIsScript()) {
                    try {
                        command = new String(Files.readAllBytes(Paths.get(command)));
                    } catch (NoSuchFileException e) {
                        logger.error("Wrong file path: " + e);
                    }
                } else {
                    executeCommand(command, displayName);
                }
            }

        } catch (Exception e) {
            logger.error("Error in processing the commands:" + e);
            status = "Failure";
        }

        return status;
    }

    private String executeCommand(String command, String displayName) {
        String output = LinuxInteractor.execute(command, true);
        if (output.contains("command not found")) {
            logger.error("Invalid bash command - " + command);
        } else if (!isInteger(output)) {

            logger.error("Command \"" + command + "\" doesn't generate recommended output");

        } else {
            StringBuffer metricPath = new StringBuffer();
            metricPath.append(metricPrefix);
            printCollectiveObservedCurrent(metricPath.toString() + displayName, output);
        }

        return output;
    }

    /**
     * A helper method to report the metrics.
     *
     * @param metricPath
     * @param metricValue
     * @param aggType
     * @param timeRollupType
     * @param clusterRollupType
     */
    public void printMetric(String metricPath, String metricValue, String aggType, String timeRollupType, String clusterRollupType) {
        MetricWriter metricWriter = getMetricWriter(metricPath,
                aggType,
                timeRollupType,
                clusterRollupType
        );

        if (logger.isDebugEnabled()) {
            logger.debug(getLogPrefix() + "Sending [" + aggType + METRIC_SEPARATOR + timeRollupType + METRIC_SEPARATOR + clusterRollupType
                    + "] metric = " + metricPath + " = " + metricValue);
        }

        metricWriter.printMetric(metricValue);
    }


    private void printCollectiveObservedCurrent(String metricPath, String metricValue) {
        printMetric(metricPath, metricValue,
                MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL
        );
    }

    /**
     * Returns a config file name,
     *
     * @param filename
     * @return String
     */

    private String getConfigFilename(String filename) {
        if (filename == null) {
            return "";
        }
        //for absolute paths
        if (new File(filename).exists()) {
            return filename;
        }
        //for relative paths
        File jarPath = PathResolver.resolveDirectory(AManagedMonitor.class);
        String configFileName = "";
        if (!Strings.isNullOrEmpty(filename)) {
            configFileName = jarPath + File.separator + filename;
        }
        return configFileName;
    }

    private void processMetricPrefix(String metricPrefix) {

        if (!metricPrefix.endsWith("|")) {
            metricPrefix = metricPrefix + "|";
        }
        if (!metricPrefix.startsWith("Custom Metrics|")) {
            metricPrefix = "Custom Metrics|" + metricPrefix;
        }

        this.metricPrefix = metricPrefix;
    }

    private boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c <= '/' || c >= ':') {
                return false;
            }
        }
        return true;
    }

    public String getLogPrefix() {
        return logPrefix;
    }

    public void setLogPrefix(String logPrefix) {
        this.logPrefix = (logPrefix != null) ? logPrefix : "";
    }

    private static String getImplementationVersion() {
        return CommandWatcherMonitor.class.getPackage().getImplementationTitle();
    }
}
