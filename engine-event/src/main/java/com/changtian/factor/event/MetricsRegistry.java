package com.changtian.factor.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 各阶段时间监控
 */
@Getter@Setter@ToString
public class MetricsRegistry implements Serializable {
    private static final long serialVersionUID = 1L;

    private String dataSource;
    private long globalId;
    private long nowTimestamp;
    private long receiveTimestamp;
    private long sendTimestamp;
    private long readyToSendTimestamp;
    private long createdTimestamp;
    private long normalizeInTimestamp;
    private long normalizeOutTimestamp;
    private long normalizeRdStartTimestamp;
    private long normalizeAllInTimestamp;
    private long normalizeAllOutTimestamp;
    private long normalizeAllRdStartTimestamp;
    private long sendingTimestamp;
    private long beforeSerializeTimestamp;
    private long beforeCompressTimestamp;
    private long calculateStartTimestamp;
    private long calculateEndTimestamp;
    private long tcpTransferLatency;
    private long beforeSerialize;
    private long beforeCompress;
    private long odsLatency;
    private long connectorLatency;
    private long normalizeLatency;
    private long normalizeRdLatency;
    private long normalizeAllLatency;
    private long normalizeAllRdLatency;
    private long noiseCalculateLatency;

}
