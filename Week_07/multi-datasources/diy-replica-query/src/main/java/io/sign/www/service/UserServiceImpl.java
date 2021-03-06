package io.sign.www.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.sign.www.dao.UserDao;
import io.sign.www.entity.User;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserDao, User> implements UserService{

}
