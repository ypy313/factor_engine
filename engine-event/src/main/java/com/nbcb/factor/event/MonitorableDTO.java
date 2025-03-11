package com.nbcb.factor.event;

import lombok.Setter;

import java.io.Serializable;

public class MonitorableDTO<T> implements Monitorable, Serializable {
    private static final long serialVersionUID = 1L;
    @Setter
    private MetricsRegistry metricsRegistry;
    @Setter
    private T content;

    @Override
    public MetricsRegistry getMetricsRegistry() {
        return metricsRegistry;
    }

    @Override
    public Object getContent() {
        return content;
    }

}
