package com.appdynamics.extensions.commandwatcher;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.AssertUtils;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.appdynamics.extensions.commandwatcher.utility.Constants.COMMAND;
import static com.appdynamics.extensions.commandwatcher.utility.Constants.COMMAND_TO_PROCESS;
import static com.appdynamics.extensions.commandwatcher.utility.Constants.DEFAULT_METRIC_PREFIX;
import static com.appdynamics.extensions.commandwatcher.utility.Constants.DISPLAY_NAME;
import static com.appdynamics.extensions.commandwatcher.utility.Constants.IS_SCRIPT;
import static com.appdynamics.extensions.commandwatcher.utility.Constants.METRIC_NAME;
import static com.appdynamics.extensions.commandwatcher.utility.Constants.MONITOR_NAME;
import static com.appdynamics.extensions.commandwatcher.utility.Constants.THREAD_TIMEOUT;

public class CommandWatcherMonitor extends ABaseMonitor {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(CommandWatcherMonitor.class);

    @Override
    protected String getDefaultMetricPrefix() {
        return DEFAULT_METRIC_PREFIX;
    }

    @Override
    public String getMonitorName() {
        return MONITOR_NAME;
    }

    @Override
    protected void doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider) {

        BlockingQueue<String> completed = new LinkedBlockingQueue<>();

        MonitorContextConfiguration contextConfiguration = getContextConfiguration();
        List<Map<String, ?>> commandsToProcess = (List<Map<String, ?>>) contextConfiguration.getConfigYml().get(COMMAND_TO_PROCESS);
        int commandCount = commandsToProcess.size();
        for (Map<String, ?> commandToProcess : commandsToProcess) {
            String displayName = (String) commandToProcess.get(DISPLAY_NAME);
            String metricName = (String) commandToProcess.get(METRIC_NAME);
            String command = (String) commandToProcess.get(COMMAND);
            boolean isScript = (Boolean) commandToProcess.get(IS_SCRIPT);
            AssertUtils.assertNotNull(displayName, "Display name cannot be empty.");
            AssertUtils.assertNotNull(command, "command cannot be empty.");
            AssertUtils.assertNotNull(isScript, "isScript cannot be empty.");

            CommandWatcherMonitorTask commandWatcherMonitorTask = new CommandWatcherMonitorTask(tasksExecutionServiceProvider.getMetricWriteHelper(), contextConfiguration.getMetricPrefix(), displayName, metricName, command, isScript, commandToProcess);
            Future<?> handle = getContextConfiguration().getContext().getExecutorService().submit(displayName, commandWatcherMonitorTask);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                executor.submit(() -> {
                    try {
                        handle.get((Integer) contextConfiguration.getConfigYml().get(THREAD_TIMEOUT), TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        logger.error("Task interrupted for {}", displayName, e);
                    } catch (ExecutionException e) {
                        logger.error("Task execution failed for {}", displayName, e);
                    } catch (TimeoutException e) {
                        logger.error("Task timed out for {}", displayName, e);
                    } finally {
                        if (!handle.isDone()) {
                            handle.cancel(true);
                        }
                        completed.add(displayName);
                    }
                });
            } finally {
                executor.shutdown();
            }
        }

        for (int i = 0; i < commandCount; i++) {
            try {
                logger.debug("Finished processing task " + completed.take());
            } catch (InterruptedException e) {
                logger.error("Interrupted waiting for task finish", e);
            }
        }

        logger.info("Finished processing of all commands!!!");
    }

    @Override
    protected List<Map<String, ?>> getServers() {
        List<Map<String, ?>> commandToProcess = (List<Map<String, ?>>) getContextConfiguration().getConfigYml().get(COMMAND_TO_PROCESS);
        AssertUtils.assertNotNull(commandToProcess, "The commandToProcess section is not configured in config.yml");
        return commandToProcess;
    }
}
