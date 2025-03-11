package com.nbcb.factor.event.cfets;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter@Setter@ToString
public class NoPartyID implements Serializable {
    private static final long SerialVersionUID = 1L;
    /**
     * 组件必须域，取值“-”
     */
    private String partyID;
    /**
     * 发行人 1-本方
     */
    private String partyRole;
    /**
     * 重复组个数
     */
    private String noPartySubIDs;
    /**
     * NoParthSubID集合
     */
    private List<NoPartySubId> noPartySubDList;
    /**
     * 本方主体类型
     * 103-自营 104-代理 105-资营
     */
    private String partyRoleQualifier;
}
