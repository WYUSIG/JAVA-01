package io.sign.www.rpc;

import io.sign.www.rpc.annotation.SignRpcReference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单 controller
 *
 * @author sign
 * @since 1.0
 **/
@RestController
@RequestMapping("/order")
public class OrderController {

    @SignRpcReference
    private OrderService orderService;

    @RequestMapping("/getOder")
    public Order getOderById(int id) {
        return orderService.findOrderById(id);
    }
}
