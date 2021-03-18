package io.sign.www.rpc;

import io.sign.www.rpc.annotation.SignRpcReference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户 Controller
 *
 * @author sign
 * @since 1.0
 **/
@RestController
@RequestMapping("/user")
public class UserController {

    @SignRpcReference
    UserService userService;

    @RequestMapping("/getUser")
    public User getUser(int id) {
        return userService.findById(id);
    }
}
