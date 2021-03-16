package io.sign.www.rpc;

import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 *
 * @author sign
 * @since 1.0
 **/
@Service(value = "io.sign.www.rpc.UserService")
public class UserServiceImpl implements UserService {

    @Override
    public User findById(int id) {
        return new User(id, "sign" + System.currentTimeMillis());
    }
}
