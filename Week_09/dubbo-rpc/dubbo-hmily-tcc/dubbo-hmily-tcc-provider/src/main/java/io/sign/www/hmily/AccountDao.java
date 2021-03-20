package io.sign.www.hmily;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface AccountDao {

    @Update("update account set balance = balance + #{amount}, update_time = now()" +
            " where user_id =#{userId}")
    int increase(AccountDTO accountDTO);


    @Update("update account set balance = balance - #{amount}, update_time = now()" +
            " where user_id =#{userId} and balance >= #{amount}")
    int reduce(AccountDTO accountDTO);
}
