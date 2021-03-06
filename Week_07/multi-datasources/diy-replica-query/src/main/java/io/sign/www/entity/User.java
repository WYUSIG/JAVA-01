package io.sign.www.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.stereotype.Component;

@Data
@Builder
@ToString
@TableName(value = "users")
public class User {

    @TableId(value = "id")
    private Integer id;

    private String name;
}
