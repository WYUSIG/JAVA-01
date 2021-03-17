package io.sign.www.rpc;

import io.sign.www.rpc.annotation.SignRpcService;
import org.springframework.stereotype.Service;

/**
 * 订单服务实现类
 *
 * @author sign
 * @since 1.0
 **/
@Service(value = "io.sign.www.rpc.OrderService")
@SignRpcService
public class OrderServiceImpl implements OrderService{

    @Override
    public Order findOrderById(int id) {
        return new Order(id, "Order" + System.currentTimeMillis(), 9.9f);
    }
}
