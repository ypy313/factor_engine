package com.nbcb.factor.event;

import java.io.Serializable;

public interface Streamable extends Serializable {
    String KEY ="key";
    long getCreated();
    long getEventTime();
    String getKey();
    String getTopic();
}
