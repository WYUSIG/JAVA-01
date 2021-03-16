package io.sign.www.rpc.api;

import lombok.Data;

/**
 * RPC 请求实体
 *
 * @author sign
 * @since 1.0
 **/
@Data
public class SignRpcRequest {

    private String serviceClass;

    private String method;

    private Object[] params;
}
