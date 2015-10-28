package com.gyang.tutu.util;

import com.google.common.io.Files;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by declan.guo on 15-10-28.
 */
public final class LogUtil {

    private LogUtil() {

    }

    private static final String LOG_FILE_NAME = "result.txt";

    private static final ArrayBlockingQueue<String> logQueue = new ArrayBlockingQueue<String>(2048);

    public static void start(String logFilePath) throws Exception {
        final File logFile = new File(logFilePath, LOG_FILE_NAME);
        Files.createParentDirs(logFile);

        Thread writeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String logStr = null;
                while(true) {
                    try {
                        logStr = logQueue.take();
                        Files.append(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss") + " " + logStr + "\n", logFile, Charset.defaultCharset());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        writeThread.setDaemon(true);
        writeThread.start();
    }

    public static void logInfo(String log) {
        logQueue.add("[info]  " + log);
    }

    public static void logError(String log) {
        logQueue.add("[error]  " + log);
    }

    public static void main(String[] args) throws Exception {
        LogUtil.start("/home/gyang/tutu/log/");

        for(int i = 0; i < 10; i++) {
            LogUtil.logInfo("heihei: " + i);
            LogUtil.logError("haha: " + i);

            TimeUnit.MILLISECONDS.sleep(100);
        }
    }
}
