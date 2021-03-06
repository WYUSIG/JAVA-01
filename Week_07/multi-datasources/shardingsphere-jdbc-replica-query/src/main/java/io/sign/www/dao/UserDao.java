package io.sign.www.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.sign.www.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

public interface UserDao extends BaseMapper<User> {
}
