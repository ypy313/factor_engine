package com.changtian.factor.event;

public interface Monitorable<T>{
    MetricsRegistry getMetricsRegistry();
    T getContent();
}
