package com.nbcb.factor.web.job.history.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HistorySyncJob extends AbstractProcessExecPythonJob{
    @Value("${job.history.python-path}")
    private String pythonPath;
    @Value("${job.history.history-sync-script-path}")
    private String scriptPath;

    @Override
    protected String[] getArgs(String param){
        return new String[]{pythonPath,scriptPath,param};
    }
}
