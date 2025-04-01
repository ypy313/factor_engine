package com.changtian.factor.job.engine;

import com.changtian.factor.event.BondInputEvent;
import com.changtian.factor.event.OutputEvent;

import java.util.List;

/**
 * 事件处理器。策略和事件的桥梁
 * 必须实现equals方法，否则事件引擎不增加和删除不正确
 */
public interface EventHandler {
    List<OutputEvent> processEvent(BondInputEvent event);
}
