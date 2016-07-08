package com.appdynamics.extensions.commandwatcher;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
        ProcessExecutor.Response response = executor.execute("bash", "-c", "ls -al ~ | wc -l");
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getOut());
        Assert.assertTrue(StringUtils.isEmpty(response.getError()));
        Assert.assertTrue(NumberUtils.isNumber(response.getOut()));

        response = executor.execute("bash", "-c", "ps aux | grep java | wc -l");
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getOut());
        Assert.assertTrue(StringUtils.isEmpty(response.getError()));
        Assert.assertTrue(NumberUtils.isNumber(response.getOut()));

        response = executor.execute(new File("src/test/resources/scripts/filecount.sh"));
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getOut());
        Assert.assertTrue(StringUtils.isEmpty(response.getError()));
        Assert.assertTrue(NumberUtils.isNumber(response.getOut()));

        executor.shutdown();
    }

}