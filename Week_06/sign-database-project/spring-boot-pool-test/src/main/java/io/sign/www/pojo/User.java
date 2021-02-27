package io.sign.www.pojo;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Entity(name = "shop_user")
public class User implements Serializable {

    @Id
    private Integer id;

    private String phone;

    private String userName;

    private String avatar;

    private String password;

    private Long createTime;

    private Long updateTime;
}
