/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.commandwatcher;

import com.appdynamics.extensions.util.NumberUtils;
import com.google.common.base.Strings;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Created by abey.tom on 6/29/16.
 */
public class ProcessExecutorTest {
    @Test
    public void testAll(){
        ProcessExecutor executor = new ProcessExecutor();

        //Valid command test
        ProcessExecutor.Response response = executor.execute("bash", "-c", "ls -al ~ | wc -l");
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getOut());
        Assert.assertTrue(Strings.isNullOrEmpty(response.getError()));
        Assert.assertTrue(NumberUtils.isNumber(response.getOut()));

        //Valid command test
        response = executor.execute("bash", "-c", "ps aux | grep java | wc -l");
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getOut());
        Assert.assertTrue(Strings.isNullOrEmpty(response.getError()));
        Assert.assertTrue(NumberUtils.isNumber(response.getOut()));

        //Invalid command test
        response = executor.execute("bash", "-c", "nsstat");
        Assert.assertNotNull(response);
        Assert.assertTrue(Strings.isNullOrEmpty(response.getOut()));
        Assert.assertNotNull(response.getError());

        //Command which does not return valid output as number
        response = executor.execute("bash", "-c", "ls -ltr ~");
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getOut());
        Assert.assertTrue(Strings.isNullOrEmpty(response.getError()));
        Assert.assertFalse(NumberUtils.isNumber(response.getOut()));

        //Valid script test
        response = executor.execute(new File("src/test/resources/scripts/filecount.sh"));
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getOut());
        Assert.assertTrue(Strings.isNullOrEmpty(response.getError()));
        Assert.assertTrue(NumberUtils.isNumber(response.getOut()));

        //Script which does not give valid output as number
        response = executor.execute(new File("src/test/resources/scripts/invalidoutputscript.sh"));
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getOut());
        Assert.assertTrue(Strings.isNullOrEmpty(response.getError()));
        Assert.assertFalse(NumberUtils.isNumber(response.getOut()));

        //Script not found at given path test
        response = executor.execute(new File("src/test/resources/filecount.sh"));
        Assert.assertNotNull(response);
        Assert.assertTrue(Strings.isNullOrEmpty(response.getOut()));
        Assert.assertNotNull(response.getError());

        executor.shutdown();
    }

}