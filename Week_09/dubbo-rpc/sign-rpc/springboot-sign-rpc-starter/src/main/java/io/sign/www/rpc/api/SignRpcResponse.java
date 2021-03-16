package io.sign.www.rpc.api;

import lombok.Data;

/**
 * RPC 请求响应实体
 *
 * @author sign
 * @since 1.0
 **/
@Data
public class SignRpcResponse {

    private Object result;

    private boolean status;

    private Exception exception;
}
