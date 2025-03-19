package com.nbcb.factor.web.job.history.impl;

import com.nbcb.factor.web.job.FactorXxlJob;
import com.nbcb.factor.web.job.history.FactorHistoryJob;
import com.nbcb.factor.web.util.ExceptionUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.log.XxlJobLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 执行python脚本公共类
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractProcessExecPythonJob implements FactorHistoryJob<String, ReturnT> {
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
        XxlJobLogger.log("the param is {}",param);
        String[] args = getArgs(param);
        XxlJobLogger.log("begin to run python:{}",args[1]);
        try{
            return exec(args) ? FactorXxlJob.RETURN_SUCCESS:FactorXxlJob.RETURN_FAIL;
        }catch (Exception e) {
            XxlJobLogger.log("run python Exception:{}", ExceptionUtils.toString(e));
            return FactorXxlJob.RETURN_FAIL;
        }
    }

    /**
     * 执行参数
     */
    protected abstract String[] getArgs(String param);
}
