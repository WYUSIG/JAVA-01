package io.sign.www.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.sign.www.entity.Order;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDao extends BaseMapper<Order> {
}
