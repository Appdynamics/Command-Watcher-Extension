/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.commandwatcher;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by abey.tom on 6/29/16.
 * NOT Threadsafe
 */
public class ProcessExecutor {
    public static final Logger logger = ExtensionsLoggerFactory.getLogger(ProcessExecutor.class);

    private ExecutorService outReaderService;
    private ExecutorService errReaderService;

    public ProcessExecutor() {
        outReaderService = Executors.newSingleThreadExecutor();
        errReaderService = Executors.newSingleThreadExecutor();
        logger.info("initialized the Process Executor with OutReader and Error Reader");
    }


    public Response execute(File file) {
        if (file.exists()) {
            if (!file.canExecute()) {
                makeExecutable(file);
            }
            return execute(new String[]{file.getAbsolutePath()});
        } else {
            String msg = String.format("The file [%s] doesn't exist", file.getAbsolutePath());
            logger.error(msg);
            return new Response(null, msg);
        }
    }

    public Response execute(String... commands) {
        Response response;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("The command is  {}", Arrays.toString(commands));
            }
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(commands);
            Process process = builder.start();
            Future<String> errorFuture = errReaderService.submit(new OutReader(process.getErrorStream()));
            Future<String> outFuture = outReaderService.submit(new OutReader(process.getInputStream()));
            process.waitFor();
            response = new Response(getSilent(outFuture, commands), getSilent(errorFuture, commands));
        } catch (Exception e) {
            logger.error("Error while executing the command " + Arrays.toString(commands), e);
            response = new Response(null, e.getMessage());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("The response of the command is {}", response);
        }
        return response;
    }

    private String getSilent(Future<String> errorFuture, String[] commands) {
        try {
            return errorFuture.get();
        } catch (Exception e) {
            logger.error("Error while getting the result of the command " + Arrays.toString(commands), e);
            return e.getMessage();
        }
    }

    private static class OutReader implements Callable<String> {

        private InputStream inputStream;

        public OutReader(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public String call() throws Exception {
            return IOUtils.toString(inputStream);
        }
    }

    public static class Response {

        private String out;
        private String error;

        public Response(String out, String error) {
            if(!StringUtils.isEmpty(out)){
                this.out = out.trim();
            }
            if(!StringUtils.isEmpty(error)){
                this.error = error.trim();
            }
        }

        public String getOut() {
            return out;
        }

        public String getError() {
            return error;
        }

        @Override
        public String toString() {
            return "Response{" +
                    "out='" + out + '\'' +
                    ", error='" + error + '\'' +
                    '}';
        }
    }

    public static void makeExecutable(File file) {
        if (file.exists()) {
            String[] cmds = {"chmod", "+x", file.getAbsolutePath()};
            try {
                logger.debug("Making the file executable with chmod +x {}", file.getAbsolutePath());
                Process exec = Runtime.getRuntime().exec(cmds);
                int exitCode = exec.waitFor();
                if (exitCode != 0) {
                    logger.error("The command {} exited with a status code of {}", Arrays.toString(cmds), exitCode);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("The command {} exited with a status code of {}", Arrays.toString(cmds), exitCode);
                    }
                }
            } catch (Exception e) {
                logger.error("Error while making the file as executable" + Arrays.toString(cmds), e);
            }
        }
    }

    public void shutdown(){
        errReaderService.shutdown();
        outReaderService.shutdown();
    }
}
