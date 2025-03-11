package com.nbcb.factor.external.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class MyResponse<T> {
    @Getter@Setter
    private int code;
    @Getter@Setter
    private String status;
    @Getter@Setter
    private String msg;

    private T data;
    public T getData(){return data;}
    public void setData(T data){this.data=data;}
}
