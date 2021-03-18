package io.sign.www.rpc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户实体
 *
 * @author sign
 * @since 1.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private int id;

    private String name;
}
