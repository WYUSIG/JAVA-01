package io.sign.www;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.sign.www.dao.OrderDao;
import io.sign.www.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class OrderDaoTest {

    @Autowired
    private OrderDao orderDao;

    @Test
    public void testSelectById() {
        Order order = orderDao.selectById(1);
        System.out.println(order);
    }

    @Test
    public void testSelectListByUserId() {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq("user_id", 1);
        List<Order> orders = orderDao.selectList(queryWrapper);
        System.out.println(orders.size());
    }

    @Test
    public void testInsertUserId1() {
        Order order = new Order();
        order.setUserId(1);
        orderDao.insert(order);
    }

    @Test
    public void testInsertUserId2() {
        Order order = new Order();
        order.setUserId(2);
        orderDao.insert(order);
    }
}
