package com.appdynamics.extensions.commandwatcher;

import com.appdynamics.extensions.AMonitorTaskRunnable;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.NumberUtils;
import com.appdynamics.extensions.util.PathResolver;
import com.google.common.base.Strings;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.slf4j.Logger;

import java.io.File;
import java.util.Map;

import static com.appdynamics.extensions.commandwatcher.utility.Constants.*;

public class CommandWatcherMonitorTask implements AMonitorTaskRunnable {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(CommandWatcherMonitorTask.class);
    private MetricWriteHelper metricWriteHelper;
    private String metricPrefix;
    private String displayName;
    private String metricName;
    private String command;
    private boolean isScript;
    private Map<String,?> commandToProcess;
    private ProcessExecutor processExecutor;

    public CommandWatcherMonitorTask(MetricWriteHelper metricWriteHelper, String metricPrefix, String displayName, String metricName, String command, boolean isScript, Map<String, ?> commandToProcess) {
        this.metricWriteHelper = metricWriteHelper;
        this.metricPrefix = metricPrefix;
        this.displayName = displayName;
        this.metricName = metricName;
        this.command = command;
        this.isScript = isScript;
        this.commandToProcess = commandToProcess;
        this.processExecutor = new ProcessExecutor();
    }

    @Override
    public void run() {
        try {
            ProcessExecutor.Response response;
            if (Boolean.TRUE.equals(isScript)) {
                File file = PathResolver.getFile(command.trim(), AManagedMonitor.class);
                if (file != null && file.exists()) {
                    response = processExecutor.execute(file);
                } else {
                    String err = String.format("The file [%s] was resolved to %s ", command, file != null ? file.getAbsolutePath() : "[null]. Couldn't resolve file.");
                    response = new ProcessExecutor.Response(null, err);
                }
            } else {
                response = processExecutor.execute("bash", "-c", command);
            }
            logger.debug("The response of the command [" + command + "] is " + response);
            if(response != null){
                processResponse(response);
            }
        } catch (Exception e) {
            logger.error("Error occurred while running task for " + displayName, e);
        }
    }

    private void processResponse(ProcessExecutor.Response response){
        String output = response.getOut();
        String error = response.getError();

        if(!Strings.isNullOrEmpty(output) && NumberUtils.isNumber(output.trim())){
            metricWriteHelper.printMetric(metricPrefix+SEPARATOR+displayName+(metricName!=null?SEPARATOR+metricName:""),output.trim(), MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
                    MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL);
        } else{
            logger.error("Output of command [{}] is either null or it is not a number.",command);
        }
        if(!Strings.isNullOrEmpty(error)){
            logger.error("There is an error running the command [{}]. The output is [{}] and the error is [{}]",command,output,error);
        }
    }

    @Override
    public void onTaskComplete() {

    }
}
