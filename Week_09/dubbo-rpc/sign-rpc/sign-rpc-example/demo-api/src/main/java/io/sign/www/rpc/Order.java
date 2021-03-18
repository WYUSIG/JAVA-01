package io.sign.www.rpc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单实体
 *
 * @author sign
 * @since 1.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    private int id;

    private String name;

    private float amount;
}
