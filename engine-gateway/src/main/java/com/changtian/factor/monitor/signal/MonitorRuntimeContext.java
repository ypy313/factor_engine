package com.changtian.factor.monitor.signal;

import com.changtian.factor.cache.TaCacheManager;
import lombok.Getter;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.Map;

@Getter
public class MonitorRuntimeContext {
    //key:indexName,value:指标值
    private final Map<String,Object> map = new HashMap<>();
    private TaCacheManager.IndexDTO indexDTO;
    public static MonitorRuntimeContext of(TaCacheManager.IndexDTO dto){
        MonitorRuntimeContext e = new MonitorRuntimeContext();
        e.indexDTO = dto;
        return e;
    }

    public void put(String indexName, Object value){
        if (value == null) {
            return;
        }
        map.put(indexName,value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("map", map)
                .append("indexDTO", indexDTO)
                .toString();
    }
}
