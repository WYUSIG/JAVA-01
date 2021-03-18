package io.sign.www.rpc.controller;

import io.sign.www.rpc.User;
import io.sign.www.rpc.UserService;
import io.sign.www.rpc.annotation.SignRpcReference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户服务
 **/
@RestController
@RequestMapping("/user")
public class UserController {

    @SignRpcReference
    UserService userService;

    @RequestMapping("/getUser")
    public User getUser(int id) {
        return (User)userService.findById(id);
    }
}
