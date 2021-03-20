package io.sign.www.hmily;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class Freeze implements Serializable {

    private static final long serialVersionUID = -81849676368907419L;

    private int id;

    private int userId;

    private BigDecimal freeze;

    private Date createTime;

    private Date updateTime;
}
