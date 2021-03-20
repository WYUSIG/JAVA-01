package io.sign.www.hmily;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/account")
public class AccountController {

    @DubboReference(version = "1.0.0")
    private AccountService accountService;

    @RequestMapping("/payment")
    public boolean payment() {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setUserId("10000");
        accountDTO.setAmount(new BigDecimal("100"));
        return accountService.payment(accountDTO);
    }
}
