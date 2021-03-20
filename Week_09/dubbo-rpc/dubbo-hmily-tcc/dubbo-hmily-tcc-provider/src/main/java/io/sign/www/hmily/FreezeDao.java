package io.sign.www.hmily;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface FreezeDao {

    @Update("update freeze set freeze = freeze + #{amount}, update_time = now()" +
            " where user_id =#{userId}")
    int increase(AccountDTO accountDTO);


    @Update("update freeze set freeze = freeze - #{amount}, update_time = now()" +
            " where user_id =#{userId} and freeze >= #{amount}")
    int reduce(AccountDTO accountDTO);

    @Insert("insert  into freeze(`user_id`,`freeze`,`create_time`,`update_time`) values" +
            "(#{userId}, #{amount},now(),NULL);")
    int insert(AccountDTO accountDTO);

    @Select("select * from freeze where user_id = #{userId} for update")
    Freeze select(String userId);
}
