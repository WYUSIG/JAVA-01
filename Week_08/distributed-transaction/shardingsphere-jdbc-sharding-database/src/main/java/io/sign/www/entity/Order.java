package io.sign.www.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "orders")
public class Order {

    @TableId(value = "id")
    private Long id;

    private Integer userId;
}
