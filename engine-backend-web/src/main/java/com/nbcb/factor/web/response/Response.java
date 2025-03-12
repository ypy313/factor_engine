package com.nbcb.factor.web.response;

import com.nbcb.factor.common.Constants;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 统一返回对象
 */
@Getter
@Setter
public class Response implements Serializable {
    private static final long serialVersionUID = 1L;
    private int code;
    private String status;
    private String msg;
    private Object data;
    private Response(){

    }
    public Response(String msg,Object data){
        this.code = 100;
        this.status = Constants.HTTP_SUCC;
        this.msg = msg;
        this.data = data;
    }

    public Response(Object data){
        this.code = 100;
        this.status = Constants.HTTP_SUCC;
        this.data = data;
    }

    public Response(int code,String msg){
        this.code = code;
        this.status = Constants.HTTP_FAIL;
        this.msg = msg;
    }
}
