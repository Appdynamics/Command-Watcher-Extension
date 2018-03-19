/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.commandwatcher;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by abhi.pandey on 4/24/15.
 */
public class LinuxInteractor {
    protected static final Logger logger = Logger.getLogger(LinuxInteractor.class.getName());

    public static String execute(String command, boolean waitForResponse) {

        String response = "";

        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        pb.redirectErrorStream(true);

        logger.info("Linux command: " + command);

        try {
            Process shell = pb.start();

            if (waitForResponse) {
                InputStream shellIn = shell.getInputStream();
                int shellExitStatus = shell.waitFor();
                response = convertStreamToStr(shellIn);
                shellIn.close();
            }

        } catch (IOException e) {
            logger.error("Error occurred  while executing Linux command. Error Description: "
                    + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("Error occurred while executing Linux command. Error Description: "
                    + e.getMessage());
        }

        return response.trim();
    }
 
/*
* To convert the InputStream to String we use the Reader.read(char[]
* buffer) method. We iterate until the Reader return -1 which means
* there's no more data to read. We use the StringWriter class to
* produce the string.
*/
    private static String convertStreamToStr(InputStream is) throws IOException {

        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is,
                        "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }

}
