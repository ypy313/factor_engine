package com.nbcb.factor.web.job.history.impl;

import com.nbcb.factor.web.job.history.FactorHistoryJob;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.log.XxlJobLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 执行python脚本公共类
 */
@SuppressWarnings("rawtypes")
public class AbstractProcessExecPythonJob implements FactorHistoryJob<String, ReturnT> {
    /**
     * 执行cmd命令
     */
    public static boolean exec(String[] args) throws IOException ,InterruptedException {
        boolean flag = false;
        XxlJobLogger.log("begin to run python:{}",args[1]);
        Process proc = Runtime.getRuntime().exec(args);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))){
            String line;
            while ((line = reader.readLine()) != null) {
                flag = "T".equals(line);
                XxlJobLogger.log(line);
            }
        } catch (Exception e) {
            XxlJobLogger.log(e);
        }
        proc.waitFor();
        return flag;
    }
    @Override
    public ReturnT executeJob(String param) {
        return null;
    }
}
