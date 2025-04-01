package com.changtian.factor.event.cfets;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
@Getter@Setter@ToString
public class NoPartySubId implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * xxx文本
     */
    private String partySubID;
    /**
     * 135-机构 6位码
     */
    private String partySubIDType;
}
