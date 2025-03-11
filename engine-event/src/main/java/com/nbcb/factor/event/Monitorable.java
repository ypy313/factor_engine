package com.nbcb.factor.event;

public interface Monitorable<T>{
    MetricsRegistry getMetricsRegistry();
    T getContent();
}
