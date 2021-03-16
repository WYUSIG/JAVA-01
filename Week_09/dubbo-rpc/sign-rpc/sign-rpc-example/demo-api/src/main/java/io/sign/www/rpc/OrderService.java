package io.sign.www.rpc;

/**
 * 订单服务接口
 *
 * @author sign
 * @since 1.0
 **/
public interface OrderService {

    Order findOrderById(int id);
}
